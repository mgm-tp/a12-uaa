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
package com.mgmtp.a12.uaa.authentication.jwt.internal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import com.mgmtp.a12.uaa.authentication.internal.AuthenticationTokenLocator;
import com.mgmtp.a12.uaa.authentication.internal.RedirectSupport;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenStorage;

public class JwtTokenLogoutHandler implements LogoutHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenLogoutHandler.class);

	private AuthenticationTokenLocator tokenLocator;
	private JwtTokenStorage tokenStorage;
	private RedirectSupport redirectSupport;

	public JwtTokenLogoutHandler(AuthenticationTokenLocator tokenLocator, JwtTokenStorage tokenStorage, RedirectSupport redirectSupport) {
		this.tokenLocator = tokenLocator;
		this.tokenStorage = tokenStorage;
		this.redirectSupport = redirectSupport;
	}

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		String token = locateToken(request, response);
		storeToken(token);
		redirectSupport.addCookies(request, response);
	}

	private void storeToken(String token) {
		tokenStorage.storeToken(token);
		LOGGER.info("JWT token invalidated due to logout [{}].", token);

	}

	private String locateToken(HttpServletRequest request, HttpServletResponse response) {
		return tokenLocator
			.locateToken(request)
			.orElseThrow(() -> new AccessDeniedException("No JWT token found in the request or SAML storage"));
	}

}
