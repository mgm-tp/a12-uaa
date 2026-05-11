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

import java.net.MalformedURLException;

import org.htmlunit.FormEncodingType;
import org.htmlunit.HttpMethod;
import org.htmlunit.WebClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import com.mgmtp.a12.uaa.client.rest.auth.TokenRefresher;
import com.mgmtp.a12.uaa.client.rest.auth.internal.store.AtomicAuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.Request;
import com.mgmtp.a12.uaa.client.rest.config.common.UrlProperty;
import com.mgmtp.a12.uaa.client.rest.config.properties.OidcProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientAuthenticationProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

@ExtendWith(MockitoExtension.class)
public class UAAOauth2ConfidentialTokenAcquirerTest {

	private static final String LOGIN_URI = "http://localhost:9090/realms/realmNameTest/protocol/openid-connect/token";
	private static final String VALUE_CONTENT_TYPE = "application/x-www-form-urlencoded";
	private static final String DATA_BODY = "client_id=clientIdTest&grant_type=password&client_secret=secret&username=admin&password=admin";

	private UAAOauth2ConfidentialTokenAcquirer uaaOauth2ConfidentialTokenAcquirer;
	private UAARestClientProperties uaaRestClientProperties;
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
		OidcProperties.ConfidentialClientProperties confidentialClientProperties = new OidcProperties.ConfidentialClientProperties();
		confidentialClientProperties.setLoginRelative(new UrlProperty("/token"));
		confidentialClientProperties.setClientId("clientIdTest");
		confidentialClientProperties.setRealmName("realmNameTest");
		confidentialClientProperties.setIdpBase(new UrlProperty("http://localhost:9090"));
		confidentialClientProperties.setClientSecret("secret");
		oidcProperties.setConfidentialClient(confidentialClientProperties);
		authConfiguration.setOidc(oidcProperties);
		uaaRestClientProperties.setAuthenticationConfiguration(authConfiguration);
		uaaOauth2ConfidentialTokenAcquirer =
			new UAAOauth2ConfidentialTokenAcquirer(uaaRestClientProperties, new AtomicAuthorizationDataStore(), tokenRefresher);
	}

	@Test
	void createWebClientTest() {
		WebClient webClient = uaaOauth2ConfidentialTokenAcquirer.createWebClient();

		Assertions.assertFalse(webClient.getOptions().isRedirectEnabled());
		Assertions.assertTrue(webClient.getOptions().isJavaScriptEnabled());
		Assertions.assertFalse(webClient.getOptions().isThrowExceptionOnFailingStatusCode());
		Assertions.assertFalse(webClient.getOptions().isThrowExceptionOnScriptError());

		webClient.close();
	}

	@Test
	void createLoginRequestTest() throws MalformedURLException {
		Request loginRequest = uaaOauth2ConfidentialTokenAcquirer.createLoginRequest();

		Assertions.assertEquals(HttpMethod.POST, loginRequest.getRequest().getHttpMethod());
		Assertions.assertEquals(FormEncodingType.URL_ENCODED, loginRequest.getRequest().getEncodingType());
		Assertions.assertEquals("*/*", loginRequest.getRequest().getAdditionalHeader(HttpHeaders.ACCEPT));
		Assertions.assertEquals(LOGIN_URI, loginRequest.getRequest().getUrl().toString());
		Assertions.assertEquals(VALUE_CONTENT_TYPE, loginRequest.getRequest().getAdditionalHeader(HttpHeaders.CONTENT_TYPE));
		Assertions.assertEquals(DATA_BODY, loginRequest.getRequest().getRequestBody());
	}

}