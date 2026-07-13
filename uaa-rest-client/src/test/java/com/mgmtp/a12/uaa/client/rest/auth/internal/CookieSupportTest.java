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

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.TokenType;

public class CookieSupportTest {

	@Test
	void createCookiesTest() {
		List<String> cookies = CookieSupport.createCookies(new AuthorizationData("tokenData", TokenType.BEARER, "sessionData", 300));

		Assertions.assertEquals(1, cookies.size());
		Assertions.assertEquals("JSESSIONID=\"sessionData\"", cookies.get(0));
	}

	@Test
	void convertTestSuccessful() {
		List<String> cookies = new ArrayList<>();
		cookies.add("JSESSIONID=\"sessionData\"");

		List<HttpCookie> convert = CookieSupport.convert(cookies);

		Assertions.assertFalse(convert.isEmpty());
		Assertions.assertEquals("JSESSIONID", convert.get(0).getName());
		Assertions.assertEquals("sessionData", convert.get(0).getValue());
	}

	@Test
	void convertTestFail() {
		List<String> cookies = new ArrayList<>();
		cookies.add(null);

		List<HttpCookie> convert = CookieSupport.convert(cookies);

		Assertions.assertTrue(convert.isEmpty());
	}

	@Test
	void findCookieTest() {
		List<HttpCookie> cookies = new ArrayList<>();
		cookies.add(new HttpCookie(RequestKeys.SESSION_KEY, "sessionData"));

		Optional<HttpCookie> cookie = CookieSupport.findCookie(cookies, RequestKeys.SESSION_KEY);

		Assertions.assertEquals("sessionData", cookie.get().getValue());
	}

}