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
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.mgmtp.a12.connector.rest.UrlBuilderSupport;
import com.mgmtp.a12.uaa.client.rest.auth.AuthenticationHandler;
import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.config.AuthenticationType;

public class AuthorizationInterceptor implements ClientHttpRequestInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationInterceptor.class);
	private static String LOGOUT_URL = "user/logout";

	private AuthenticationType authenticationType;
	private AuthenticationHandler authenticationHandler;
	private URI logoutUri;
	private String authorizationHeaderName;

	public AuthorizationInterceptor(AuthenticationType authenticationType, AuthenticationHandler authenticationHandler, String baseUaaUrl,
		String authorizationHeaderName) {
		this.authenticationType = authenticationType;
		this.authenticationHandler = authenticationHandler;
		this.authorizationHeaderName = authorizationHeaderName;
		this.logoutUri = UrlBuilderSupport.withBaseUrl(baseUaaUrl, LOGOUT_URL).createBuilder().build().toUri();
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		try {
			AuthorizationData credentialData = authenticationHandler.authenticate();
			if (credentialData != null && !AuthenticationType.CERTIFICATE.equals(authenticationType)) {
				HttpHeaders headers = request.getHeaders();
				headers.put(authorizationHeaderName,
					Collections.singletonList(
						"%s %s".formatted(credentialData.getAuthenticationTokenType().getTypeName(), credentialData.getAuthenticationToken())));
			}
		} catch (Exception e) {
			LOGGER.error("Unable to authenticate", e);

			ClientHttpResponse response = new ClientHttpResponse() {

				@Override
				public HttpHeaders getHeaders() {
					return new HttpHeaders();
				}

				@Override
				public InputStream getBody() throws IOException {
					return null;
				}

				@Override
				public String getStatusText() throws IOException {
					return "Unauthorized";
				}

				@Override
				public HttpStatusCode getStatusCode() throws IOException {
					return HttpStatusCode.valueOf(401);
				}

				@Override
				public void close() {

				}
			};
			return response;
		}
		if (request.getURI().equals(logoutUri)) {
			authenticationHandler.logout(request.getHeaders());
			ClientHttpResponse response = new ClientHttpResponse() {

				@Override
				public HttpHeaders getHeaders() {
					return new HttpHeaders();
				}

				@Override
				public InputStream getBody() throws IOException {
					return null;
				}

				@Override
				public String getStatusText() throws IOException {
					return "Logged out";
				}

				@Override
				public HttpStatusCode getStatusCode() throws IOException {
					return HttpStatusCode.valueOf(200);
				}

				@Override
				public void close() {

				}
			};
			return response;
		}
		return execution.execute(request, body);
	}

}
