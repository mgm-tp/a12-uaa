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

import java.io.IOException;
import java.util.Optional;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mgmtp.a12.uaa.authentication.internal.AuthenticationTokenLocator;

public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

	private final AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver;
	private AuthenticationTokenLocator jwtTokenLocator;
	private AuthenticationEntryPoint authenticationEntryPoint;
	private AuthenticationFailureHandler authenticationFailureHandler =
		(request, response, exception) -> this.authenticationEntryPoint.commence(request, response, exception);

	public JwtTokenAuthenticationFilter(AuthenticationTokenLocator jwtTokenLocator, AuthenticationManager authenticationManager,
		AuthenticationEntryPoint authenticationEntryPoint) {
		this.jwtTokenLocator = jwtTokenLocator;
		this.authenticationEntryPoint = authenticationEntryPoint;
		this.authenticationManagerResolver = (request) -> authenticationManager;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
		throws ServletException, IOException {

		Optional<String> token = jwtTokenLocator.locateToken(request);
		if (token.isEmpty()) {
			chain.doFilter(request, response);
			return;
		}

		JwtAuthenticationToken authenticationRequest = new JwtAuthenticationToken(token.get());
		try {
			AuthenticationManager authenticationManager = this.authenticationManagerResolver.resolve(request);
			Authentication authenticationResult = authenticationManager.authenticate(authenticationRequest);

			SecurityContextHolder.getContext().setAuthentication(authenticationResult);
			chain.doFilter(request, response);

		} catch (AuthenticationException e) {
			logger.debug("Invalid token or request processing. Clearing user from security context", e);
			// In case of failure. Make sure it's clear; so guarantee user won't be authenticated
			SecurityContextHolder.clearContext();
			authenticationFailureHandler.onAuthenticationFailure(request, response, e);
		}
	}
}
