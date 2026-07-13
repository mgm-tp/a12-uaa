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
package com.mgmtp.a12.uaa.client.rest.internal;

import java.io.IOException;
import java.net.URI;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

import com.mgmtp.a12.uaa.client.rest.UAARestClientException;

public class UAAResponseErrorHandlerTest {

	private UAAResponseErrorHandler uaaResponseErrorHandler = new UAAResponseErrorHandler();

	@Test
	void handleErrorTestForbidden() throws IOException {
		MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://localhost:8080/test"));
		ClientHttpResponse response = new MockClientHttpResponse(new byte[] {}, HttpStatus.FORBIDDEN);

		UAARestClientException uaaRestClientException = Assertions.assertThrows(UAARestClientException.class, () ->
			uaaResponseErrorHandler.handleError(request, response));

		Assertions.assertEquals(HttpStatus.FORBIDDEN, uaaRestClientException.getStatus());
	}

	@Test
	void handleErrorTestUnauthorized() throws IOException {
		MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://localhost:8080/test"));
		ClientHttpResponse response = new MockClientHttpResponse(new byte[] {}, HttpStatus.UNAUTHORIZED);

		UAARestClientException uaaRestClientException = Assertions.assertThrows(UAARestClientException.class, () ->
			uaaResponseErrorHandler.handleError(request, response));

		Assertions.assertEquals(HttpStatus.UNAUTHORIZED, uaaRestClientException.getStatus());
		Assertions.assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), uaaRestClientException.getMessage());
	}

	@Test
	void handleErrorTestOk() throws IOException {
		MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://localhost:8080/test"));
		ClientHttpResponse response = new MockClientHttpResponse(new byte[] {}, HttpStatus.OK);

		// Should not throw for OK status
		Assertions.assertDoesNotThrow(() -> uaaResponseErrorHandler.handleError(request, response));
	}

}