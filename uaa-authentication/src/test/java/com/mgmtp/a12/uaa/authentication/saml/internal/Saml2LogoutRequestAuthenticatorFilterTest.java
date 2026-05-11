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

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.SessionIndex;
import org.opensaml.saml.saml2.core.impl.LogoutRequestUnmarshaller;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml2.core.Saml2ParameterNames;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;

import com.mgmtp.a12.uaa.authentication.principal.UAAPrincipal;
import com.mgmtp.a12.uaa.authentication.saml.SamlJwtTokenData;
import com.mgmtp.a12.uaa.authentication.saml.SamlJwtTokenStorage;
import com.mgmtp.a12.uaa.authentication.saml.SamlPrincipal;

import net.shibboleth.shared.xml.impl.BasicParserPool;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Saml2LogoutRequestAuthenticatorFilterTest {

	@Mock
	private AuthenticationManager authenticationManager;
	@Mock
	private FilterChain filterChain;
	@Mock
	private SamlJwtTokenStorage samlJwtTokenStorage;
	@Mock
	private RelyingPartyRegistrationResolver relyingPartyRegistrationResolver;
	@Mock
	private RelyingPartyRegistration relyingPartyRegistration;
	@Mock
	private LogoutRequestUnmarshaller logoutRequestUnmarshaller;
	@Mock
	private LogoutRequest logoutRequest;
	@Mock
	private SessionIndex sessionIndex;
	private Saml2LogoutRequestAuthenticatorFilter saml2LogoutRequestAuthenticatorFilter;
	private MockedStatic<ConfigurationService> configService;
	private ResourceLoader resourceLoader = new DefaultResourceLoader();
	private MockedStatic<XMLObjectProviderRegistrySupport> xmlRegistry;

	@BeforeEach
	public void init() throws Exception {
		UAAPrincipal<?> userDetail = new TestUser<>("test_server", "*****", convertAuthorities("Admin;Manager"));
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetail, null);
		Mockito.when(authenticationManager.authenticate(Mockito.any())).thenReturn(token);
		Mockito.when(relyingPartyRegistration.getSingleLogoutServiceBindings()).thenReturn(Arrays.asList(Saml2MessageBinding.POST));
		Mockito.when(relyingPartyRegistrationResolver.resolve(Mockito.any(), Mockito.eq("uaa"))).thenReturn(relyingPartyRegistration);

		BasicParserPool parserPool = new BasicParserPool();
		parserPool.initialize();
		XMLObjectProviderRegistry registry = new XMLObjectProviderRegistry();
		registry.setParserPool(parserPool);
		configService = Mockito.mockStatic(ConfigurationService.class);
		configService.when(() -> ConfigurationService.get(Mockito.eq(XMLObjectProviderRegistry.class))).thenReturn(registry);

		Mockito.when(sessionIndex.getValue()).thenReturn("test-session-index-01");
		Mockito.when(logoutRequest.getSessionIndexes()).thenReturn(Arrays.asList(sessionIndex));
		Mockito.when(logoutRequestUnmarshaller.unmarshall(Mockito.any())).thenReturn(logoutRequest);

		UnmarshallerFactory factory = new UnmarshallerFactory();
		factory.registerUnmarshaller(LogoutRequest.DEFAULT_ELEMENT_NAME, logoutRequestUnmarshaller);

		xmlRegistry = Mockito.mockStatic(XMLObjectProviderRegistrySupport.class);
		xmlRegistry.when(XMLObjectProviderRegistrySupport::getUnmarshallerFactory).thenReturn(factory);

		Mockito.when(samlJwtTokenStorage.loadAccessTokenBySessionId(Mockito.eq("test-session-index-01"))).thenReturn(Optional.of(
			new SamlJwtTokenData.Builder("accessToken").withExpirationTime(
					Instant.now().plus(Duration.ofMinutes(20)))
				.withSessionId("sessionID").build()));

		saml2LogoutRequestAuthenticatorFilter =
			new Saml2LogoutRequestAuthenticatorFilter(relyingPartyRegistrationResolver, samlJwtTokenStorage, authenticationManager);
	}

	@AfterEach
	void tearDown() {
		configService.close();
		xmlRegistry.close();
	}

	@Test
	public void checkWrongUrl() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServletPath("/wrong");
		MockHttpServletResponse response = new MockHttpServletResponse();
		saml2LogoutRequestAuthenticatorFilter.doFilter(request, response, filterChain);

		Mockito.verify(filterChain, Mockito.times(1)).doFilter(request, response);
		Mockito.verify(samlJwtTokenStorage, Mockito.times(0)).loadAccessTokenBySessionId(Mockito.anyString());
		Assertions.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
	}

	@Test
	public void checkMissingParameter() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServletPath("/logout/saml2/slo");
		MockHttpServletResponse response = new MockHttpServletResponse();
		saml2LogoutRequestAuthenticatorFilter.doFilter(request, response, filterChain);

		Mockito.verify(filterChain, Mockito.times(1)).doFilter(request, response);
		Mockito.verify(samlJwtTokenStorage, Mockito.times(0)).loadAccessTokenBySessionId(Mockito.anyString());
		Assertions.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
	}

	@Test
	public void checkProcessing() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServletPath("/logout/saml2/slo");
		request.setMethod("POST");
		MockHttpServletResponse response = new MockHttpServletResponse();

		Resource resource = resourceLoader.getResource("classpath:/saml/logoutRequest.base64");
		String parameter = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
		request.addParameter(Saml2ParameterNames.SAML_REQUEST, parameter);

		saml2LogoutRequestAuthenticatorFilter.doFilter(request, response, filterChain);

		Assertions.assertEquals(HttpServletResponse.SC_OK, response.getStatus());

		//request is different we add header to it
		Mockito.verify(filterChain, Mockito.times(1)).doFilter(ArgumentMatchers.isA(HttpServletRequest.class), ArgumentMatchers.isA(HttpServletResponse.class));
		Mockito.verify(samlJwtTokenStorage, Mockito.times(1)).loadAccessTokenBySessionId(Mockito.anyString());
		Mockito.verify(authenticationManager, Mockito.times(1)).authenticate(Mockito.any());

	}

	@Test
	public void checkWrongBinding() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServletPath("/logout/saml2/slo");
		request.setMethod("GET");
		MockHttpServletResponse response = new MockHttpServletResponse();

		Resource resource = resourceLoader.getResource("classpath:/saml/logoutRequest.base64");
		String parameter = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
		request.addParameter(Saml2ParameterNames.SAML_REQUEST, parameter);

		saml2LogoutRequestAuthenticatorFilter.doFilter(request, response, filterChain);

		Assertions.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
		Mockito.verify(filterChain, Mockito.times(0)).doFilter(ArgumentMatchers.isA(HttpServletRequest.class), ArgumentMatchers.isA(HttpServletResponse.class));
	}

	@Test
	public void checkMissingTokenInStorage() throws Exception {
		Mockito.when(samlJwtTokenStorage.loadAccessTokenBySessionId(Mockito.eq("test-session-index-01"))).thenReturn(Optional.empty());

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServletPath("/logout/saml2/slo");
		request.setMethod("GET");
		MockHttpServletResponse response = new MockHttpServletResponse();

		Resource resource = resourceLoader.getResource("classpath:/saml/logoutRequest.base64");
		String parameter = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
		request.addParameter(Saml2ParameterNames.SAML_REQUEST, parameter);

		saml2LogoutRequestAuthenticatorFilter.doFilter(request, response, filterChain);

		Assertions.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
		Mockito.verify(filterChain, Mockito.times(0)).doFilter(ArgumentMatchers.isA(HttpServletRequest.class), ArgumentMatchers.isA(HttpServletResponse.class));
	}

	private Set<GrantedAuthority> convertAuthorities(String roles) {
		return Arrays.asList(roles.split(";")).stream()
			.map(roleName -> new SimpleGrantedAuthority(roleName))
			.collect(Collectors.toSet());
	}

	static class TestUser<T> extends UAAPrincipal<T> implements SamlPrincipal {

		public TestUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
			super(username, password, authorities);
		}

		@Override
		public String getRelyingPartyRegistrationId() {
			return "uaa";
		}

		@Override
		public String getName() {
			return getUsername();
		}

	}

}
