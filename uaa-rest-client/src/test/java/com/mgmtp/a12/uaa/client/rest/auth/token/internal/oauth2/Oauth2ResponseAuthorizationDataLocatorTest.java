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

import java.time.Instant;

import org.htmlunit.Page;
import org.htmlunit.WebResponse;
import org.htmlunit.WebWindow;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.TokenType;
import com.mgmtp.a12.uaa.client.rest.config.properties.OidcProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientAuthenticationProperties;

@ExtendWith(MockitoExtension.class)
public class Oauth2ResponseAuthorizationDataLocatorTest {

	private static final String DATA_RESPONSE = """
		{
		  "access_token": "tokenData",
		  "expires_in": 300,
		  "refresh_expires_in": 1800,
		  "refresh_token": "refreshTokenData",
		  "token_type": "Bearer",
		  "access_token": "accessTokenData",
		  "not-before-policy": 10000,
		  "session_state": "sessionState",
		  "scope": "openid profile email"
		}\
		""";

	private Oauth2ResponseAuthorizationDataLocator oauth2ResponseAuthorizationDataLocator =
		new Oauth2ResponseAuthorizationDataLocator(new UAARestClientAuthenticationProperties());
	private Page page;
	@Mock
	private WebResponse webResponse;
	@Mock
	private WebWindow webWindow;

	@BeforeEach
	void setUp() {
		page = new HtmlPage(webResponse, webWindow);
	}

	@Test
	void convertTestSuccessful() {
		Mockito.when(webResponse.getContentAsString()).thenReturn(DATA_RESPONSE);

		AuthorizationData authorizationData = oauth2ResponseAuthorizationDataLocator.convert(page);
		long expiration = Instant.now().plusSeconds(300).toEpochMilli() / 1000;
		Assertions.assertEquals("accessTokenData", authorizationData.getAuthenticationToken());
		Assertions.assertEquals(TokenType.BEARER, authorizationData.getAuthenticationTokenType());
		Assertions.assertEquals(240, authorizationData.getTokenRenewInSeconds());
		Assertions.assertEquals("refreshTokenData", authorizationData.getRefreshToken());
	}

	@Test
	void convertTestSuccessful_changeTokenRenewInterval() {
		Mockito.when(webResponse.getContentAsString()).thenReturn(DATA_RESPONSE);

		UAARestClientAuthenticationProperties restClientAuthenticationProperties = new UAARestClientAuthenticationProperties();
		OidcProperties oidcProperties = new OidcProperties();
		restClientAuthenticationProperties.setOidc(oidcProperties);
		Oauth2ResponseAuthorizationDataLocator oauth2ResponseAuthorizationDataLocator1 =
			new Oauth2ResponseAuthorizationDataLocator(restClientAuthenticationProperties);
		AuthorizationData authorizationData = oauth2ResponseAuthorizationDataLocator1.convert(page);
		Assertions.assertEquals(240, authorizationData.getTokenRenewInSeconds());
	}

	@Test
	void convertTestFail() {
		Mockito.when(webResponse.getContentAsString()).thenReturn("");

		IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> oauth2ResponseAuthorizationDataLocator.convert(page));

		Assertions.assertEquals("Unable to convert token response", exception.getMessage());
	}

}