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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.provider.service.authentication.logout.Saml2LogoutRequest;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutRequestResolver;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.mgmtp.a12.uaa.authentication.jwt.internal.JWTLogoutSuccessHandler;

/**
 * The filter is used in initial logout request.
 * In case that logout request is SAML which is detected by principal type: {@link com.mgmtp.a12.uaa.authentication.saml.SamlPrincipal}
 * Then generates SAML logout request to IDP.
 *
 */
public class SamlDelegatedLogoutSuccessHandler implements LogoutSuccessHandler {

	private Saml2LogoutRequestResolver logoutRequestResolver;
	private JWTLogoutSuccessHandler jwtLogoutSuccessHandler;
	private LogoutSuccessHandler delegate;

	public SamlDelegatedLogoutSuccessHandler(Saml2LogoutRequestResolver logoutRequestResolver, JWTLogoutSuccessHandler jwtLogoutSuccessHandler,
		LogoutSuccessHandler delegate) {
		this.logoutRequestResolver = logoutRequestResolver;
		this.jwtLogoutSuccessHandler = jwtLogoutSuccessHandler;
		this.delegate = delegate;
	}

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		Saml2LogoutRequest logoutRequest = this.logoutRequestResolver.resolve(request, authentication);
		if (logoutRequest == null) {
			//not SAML logout
			jwtLogoutSuccessHandler.onLogoutSuccess(request, response, authentication);
		} else {
			delegate.onLogoutSuccess(request, response, authentication);
		}

	}

}
