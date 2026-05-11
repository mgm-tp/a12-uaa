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

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

public class CookieUtil {

	/**
	 * Secured attribute is detected by the request scheme.
	 */
	public static Cookie createCookie(String name, String value, String domain, HttpServletRequest request, boolean httpOnly, Integer maxAgeSeconds) {
		return createCookie(name, value, domain, request, httpOnly, false, maxAgeSeconds);
	}

	public static Cookie createCookie(String name, String value, String domain, HttpServletRequest request, boolean httpOnly, boolean secure,
		Integer maxAgeSeconds) {
		Cookie c = new Cookie(name, value);
		String contextPath = request.getServletContext().getContextPath();
		c.setPath(StringUtils.isEmpty(contextPath) ? "/" : contextPath);
		if (StringUtils.isNotBlank(domain)) {
			c.setDomain(domain);
		}
		c.setHttpOnly(httpOnly);
		c.setSecure(secure);
		c.setMaxAge(maxAgeSeconds);
		return c;
	}

	public static Optional<String> locateCookie(HttpServletRequest request, String cookieName) {
		return locateCookie(request.getCookies(), cookieName);
	}

	public static Optional<String> locateCookie(Cookie[] cookies, String cookieName) {

		Optional<String> cookieValue = Optional.ofNullable(cookies).map(Arrays::stream).orElse(Stream.empty())
			.filter(cookie -> cookie.getName().equals(cookieName))
			.findFirst()
			.map(cookie -> cookie.getValue());

		return cookieValue;
	}

	public static void removeCookie(String name, HttpServletRequest request, HttpServletResponse response) {
		Cookie c = CookieUtil.createCookie(name, "", null, request, false, true, 0);
		response.addCookie(c);
	}
}
