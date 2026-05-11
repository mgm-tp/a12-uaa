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
package com.mgmtp.a12.uaa.client.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.mgmtp.a12.connector.rest.RestGetConnector;
import com.mgmtp.a12.connector.rest.RestPostConnector;
import com.mgmtp.a12.connector.rest.RestServerResponse;

@ExtendWith(MockitoExtension.class)
public class AuthenticationRestClientTest {

	private AuthenticationRestClient authenticationRestClient;
	@Mock
	private RestGetConnector getConnector;
	@Mock
	private RestPostConnector postConnector;

	@BeforeEach
	void setUp() {
		authenticationRestClient = new AuthenticationRestClient("http://localhost:8080", getConnector, postConnector);
	}

	@Test
	void currentUserTest() {
		CurrentUser expected = new CurrentUser();
		expected.setUsername("test");
		expected.setDisplayName("Test");
		RestServerResponse restServerResponse = new RestServerResponse(ResponseEntity.ok().body(expected));

		Mockito.when(getConnector.callServer(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(restServerResponse);

		CurrentUser actual = authenticationRestClient.currentUser();

		Assertions.assertEquals(expected, actual);
		Assertions.assertEquals(expected.getUsername(), actual.getUsername());
		Assertions.assertEquals(expected.getDisplayName(), actual.getDisplayName());
	}

	@Test
	void otherUserTest() {
		OtherUser expected = new OtherUser();
		expected.setUsername("test");
		expected.setDisplayName("Test");
		RestServerResponse restServerResponse = new RestServerResponse(ResponseEntity.ok().body(expected));
		Mockito.when(getConnector.callServer(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(restServerResponse);

		OtherUser actual = authenticationRestClient.currentUser(OtherUser.class);

		Assertions.assertEquals(expected, actual);
		Assertions.assertEquals(expected.getUsername(), actual.getUsername());
		Assertions.assertEquals(expected.getDisplayName(), actual.getDisplayName());
		Assertions.assertNull(actual.getEmpty());
	}

	@Test
	void tokenValidTest() {
		RestServerResponse restServerResponse = new RestServerResponse(ResponseEntity.ok().body(true));
		Mockito.when(postConnector.callServer(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(restServerResponse);

		boolean isTokenValid = authenticationRestClient.tokenValid("Token valid");
		Assertions.assertTrue(isTokenValid);
	}

	@Test
	void tokenInvalidTest() {
		UAARestClientException uaaRestClientException = new UAARestClientException(HttpStatus.UNAUTHORIZED, "401");
		Mockito.when(postConnector.callServer(Mockito.anyString(), Mockito.any(), Mockito.any())).thenThrow(uaaRestClientException);

		boolean isTokenValid = authenticationRestClient.tokenValid("Token invalid");

		Assertions.assertFalse(isTokenValid);
	}

	@Test
	void logoutTest() {
		authenticationRestClient.logout();

		Mockito.verify(getConnector, Mockito.atLeastOnce()).callServer(Mockito.anyString(), Mockito.any(), Mockito.any());
	}

	class OtherUser extends CurrentUser {

		private String empty;

		public String getEmpty() {
			return empty;
		}

		public void setEmpty(String empty) {
			this.empty = empty;
		}

	}

}