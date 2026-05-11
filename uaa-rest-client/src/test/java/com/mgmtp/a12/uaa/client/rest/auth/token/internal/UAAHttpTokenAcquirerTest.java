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
package com.mgmtp.a12.uaa.client.rest.auth.token.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.HttpMethod;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.TokenRefresher;
import com.mgmtp.a12.uaa.client.rest.auth.internal.WebClientFactory;
import com.mgmtp.a12.uaa.client.rest.auth.internal.locator.AuthorizationDataLocator;
import com.mgmtp.a12.uaa.client.rest.auth.internal.store.AtomicAuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.config.common.UrlProperty;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientAuthenticationProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UAAHttpTokenAcquirerTest {

	private static final String LOGIN_URI = "http://localhost:8080/user/login";

	private UAAHttpTokenAcquirer uaaHttpTokenAcquirer;
	private UAARestClientProperties uaaRestClientProperties;
	@Mock
	private AuthorizationDataLocator<Page> authorizationDataLocator;
	@Mock
	private Page loginResponsePage;
	@Mock
	private static WebClient webClient;
	@Mock
	private WebResponse webResponse;
	@Mock
	private TokenRefresher tokenRefresher;

	@BeforeEach
	void setUp() {
		uaaRestClientProperties = new UAARestClientProperties();
		uaaRestClientProperties.setUaaBase(new UrlProperty("http://localhost:8080/"));
		uaaRestClientProperties.setAuthorizationHeaderName("Authorization");
		uaaRestClientProperties.setGeneratedTokenHeaderName("access_token");
		uaaRestClientProperties.setGeneratedTokenRenewInSecondsHeaderName("token_renew_in_seconds");
		UAARestClientAuthenticationProperties authConfiguration = new UAARestClientAuthenticationProperties();
		authConfiguration.setUsername("admin");
		authConfiguration.setPassword("admin");
		authConfiguration.setLoginRelative(new UrlProperty("/user/login"));
		uaaRestClientProperties.setAuthenticationConfiguration(authConfiguration);
		uaaHttpTokenAcquirer = new TestUAAHttpTokenAcquirer(uaaRestClientProperties, authorizationDataLocator, tokenRefresher);
	}

	@AfterEach
	void tearDown() {
		RefreshTokenScheduler.stopTokenRenewal();
	}

	@Test
	void acquireTokenTestSuccessful() throws IOException {
		AuthorizationData authorizationDataNew =
			new AuthorizationData("newTokenData", TokenType.BEARER, "sessionData", 50);
		//Mock static method
		try (MockedStatic<WebClientFactory> webClientFactoryMockedStatic = Mockito.mockStatic(WebClientFactory.class)) {
			webClientFactoryMockedStatic.when(WebClientFactory::createWebClient).thenReturn(webClient);
			//Reuse test
			acquireTokenResponseTestSuccessful();
			extractAuthorizationDataTestSuccessful();

			AuthorizationData authorizationData = uaaHttpTokenAcquirer.acquireToken();

			Assertions.assertTrue(authorizationData.equals(authorizationDataNew));
		}
	}

	@Test
	void acquireTokenTestFail() throws IOException {
		//Mock static method.
		try (MockedStatic<WebClientFactory> webClientFactoryMockedStatic = Mockito.mockStatic(WebClientFactory.class)) {
			webClientFactoryMockedStatic.when(WebClientFactory::createWebClient).thenReturn(webClient);
			Mockito.when(webClient.getPage(Mockito.any(WebRequest.class))).thenThrow(FailingHttpStatusCodeException.class);

			RuntimeException runtimeException = Assertions.assertThrows(RuntimeException.class, () -> uaaHttpTokenAcquirer.acquireToken());

			Assertions.assertEquals("Unable to login", runtimeException.getMessage());
		}
	}

	@Test
	void acquireTokenResponseTestSuccessful() throws IOException {
		Request loginRequest = uaaHttpTokenAcquirer.createLoginRequest();
		Mockito.when(webClient.getPage(Mockito.any(WebRequest.class))).thenReturn(loginResponsePage);
		Mockito.when(loginResponsePage.getWebResponse()).thenReturn(webResponse);
		Mockito.when(webResponse.getStatusCode()).thenReturn(HttpStatus.OK.value());

		Page page = uaaHttpTokenAcquirer.acquireTokenResponse(webClient, loginRequest);

		Assertions.assertEquals(HttpStatus.OK.value(), page.getWebResponse().getStatusCode());
	}

	@Test
	void acquireTokenResponseTestFail() throws IOException {
		Request loginRequest = uaaHttpTokenAcquirer.createLoginRequest();
		Mockito.when(webClient.getPage(Mockito.any(WebRequest.class))).thenReturn(loginResponsePage);
		Mockito.when(loginResponsePage.getWebResponse()).thenReturn(webResponse);
		Mockito.when(webResponse.getStatusCode()).thenReturn(HttpStatus.UNAUTHORIZED.value());
		Mockito.when(webResponse.getStatusMessage()).thenReturn(HttpStatus.UNAUTHORIZED.getReasonPhrase());

		RuntimeException runtimeException =
			Assertions.assertThrows(RuntimeException.class, () -> uaaHttpTokenAcquirer.acquireTokenResponse(webClient, loginRequest));
		Assertions.assertEquals("Unable to login response 401:Unauthorized", runtimeException.getMessage());
	}

	@Test
	void extractAuthorizationDataTestSuccessful() {
		AuthorizationData authorizationDataNew =
			new AuthorizationData("newTokenData", TokenType.BEARER, "sessionData", 50);
		Mockito.when(authorizationDataLocator.convert(loginResponsePage)).thenReturn(authorizationDataNew);

		AuthorizationData authorizationData = uaaHttpTokenAcquirer.extractAuthorizationData(loginResponsePage);

		Assertions.assertTrue(authorizationData.equals(authorizationDataNew));
	}

	@Test
	void getFullUrlTestSuccessful() {
		String fullUrl = uaaHttpTokenAcquirer.getFullUrlWithUaaBasePrefix(uaaRestClientProperties.getAuthenticationConfiguration().getLoginRelative().getUrl());
		Assertions.assertEquals(LOGIN_URI, fullUrl);
	}

	static class TestUAAHttpTokenAcquirer extends UAAHttpTokenAcquirer {

		public TestUAAHttpTokenAcquirer(UAARestClientProperties clientConfiguration,
			AuthorizationDataLocator<Page> authorizationDataLocator, TokenRefresher tokenRefresher) {
			super(clientConfiguration, new AtomicAuthorizationDataStore(), authorizationDataLocator, tokenRefresher);
		}

		@Override
		protected Request createLoginRequest() throws MalformedURLException {
			WebRequest webRequest = new WebRequest(new URL(LOGIN_URI), HttpMethod.GET);
			return new Request.Builder(webRequest).build();
		}

		@Override
		protected LogoutConfig getLogoutConfig() {
			return new LogoutConfig("user/logout", HttpMethod.GET);
		}
	}

}