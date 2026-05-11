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

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import com.mgmtp.a12.uaa.authentication.internal.RedirectSupport;
import com.mgmtp.a12.uaa.authentication.internal.RedirectType;
import com.mgmtp.a12.uaa.authentication.internal.RedirectType.Type;
import com.mgmtp.a12.uaa.authentication.jwt.internal.CookieUtil;

public class SamlAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(SamlAuthenticationFailureHandler.class);

	@Inject
	@RedirectType(type = Type.LOGIN)
	private RedirectSupport loginRedirectSupport;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
		throws IOException, ServletException {

		LOGGER.error("Authentication failed: [{}]", exception.getMessage());
		CookieUtil.removeCookie(UAASamlAuthenticationRequestFilter.COOKIE_SAML_REQUEST_ID, request, response);
		if (!loginRedirectSupport.performFailureRedirect(getRedirectStrategy(), request, response, null)) {
			super.onAuthenticationFailure(request, response, exception);
		}
	}
}
