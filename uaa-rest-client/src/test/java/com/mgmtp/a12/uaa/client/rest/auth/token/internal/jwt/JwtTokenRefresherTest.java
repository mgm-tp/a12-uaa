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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.internal.data.AuthorizeData;
import com.mgmtp.a12.uaa.client.rest.auth.internal.data.TokenData;
import com.mgmtp.a12.uaa.client.rest.auth.internal.store.AtomicAuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.TokenType;
import com.mgmtp.a12.uaa.client.rest.config.common.UrlProperty;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientAuthenticationProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

@ExtendWith(MockitoExtension.class)
public class JwtTokenRefresherTest {

	private JwtTokenRefresher jwtTokenRefresher;
	private UAARestClientProperties uaaRestClientProperties;
	private AuthorizationData authorizationData;
	private AuthorizationDataStore authorizationDataStore;
	@Mock
	private RestTemplate restTemplate;

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
		uaaRestClientProperties.setAuthenticationConfiguration(authConfiguration);

		authorizationDataStore = new AtomicAuthorizationDataStore();
		authorizationData = new AuthorizationData("tokenData", TokenType.BEARER, "sessionData", 50);
		authorizationDataStore.setAuthorizationData(authorizationData);
		jwtTokenRefresher = new JwtTokenRefresher(uaaRestClientProperties, authorizationDataStore);
		ReflectionTestUtils.setField(jwtTokenRefresher, "restTemplate", restTemplate);

		AuthorizeData authorizeData = new AuthorizeData();
		authorizeData.setState("state");
		authorizeData.setCode("code");
		TokenData tokenData = new TokenData();
		tokenData.setAccessToken("newTokenData");
		tokenData.setTokenRenewInSeconds("50");

		Mockito.when(restTemplate.postForObject(Mockito.anyString(), Mockito.any(), Mockito.any()))
			.thenReturn(authorizeData)
			.thenReturn(tokenData);
	}

	@Test
	void refreshTokenTestSuccessful() throws Exception {
		//Verify that authorizationData in the store is an old token.
		AuthorizationData oldAuthorizationData = authorizationDataStore.getAuthorizationData();
		Assertions.assertNotNull(oldAuthorizationData);
		Assertions.assertTrue(authorizationData.equals(oldAuthorizationData));

		jwtTokenRefresher.refreshAuthorizationData();

		//Verify that authorizationData in the store is a new token.
		AuthorizationData newAuthorizationData = authorizationDataStore.getAuthorizationData();
		Assertions.assertNotEquals(oldAuthorizationData.hashCode(), newAuthorizationData.hashCode());
		Assertions.assertFalse(authorizationData.equals(newAuthorizationData));
		Assertions.assertEquals("newTokenData", newAuthorizationData.getAuthenticationToken());
		//Verify that the new authorizationData still kept both authenticationTokenType and sessionId.
		Assertions.assertEquals(authorizationData.getAuthenticationTokenType(), newAuthorizationData.getAuthenticationTokenType());
		Assertions.assertEquals(authorizationData.getSessionId(), newAuthorizationData.getSessionId());
		Assertions.assertEquals(authorizationData.getUniqueUserIdentification(), newAuthorizationData.getUniqueUserIdentification());
	}

}