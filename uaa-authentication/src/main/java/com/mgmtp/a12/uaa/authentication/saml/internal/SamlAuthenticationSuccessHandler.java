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

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.saml2.provider.service.authentication.AbstractSaml2AuthenticationRequest;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider.ResponseToken;
import org.springframework.util.CollectionUtils;

import com.mgmtp.a12.uaa.authentication.internal.RedirectSupport;
import com.mgmtp.a12.uaa.authentication.internal.RedirectType;
import com.mgmtp.a12.uaa.authentication.internal.RedirectType.Type;
import com.mgmtp.a12.uaa.authentication.jwt.internal.CookieUtil;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenVerifier;
import com.mgmtp.a12.uaa.authentication.saml.AuthorizationCodeStorage;
import com.mgmtp.a12.uaa.authentication.saml.SamlJwtTokenData;
import com.mgmtp.a12.uaa.authentication.saml.SamlJwtTokenData.Builder;
import com.mgmtp.a12.uaa.authentication.saml.SamlJwtTokenStorage;
import com.mgmtp.a12.uaa.authentication.security.login.internal.TypedUsernamePasswordAuthenticationToken;
import com.mgmtp.a12.uaa.authentication.security.login.internal.UAAAuthenticationSuccessHandler;

public class SamlAuthenticationSuccessHandler extends UAAAuthenticationSuccessHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(SamlAuthenticationSuccessHandler.class);

	private static final int AUTHORIZATION_CODE_CHARACTER_LENGTH = 128;
	private static final String AUTHORIZATION_CODE = "authorizationCode";
	private static final String EXCHANGE_AUTHORIZATION_CODE_TO_TOKEN = "exchangeAuthorizationCodeToToken";

	private final boolean httpOnly;
	private final boolean secured;
	private final int authorizationCodeExpirationSeconds;

	@Inject
	private AuthorizationCodeStorage authorizationCodeStorage;
	@Inject
	@RedirectType(type = Type.LOGIN)
	private RedirectSupport loginRedirectSupport;
	@Inject
	private SamlJwtTokenStorage samlJwtTokenStorage;
	@Inject
	private JwtTokenVerifier jwtTokenVerifier;

	public SamlAuthenticationSuccessHandler(boolean httpOnly, boolean secured, int authorizationCodeExpirationSeconds) {
		this.httpOnly = httpOnly;
		this.secured = secured;
		this.authorizationCodeExpirationSeconds = authorizationCodeExpirationSeconds;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
		try {
			LOGGER.debug("User [{}] has been authenticated", authentication.getName());

			@SuppressWarnings("unchecked")
			TypedUsernamePasswordAuthenticationToken<ResponseToken> samlUsernamePasswordAuthenticationToken =
				((TypedUsernamePasswordAuthenticationToken<ResponseToken>) authentication);
			ResponseToken samlResponse = samlUsernamePasswordAuthenticationToken.getAuthenticationData();
			if (samlResponse == null || StringUtils.isEmpty(samlResponse.getResponse().getInResponseTo())) {
				throw new ServletException("The In Response To ID could not be found");
			}

			String samlRequestId = Optional.ofNullable(samlResponse.getToken().getAuthenticationRequest())
				.map(AbstractSaml2AuthenticationRequest::getId)
				.filter(StringUtils::isNotEmpty)
				.orElseThrow(() -> new ServletException("The SAML authentication request could not be found"));

			if (!samlRequestId.equals(samlResponse.getResponse().getInResponseTo())) {
				throw new ServletException("The Request and In Response To ID does not match");
			}

			String authorizationCode = generateAuthorizationCode();
			String jwtToken = generateJwtToken((UserDetails) authentication.getPrincipal()).getToken();
			authorizationCodeStorage.storeAuthorizationCode(authorizationCode, jwtToken);
			storeGeneratedTokenToStorage(jwtToken, samlResponse);
			Map<String, String> parameters = new HashMap<>();
			parameters.put(EXCHANGE_AUTHORIZATION_CODE_TO_TOKEN, String.valueOf(true));
			response.addCookie(
				CookieUtil.createCookie(AUTHORIZATION_CODE, authorizationCode, "", request, httpOnly, secured, authorizationCodeExpirationSeconds));
			loginRedirectSupport.performSuccessRedirect(getRedirectStrategy(), request, response, parameters);
		} catch (Exception e) {
			LOGGER.error("The authentication success handler is not finished [{}]", e.getMessage());
			loginRedirectSupport.performFailureRedirect(getRedirectStrategy(), request, response, null);
		}

	}

	private String generateAuthorizationCode() {
		return RandomStringUtils.random(AUTHORIZATION_CODE_CHARACTER_LENGTH, 0, 0, true, true, null,
			new SecureRandom());
	}

	private SamlJwtTokenData storeGeneratedTokenToStorage(String accessToken, ResponseToken samlResponse) {
		Instant expirationDate = jwtTokenVerifier.unpackToken(accessToken).getExpirationTime();
		Assertion assertion = CollectionUtils.firstElement(samlResponse.getResponse().getAssertions());
		Builder jwtDataBuilder = new SamlJwtTokenData.Builder(accessToken).withExpirationTime(expirationDate);
		SamlJwtTokenData jwtTokenData;
		if (assertion != null) {
			String sessionIndex = assertion.getAuthnStatements().get(0).getSessionIndex();
			jwtTokenData = jwtDataBuilder.withSessionId(sessionIndex).build();
			if (StringUtils.isNotEmpty(sessionIndex)) {
				jwtTokenData = samlJwtTokenStorage.storeJwtToken(sessionIndex, jwtTokenData);
			}
		} else {
			jwtTokenData = jwtDataBuilder.build();
		}
		LOGGER.debug("JWT token to session mapping generated [{}]", jwtTokenData);
		return jwtTokenData;
	}

}
