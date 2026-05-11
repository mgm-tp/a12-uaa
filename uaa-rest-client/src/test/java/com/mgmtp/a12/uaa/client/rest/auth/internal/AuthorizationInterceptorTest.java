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

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.util.UriComponentsBuilder;

import com.mgmtp.a12.uaa.client.rest.auth.AuthenticationHandler;
import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.TokenType;
import com.mgmtp.a12.uaa.client.rest.config.AuthenticationType;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthorizationInterceptorTest {

	private AuthorizationInterceptor authorizationInterceptor;
	@Mock
	private AuthenticationHandler authenticationHandler;
	@Mock
	private ClientHttpRequestExecution clientHttpRequestExecution;

	@BeforeEach
	void setUp() {
		authorizationInterceptor = new AuthorizationInterceptor(AuthenticationType.LOCAL, authenticationHandler,
			"http://localhost:8080", "Authorization");
	}

	@Test
	void interceptTestSuccessful() throws IOException {
		MockClientHttpRequest request = new MockClientHttpRequest();
		byte[] body = "data body test".getBytes(StandardCharsets.UTF_8);

		Mockito.when(authenticationHandler.authenticate()).thenReturn(new AuthorizationData("tokenData", Instant.now(),
			TokenType.BEARER, "sessionData"));
		Mockito.when(clientHttpRequestExecution.execute(Mockito.any(), Mockito.any())).thenReturn(new MockClientHttpResponse(body, HttpStatus.OK));

		ClientHttpResponse response = authorizationInterceptor.intercept(request, body, clientHttpRequestExecution);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

		StringWriter writer = new StringWriter();
		IOUtils.copy(response.getBody(), writer, StandardCharsets.UTF_8);
		Assertions.assertEquals("data body test", writer.toString());
		response.close();
	}

	@Test
	void interceptTestDelegated() throws IOException {
		MockClientHttpRequest request = new MockClientHttpRequest();
		byte[] body = "data body test".getBytes(StandardCharsets.UTF_8);

		Mockito.when(authenticationHandler.authenticate()).thenReturn(new AuthorizationData("UAABearer tokenData", Instant.now(), TokenType.DELEGATED, null));
		Mockito.when(clientHttpRequestExecution.execute(Mockito.any(), Mockito.any())).thenReturn(new MockClientHttpResponse(body, HttpStatus.OK));

		authorizationInterceptor.intercept(request, body, clientHttpRequestExecution);
		List<String> authorizations = request.getHeaders().getValuesAsList(HttpHeaders.AUTHORIZATION);
		Assertions.assertEquals("UAABearer tokenData", authorizations.get(0));

	}

	@Test
	void interceptTestFail() throws IOException {
		MockClientHttpRequest request = new MockClientHttpRequest();
		byte[] body = "data body test".getBytes(StandardCharsets.UTF_8);

		Mockito.when(authenticationHandler.authenticate()).thenThrow(new RuntimeException("Authentication failed"));
		ClientHttpResponse response = authorizationInterceptor.intercept(request, body, clientHttpRequestExecution);

		Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		Assertions.assertEquals(0, response.getHeaders().size());
		Assertions.assertNull(response.getBody());
		Assertions.assertEquals("Unauthorized", response.getStatusText());
		Assertions.assertEquals(401, response.getStatusCode().value());
		response.close();
	}

	@Test
	void interceptTestLogout() throws IOException {
		MockClientHttpRequest request = new MockClientHttpRequest();
		byte[] body = "data body test".getBytes(StandardCharsets.UTF_8);
		request.setURI(UriComponentsBuilder.fromHttpUrl("http://localhost:8080/user/logout").build().toUri());
		Mockito.when(authenticationHandler.authenticate()).thenReturn(new AuthorizationData("tokenData", Instant.now(),
			TokenType.BEARER, "sessionData"));

		Mockito.when(clientHttpRequestExecution.execute(Mockito.any(), Mockito.any())).thenReturn(new MockClientHttpResponse(body, HttpStatus.OK));

		ClientHttpResponse response = authorizationInterceptor.intercept(request, body, clientHttpRequestExecution);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
		Mockito.verify(authenticationHandler, Mockito.atLeastOnce()).logout(Mockito.any());
		response.close();
	}

}