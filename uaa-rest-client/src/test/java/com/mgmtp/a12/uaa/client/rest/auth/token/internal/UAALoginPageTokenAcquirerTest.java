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
import java.util.Collections;

import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.HttpMethod;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.mgmtp.a12.uaa.client.rest.auth.TokenRefresher;
import com.mgmtp.a12.uaa.client.rest.auth.internal.locator.AuthorizationDataLocator;
import com.mgmtp.a12.uaa.client.rest.auth.internal.store.AtomicAuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.oauth2.ClientType;
import com.mgmtp.a12.uaa.client.rest.config.AuthenticationType;
import com.mgmtp.a12.uaa.client.rest.config.common.UrlProperty;
import com.mgmtp.a12.uaa.client.rest.config.properties.OidcProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.SsoProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientAuthenticationProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

@ExtendWith(MockitoExtension.class)
public class UAALoginPageTokenAcquirerTest {

	private static final String LOGIN_URI = "http://localhost:8080/user/login";

	private UAALoginPageTokenAcquirer uaaLoginPageTokenAcquirer;
	private UAARestClientProperties uaaRestClientProperties;
	private Request loginRequest;
	@Mock
	private AuthorizationDataLocator<Page> authorizationDataLocator;
	@Mock
	private HtmlPage loginResponsePage;
	@Mock
	private HtmlPage authenticatedPage;
	@Mock
	private HtmlPage tokenExchangePage;
	@Mock
	private WebClient webClient;
	@Mock
	private WebResponse loginWebResponse;
	@Mock
	private WebResponse tokenWebResponse;
	@Mock
	private HtmlInput username;
	@Mock
	private HtmlInput password;
	@Mock
	private HtmlElement loginInButton;
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
		uaaRestClientProperties.setAuthenticationConfiguration(authConfiguration);

