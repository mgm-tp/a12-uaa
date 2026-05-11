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
package com.mgmtp.a12.uaa.client.rest.auth.token.internal.oauth2;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

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
import com.mgmtp.a12.uaa.client.rest.auth.internal.store.AtomicAuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.Request;
import com.mgmtp.a12.uaa.client.rest.config.common.UrlProperty;
import com.mgmtp.a12.uaa.client.rest.config.properties.OidcProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.SsoProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientAuthenticationProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

@ExtendWith(MockitoExtension.class)
public class UAAOauth2PublicTokenAcquirerTest {

	private static final String AUTH_URI = """
		http://localhost:9090/realms/realmNameTest/protocol/openid-connect/auth?\
		client_id=clientIdTest\
		&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fcallback\
		&response_type=code\
		&scope=openid\
		&state=randomString\
		&code_challenge=y7SmQAY3jsJhhA05q2zHYEjz2tFuGbfbUI-xG6RZTFE\
		&code_challenge_method=S256\
		""";
	private static final String CALLBACK_URI = "http://localhost:3000/callback?state=randomString&code=AUTHORIZATION_CODE";
	private static final String TOKEN_URI = "http://localhost:9090/realms/realmNameTest/protocol/openid-connect/token";
	private static final String DATA_BODY = """
		client_id=clientIdTest\
		&code=AUTHORIZATION_CODE\
		&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fcallback\
		&code_verifier=randomString\
		&grant_type=authorization_code\
		""";
	private static final String PARAM_VERIFIER = "verifier";
	private static final String PARAM_STATE = "state";

	private UAAOauth2PublicTokenAcquirer uaaOauth2PublicTokenAcquirer;
	private UAARestClientProperties uaaRestClientProperties;
	private Page page;
	@Mock
	private WebClient webClient;
	@Mock
	private WebRequest webRequest;
	@Mock
	private WebResponse webResponse;
	@Mock
	private WebWindow webWindow;
	@Mock
	private Base64.Encoder encoder;
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
		authConfiguration.setLoginRelative(new UrlProperty("user/login"));

		OidcProperties oidcProperties = new OidcProperties();
		OidcProperties.PublicClientProperties publicClientProperties = new OidcProperties.PublicClientProperties();
		publicClientProperties.setClientId("clientIdTest");
		publicClientProperties.setRealmName("realmNameTest");
		publicClientProperties.setIdpBase(new UrlProperty("http://localhost:9090"));
		publicClientProperties.setLoginRelative(new UrlProperty("/auth"));
		publicClientProperties.setTokenExchangeRelative(new UrlProperty("/token"));
		publicClientProperties.setLoginRedirectRelative(new UrlProperty("/callback"));

		SsoProperties ssoProperties = new SsoProperties();
		ssoProperties.setUsernameXpath("//input[@name='username']");
		ssoProperties.setPasswordXpath("//input[@name='password']");
		ssoProperties.setLoginButtonXpath("//button[@name='login']");
		publicClientProperties.setSsoConfiguration(ssoProperties);
		oidcProperties.setPublicClient(publicClientProperties);
		authConfiguration.setOidc(oidcProperties);

		uaaRestClientProperties.setAuthenticationConfiguration(authConfiguration);
		uaaOauth2PublicTokenAcquirer = new UAAOauth2PublicTokenAcquirer(uaaRestClientProperties, new AtomicAuthorizationDataStore(), tokenRefresher);
		page = new AbstractPage(webResponse, webWindow);
	}

	@Test
	void createWebClientTest() {
		WebClient webClient = uaaOauth2PublicTokenAcquirer.createWebClient();

		Assertions.assertFalse(webClient.getOptions().isRedirectEnabled());
		Assertions.assertTrue(webClient.getOptions().isJavaScriptEnabled());
		Assertions.assertFalse(webClient.getOptions().isThrowExceptionOnFailingStatusCode());
		Assertions.assertFalse(webClient.getOptions().isThrowExceptionOnScriptError());

		webClient.close();
	}

	@Test
	void createLoginRequestTest() throws MalformedURLException {
		//Mock static method.
		try (MockedStatic<Base64> base64MockedStatic = Mockito.mockStatic(Base64.class)) {
			base64MockedStatic.when(Base64::getUrlEncoder).thenReturn(encoder);
			Mockito.when(encoder.withoutPadding()).thenReturn(encoder);
			Mockito.when(encoder.encodeToString(Mockito.any())).thenReturn("randomString");

			Request loginRequest = uaaOauth2PublicTokenAcquirer.createLoginRequest();

			Assertions.assertEquals(HttpMethod.GET, loginRequest.getRequest().getHttpMethod());
			Assertions.assertEquals(FormEncodingType.URL_ENCODED, loginRequest.getRequest().getEncodingType());
			Assertions.assertEquals("*/*", loginRequest.getRequest().getAdditionalHeader(HttpHeaders.ACCEPT));
			Assertions.assertEquals(AUTH_URI, loginRequest.getRequest().getUrl().toString());
			Assertions.assertEquals("randomString", loginRequest.getParameters().get("verifier"));
			Assertions.assertEquals("randomString", loginRequest.getParameters().get("state"));
		}
	}

	@Test
	void exchangeAuthorizationCodeTest() throws IOException {
		Mockito.when(webResponse.getResponseHeaderValue(HttpHeaders.LOCATION)).thenReturn(CALLBACK_URI);
		Request loginRequest = new Request.Builder(new WebRequest(new URL(AUTH_URI), HttpMethod.GET))
			.withParameter(PARAM_VERIFIER, "randomString")
			.withParameter(PARAM_STATE, "randomString")
			.build();

		Mockito.when(webClient.getPage(Mockito.any(WebRequest.class))).thenReturn(page);
		Mockito.when(webResponse.getWebRequest()).thenReturn(webRequest);
		Mockito.when(webRequest.getUrl()).thenReturn(new URL(TOKEN_URI));
		Mockito.when(webRequest.getRequestBody()).thenReturn(DATA_BODY);

		Page exchangeAuthorizationCodePage = uaaOauth2PublicTokenAcquirer.exchangeAuthorizationCode(webClient, page, loginRequest);

		Assertions.assertEquals(TOKEN_URI, exchangeAuthorizationCodePage.getUrl().toString());
		Assertions.assertEquals(DATA_BODY, exchangeAuthorizationCodePage.getWebResponse().getWebRequest().getRequestBody());
	}

}