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
package com.mgmtp.a12.uaa.authentication.saml.internal;

import java.util.Collection;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties.Cookie.SameSite;
import com.mgmtp.a12.uaa.authentication.jwt.internal.CookieUtil;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SameSiteFilterTest {

	private static final String COOKIE_TEST2 = "TEST2";
	private static final String COOKIE_TEST = "TEST";
	private static final String SCHEME_HTTP = "http";
	private static final String SCHEME_HTTPS = "https";

	@Test
	public void checkUnsecuredConnection() throws Exception {
		checkFilter(SameSite.STRICT, SCHEME_HTTP, true);
		checkFilter(SameSite.LAX, SCHEME_HTTP, true);
		checkFilter(SameSite.NONE, SCHEME_HTTP, true);
		checkFilter(SameSite.UNSET, SCHEME_HTTP, false);
	}

	@Test
	public void checkSecuredConnection() throws Exception {
		checkFilter(SameSite.STRICT, SCHEME_HTTPS, true);
		checkFilter(SameSite.LAX, SCHEME_HTTPS, true);
		checkFilter(SameSite.NONE, SCHEME_HTTPS, true);
		checkFilter(SameSite.UNSET, SCHEME_HTTPS, false);
	}

	private void checkFilter(SameSite value, String scheme, boolean expectedMatch) throws Exception {
		SameSiteFilter filter = new SameSiteFilter(value);
		MockHttpServletRequest request = prepareRequest(scheme);
		MockHttpServletResponse response = prepareResponse(request);
		filter.doFilter(request, response, (req, resp) -> {
		});
		checkCookieForSameSite(value, response, expectedMatch);
	}

	private void checkCookieForSameSite(SameSite value, HttpServletResponse response, boolean expected) {
		Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE);
		headers.forEach(cookie -> {
			Assertions.assertEquals(expected, StringUtils.contains(cookie, value.name()));
		});
	}

	private MockHttpServletRequest prepareRequest(String scheme) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setScheme(scheme);

		return request;
	}

	private MockHttpServletResponse prepareResponse(HttpServletRequest request) {
		MockHttpServletResponse response = new MockHttpServletResponse();
		response.addCookie(CookieUtil.createCookie(COOKIE_TEST, "TEST_VALUE", null, request, false, 10));
		response.addCookie(CookieUtil.createCookie(COOKIE_TEST2, "TEST_VALUE_2", null, request, false, 10));

		return response;
	}
}
