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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.mgmtp.a12.uaa.client.rest.auth.token.internal.URLUtils;
import com.mgmtp.a12.uaa.client.rest.config.properties.OidcProperties;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class Oauth2TokenValidatorTest {

	@Mock
	private RestTemplate restTemplate;
	private Oauth2TokenValidator oauth2TokenValidator = new Oauth2TokenValidator(new OidcProperties.ConfidentialClientProperties());
	private MockedStatic<URLUtils> urlUtilsMockedStatic;

	@BeforeEach
	void setUp() {

		urlUtilsMockedStatic = Mockito.mockStatic(URLUtils.class);
		urlUtilsMockedStatic.when(() -> URLUtils.getIdpBaseUrl(Mockito.any())).thenReturn("http://localhost:9090/realms/UAARealm/protocol/openid-connect");
		urlUtilsMockedStatic.when(() -> URLUtils.getFullUrl(Mockito.any(), Mockito.any()))
			.thenReturn("http://localhost:9090/realms/UAARealm/protocol/openid-connect/userinfo");

		//We need to init mocks again because setter injection is not working with annotations
		ResponseEntity<String> response = new ResponseEntity<>("OK", HttpStatus.OK);

		ArgumentMatcher<HttpEntity<String>> validMatcher = argument -> {
			String expected = "Bearer valid";
			String actual = argument.getHeaders().get("Authorization").get(0);
			return expected.equals(actual);
		};

		Mockito.when(restTemplate
				.exchange(
					ArgumentMatchers.anyString(),
					ArgumentMatchers.eq(HttpMethod.GET),
					Mockito.argThat(validMatcher),
					ArgumentMatchers.<Class<String>>any()))
			.thenReturn(response);

		ArgumentMatcher<HttpEntity<String>> invalidMatcher = argument -> {
			String expected = "Bearer invalid";
			return expected.equals(argument.getHeaders().get("Authorization").get(0));
		};
		Mockito.when(restTemplate
				.exchange(
					ArgumentMatchers.anyString(),
					ArgumentMatchers.eq(HttpMethod.GET),
					Mockito.argThat(invalidMatcher),
					ArgumentMatchers.<Class<String>>any()))
			.thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

		ReflectionTestUtils.setField(oauth2TokenValidator, "restTemplate", restTemplate);
	}

	@AfterEach
	void terDown() {
		urlUtilsMockedStatic.close();
	}

	@Test
	public void validToken() {
		boolean tokenValid = oauth2TokenValidator.isTokenValid("valid");
		Assertions.assertTrue(tokenValid);
	}

	@Test
	public void invalidToken() {
		boolean tokenValid = oauth2TokenValidator.isTokenValid("invalid");
		Assertions.assertFalse(tokenValid);
	}

}
