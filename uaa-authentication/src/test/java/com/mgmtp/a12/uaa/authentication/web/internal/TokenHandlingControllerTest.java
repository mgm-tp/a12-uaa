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
package com.mgmtp.a12.uaa.authentication.web.internal;

import java.io.IOException;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.config.common.UrlProperty;
import com.mgmtp.a12.uaa.authentication.internal.RedirectSupport;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenData;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenVerifier;
import com.mgmtp.a12.uaa.authentication.jwt.internal.renew.RenewTokenService;
import com.mgmtp.a12.uaa.authentication.saml.AuthorizationCodeStorage;
import com.mgmtp.a12.uaa.authentication.saml.internal.SamlTokenExchangeService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TokenHandlingControllerTest {

	private static final String STATE_KEY = "state";
	private static final String CODE_KEY = "code";
	private static final String TOKEN_KEY = "access_token";
	private static final String TOKEN_EXPIRATION_IN_SECONDS = "token_expiration_in_seconds";
	private static final String TOKEN_RENEW_IN_SECONDS = "token_renew_in_seconds";

	@InjectMocks
	private final TokenHandlingController tokenHandlingController = new TokenHandlingController();

	@Mock
	private AuthorizationCodeStorage samlAuthorizationCodeStorage;

	@Mock
	private JwtTokenVerifier jwtTokenVerifier;

	@Mock
	private RenewTokenService renewTokenService;

	@Mock
	private RedirectSupport loginRedirectSupport;

	@Mock
	private MockHttpServletRequest request;

	@Mock
	private MockHttpServletResponse response;

	@Mock
	private UrlProperty failure;

	@Mock
	private AuthenticationProperties authenticationProperties;

	@Mock
	private SamlTokenExchangeService samlTokenExchangeService;

	@Mock
	private ServletContext servletContext;

	@BeforeEach
	public void init() {
		AuthenticationProperties.Cors cors = new AuthenticationProperties.Cors();
		cors.setExposedHeaders(List.of(TOKEN_KEY));
		cors.setExposedHeaders(List.of(TOKEN_EXPIRATION_IN_SECONDS));
		cors.setExposedHeaders(List.of(TOKEN_RENEW_IN_SECONDS));
		Mockito.when(authenticationProperties.getCors()).thenReturn(cors);

		ReflectionTestUtils.setField(tokenHandlingController, "renewTokenService", renewTokenService);
		ReflectionTestUtils.setField(tokenHandlingController, "jwtTokenVerifier", jwtTokenVerifier);
		ReflectionTestUtils.setField(tokenHandlingController, "loginRedirectSupport", Optional.of(loginRedirectSupport));
		ReflectionTestUtils.setField(tokenHandlingController, "samlTokenExchangeService", Optional.of(samlTokenExchangeService));
	}

	@Test
	void exchangeAuthorizationCodeToTokenSuccessfullyTest() throws IOException {
		Calendar calendarInstance = Calendar.getInstance();
		calendarInstance.add(Calendar.MINUTE, 1);
		Mockito.when(samlTokenExchangeService.verifyAndClaimToken(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of("token"));
		JwtTokenData jwtTokenData =
			new JwtTokenData.Builder("user").withToken("token").withIssuedTime(Instant.now()).withExpirationSeconds(100).withTokenRenewThresholdInSeconds(50)
				.build();

		Mockito.when(jwtTokenVerifier.unpackToken(Mockito.anyString())).thenReturn(jwtTokenData);
		Cookie[] cookies = new Cookie[] { new Cookie("authorizationCode", "12345") };
		Mockito.when(request.getCookies()).thenReturn(cookies);
		Mockito.when(request.getServletContext()).thenReturn(servletContext);
		Mockito.when(servletContext.getContextPath()).thenReturn("http://localhost");

		TokenFormData tokenFormData = new TokenFormData("code", "code_verifier");
		ResponseEntity<?> tokenResponse = tokenHandlingController.exchangeAuthorizationCodeToToken(request, response, tokenFormData);
		Assertions.assertEquals(HttpStatus.OK, tokenResponse.getStatusCode());
		Assertions.assertEquals("token", tokenResponse.getHeaders().get(TOKEN_KEY).get(0));
		Assertions.assertEquals(String.valueOf(100), tokenResponse.getHeaders().get(TOKEN_EXPIRATION_IN_SECONDS).get(0));
		Assertions.assertEquals(String.valueOf(50), tokenResponse.getHeaders().get(TOKEN_RENEW_IN_SECONDS).get(0));

		Mockito.verify(loginRedirectSupport, Mockito.never()).performFailureRedirect(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

	}

	@Test
	void exchangeAuthorizationCodeToTokenFailureWithRedirectTest() throws IOException {
		Mockito.when(samlAuthorizationCodeStorage.loadAccessTokenByAuthorizationCode("12345")).thenReturn(Optional.empty());
		Cookie[] cookies = new Cookie[] { new Cookie("uaa_failure", "http://localhost:3000"), new Cookie("authorizationCode", "none") };
		Mockito.when(request.getCookies()).thenReturn(cookies);
		Mockito.when(renewTokenService.isRequestTokenValid(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
		TokenFormData tokenFormData = new TokenFormData("code", "code_verifier");
		tokenHandlingController.exchangeAuthorizationCodeToToken(request, response, tokenFormData);
		Mockito.verify(loginRedirectSupport, Mockito.atLeastOnce()).performFailureRedirect(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void exchangeAuthorizationCodeToTokenFailureWithoutRedirectTest() throws IOException {
		Mockito.when(samlAuthorizationCodeStorage.loadAccessTokenByAuthorizationCode("12345")).thenReturn(Optional.empty());
		Mockito.when(failure.getUrl()).thenReturn("");
		Cookie[] cookies = new Cookie[] { new Cookie("uaa_failure", "http://localhost:3000"), new Cookie("authorizationCode", "none") };
		Mockito.when(request.getCookies()).thenReturn(cookies);
		Mockito.when(renewTokenService.isRequestTokenValid(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
		TokenFormData tokenFormData = new TokenFormData("code", "code_verifier");
		ResponseEntity<?> tokenResponse = tokenHandlingController.exchangeAuthorizationCodeToToken(request, response, tokenFormData);
		Assertions.assertNull(tokenResponse);
	}

	@Test
	void exchangeAuthorizationCodeWithNoCode() {
		Mockito.when(samlAuthorizationCodeStorage.loadAccessTokenByAuthorizationCode("12345")).thenReturn(Optional.empty());
		Cookie[] cookies = new Cookie[] { new Cookie("uaa_failure", "http://localhost:3000") };
		Mockito.when(request.getCookies()).thenReturn(cookies);
		Mockito.when(renewTokenService.isRequestTokenValid(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
		TokenFormData tokenFormData = new TokenFormData("code", "code_verifier");
		ResponseEntity<?> tokenResponse = tokenHandlingController.exchangeAuthorizationCodeToToken(request, response, tokenFormData);
		Assertions.assertNull(tokenResponse);
	}

	@Test
	void authorizeExchangeAuthorizationCodeToTokenSuccessfullyTest() throws IOException {
		Calendar calendarInstance = Calendar.getInstance();
		calendarInstance.add(Calendar.MINUTE, 1);
		Mockito.when(samlAuthorizationCodeStorage.loadAccessTokenByAuthorizationCode("12345")).thenReturn(Optional.of("token"));

		Cookie[] cookies = new Cookie[] { new Cookie("authorizationCode", "12345") };
		Mockito.when(request.getCookies()).thenReturn(cookies);

		var codeExchangeRequest = new CodeExchangeRequest();
		codeExchangeRequest.setState("state");
		codeExchangeRequest.setCodeChallenge("code_c");
		ResponseEntity<?> authorizeResponse = tokenHandlingController.authorizeExchangeAuthorizationCodeToToken(request, response, codeExchangeRequest);
		Assertions.assertEquals(HttpStatus.OK, authorizeResponse.getStatusCode());

		Mockito.verify(loginRedirectSupport, Mockito.never()).performFailureRedirect(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void authorizeExchangeAuthorizationCodeToTokenFailureWithRedirectTest() throws IOException {
		Mockito.when(samlAuthorizationCodeStorage.loadAccessTokenByAuthorizationCode("12345")).thenReturn(Optional.empty());
		Mockito.when(samlTokenExchangeService.isCodeChallengeValid(Mockito.anyString())).thenReturn(true);
		var codeExchangeRequest = new CodeExchangeRequest();
		codeExchangeRequest.setState("state");
		codeExchangeRequest.setCodeChallenge("code_c");
		tokenHandlingController.authorizeExchangeAuthorizationCodeToToken(request, response, codeExchangeRequest);
		Mockito.verify(loginRedirectSupport, Mockito.atLeastOnce()).performFailureRedirect(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void authorizeSuccessfullyTest() {
		Mockito.when(renewTokenService.isRequestAuthorizeValid(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
		Mockito.when(renewTokenService.authorize(Mockito.anyString(), Mockito.anyString())).thenReturn("code");

		var codeExchangeRequest = new CodeExchangeRequest();
		codeExchangeRequest.setState("state");
		codeExchangeRequest.setCodeChallenge("code_c");
		codeExchangeRequest.setIdTokenHint("id_token_hint");
		ResponseEntity<?> responseEntity = tokenHandlingController.authorize(codeExchangeRequest);
		Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		Map<String, Object> body = (Map<String, Object>) responseEntity.getBody();
		Assertions.assertEquals("code", String.valueOf(body.get(CODE_KEY)));
		Assertions.assertEquals("state", String.valueOf(body.get(STATE_KEY)));
	}

	@Test
	void authorizeFailureTest() {
		Mockito.when(renewTokenService.isRequestAuthorizeValid(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

		var codeExchangeRequest = new CodeExchangeRequest();
		codeExchangeRequest.setState("state");
		codeExchangeRequest.setCodeChallenge("code_c");
		codeExchangeRequest.setIdTokenHint("id_token_hint");
		ResponseEntity<?> responseEntity = tokenHandlingController.authorize(codeExchangeRequest);
		Assertions.assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
	}

	@Test
	void tokenSuccessfullyTest() {
		Mockito.when(renewTokenService.isRequestTokenValid(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
		JwtTokenData jwtTokenData = new JwtTokenData.Builder("user")
			.withToken("new_token")
			.withIssuedTime(Instant.now())
			.withExpirationSeconds(100)
			.withTokenRenewThresholdInSeconds(50)
			.build();
		Mockito.when(renewTokenService.generateNewToken(Mockito.anyString())).thenReturn(jwtTokenData);

		TokenFormData tokenFormData = new TokenFormData("code", "code_verifier");
		ResponseEntity<Map> responseEntity =
			(ResponseEntity<Map>) tokenHandlingController.token(tokenFormData);
		Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		Assertions.assertEquals("new_token", responseEntity.getBody().get(TOKEN_KEY));
		Assertions.assertEquals(String.valueOf(100), responseEntity.getBody().get(TOKEN_EXPIRATION_IN_SECONDS));
		Assertions.assertEquals(String.valueOf(50), responseEntity.getBody().get(TOKEN_RENEW_IN_SECONDS));
	}

	@Test
	void tokenFailureTest() {
		Mockito.when(renewTokenService.isRequestTokenValid(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

		ResponseEntity<?> responseEntity = tokenHandlingController.token(new TokenFormData("code", "code_verifier"));
		Assertions.assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
	}

	@Test
	void tokenFailureWithEmptyValuesTest() {
		Mockito.when(renewTokenService.isRequestTokenValid(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

		ResponseEntity<?> responseEntity = tokenHandlingController.token(new TokenFormData("", " "));
		Assertions.assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
	}

}
