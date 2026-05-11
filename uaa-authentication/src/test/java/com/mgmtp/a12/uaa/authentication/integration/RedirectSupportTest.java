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
package com.mgmtp.a12.uaa.authentication.integration;

import java.io.IOException;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties.Redirect;
import com.mgmtp.a12.uaa.authentication.internal.RedirectSupport;

public class RedirectSupportTest {

	private RedirectSupport samlRedirectSupport;
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	@BeforeEach
	void setUp() {
		Redirect redirect = new Redirect();
		redirect.getSuccess().setUrl("http://localhost:8080/");
		redirect.setUrlPattern("http://localhost:8080/");
		samlRedirectSupport = new RedirectSupport(redirect, false, false, 180);
	}

	@Test
	public void removeFailureCookieRedirect() throws IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setCookies(new Cookie(RedirectSupport.COOKIE_FAILURE, "http://localhost:8080/"));
		samlRedirectSupport.performFailureRedirect(redirectStrategy, request, response, null);
		Assertions.assertEquals("", response.getCookies()[0].getValue());
		Assertions.assertEquals(0, response.getCookies()[0].getMaxAge());
	}

	@Test
	@Disabled
	public void removeSuccessCookieRedirect() throws IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setCookies(new Cookie(RedirectSupport.COOKIE_SUCCESS, "http://localhost:8080/"));
		samlRedirectSupport.performSuccessRedirect(redirectStrategy, request, response, null);
		Assertions.assertEquals("", response.getCookies()[0].getValue());
		Assertions.assertEquals(0, response.getCookies()[0].getMaxAge());
	}

	@Test
	public void redirectUrlWithEmptyValueSupport() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.addParameter("uaa_success", "");
		Assertions.assertDoesNotThrow(() -> {
			samlRedirectSupport.addCookies(request, response);
		});
	}

	@Test
	public void redirectUrlWithNotEmptyValueAndValidateWrongSupport() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.addParameter("uaa_success", "http://localhost:8080/abc");
		Assertions.assertThrows(RuntimeException.class, () -> {
			samlRedirectSupport.addCookies(request, response);
		});
	}

	@Test
	public void redirectUrlWithNotEmptyValueAndValidateRightSupport() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.addHeader("uaa_success", "http://localhost:8080/");
		Assertions.assertDoesNotThrow(() -> {
			samlRedirectSupport.addCookies(request, response);
		});
	}

}
