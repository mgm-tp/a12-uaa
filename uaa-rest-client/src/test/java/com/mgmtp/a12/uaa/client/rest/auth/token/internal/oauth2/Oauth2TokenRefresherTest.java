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

import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.internal.store.AtomicAuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.RefreshTokenScheduler;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.TokenType;
import com.mgmtp.a12.uaa.client.rest.config.common.UrlProperty;
import com.mgmtp.a12.uaa.client.rest.config.properties.OidcProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientAuthenticationProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

@ExtendWith(MockitoExtension.class)
public class Oauth2TokenRefresherTest {

	private Oauth2TokenRefresher oauth2TokenRefresher;
	private UAARestClientProperties uaaRestClientProperties;
	private AuthorizationData authorizationData;
	private AuthorizationDataStore authorizationDataStore;
	@Mock
	private Oauth2ResponseAuthorizationDataLocator authorizationDataLocator;
	@Mock
	private Page page;
	@Mock
	private WebClient webClient;

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
		OidcProperties oauth2Configuration = new OidcProperties();
		OidcProperties.ConfidentialClientProperties confidentialClientProperties = new OidcProperties.ConfidentialClientProperties();
		confidentialClientProperties.setClientId("clientIdTest");
		confidentialClientProperties.setRealmName("realmNameTest");
		confidentialClientProperties.setIdpBase(new UrlProperty("http://localhost:9090"));
		confidentialClientProperties.setClientSecret("secret");
		confidentialClientProperties.setLoginRelative(new UrlProperty("/token"));
		oauth2Configuration.setConfidentialClient(confidentialClientProperties);
		authConfiguration.setOidc(oauth2Configuration);
		uaaRestClientProperties.setAuthenticationConfiguration(authConfiguration);
		authorizationDataStore = new AtomicAuthorizationDataStore();
		authorizationData = new AuthorizationData("tokenData", TokenType.BEARER, null, 50);
		authorizationDataStore.setAuthorizationData(authorizationData);
		oauth2TokenRefresher = new Oauth2TokenRefresher(uaaRestClientProperties, authorizationDataStore);
		ReflectionTestUtils.setField(oauth2TokenRefresher, "authorizationDataLocator", authorizationDataLocator);
		ReflectionTestUtils.setField(oauth2TokenRefresher, "webClient", webClient);
	}

	@AfterEach
	void tearDown() {
		RefreshTokenScheduler.stopTokenRenewal();
	}

	@Test
	void refreshTokenTestSuccessful() throws Exception {
		//Mock new token
		AuthorizationData otherAuthorizationData = new AuthorizationData("newTokenData", TokenType.BEARER, null, 50);
		otherAuthorizationData.setRefreshToken("newRefreshToken");
		Mockito.when(webClient.getPage(Mockito.any(WebRequest.class))).thenReturn(page);
		Mockito.when(authorizationDataLocator.convert(page)).thenReturn(otherAuthorizationData);

		//Verify that authorizationData in the store is an old token.
		AuthorizationData oldAuthorizationData = authorizationDataStore.getAuthorizationData();
		Assertions.assertNotNull(oldAuthorizationData);
		Assertions.assertTrue(authorizationData.equals(oldAuthorizationData));

		oauth2TokenRefresher.refreshAuthorizationData();

		//Verify that authorizationData in the store is a new token.
		AuthorizationData newAuthorizationData = authorizationDataStore.getAuthorizationData();
		Assertions.assertNotNull(newAuthorizationData);
		Assertions.assertFalse(authorizationData.equals(newAuthorizationData));
		Assertions.assertNotEquals(oldAuthorizationData.hashCode(), newAuthorizationData.hashCode());
		Assertions.assertTrue(otherAuthorizationData.equals(newAuthorizationData));
		Assertions.assertEquals("newTokenData", newAuthorizationData.getAuthenticationToken());
		Assertions.assertEquals("newRefreshToken", newAuthorizationData.getRefreshToken());
		//The sessionId in Oauth2 always is null.
		Assertions.assertNull(newAuthorizationData.getSessionId());
		Assertions.assertEquals(oldAuthorizationData.getUniqueUserIdentification(), newAuthorizationData.getUniqueUserIdentification());
	}

}