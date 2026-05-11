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

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

public class HeaderAuthenticationTokenLocator implements AuthenticationTokenLocator {

	private final String TOKEN_PARAM_NAME = "token";

	private String headerName;
	private TokenType tokenType;

	public HeaderAuthenticationTokenLocator(String headerName, TokenType tokenType) {
		this.headerName = headerName;
		this.tokenType = tokenType;
	}

	@Override
	public Optional<String> locateToken(HttpServletRequest request) {
		String authHeader = request.getHeader(headerName);

		if (StringUtils.isEmpty(authHeader)) {
			return loadTokenFromFormData(request);
		}

		if (!StringUtils.startsWithIgnoreCase(authHeader, tokenType.name())) {
			return Optional.empty();
		}
		String token = StringUtils.trim(StringUtils.replaceIgnoreCase(authHeader, tokenType.name(), ""));
		return Optional.of(token);
	}

	private Optional<String> loadTokenFromFormData(HttpServletRequest request) {
		String token = request.getParameter(TOKEN_PARAM_NAME);
		return StringUtils.isEmpty(token) ? Optional.empty() : Optional.of(token);
	}
}
