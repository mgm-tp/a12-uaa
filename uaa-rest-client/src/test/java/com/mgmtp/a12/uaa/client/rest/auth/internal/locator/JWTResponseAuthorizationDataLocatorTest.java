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
package com.mgmtp.a12.uaa.client.rest.auth.internal.locator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.htmlunit.CookieManager;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.WebWindow;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.util.Cookie;
import org.htmlunit.util.NameValuePair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.internal.RequestKeys;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.TokenType;

@ExtendWith(MockitoExtension.class)
public class JWTResponseAuthorizationDataLocatorTest {

	private JWTResponseAuthorizationDataLocator jwtResponseAuthorizationDataLocator =
		new JWTResponseAuthorizationDataLocator("access_token", "token_renew_in_seconds");
	private String baseUrl = "http://localhost:8080";
	private CookieManager cookieManager = new CookieManager();
	private Page page;
	@Mock
	private WebClient webClient;
	@Mock
	private WebRequest webRequest;
	@Mock
	private WebResponse webResponse;
	@Mock
	private WebWindow webWindow;

	@BeforeEach
	void setUp() {
		page = new HtmlPage(webResponse, webWindow);
		cookieManager.addCookie(new Cookie(baseUrl, RequestKeys.SESSION_KEY, "sessionData"));
	}

	@Test
	void convertTest() throws MalformedURLException {
		String authToken = "token data";

		List<NameValuePair> responseHeaders = new ArrayList<>();
		responseHeaders.add(new NameValuePair("access_token", authToken));
		responseHeaders.add(new NameValuePair("token_renew_in_seconds", "50"));

		Mockito.when(webWindow.getWebClient()).thenReturn(webClient);
		Mockito.when(webClient.getCookieManager()).thenReturn(cookieManager);
		Mockito.when(webResponse.getResponseHeaders()).thenReturn(responseHeaders);
		Mockito.when(webResponse.getWebRequest()).thenReturn(webRequest);
		Mockito.when(webRequest.getUrl()).thenReturn(new URL(baseUrl));

		AuthorizationData authorizationData = jwtResponseAuthorizationDataLocator.convert(page);

		Assertions.assertEquals(authToken, authorizationData.getAuthenticationToken());
		Assertions.assertEquals(50, authorizationData.getTokenRenewInSeconds());
		Assertions.assertEquals(TokenType.UAABEARER, authorizationData.getAuthenticationTokenType());
		Assertions.assertEquals("sessionData", authorizationData.getSessionId());
	}

}