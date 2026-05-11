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
package com.mgmtp.a12.uaa.client.rest.auth.token.internal.jwt;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.htmlunit.AbstractPage;
import org.htmlunit.FormEncodingType;
import org.htmlunit.HttpMethod;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.WebWindow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import com.mgmtp.a12.uaa.client.rest.auth.TokenRefresher;
import com.mgmtp.a12.uaa.client.rest.auth.internal.locator.AuthorizationDataLocator;
import com.mgmtp.a12.uaa.client.rest.auth.internal.store.AtomicAuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.CodeExchangeUtils;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.Request;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.TokenType;
import com.mgmtp.a12.uaa.client.rest.config.common.UrlProperty;
import com.mgmtp.a12.uaa.client.rest.config.properties.SamlProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.SsoProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientAuthenticationProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

@ExtendWith(MockitoExtension.class)
public class UAAJwtSamlTokenAcquirerTest {

	private static final String LOGIN_URI = "http://localhost:8080/saml2/authenticate/uaa";
	private static final String EXCHANGE_REQUEST_URI = "http://localhost:8080/uaa-authentication/exchangeAuthorizationCodeToToken";
	private static final String EXCHANGE_REDIRECT_URI = "http://localhost:8080?exchangeAuthorizationCodeToToken=true";
	private static final String ERROR_URI = "http://localhost:8080/error";

	private UAAJwtSamlTokenAcquirer uaaJwtSamlTokenAcquirer;
	private UAARestClientProperties uaaRestClientProperties;
	private Page tokenExchangeRedirectPage;
	@Mock
	AuthorizationDataLocator<Page> authorizationDataLocator;
	@Mock
	private WebClient webClient;
	@Mock
	private WebRequest redirectExchangeTokenWebRequest;
	@Mock
	private WebResponse redirectExchangeTokenWebResponse;
	@Mock
	private WebWindow redirectExchangeTokenWebWindow;

	@Mock
	private WebRequest exchangeTokenWebRequest;
	@Mock
	private WebResponse exchangeTokenWebResponse;

	@Mock
	private WebWindow exchangeTokenWebWindow;

	@Mock
	private WebResponse exchangeTokenAuthorizeWebResponse;

	@Mock
	private WebWindow exchangeTokenAuthorizeWebWindow;

	@Mock
	private TokenRefresher tokenRefresher;

	@BeforeEach
	void setUp() {
		uaaRestClientProperties = new UAARestClientProperties();
		uaaRestClientProperties.setUaaBase(new UrlProperty("http://localhost:8080"));
		uaaRestClientProperties.setAuthorizationHeaderName("Authorization");
		uaaRestClientProperties.setGeneratedTokenHeaderName("access_token");
		uaaRestClientProperties.setGeneratedTokenRenewInSecondsHeaderName("token_renew_in_seconds");
		UAARestClientAuthenticationProperties authConfiguration = new UAARestClientAuthenticationProperties();
		authConfiguration.setUsername("admin");
		authConfiguration.setPassword("admin");
		SamlProperties samlProperties = new SamlProperties();
		SsoProperties ssoProperties = new SsoProperties();
		ssoProperties.setUsernameXpath("//input[@name='username']");
		ssoProperties.setPasswordXpath("//input[@name='password']");
		ssoProperties.setLoginButtonXpath("//button[@name='login']");
		samlProperties.setSsoConfiguration(ssoProperties);
		samlProperties.setLoginRelative(new UrlProperty("/saml2/authenticate/uaa"));
		samlProperties.setLogoutRelative(new UrlProperty("/user/logout"));
		samlProperties.setTokenType(TokenType.UAABEARER);
		authConfiguration.setSaml(samlProperties);
		uaaRestClientProperties.setAuthenticationConfiguration(authConfiguration);
		uaaJwtSamlTokenAcquirer =
			new UAAJwtSamlTokenAcquirer(uaaRestClientProperties, new AtomicAuthorizationDataStore(), authorizationDataLocator, tokenRefresher);
		tokenExchangeRedirectPage = new AbstractPage(redirectExchangeTokenWebResponse, redirectExchangeTokenWebWindow);
	}

	@Test
	void createLoginRequestTestSuccessful() throws MalformedURLException {
		Request loginRequest = uaaJwtSamlTokenAcquirer.createLoginRequest();

		Assertions.assertEquals(HttpMethod.GET, loginRequest.getRequest().getHttpMethod());
		Assertions.assertEquals(FormEncodingType.URL_ENCODED, loginRequest.getRequest().getEncodingType());
		Assertions.assertEquals("*/*", loginRequest.getRequest().getAdditionalHeader(HttpHeaders.ACCEPT));
		Assertions.assertEquals(LOGIN_URI, loginRequest.getRequest().getUrl().toString());
	}

	@Test
	void exchangeAuthorizationCodeTestSuccessful() throws IOException {
		try (MockedStatic<CodeExchangeUtils> utilities = Mockito.mockStatic(CodeExchangeUtils.class)) {
			Mockito.when(redirectExchangeTokenWebResponse.getWebRequest()).thenReturn(redirectExchangeTokenWebRequest);
			Mockito.when(redirectExchangeTokenWebRequest.getUrl()).thenReturn(new URL(EXCHANGE_REDIRECT_URI));

			utilities.when(CodeExchangeUtils::generateState).thenReturn("state");
			utilities.when(CodeExchangeUtils::generateCodeVerifier).thenReturn("code_v");
			utilities.when(() -> CodeExchangeUtils.generateCodeChallenge("code_v")).thenReturn("code_c");

			Mockito.when(exchangeTokenAuthorizeWebResponse.getContentAsString()).thenReturn("{\"state\": \"state\"}");

			Mockito.when(exchangeTokenWebResponse.getWebRequest()).thenReturn(exchangeTokenWebRequest);
			Mockito.when(exchangeTokenWebRequest.getUrl()).thenReturn(new URL(EXCHANGE_REQUEST_URI));

			Mockito.when(webClient.getPage(Mockito.any(WebRequest.class)))
				.thenReturn(new AbstractPage(exchangeTokenAuthorizeWebResponse, exchangeTokenAuthorizeWebWindow))
				.thenReturn(new AbstractPage(exchangeTokenWebResponse, exchangeTokenWebWindow));
			Page exchangeAuthorizationCodePage = uaaJwtSamlTokenAcquirer.exchangeAuthorizationCode(webClient, tokenExchangeRedirectPage, null);
			Assertions.assertEquals(EXCHANGE_REQUEST_URI, exchangeAuthorizationCodePage.getUrl().toString());
		}
	}

	@Test
	void exchangeAuthorizationCodeTestFail() throws IOException {
		Mockito.when(redirectExchangeTokenWebResponse.getWebRequest()).thenReturn(redirectExchangeTokenWebRequest);
		Mockito.when(redirectExchangeTokenWebRequest.getUrl()).thenReturn(new URL(ERROR_URI));

		Page exchangeAuthorizationCodePage = uaaJwtSamlTokenAcquirer.exchangeAuthorizationCode(webClient, tokenExchangeRedirectPage, null);
		Assertions.assertEquals(ERROR_URI, exchangeAuthorizationCodePage.getUrl().toString());
	}

}
