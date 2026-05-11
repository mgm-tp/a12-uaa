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
package com.mgmtp.a12.uaa.authentication.oauth.client.internal;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.mgmtp.a12.uaa.authentication.filter.AbstractPathFilter;
import com.mgmtp.a12.uaa.authentication.oauth.client.UaaOauth2ClientAuthenticationToken;

public class ReverseLogoutOauth2AuthenticationTokenFilter extends AbstractPathFilter {

	private static final String URL_LOGOUT = "/logout";

	public ReverseLogoutOauth2AuthenticationTokenFilter() {
		super(new AntPathRequestMatcher(URL_LOGOUT, "GET"));
	}

	@Override protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
		throws ServletException, IOException {
		UaaOauth2ClientAuthenticationToken authenticationToken = (UaaOauth2ClientAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		if (authenticationToken != null) {
			OAuth2AuthenticationToken oAuth2AuthenticationTokenBack = new OAuth2AuthenticationToken(
				authenticationToken.getoAuth2UserPrincipal(),
				authenticationToken.getAuthorities(),
				authenticationToken.getAuthorizedClientRegistrationId()
			);
			oAuth2AuthenticationTokenBack.setAuthenticated(true);
			oAuth2AuthenticationTokenBack.setDetails(authenticationToken.getDetails());
			SecurityContextHolder.getContext().setAuthentication(oAuth2AuthenticationTokenBack);
		}
		filterChain.doFilter(httpServletRequest, httpServletResponse);
	}
}
