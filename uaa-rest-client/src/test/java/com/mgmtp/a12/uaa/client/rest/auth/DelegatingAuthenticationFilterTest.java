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
package com.mgmtp.a12.uaa.client.rest.auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.mgmtp.a12.uaa.client.rest.auth.internal.AuthorizationInterceptor;
import com.mgmtp.a12.uaa.client.rest.auth.internal.DelegatedAuthenticationHandler;
import com.mgmtp.a12.uaa.client.rest.auth.internal.delegated.AuthorizationDataHolder;
import com.mgmtp.a12.uaa.client.rest.config.AuthenticationType;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DelegatingAuthenticationFilterTest {

	private DelegatingAuthenticationFilter delegatingAuthenticationFilter;

	@BeforeAll
	public void setUp() {
		delegatingAuthenticationFilter = new DelegatingAuthenticationFilter("Authorization");
	}

	@Test
	public void testWithAuthorizationHeaderExist() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "abc12345@@@");
		MockHttpServletResponse response = new MockHttpServletResponse();
		delegatingAuthenticationFilter.doFilter(request, response, new AuthorizationDataCheckEqual("abc12345@@@"));
	}

	@Test
	public void testWithAuthorizationHeaderDoesNotExist() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		Assertions.assertThrows(RuntimeException.class,
			() -> delegatingAuthenticationFilter.doFilter(request, response, new AuthorizationDataCheckEqual("abc12345@@@")));
	}

	@Test
	public void testWithRequestExcludedUrl() throws ServletException, IOException {
		DelegatingAuthenticationFilter excludedDelegatingAuthenticationFilter = new DelegatingAuthenticationFilter("Authorization", "/api/current-user");
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServletPath("/api");
		request.setPathInfo("/current-user");
		request.addHeader("Authorization", "abc12345@@@");
		MockHttpServletResponse response = new MockHttpServletResponse();
		excludedDelegatingAuthenticationFilter.doFilter(request, response, new AuthorizationDataIsNotCreated());
	}

	@Test
	public void delegatedMode() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		String tokenData = "UAABearer TokenToken";
		request.addHeader("Authorization", tokenData);
		delegatingAuthenticationFilter.doFilter(request, response, new DelegatedModeChecker(tokenData));

	}

	static class AuthorizationDataCheckEqual implements FilterChain {
		String idToken;

		AuthorizationDataCheckEqual(String idToken) {
			this.idToken = idToken;
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response) {
			AuthorizationDataStore credentialContext = AuthorizationDataHolder.getCredentialContext();
			Assertions.assertEquals(idToken, credentialContext.getAuthorizationData().getAuthenticationToken());
		}
	}

	static class AuthorizationDataIsNotCreated implements FilterChain {
		@Override
		public void doFilter(ServletRequest request, ServletResponse response) {
			AuthorizationDataStore credentialContext = AuthorizationDataHolder.getCredentialContext();
			Assertions.assertNull(credentialContext.getAuthorizationData());
		}
	}

	static class DelegatedModeChecker implements FilterChain {
		private AuthorizationInterceptor authorizationInterceptor =
			new AuthorizationInterceptor(AuthenticationType.LOCAL, new DelegatedAuthenticationHandler(), "http://localhost:8080", "Authorization");
		private String tokenValue;

		DelegatedModeChecker(String tokenValue) {
			this.tokenValue = tokenValue;
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response) {
			try {
				MockClientHttpRequest interceptorRequest = new MockClientHttpRequest();
				byte[] body = "test".getBytes(StandardCharsets.UTF_8);
				ClientHttpRequestExecution requestExecution = Mockito.mock(ClientHttpRequestExecution.class);
				Mockito.when(requestExecution.execute(Mockito.any(), Mockito.any())).thenReturn(new MockClientHttpResponse(body, HttpStatus.OK));
				authorizationInterceptor.intercept(interceptorRequest, body, requestExecution);

				List<String> authorizations = interceptorRequest.getHeaders().getValuesAsList(HttpHeaders.AUTHORIZATION);
				Assertions.assertEquals(tokenValue, authorizations.get(0));
			} catch (Exception e) {
				Assertions.fail("Exception has been thrown");
			}

		}
	}

}
