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
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import jakarta.servlet.http.Cookie;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider.ResponseToken;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.AuthenticationProperties.RedirectHolder;
import com.mgmtp.a12.uaa.authentication.AuthenticationProperties.SamlProperties;
import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.config.common.UrlProperty;
import com.mgmtp.a12.uaa.authentication.internal.RedirectSupport;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenData;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenGenerator;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenVerifier;
import com.mgmtp.a12.uaa.authentication.principal.UAAPrincipal;
import com.mgmtp.a12.uaa.authentication.saml.AuthorizationCodeStorage;
import com.mgmtp.a12.uaa.authentication.saml.SamlJwtTokenStorage;
import com.mgmtp.a12.uaa.authentication.security.login.internal.TypedUsernamePasswordAuthenticationToken;
import com.mgmtp.a12.uaa.authentication.utils.UserDataCreator;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SamlAuthenticationSuccessHandlerTest {

	private static final String REDIRECT_URL_ABSOLUTE = "http://redirect.com";

	@Mock
	private ResponseToken responseToken;
	@Mock
	private Response response;
	@Mock
	private Assertion assertion;
	@Mock
	private AuthnStatement authnStatement;
	@Mock
	private JwtTokenGenerator jwtTokenGeneratorSupport;
	@Mock
	private JwtTokenVerifier jwtTokenVerifier;
	@Mock
	private AuthenticationProperties authenticationProperties;
	@Mock
	private SamlProperties samlProperties;
	@Mock
	private RedirectHolder logoutProperties;
	@Mock
	private AuthenticationProperties.Redirect redirect;
	@Mock
	private UrlProperty success;
	@Mock
	private AuthorizationCodeStorage authorizationCodeStorage;
	@Mock
	private SamlJwtTokenStorage samlJwtTokenStorage;
	@Mock
	private RedirectSupport redirectSupport;
	@InjectMocks
	private SamlAuthenticationSuccessHandler samlAuthenticationSuccessHandler = new SamlAuthenticationSuccessHandler(true, false, 5);
	private UAAPrincipal<?> user;
	private TypedUsernamePasswordAuthenticationToken<ResponseToken> authReq;
	private Integer tokenRenewInSeconds;

	@BeforeEach
	public void setUp() throws Exception {
		user = UserDataCreator.createUser("test", "password");
		authReq = new TypedUsernamePasswordAuthenticationToken<ResponseToken>(user, "password", AuthenticationType.SAML, Collections.emptyList());
		authReq.setAuthenticationData(responseToken);

		tokenRenewInSeconds =50;
		JwtTokenData jwtTokenData = new JwtTokenData.Builder("user")
			.withToken("JWT-TOKEN")
			.withIssuedTime(Instant.now())
			.withExpirationSeconds(100)
			.withTokenRenewThresholdInSeconds(50)
			.build();
		Mockito.when(jwtTokenGeneratorSupport.generateToken(Mockito.any())).thenReturn(jwtTokenData);
		Mockito.when(jwtTokenVerifier.unpackToken(Mockito.any())).thenReturn(jwtTokenData);
		Mockito.when(authenticationProperties.getLogout()).thenReturn(logoutProperties);
		Mockito.when(authenticationProperties.getLogout().getRedirect()).thenReturn(redirect);
		Mockito.when(authenticationProperties.getLogout().getRedirect().getSuccess()).thenReturn(success);
		Mockito.when(authenticationProperties.getLogout().getRedirect().getSuccess().getUrl()).thenReturn(REDIRECT_URL_ABSOLUTE);
		Mockito.when(authenticationProperties.getSaml()).thenReturn(samlProperties);
		Mockito.when(responseToken.getResponse()).thenReturn(response);
		Mockito.when(response.getInResponseTo()).thenReturn("saml_request_id");
		Mockito.when(authnStatement.getSessionIndex()).thenReturn("session-01");
		Mockito.when(assertion.getAuthnStatements()).thenReturn(Arrays.asList(authnStatement));
		Mockito.when(response.getAssertions()).thenReturn(Arrays.asList(assertion));
	}

	@Test
	public void authenticationSuccessWithoutSuccessCookieAndNOConfiguration() throws Exception {
		Mockito.when(authenticationProperties.getLogout().getRedirect().getSuccess().getUrl()).thenReturn(null);

		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		MockHttpServletResponse httpResponse = new MockHttpServletResponse();
		httpRequest.setCookies(new Cookie(UAASamlAuthenticationRequestFilter.COOKIE_SAML_REQUEST_ID, "saml_request_id"));
		samlAuthenticationSuccessHandler.onAuthenticationSuccess(httpRequest, httpResponse, authReq);
		assertSuccessOutputHandler(httpResponse);

		Mockito.verify(samlJwtTokenStorage, Mockito.times(1)).storeJwtToken(ArgumentMatchers.eq("session-01"), ArgumentMatchers.any());
	}

	@Test
	public void authenticationSuccessWithoutSuccessCookieAndAbsoluteUrl() throws Exception {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		MockHttpServletResponse httpResponse = new MockHttpServletResponse();
		httpRequest.setCookies(new Cookie(UAASamlAuthenticationRequestFilter.COOKIE_SAML_REQUEST_ID, "saml_request_id"));
		samlAuthenticationSuccessHandler.onAuthenticationSuccess(httpRequest, httpResponse, authReq);
		assertSuccessOutputHandler(httpResponse);

	}

	@Test
	public void authenticationSuccessWithSuccessCookieAndAbsoluteUrl() throws Exception {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		MockHttpServletResponse httpResponse = new MockHttpServletResponse();
		Mockito.when(redirectSupport.performSuccessRedirect(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Boolean.TRUE);
		httpRequest.setCookies(new Cookie(UAASamlAuthenticationRequestFilter.COOKIE_SAML_REQUEST_ID, "saml_request_id"));
		samlAuthenticationSuccessHandler.onAuthenticationSuccess(httpRequest, httpResponse, authReq);
		assertSuccessOutputHandler(httpResponse);

	}

	@Test
	public void authenticationSuccessWithSuccessCookieAndRelativeUrl() throws Exception {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		MockHttpServletResponse httpResponse = new MockHttpServletResponse();
		Mockito.when(redirectSupport.performSuccessRedirect(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Boolean.TRUE);
		httpRequest.setCookies(new Cookie(UAASamlAuthenticationRequestFilter.COOKIE_SAML_REQUEST_ID, "saml_request_id"));
		samlAuthenticationSuccessHandler.onAuthenticationSuccess(httpRequest, httpResponse, authReq);
		assertSuccessOutputHandler(httpResponse);

	}

	@Test
	public void authenticationWithEmptyRequestIdCookie() throws IOException {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		MockHttpServletResponse httpResponse = new MockHttpServletResponse();
		samlAuthenticationSuccessHandler.onAuthenticationSuccess(httpRequest, httpResponse, authReq);
		Mockito.verify(redirectSupport, Mockito.atLeastOnce()).performFailureRedirect(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	public void authenticationHandlerWithEmptyInResponseToSupport() throws IOException {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		MockHttpServletResponse httpResponse = new MockHttpServletResponse();
		httpRequest.setCookies(new Cookie(UAASamlAuthenticationRequestFilter.COOKIE_SAML_REQUEST_ID, "saml_request_id"));
		Mockito.when(response.getInResponseTo()).thenReturn("");
		samlAuthenticationSuccessHandler.onAuthenticationSuccess(httpRequest, httpResponse, authReq);
		Mockito.verify(redirectSupport, Mockito.atLeastOnce()).performFailureRedirect(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	public void uaaAuthenticationHandlerWithRequestAndInResponseToDoesNotMatchSupport() throws IOException {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		MockHttpServletResponse httpResponse = new MockHttpServletResponse();
		httpRequest.setCookies(new Cookie(UAASamlAuthenticationRequestFilter.COOKIE_SAML_REQUEST_ID, "saml_request_id"));
		Mockito.when(response.getInResponseTo()).thenReturn("wrong_request_id");
		samlAuthenticationSuccessHandler.onAuthenticationSuccess(httpRequest, httpResponse, authReq);
		Mockito.verify(redirectSupport, Mockito.atLeastOnce()).performFailureRedirect(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	private void assertSuccessOutputHandler(MockHttpServletResponse httpResponse) throws IOException {
		MatcherAssert.assertThat(httpResponse.getCookie(RedirectSupport.COOKIE_SUCCESS), Matchers.nullValue());
		MatcherAssert.assertThat(httpResponse.getCookie(RedirectSupport.COOKIE_FAILURE), Matchers.nullValue());
		MatcherAssert.assertThat(httpResponse.getCookie(UAASamlAuthenticationRequestFilter.COOKIE_SAML_REQUEST_ID), Matchers.notNullValue());
		MatcherAssert.assertThat(httpResponse.getCookie(UAASamlAuthenticationRequestFilter.COOKIE_SAML_REQUEST_ID).getMaxAge(),
			Matchers.is(Integer.valueOf(0)));
		Mockito.verify(redirectSupport, Mockito.never()).performFailureRedirect(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(authorizationCodeStorage, Mockito.atLeastOnce()).storeAuthorizationCode(Mockito.any(), Mockito.any());
	}
}
