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
import java.util.Collection;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties.Cookie.SameSite;

/**
 * Add SameSite parameter to all SML related cookies.
 * It add only when the request is made over https connection. 
 *
 */
public class SameSiteFilter implements Filter {

	private static final String SAME_SITE_NAME = "SameSite";
	private static final String COOKIE_SESSION = "JSESSIONID";

	private SameSite sameSiteValue;

	public SameSiteFilter(SameSite sameSiteValue) {
		this.sameSiteValue = sameSiteValue;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		chain.doFilter(request, response);
		if (SameSite.UNSET != sameSiteValue) {
			addSameSiteCookieAttribute((HttpServletResponse) response);
		}

	}

	private void addSameSiteCookieAttribute(HttpServletResponse response) {
		Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE);
		boolean firstHeader = true;
		for (String header : headers) {
			if (StringUtils.containsIgnoreCase(header, COOKIE_SESSION)) {
				continue;
			}
			String newCookieValue = "%s; %s=%s".formatted(header, SAME_SITE_NAME, sameSiteValue.name());
			if (firstHeader) {
				response.setHeader(HttpHeaders.SET_COOKIE, newCookieValue);
				firstHeader = false;
				continue;
			}
			response.addHeader(HttpHeaders.SET_COOKIE, newCookieValue);
		}
	}
}
