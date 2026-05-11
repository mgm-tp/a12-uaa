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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.htmlunit.CookieManager;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.htmlunit.WebResponse;
import org.htmlunit.util.Cookie;
import org.htmlunit.util.NameValuePair;
import org.springframework.util.Assert;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.internal.CookieSupport;
import com.mgmtp.a12.uaa.client.rest.auth.internal.RequestKeys;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.TokenType;

public class JWTResponseAuthorizationDataLocator extends AbstractHttpAuthorizationDataLocator implements AuthorizationDataLocator<Page> {

	private String generatedTokenHeaderName;
	private String generatedTokenRenewInSecondsHeaderName;

	public JWTResponseAuthorizationDataLocator(String generatedTokenHeaderName, String generatedTokenRenewInSecondsHeaderName) {
		Assert.notNull(generatedTokenHeaderName, "Generated token header name must be specified");
		Assert.notNull(generatedTokenRenewInSecondsHeaderName, "Generated token renew in seconds header name must be specified");
		this.generatedTokenHeaderName = generatedTokenHeaderName;
		this.generatedTokenRenewInSecondsHeaderName = generatedTokenRenewInSecondsHeaderName;
	}

	@Override
	public AuthorizationData convert(Page page) {
		WebClient webClient = page.getEnclosingWindow().getWebClient();
		WebResponse webResponse = page.getWebResponse();
		List<NameValuePair> responseHeaders = webResponse.getResponseHeaders();
		List<HttpCookie> cookies = extractCookies(webClient.getCookieManager());
		String authHeader = findAuthenticationTokenHeader(responseHeaders);
		String tokenRenewInSecondsHeader = findTokenRenewInSecondsHeader(responseHeaders);
		return convertData(cookies, authHeader, tokenRenewInSecondsHeader, webResponse.getWebRequest().getUrl().toString(), TokenType.UAABEARER);
	}

	private List<HttpCookie> extractCookies(CookieManager cookieManager) {
		final Set<Cookie> cookies = cookieManager.getCookies();
		List<String> cookiesArr = cookies.stream()
			.filter(header -> header.getName().equals(RequestKeys.SESSION_KEY))
			.map(Cookie::toString)
			.collect(Collectors.toList());
		return CookieSupport.convert(cookiesArr);
	}

	private String findAuthenticationTokenHeader(List<NameValuePair> responseHeaders) {
		return responseHeaders.stream()
			.filter(header -> generatedTokenHeaderName.equals(header.getName()))
			.map(NameValuePair::getValue)
			.findFirst().orElse(null);
	}

	private String findTokenRenewInSecondsHeader(List<NameValuePair> responseHeaders) {
		return responseHeaders.stream()
			.filter(header -> generatedTokenRenewInSecondsHeaderName.equals(header.getName()))
			.map(NameValuePair::getValue)
			.findFirst().orElse(null);
	}
}
