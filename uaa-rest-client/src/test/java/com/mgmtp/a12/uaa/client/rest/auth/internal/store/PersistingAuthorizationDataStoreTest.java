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
package com.mgmtp.a12.uaa.client.rest.auth.internal.store;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ScheduledFuture;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.TokenRefresher;
import com.mgmtp.a12.uaa.client.rest.auth.TokenValidator;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.RefreshTokenScheduler;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.TokenType;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PersistingAuthorizationDataStoreTest {

	private static final Path AUTH_FILE = Paths.get("auth.txt");
	@Mock
	private TokenValidator tokenValidator;
	@Mock
	private TokenRefresher tokenRefresher;
	@Mock
	private ScheduledFuture<?> renewalFuture;
	private MockedStatic<RefreshTokenScheduler> refreshTokenSchedulerMockedStatic;
	private PersistingAuthorizationDataStore dataStore;
	private AuthorizationData authData;

	@BeforeEach
	void setUp() {
		authData = new AuthorizationData("tokenData", TokenType.BEARER, "sessionData", 99999);
		Mockito.when(tokenValidator.isTokenValid(Mockito.anyString())).thenReturn(true);
		dataStore = new PersistingAuthorizationDataStore(AUTH_FILE, tokenRefresher, tokenValidator);
		refreshTokenSchedulerMockedStatic = Mockito.mockStatic(RefreshTokenScheduler.class);
	}

	@AfterEach
	void tearDown() {
		refreshTokenSchedulerMockedStatic.close();
	}

	@AfterAll
	void cleanUp() throws Exception {
		Files.delete(AUTH_FILE);
	}

	@Test
	public void serializableStoreTestSuccessful() {
		refreshTokenSchedulerMockedStatic.when(() ->
			RefreshTokenScheduler.scheduleTokenRenewal(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt())).thenReturn(renewalFuture);

		dataStore.setAuthorizationData(authData);

		AuthorizationData authorizationData = dataStore.getAuthorizationData();
		authorizationData.validate();

		Assertions.assertNotNull(authorizationData);
		Assertions.assertTrue(authData.equals(authorizationData));

		dataStore.cleanUpAuthorizationStore();
	}

	@Test
	public void serializableStoreTestFail() {
		dataStore.setAuthorizationData(new AuthorizationData("tokenData", TokenType.BEARER, "sessionData", 300));

		AuthorizationData authorizationData = dataStore.getAuthorizationData();

		Assertions.assertNull(authorizationData);
	}

	@Test
	public void serializableStoreTestFileNotFound() {
		dataStore = new PersistingAuthorizationDataStore(Paths.get("fileNotFound.txt"), tokenRefresher, tokenValidator);

		AuthorizationData authorizationData = dataStore.getAuthorizationData();

		Assertions.assertNull(authorizationData);

		dataStore.cleanUpAuthorizationStore();
	}

}
