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

import java.net.HttpCookie;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;

public class CookieSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(CookieSupport.class);

	public static List<String> createCookies(AuthorizationData credentialStore) {
		List<String> cookies = new LinkedList<>();
		cookies.add(new HttpCookie(RequestKeys.SESSION_KEY, credentialStore.getSessionId()).toString());
		return cookies;
	}

	public static List<HttpCookie> convert(List<String> cookies) {
		if (!CollectionUtils.isEmpty(cookies)) {
			try {
				return HttpCookie.parse("set-cookie2:" + StringUtils.join(cookies, ","));
			} catch (IllegalArgumentException | NullPointerException e) {
				LOGGER.error("Unable to convert to HttpCookie from String", e);
			}
		}
		return Collections.emptyList();
	}

	public static Optional<HttpCookie> findCookie(Collection<HttpCookie> cookies, String cookieName) {

		return cookies.stream()
			.filter(cookie -> cookieName.equals(cookie.getName()))
			.findFirst();
	}
}
