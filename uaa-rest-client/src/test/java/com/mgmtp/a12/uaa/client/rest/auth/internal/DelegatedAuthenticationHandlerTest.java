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


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.internal.delegated.AuthorizationDataHolder;
import com.mgmtp.a12.uaa.client.rest.auth.internal.store.ThreadLocalAuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.TokenType;

public class DelegatedAuthenticationHandlerTest {

	private DelegatedAuthenticationHandler delegatedAuthenticationHandler = new DelegatedAuthenticationHandler();
	private AuthorizationDataStore credentialsContext = new ThreadLocalAuthorizationDataStore();
	private AuthorizationData authorizationData = new AuthorizationData("tokenData", TokenType.BEARER, "sessionData", 300);
	private MockedStatic<AuthorizationDataHolder> authorizationDataHolder;

	@BeforeEach
	void setUp() {
		credentialsContext.setAuthorizationData(authorizationData);
		authorizationDataHolder = Mockito.mockStatic(AuthorizationDataHolder.class);
		authorizationDataHolder.when(AuthorizationDataHolder::getCredentialContext).thenReturn(credentialsContext);
	}

	@AfterEach
	void tearDown() {
		authorizationDataHolder.close();
	}

	@Test
	void authenticateTest() {
		AuthorizationData authenticate = delegatedAuthenticationHandler.authenticate();

		Assertions.assertNotNull(AuthorizationDataHolder.getCredentialContext().getAuthorizationData());
		Assertions.assertTrue(AuthorizationDataHolder.getCredentialContext().getAuthorizationData().equals(authenticate));
	}

	@Test
	void logoutTest() {
		Assertions.assertNotNull(AuthorizationDataHolder.getCredentialContext().getAuthorizationData());

		delegatedAuthenticationHandler.logout(new HttpHeaders());

		Assertions.assertNull(AuthorizationDataHolder.getCredentialContext().getAuthorizationData());
	}

}