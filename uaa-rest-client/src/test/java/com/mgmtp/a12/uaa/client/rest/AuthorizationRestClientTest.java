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
import org.springframework.http.ResponseEntity;

import com.mgmtp.a12.connector.rest.RestGetConnector;
import com.mgmtp.a12.connector.rest.RestServerResponse;

@ExtendWith(MockitoExtension.class)
public class AuthorizationRestClientTest {

	private AuthorizationRestClient authorizationRestClient;
	@Mock
	private RestGetConnector getConnector;

	@BeforeEach
	void setUp() {
		authorizationRestClient = new AuthorizationRestClient("http://localhost:8080", getConnector);
	}

	@Test
	void reloadRulesTest() {
		RestServerResponse restServerResponse = new RestServerResponse(ResponseEntity.ok().body("rules"));
		Mockito.when(getConnector.callServer(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(restServerResponse);

		String reloadRules = authorizationRestClient.reloadRules();

		Assertions.assertEquals("rules", reloadRules);
	}

}