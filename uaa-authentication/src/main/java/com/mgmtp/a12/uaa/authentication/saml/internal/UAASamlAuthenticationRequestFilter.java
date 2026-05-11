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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher.MatchResult;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mgmtp.a12.uaa.authentication.internal.RedirectSupport;
import com.mgmtp.a12.uaa.authentication.jwt.internal.CookieUtil;

public class UAASamlAuthenticationRequestFilter extends OncePerRequestFilter {

	public static final String COOKIE_SAML_REQUEST_ID = "saml-request-id";

	private RequestMatcher authenticateMatcher;

	private RedirectSupport loginRedirectSupport;

	private boolean httpOnly;
	private boolean secured;
	private int cookieLifetimeSeconds;

	public UAASamlAuthenticationRequestFilter(String context, RedirectSupport loginRedirectSupport, boolean httpOnly, boolean secured,
		int cookieLifetimeSeconds) {
		this.authenticateMatcher = new AntPathRequestMatcher(context + "/saml2/authenticate/*");
		this.loginRedirectSupport = loginRedirectSupport;
		this.httpOnly = httpOnly;
		this.secured = secured;
		this.cookieLifetimeSeconds = cookieLifetimeSeconds;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		MatchResult matcher = authenticateMatcher.matcher(request);
		if (!matcher.isMatch()) {
			filterChain.doFilter(request, response);
			return;
		}
		loginRedirectSupport.addCookies(request, response);

		filterChain.doFilter(request, response);

		// Store saml request id for verify with response later
		AuthnRequest authnRequestData = UAAThreadLocalAuthnRequestDataStore.getAuthnRequestData();
		if (authnRequestData != null && StringUtils.isNotEmpty(authnRequestData.getID())) {
			Cookie successCookie =
				CookieUtil.createCookie(COOKIE_SAML_REQUEST_ID, authnRequestData.getID(), null, request, httpOnly, secured, cookieLifetimeSeconds);
			response.addCookie(successCookie);
			UAAThreadLocalAuthnRequestDataStore.cleanUpAuthnRequestDataStore();
		}
	}

}
