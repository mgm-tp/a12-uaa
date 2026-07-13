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
package com.mgmtp.a12.uaa.authentication.apikey.internal;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mgmtp.a12.uaa.authentication.internal.AuthenticationTokenLocator;

public class APIKeyAuthenticationFilter extends OncePerRequestFilter {

	private AuthenticationTokenLocator authenticationTokenLocator;
	private AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver;
	private List<RequestMatcher> whiteListMatchers;

	private AuthenticationEntryPoint authenticationEntryPoint;
	private AuthenticationFailureHandler authenticationFailureHandler =
		(request, response, exception) -> this.authenticationEntryPoint.commence(request, response, exception);

	public APIKeyAuthenticationFilter(AuthenticationTokenLocator authenticationTokenLocator, String context, List<String> whiteListUrls,
		AuthenticationManager authenticationManager, AuthenticationEntryPoint authenticationEntryPoint) {
		this.authenticationTokenLocator = authenticationTokenLocator;
		this.whiteListMatchers = whiteListUrls.stream()
			.map(urlPattern -> PathPatternRequestMatcher.withDefaults().matcher(context + urlPattern))
			.collect(Collectors.toList());
		this.authenticationEntryPoint = authenticationEntryPoint;
		this.authenticationManagerResolver = (request) -> authenticationManager;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
		throws ServletException, IOException {

		Optional<String> apiKey = authenticationTokenLocator.locateToken(request);
		if (apiKey.isEmpty() || !isAValidUrl(request)) {
			chain.doFilter(request, response);
			return;
		}

		try {

			APIKeyAuthenticationToken authenticationRequest = new APIKeyAuthenticationToken(apiKey.get());
			AuthenticationManager authenticationManager = this.authenticationManagerResolver.resolve(request);
			Authentication authenticationResult = authenticationManager.authenticate(authenticationRequest);

			SecurityContextHolder.getContext().setAuthentication(authenticationResult);
			chain.doFilter(request, response);
		} catch (AuthenticationException e) {
			logger.error("Invalid api_key or request processing. Clearing user from security context", e);
			SecurityContextHolder.clearContext();
			authenticationFailureHandler.onAuthenticationFailure(request, response, e);
		}
	}

	/**
	 * check request url is on white list urls or not
	 * @param request
	 * @return boolean
	 */
	private boolean isAValidUrl(HttpServletRequest request) {
		return whiteListMatchers.stream()
			.anyMatch(requestMatcher -> requestMatcher.matches(request));
	}
}
