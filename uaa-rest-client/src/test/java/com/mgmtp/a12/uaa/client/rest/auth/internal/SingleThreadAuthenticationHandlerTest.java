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
package com.mgmtp.a12.uaa.client.rest.auth.internal;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.TokenAcquirer;
import com.mgmtp.a12.uaa.client.rest.auth.internal.store.AtomicAuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.internal.store.DelegatingAuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.RefreshTokenScheduler;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.TokenType;
import com.mgmtp.a12.uaa.client.rest.config.common.UrlProperty;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientAuthenticationProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

@ExtendWith(MockitoExtension.class)
public class SingleThreadAuthenticationHandlerTest {

	private SingleThreadAuthenticationHandler singleThreadAuthenticationHandler;
	private AuthorizationData authorizationData;
	private AuthorizationDataStore authorizationDataStore;
	private UAARestClientProperties uaaRestClientProperties;
	@Mock
	private TokenAcquirer tokenAcquirer;
	private MockedStatic<RefreshTokenScheduler> refreshTokenSchedulerMockedStatic;

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
		authorizationData = new AuthorizationData("tokenData", TokenType.BEARER, "sessionData", 50);
		refreshTokenSchedulerMockedStatic = Mockito.mockStatic(RefreshTokenScheduler.class);
	}

	@AfterEach
	void tearDown() {
		refreshTokenSchedulerMockedStatic.close();
	}

	@Test
	void authenticateTest() {
		AtomicAuthorizationDataStore authorizationDataAtomic = new AtomicAuthorizationDataStore();

		List<AuthorizationDataStore> stores = new LinkedList<>();
		stores.add(authorizationDataAtomic);
		authorizationDataStore = new DelegatingAuthorizationDataStore(stores);
		singleThreadAuthenticationHandler = new SingleThreadAuthenticationHandler(uaaRestClientProperties, authorizationDataStore, tokenAcquirer);
		Assertions.assertNull(authorizationDataStore.getAuthorizationData());

		Mockito.when(tokenAcquirer.acquireToken()).thenReturn(authorizationData);

		AuthorizationData authenticate = singleThreadAuthenticationHandler.authenticate();

		Assertions.assertNotNull(authorizationDataStore.getAuthorizationData());
		Assertions.assertTrue(authorizationDataStore.getAuthorizationData().equals(authenticate));
	}

	@Test
	void logoutTest() {
		AtomicAuthorizationDataStore authorizationDataAtomic = new AtomicAuthorizationDataStore();
		authorizationDataAtomic.setAuthorizationData(authorizationData);

		List<AuthorizationDataStore> stores = new LinkedList<>();
		stores.add(authorizationDataAtomic);
		authorizationDataStore = new DelegatingAuthorizationDataStore(stores);
		singleThreadAuthenticationHandler = new SingleThreadAuthenticationHandler(uaaRestClientProperties, authorizationDataStore, tokenAcquirer);
		Assertions.assertNotNull(authorizationDataStore.getAuthorizationData());

		singleThreadAuthenticationHandler.logout(new HttpHeaders());

		refreshTokenSchedulerMockedStatic.verify(RefreshTokenScheduler::stopTokenRenewal, Mockito.atLeastOnce());
	}

}