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
package com.mgmtp.a12.uaa.authentication.apikey.internal;

import java.security.cert.X509Certificate;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import com.mgmtp.a12.uaa.authentication.apikey.APIKeyValidator;
import com.mgmtp.a12.uaa.authentication.certificate.CertificateConverter;

public class APIKeyAuthenticationProvider implements AuthenticationProvider {

	private CertificateConverter apiKeyConverter;
	private RootCAManager rootCAManager;
	private APIKeyValidator apiKeyValidator;

	public APIKeyAuthenticationProvider(CertificateConverter apiKeyConverter, RootCAManager rootCAManager,
		APIKeyValidator apiKeyValidator) {
		this.apiKeyConverter = apiKeyConverter;
		this.rootCAManager = rootCAManager;
		this.apiKeyValidator = apiKeyValidator;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		APIKeyAuthenticationToken authToken = (APIKeyAuthenticationToken) authentication;
		X509Certificate apiKey = APIKeyUtils.getX509Certificate(authToken.getApiKey());
		apiKeyValidator.validate(apiKey, rootCAManager.getRootCAs());
		UserDetails userDetails = apiKeyConverter.convert(apiKey);
		if (userDetails != null) {
			return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		}
		return null;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return APIKeyAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
