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

import java.net.HttpCookie;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.internal.RequestKeys;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.TokenType;

public class AbstractHttpAuthorizationDataLocatorTest {

	private AbstractHttpAuthorizationDataLocator abstractHttpAuthorizationDataLocator = new AbstractHttpAuthorizationDataLocator();

	@Test
	void convertDataTestSuccessful() {
		List<HttpCookie> cookies = new ArrayList<>();
		cookies.add(new HttpCookie(RequestKeys.SESSION_KEY, "sessionData"));
		String authToken = "token data";
		String url = "http://localhost:8080";

		AuthorizationData authorizationData = abstractHttpAuthorizationDataLocator.convertData(cookies, authToken, "50", url, TokenType.UAABEARER);

		Assertions.assertEquals(authToken, authorizationData.getAuthenticationToken());
		Assertions.assertEquals(50, authorizationData.getTokenRenewInSeconds());
		Assertions.assertEquals(TokenType.UAABEARER, authorizationData.getAuthenticationTokenType());
		Assertions.assertEquals("sessionData", authorizationData.getSessionId());
	}

	@Test
	void convertDataTestFail() {
		List<HttpCookie> cookies = new ArrayList<>();
		cookies.add(new HttpCookie(RequestKeys.SESSION_KEY, "sessionData"));
		String authToken = " ";
		String authTokenExpiration = String.valueOf(Instant.now().toEpochMilli());
		String url = "http://localhost:8080";

		RuntimeException runtimeException = Assertions.assertThrows(RuntimeException.class, () ->
			abstractHttpAuthorizationDataLocator.convertData(cookies, authToken, authTokenExpiration, url, TokenType.UAABEARER));

		Assertions.assertEquals("Unable convert credentials. Is your credentials valid ? - missing auth token in page %s".formatted(url),
			runtimeException.getMessage());
	}

}