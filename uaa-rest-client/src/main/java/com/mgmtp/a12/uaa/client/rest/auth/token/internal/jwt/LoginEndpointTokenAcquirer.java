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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.htmlunit.HttpMethod;
import org.htmlunit.WebRequest;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.TokenRefresher;
import com.mgmtp.a12.uaa.client.rest.auth.internal.locator.JWTResponseAuthorizationDataLocator;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.Request;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.UAAHttpTokenAcquirer;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientAuthenticationProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

public class LoginEndpointTokenAcquirer extends UAAHttpTokenAcquirer {

	private static final String LOGIN_REQUEST = "{\"username\":\"%s\", \"password\":\"%s\"}";
	private static final String LOGOUT_URL = "user/logout";

	public LoginEndpointTokenAcquirer(UAARestClientProperties clientConfiguration, AuthorizationDataStore authorizationDataStore,
		TokenRefresher tokenRefresher) {
		super(clientConfiguration, authorizationDataStore, new JWTResponseAuthorizationDataLocator(clientConfiguration.getGeneratedTokenHeaderName(),
			clientConfiguration.getGeneratedTokenRenewInSecondsHeaderName()), tokenRefresher);
	}

	@Override
	protected Request createLoginRequest() throws MalformedURLException {
		UAARestClientAuthenticationProperties authenticationConfiguration = uaaRestClientProperties.getAuthenticationConfiguration();
		URL loginUrl = new URL(getFullUrlWithUaaBasePrefix(authenticationConfiguration.getLoginRelative().getUrl()));
		WebRequest request = new WebRequest(loginUrl, HttpMethod.POST);
		request.setAdditionalHeader("Content-Type", "application/json");
		request.setCharset(StandardCharsets.UTF_8);
		request.setRequestBody(LOGIN_REQUEST.formatted(authenticationConfiguration.getUsername(), authenticationConfiguration.getPassword()));
		return new Request.Builder(request).build();
	}

	@Override
	protected LogoutConfig getLogoutConfig() {
		return new LogoutConfig(LOGOUT_URL, HttpMethod.POST);
	}
}
