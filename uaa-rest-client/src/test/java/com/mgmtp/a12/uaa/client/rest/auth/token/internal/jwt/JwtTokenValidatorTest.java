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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class JwtTokenValidatorTest {

	@Mock
	private RestClient restClient;
	@Mock
	private RestClient.RequestBodyUriSpec requestBodyUriSpec;
	@Mock
	private RestClient.RequestBodySpec validRequestBodySpec;
	@Mock
	private RestClient.RequestBodySpec invalidRequestBodySpec;
	@Mock
	private RestClient.ResponseSpec validResponseSpec;
	@Mock
	private RestClient.ResponseSpec invalidResponseSpec;

	private JwtTokenValidator jwtTokenValidator = new JwtTokenValidator("http://null");

	@BeforeEach
	void setUp() {
		Mockito.when(restClient.post()).thenReturn(requestBodyUriSpec);
		Mockito.when(requestBodyUriSpec.uri(Mockito.anyString())).thenReturn(validRequestBodySpec);

		// Valid token setup
		Mockito.when(validRequestBodySpec.body(Mockito.eq("valid"))).thenReturn(validRequestBodySpec);
		Mockito.when(validRequestBodySpec.retrieve()).thenReturn(validResponseSpec);
		Mockito.when(validResponseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

		// Invalid token setup
		Mockito.when(validRequestBodySpec.body(Mockito.eq("invalid"))).thenReturn(invalidRequestBodySpec);
		Mockito.when(invalidRequestBodySpec.retrieve()).thenReturn(invalidResponseSpec);
		Mockito.when(invalidResponseSpec.toBodilessEntity()).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

		ReflectionTestUtils.setField(jwtTokenValidator, "restClient", restClient);
	}

	@Test
	public void validToken() {
		boolean tokenValid = jwtTokenValidator.isTokenValid("valid");
		Assertions.assertTrue(tokenValid);
	}

	@Test
	public void invalidToken() {
		boolean tokenValid = jwtTokenValidator.isTokenValid("invalid");
		Assertions.assertFalse(tokenValid);
	}

}
