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
package com.mgmtp.a12.uaa.client.rest.auth;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mgmtp.a12.uaa.client.rest.auth.internal.DelegatedAuthenticationHandler;
import com.mgmtp.a12.uaa.client.rest.auth.internal.delegated.AuthorizationDataHolder;
import com.mgmtp.a12.uaa.client.rest.auth.internal.locator.RequestAuthorizationDataLocator;

/**
 * Copy credentials from incoming request into {@link AuthorizationDataHolder} used by the JAVA client
 *
 * When you use {@link DelegatedAuthenticationHandler} you might need to copy credentials. 
 *
 */
public class DelegatingAuthenticationFilter extends OncePerRequestFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(DelegatingAuthenticationFilter.class);

	private String authorizationHeaderName;
	private AntPathMatcher requestMatcher;
	private List<String> excludedContexts;

	public DelegatingAuthenticationFilter(String authorizationHeaderName, String... excludedContexts) {
		this.authorizationHeaderName = authorizationHeaderName;
		requestMatcher = createMatcher();
		this.excludedContexts = Arrays.asList(excludedContexts);
	}

	private AntPathMatcher createMatcher() {
		AntPathMatcher matcher = new AntPathMatcher();
		matcher.setTrimTokens(false);
		matcher.setCaseSensitive(false);
		return matcher;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String requestPath = getRequestPath(request);
		boolean requestExcluded = excludedContexts.stream().anyMatch(excludedContext -> requestMatcher.match(excludedContext, requestPath));
		return requestExcluded;
	}

	private String getRequestPath(HttpServletRequest request) {
		String url = request.getServletPath();
		String pathInfo = request.getPathInfo();
		if (pathInfo != null) {
			url = StringUtils.hasLength(url) ? url + pathInfo : pathInfo;
		}
		return url;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		RequestAuthorizationDataLocator dataLocator = new RequestAuthorizationDataLocator(authorizationHeaderName);
		AuthorizationData authorizationData = dataLocator.convert(request);
		if (authorizationData == null) {
			LOGGER.debug("Logging Request  {} : {} - no authorization header present, skipping credentials", request.getMethod(), request.getRequestURI());
			filterChain.doFilter(request, response);
			return;
		}
		AuthorizationDataStore credentialsContext = AuthorizationDataHolder.getCredentialContext();
		LOGGER.debug("Logging Request  {} : {} - setting credentials", request.getMethod(), request.getRequestURI());
		credentialsContext.setAuthorizationData(authorizationData);
		try {
			filterChain.doFilter(request, response);
		} finally {
			credentialsContext.cleanUpAuthorizationStore();
			LOGGER.debug("Logging Response : {} cleaning credentials", response.getContentType());
		}
	}
}
