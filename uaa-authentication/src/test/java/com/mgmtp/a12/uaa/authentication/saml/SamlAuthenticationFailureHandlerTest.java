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
package com.mgmtp.a12.uaa.authentication.saml;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;

import com.mgmtp.a12.uaa.authentication.internal.RedirectSupport;
import com.mgmtp.a12.uaa.authentication.jwt.internal.CookieUtil;
import com.mgmtp.a12.uaa.authentication.saml.internal.SamlAuthenticationFailureHandler;
import com.mgmtp.a12.uaa.authentication.saml.internal.UAASamlAuthenticationRequestFilter;

@ExtendWith(MockitoExtension.class)
public class SamlAuthenticationFailureHandlerTest {

	@Mock
	RedirectSupport samlLoginRedirectSupport;

	@InjectMocks
	private SamlAuthenticationFailureHandler samlAuthenticationFailureHandler;

	@Test
	public void testSendRedirectSupport() throws ServletException, IOException {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		MockHttpServletResponse httpResponse = new MockHttpServletResponse();
		Cookie cookieSamlRequestId =
			CookieUtil.createCookie(UAASamlAuthenticationRequestFilter.COOKIE_SAML_REQUEST_ID, "TEST_VALUE", null, httpRequest, false, true,
				10);
		httpRequest.setCookies(cookieSamlRequestId);
		samlAuthenticationFailureHandler.onAuthenticationFailure(httpRequest, httpResponse, new AuthenticationServiceException("AuthenticationException"));
		Mockito.verify(samlLoginRedirectSupport, Mockito.atLeastOnce()).performFailureRedirect(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		MatcherAssert.assertThat(httpResponse.getCookie(UAASamlAuthenticationRequestFilter.COOKIE_SAML_REQUEST_ID), Matchers.notNullValue());
		MatcherAssert.assertThat(httpResponse.getCookie(UAASamlAuthenticationRequestFilter.COOKIE_SAML_REQUEST_ID).getMaxAge(),
			Matchers.is(Integer.valueOf(0)));
	}

}
