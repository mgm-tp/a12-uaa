/*
 * SPDX-License-Identifier: EUPL-1.2 OR LicenseRef-commercial
 *
 * Copyright (c) 2012-2026 mgm technology partners GmbH
 *
 * Dual License
 * ------------
 * This source file is part of the mgm A12 Platform and available under
 * a choice of two different licenses:
 *
 * 1. Open-Source License – EUPL v1.2
 *    You may redistribute and/or modify this file under the terms of the
 *    European Union Public License, version 1.2 - see https://eupl.eu/.
 *
 * 2. Commercial License
 *    Alternatively, you may obtain a commercial license from
 *    mgm technology partners GmbH, that permits use of this software
 *    under different terms (including support and maintenance services).
 *
 *    Please contact a12-license@mgm-tp.com for more information.
 *
 * You must select and comply with exactly one of the above license options.
 *
 * Warranty Disclaimer (applies to either option)
 * ----------------------------------------------
 * THIS SOFTWARE IS PROVIDED “AS IS” AND WITHOUT WARRANTY OF ANY KIND,
 * WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT, EXCEPT WHERE SUCH DISCLAIMERS ARE HELD TO BE
 * LEGALLY INVALID. SEE THE RESPECTIVE LICENSE TEXT FOR DETAILS.
 */
package com.mgmtp.a12.uaa.authentication.saml.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.SessionIndex;
import org.opensaml.saml.saml2.core.impl.LogoutRequestUnmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml2.Saml2Exception;
import org.springframework.security.saml2.core.Saml2ParameterNames;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mgmtp.a12.uaa.authentication.internal.TokenType;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtAuthenticationToken;
import com.mgmtp.a12.uaa.authentication.saml.SamlJwtTokenData;
import com.mgmtp.a12.uaa.authentication.saml.SamlJwtTokenStorage;

import net.shibboleth.shared.xml.ParserPool;