		OidcProperties oauth2Configuration = new OidcProperties();
		oauth2Configuration.setClientType(ClientType.PUBLIC);
		OidcProperties.PublicClientProperties publicClientProperties = new OidcProperties.PublicClientProperties();
		publicClientProperties.setLoginRelative(new UrlProperty("/auth"));
		publicClientProperties.setTokenExchangeRelative(new UrlProperty("/token"));
		publicClientProperties.setLoginRedirectRelative(new UrlProperty("/callback"));
		SsoProperties ssoProperties = new SsoProperties();
		ssoProperties.setUsernameXpath("//input[@name='username']");
		ssoProperties.setPasswordXpath("//input[@name='password']");
		ssoProperties.setLoginButtonXpath("//button[@name='login']");
		publicClientProperties.setSsoConfiguration(ssoProperties);
		publicClientProperties.setClientId("clientIdTest");
		publicClientProperties.setRealmName("realmNameTest");
		publicClientProperties.setIdpBase(new UrlProperty("http://localhost:9090"));
		oauth2Configuration.setPublicClient(publicClientProperties);
		authConfiguration.setOidc(oauth2Configuration);
		uaaRestClientProperties.setAuthenticationConfiguration(authConfiguration);
		uaaRestClientProperties.setAuthenticationType(AuthenticationType.OAUTH2);
		uaaLoginPageTokenAcquirer = new TestUAALoginPageTokenAcquirer(uaaRestClientProperties, authorizationDataLocator, tokenRefresher)
			.addTokenExchangePage(tokenExchangePage);
	}

	@Test
	void acquireTokenResponseTestSuccessful() throws IOException {
		handleIDPLoginPageTestSuccessful();
		Mockito.when(webClient.getPage(Mockito.any(WebRequest.class))).thenReturn(loginResponsePage);
		Mockito.when(loginResponsePage.getWebResponse()).thenReturn(loginWebResponse);
		Mockito.when(loginWebResponse.getStatusCode()).thenReturn(HttpStatus.OK.value());
		Mockito.when(tokenExchangePage.getWebResponse()).thenReturn(tokenWebResponse);
		Mockito.when(tokenWebResponse.getStatusCode()).thenReturn(HttpStatus.OK.value());

		Page page = uaaLoginPageTokenAcquirer.acquireTokenResponse(webClient, loginRequest);

		Assertions.assertEquals(tokenExchangePage, page);
	}

	@Test
	void acquireTokenResponseTestFail() throws IOException {
		handleIDPLoginPageTestSuccessful();
		Mockito.when(webClient.getPage(Mockito.any(WebRequest.class))).thenReturn(loginResponsePage);
		Mockito.when(loginResponsePage.getWebResponse()).thenReturn(loginWebResponse);
		Mockito.when(loginWebResponse.getStatusCode()).thenReturn(HttpStatus.OK.value());

		Mockito.when(tokenExchangePage.getWebResponse()).thenReturn(tokenWebResponse);
		Mockito.when(tokenWebResponse.getStatusCode()).thenReturn(HttpStatus.UNAUTHORIZED.value());
		Mockito.when(tokenWebResponse.getStatusMessage()).thenReturn(HttpStatus.UNAUTHORIZED.getReasonPhrase());

		RuntimeException runtimeException =
			Assertions.assertThrows(RuntimeException.class, () -> uaaLoginPageTokenAcquirer.acquireTokenResponse(webClient, loginRequest));

		Assertions.assertEquals("Unable to token exchange response 401:Unauthorized", runtimeException.getMessage());
	}

	@Test
	void handleIDPLoginPageTestSuccessful() throws IOException {
		loginRequest = uaaLoginPageTokenAcquirer.createLoginRequest();
		Mockito.when(loginResponsePage.isHtmlPage()).thenReturn(true);
		Mockito.when(loginResponsePage.getByXPath(
				uaaRestClientProperties.getAuthenticationConfiguration().getOidc().getPublicClient().getSsoConfiguration().getUsernameXpath()))
			.thenReturn(Collections.singletonList(username));
		Mockito.when(loginResponsePage.getByXPath(
				uaaRestClientProperties.getAuthenticationConfiguration().getOidc().getPublicClient().getSsoConfiguration().getPasswordXpath()))
			.thenReturn(Collections.singletonList(password));
		Mockito.when(loginResponsePage.getByXPath(
				uaaRestClientProperties.getAuthenticationConfiguration().getOidc().getPublicClient().getSsoConfiguration().getLoginButtonXpath()))
			.thenReturn(Collections.singletonList(loginInButton));
		Mockito.when(loginInButton.click()).thenReturn(authenticatedPage);

		Page page = uaaLoginPageTokenAcquirer.handleIDPLoginPage(webClient, loginResponsePage, loginRequest);

		Assertions.assertEquals(authenticatedPage, page);
		Mockito.verify(username, Mockito.atLeastOnce()).setValue(uaaRestClientProperties.getAuthenticationConfiguration().getUsername());
		Mockito.verify(password, Mockito.atLeastOnce()).setValue(uaaRestClientProperties.getAuthenticationConfiguration().getPassword());
		Mockito.verify(loginInButton, Mockito.atLeastOnce()).click();
	}

	@Test
	void handleIDPLoginPageTestFail() throws IOException {
		loginRequest = uaaLoginPageTokenAcquirer.createLoginRequest();

		RuntimeException runtimeException =
			Assertions.assertThrows(RuntimeException.class, () -> uaaLoginPageTokenAcquirer.handleIDPLoginPage(webClient, loginResponsePage, loginRequest));

		Assertions.assertEquals("Unable to find login button in XPATH: //button[@name='login']", runtimeException.getMessage());
	}

	static class TestUAALoginPageTokenAcquirer extends UAALoginPageTokenAcquirer {

		private Page tokenExchangePage;

		public TestUAALoginPageTokenAcquirer(UAARestClientProperties clientConfiguration, AuthorizationDataLocator<Page> authorizationDataLocator,
			TokenRefresher tokenRefresher) {
			super(clientConfiguration, new AtomicAuthorizationDataStore(), authorizationDataLocator, tokenRefresher);
		}

		public TestUAALoginPageTokenAcquirer addTokenExchangePage(Page tokenExchangePage) {
			this.tokenExchangePage = tokenExchangePage;
			return this;
		}

		@Override
		protected Request createLoginRequest() throws MalformedURLException {
			WebRequest webRequest = new WebRequest(new URL(LOGIN_URI), HttpMethod.GET);
			return new Request.Builder(webRequest).build();
		}

		@Override
		protected Page exchangeAuthorizationCode(WebClient webClient, Page authenticatedPage, Request loginRequest)
			throws FailingHttpStatusCodeException, IOException {
			return tokenExchangePage;
		}

		@Override
		protected SsoProperties getSsoProperties() {
			return uaaRestClientProperties.getAuthenticationConfiguration().getOidc().getPublicClient().getSsoConfiguration();
		}

		@Override
		protected LogoutConfig getLogoutConfig() {
			return new LogoutConfig("user/logout", HttpMethod.GET);
		}
	}

}