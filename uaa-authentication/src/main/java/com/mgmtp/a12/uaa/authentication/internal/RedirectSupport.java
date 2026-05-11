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
package com.mgmtp.a12.uaa.authentication.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties.Redirect;
import com.mgmtp.a12.uaa.authentication.jwt.internal.CookieUtil;

public class RedirectSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(RedirectSupport.class);
	public static final String COOKIE_SUCCESS = "uaa_success";
	public static final String COOKIE_FAILURE = "uaa_failure";

	private Pattern redirectUrlValidator;
	private boolean httpOnly;
	private boolean secured;
	private int cookieLifetimeSeconds;

	private Redirect redirectConfiguration;

	public RedirectSupport(Redirect redirect, boolean httpOnly, boolean secured, int cookieLifetimeSeconds) {
		this.redirectConfiguration = redirect;
		this.redirectUrlValidator = Optional.ofNullable(redirect)
			.map(r -> r.getUrlPattern())
			.map(Pattern::compile)
			.orElse(null);
		this.httpOnly = httpOnly;
		this.secured = secured;
		this.cookieLifetimeSeconds = cookieLifetimeSeconds;
		validateRedirectUrl(redirect.getSuccess().getUrl());
		validateRedirectUrl(redirect.getFailure().getUrl());
	}

	public boolean performSuccessRedirect(RedirectStrategy redirectStrategy, HttpServletRequest request, HttpServletResponse response,
		Map<String, String> queryParameters) throws IOException {
		return performRedirect(redirectStrategy, request, response, COOKIE_SUCCESS, redirectConfiguration.getSuccess().getUrl(), queryParameters);
	}

	public boolean performFailureRedirect(RedirectStrategy redirectStrategy, HttpServletRequest request, HttpServletResponse response,
		Map<String, String> queryParameters) throws IOException {
		return performRedirect(redirectStrategy, request, response, COOKIE_FAILURE, redirectConfiguration.getFailure().getUrl(), queryParameters);
	}

	private boolean performRedirect(RedirectStrategy redirectStrategy, HttpServletRequest request, HttpServletResponse response, String redirectName,
		String configuredValue, Map<String, String> queryParameters) throws IOException {
		if (response.isCommitted()) {
			return true;
		}
		String redirectUrl = getRedirectUrl(request, redirectName).orElse(configuredValue);
		if (StringUtils.isNotBlank(redirectUrl)) {
			URI redirectUrlObject;
			try {
				redirectUrlObject = new URI(redirectUrl);
			} catch (URISyntaxException e) {
				throw new RuntimeException("Invalid URL " + redirectUrl);
			}
			UriComponentsBuilder uriBuilder;
			if (redirectUrlObject.isAbsolute()) {
				uriBuilder = UriComponentsBuilder.fromHttpUrl(redirectUrl);
			} else {
				uriBuilder = UriComponentsBuilder.fromPath(redirectUrl);
			}
			Optional.ofNullable(queryParameters)
				.orElse(Collections.emptyMap())
				.entrySet()
				.forEach(entry -> uriBuilder.queryParam(entry.getKey(), entry.getValue()));
			redirectUrl = uriBuilder.toUriString();
			CookieUtil.removeCookie(redirectName, request, response);
			validateRedirectUrl(redirectUrl);
			redirectStrategy.sendRedirect(request, response, redirectUrl);
		}
		LOGGER.info("Redirecting to a URL [{}]", Optional.ofNullable(redirectUrl).orElse(""));
		return StringUtils.isNotBlank(redirectUrl);
	}

	private Optional<String> getRedirectUrl(HttpServletRequest request, String redirectName) {
		return CookieUtil.locateCookie(request, redirectName)
			.or(() -> Optional.ofNullable(request.getParameter(redirectName)))
			.map(this::validateRedirectUrl);
	}

	public void addCookies(HttpServletRequest request, HttpServletResponse response) {
		String success = validateRedirectUrl(request.getParameter(COOKIE_SUCCESS));
		String failure = validateRedirectUrl(request.getParameter(COOKIE_FAILURE));
		if (success != null) {
			Cookie successCookie = CookieUtil.createCookie(COOKIE_SUCCESS, success, null, request, httpOnly, secured, cookieLifetimeSeconds);
			response.addCookie(successCookie);
		}
		if (failure != null) {
			Cookie failureCookie = CookieUtil.createCookie(COOKIE_FAILURE, failure, null, request, httpOnly, secured, cookieLifetimeSeconds);
			response.addCookie(failureCookie);
		}
	}

	private String validateRedirectUrl(String redirectUrl) {
		Optional.ofNullable(StringUtils.trimToNull(redirectUrl))
			.ifPresent(url -> {
				Assert.notNull(redirectUrlValidator, "Please specify configuration for Redirect URL validation");
				if (!redirectUrlValidator.matcher(url).matches()) {
					LOGGER.error("Unable to validate redirect url [{}] with validation pattern [{}]", url, redirectUrlValidator.pattern());
					throw new RuntimeException("Unable to validate redirect URL");
				}
			});
		return redirectUrl;

	}
}