public class Saml2LogoutRequestAuthenticatorFilter extends OncePerRequestFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(Saml2LogoutRequestAuthenticatorFilter.class);

	private RequestMatcher logoutRequestMatcher = PathPatternRequestMatcher.withDefaults().matcher("/logout/saml2/slo");
	private RelyingPartyRegistrationResolver relyingPartyRegistrationResolver;
	private SamlJwtTokenStorage samlJwtTokenStorage;
	private final AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver;

	private LogoutRequestUnmarshaller unmarshaller;
	private ParserPool parserPool;

	public Saml2LogoutRequestAuthenticatorFilter(RelyingPartyRegistrationResolver relyingPartyRegistrationResolver, SamlJwtTokenStorage samlJwtTokenStorage,
		AuthenticationManager authenticationManager) {
		this.relyingPartyRegistrationResolver = relyingPartyRegistrationResolver;
		this.samlJwtTokenStorage = samlJwtTokenStorage;
		XMLObjectProviderRegistry registry = ConfigurationService.get(XMLObjectProviderRegistry.class);
		this.parserPool = registry.getParserPool();
		this.authenticationManagerResolver = (request) -> authenticationManager;
		this.unmarshaller = (LogoutRequestUnmarshaller) XMLObjectProviderRegistrySupport.getUnmarshallerFactory()
			.getUnmarshaller(LogoutRequest.DEFAULT_ELEMENT_NAME);

	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		if (!this.logoutRequestMatcher.matches(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		if (request.getParameter(Saml2ParameterNames.SAML_REQUEST) == null) {
			filterChain.doFilter(request, response);
			return;
		}
		AuthenticationHolder authenticationHolder = resolveAuthenticationFromToken(request);
		if (authenticationHolder == null) {
			LOGGER.error("Unable to reconstruct authenticated user from a session ID");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		LOGGER.debug("SAML Logout request received for user [{}] with token [{}]", authenticationHolder.authentication.getName(),
			authenticationHolder.accessToken);

		RelyingPartyRegistration registration = this.relyingPartyRegistrationResolver.resolve(request, getRegistrationId(authenticationHolder.authentication));

		Saml2MessageBinding saml2MessageBinding = resolveBinding(request);
		if (!registration.getSingleLogoutServiceBindings().contains(saml2MessageBinding)) {
			LOGGER.trace("Did not process logout request since used incorrect binding");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		HeaderMapRequestWrapper requestWrapper = new HeaderMapRequestWrapper(request);
		requestWrapper.addHeader("Authorization", "%s %s".formatted(TokenType.UAABEARER, authenticationHolder.accessToken));
		SecurityContextHolder.getContext().setAuthentication(authenticationHolder.authentication);
		filterChain.doFilter(requestWrapper, response);

	}

	private AuthenticationHolder resolveAuthenticationFromToken(HttpServletRequest request) {
		String serialized = request.getParameter(Saml2ParameterNames.SAML_REQUEST);

		byte[] b = samlDecode(serialized);
		LogoutRequest logoutRequest = parse(new String(b));

		Optional<String> sessionIndex = Optional.ofNullable(logoutRequest.getSessionIndexes()).orElse(Collections.emptyList())
			.stream()
			.findFirst()
			.map(SessionIndex::getValue);
		if (sessionIndex.isEmpty()) {
			LOGGER.warn("Missing sessionID");
			return null;
		}

		Optional<SamlJwtTokenData> jwtTokenData = samlJwtTokenStorage.loadAccessTokenBySessionId(sessionIndex.get());

		if ((jwtTokenData.isEmpty())) {
			LOGGER.warn("Missing jwtToken in a storage.");
			return null;
		}
		JwtAuthenticationToken authenticationRequest = new JwtAuthenticationToken(jwtTokenData.get().getAccessToken());
		AuthenticationManager authenticationManager = this.authenticationManagerResolver.resolve(request);
		Authentication authenticationResult = authenticationManager.authenticate(authenticationRequest);

		return new AuthenticationHolder(authenticationResult, jwtTokenData.get().getAccessToken());
	}

	private Saml2MessageBinding resolveBinding(HttpServletRequest request) {
		if (isHttpPostBinding(request)) {
			return Saml2MessageBinding.POST;
		} else if (isHttpRedirectBinding(request)) {
			return Saml2MessageBinding.REDIRECT;
		}
		throw new Saml2Exception("Unable to determine message binding from request.");
	}

	private String getRegistrationId(Authentication authentication) {
		if (authentication == null) {
			return null;
		}
		Object principal = authentication.getPrincipal();
		if (principal instanceof Saml2AuthenticatedPrincipal authenticatedPrincipal) {
			return authenticatedPrincipal.getRelyingPartyRegistrationId();
		}
		return null;
	}

	static class AuthenticationHolder {
		private Authentication authentication;
		private String accessToken;

		AuthenticationHolder(Authentication authentication, String accessToken) {
			super();
			this.authentication = authentication;
			this.accessToken = accessToken;
		}

	}

	static class HeaderMapRequestWrapper extends HttpServletRequestWrapper {
		/**
		 * construct a wrapper for this request
		 * 
		 * @param request
		 */
		public HeaderMapRequestWrapper(HttpServletRequest request) {
			super(request);
		}

		private Map<String, String> headerMap = new HashMap<String, String>();

		/**
		 * add a header with given name and value
		 * 
		 * @param name
		 * @param value
		 */
		public void addHeader(String name, String value) {
			headerMap.put(name, value);
		}

		@Override
		public String getHeader(String name) {
			String headerValue = super.getHeader(name);
			if (headerMap.containsKey(name)) {
				headerValue = headerMap.get(name);
			}
			return headerValue;
		}

		/**
		 * get the Header names
		 */
		@Override
		public Enumeration<String> getHeaderNames() {
			List<String> names = Collections.list(super.getHeaderNames());
			for (String name : headerMap.keySet()) {
				names.add(name);
			}
			return Collections.enumeration(names);
		}

		@Override
		public Enumeration<String> getHeaders(String name) {
			List<String> values = Collections.list(super.getHeaders(name));
			if (headerMap.containsKey(name)) {
				values.add(headerMap.get(name));
			}
			return Collections.enumeration(values);
		}

	}

	private boolean isHttpRedirectBinding(HttpServletRequest request) {
		return request != null && "GET".equalsIgnoreCase(request.getMethod());
	}

	private boolean isHttpPostBinding(HttpServletRequest request) {
		return request != null && "POST".equalsIgnoreCase(request.getMethod());
	}

	private byte[] samlDecode(String s) {
		return Base64.getMimeDecoder().decode(s);
	}

	private LogoutRequest parse(String request) throws Saml2Exception {
		try {
			Document document = this.parserPool
				.parse(new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8)));
			Element element = document.getDocumentElement();
			return (LogoutRequest) this.unmarshaller.unmarshall(element);
		} catch (Exception ex) {
			throw new Saml2Exception("Failed to deserialize LogoutRequest", ex);
		}
	}

}
