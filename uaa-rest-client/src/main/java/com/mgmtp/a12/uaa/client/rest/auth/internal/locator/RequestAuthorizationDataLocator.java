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
package com.mgmtp.a12.uaa.client.rest.auth.internal.locator;

import java.net.HttpCookie;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.internal.CookieSupport;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.TokenType;

public class RequestAuthorizationDataLocator extends AbstractHttpAuthorizationDataLocator implements AuthorizationDataLocator<HttpServletRequest> {

	private String authorizationHeaderName;

	public RequestAuthorizationDataLocator(String authorizationHeaderName) {
		this.authorizationHeaderName = authorizationHeaderName;
	}

	@Override
	public AuthorizationData convert(HttpServletRequest request) {
		if (StringUtils.isBlank(authorizationHeaderName) || StringUtils.isBlank(request.getHeader(authorizationHeaderName))) {
			return null;
		}
		List<HttpCookie> cookies = CookieSupport.convert(extractCookies(request));
		String authenticationToken = request.getHeader(authorizationHeaderName);
		AuthorizationData authorizationData = convertData(cookies, authenticationToken, null, request.getRequestURL().toString(), TokenType.DELEGATED);
		//in delegated mode we can;t have userID calculated with each req. This would make caching useless 
		authorizationData.setUniqueUserIdentification(null);
		return authorizationData;
	}

	private List<String> extractCookies(HttpServletRequest request) {
		String cookie = request.getHeader(HttpHeaders.COOKIE);
		if (StringUtils.isNotBlank(cookie)) {
			String[] cookies = StringUtils.split(cookie, ";");
			return Arrays.asList(cookies);
		}
		return Collections.emptyList();
	}
}
