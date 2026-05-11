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
package com.mgmtp.a12.uaa.client.rest.auth.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class SimpleOkHttpCookieJar implements CookieJar {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleOkHttpCookieJar.class);

	private List<Cookie> storage = new ArrayList<>();

	@Override
	public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
		LOGGER.debug("Received response [{}] cookies [{}]", url, cookies);
		storage.addAll(cookies);
	}

	@Override
	public List<Cookie> loadForRequest(HttpUrl url) {

		// Remove expired Cookies
		storage.removeIf(cookie -> cookie.expiresAt() < System.currentTimeMillis());

		// Only return matching Cookies
		List<Cookie> requestCookies = storage.stream().filter(cookie -> cookie.matches(url)).collect(Collectors.toList());
		LOGGER.debug("Sending request [{}] cookies: [{}]", url, requestCookies);
		return requestCookies;

	}
}