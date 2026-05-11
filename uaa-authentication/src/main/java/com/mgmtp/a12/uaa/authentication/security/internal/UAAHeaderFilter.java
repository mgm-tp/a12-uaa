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
package com.mgmtp.a12.uaa.authentication.security.internal;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties.HeaderConfiguration;

public class UAAHeaderFilter extends OncePerRequestFilter {

	private static final String HEADER_NAME_CONTENT_TYPE = "X-Content-Type-Options";
	private static final String HEADER_NAME_FRAME_OPTIONS = "X-Frame-Options";
	private static final String HEADER_NAME_CSP = "Content-Security-Policy";
	private static final String HEADER_CSP_PREFIX = "frame-ancestors ";
	private HeaderConfiguration headerConfiguration;

	public UAAHeaderFilter(HeaderConfiguration headerConfiguration) {
		this.headerConfiguration = headerConfiguration;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} finally {
			generateHeaders(response);
		}
	}

	private void generateHeaders(HttpServletResponse response) {
		response.addHeader(HEADER_NAME_CONTENT_TYPE, headerConfiguration.getxContentType());
		if (StringUtils.isNotBlank(headerConfiguration.getxFrameOptions())) {
			response.addHeader(HEADER_NAME_FRAME_OPTIONS, headerConfiguration.getxFrameOptions());
		} else if (headerConfiguration.getContentSecurityPolicySources() != null) {
			response.addHeader(HEADER_NAME_CSP,
				"%s %s".formatted(HEADER_CSP_PREFIX, StringUtils.join(headerConfiguration.getContentSecurityPolicySources(), StringUtils.SPACE)));
		}
	}
}
