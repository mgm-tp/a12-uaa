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
package com.mgmtp.a12.uaa.authentication.oauth2.internal;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

public class OpaqueTokenValidator implements OAuth2TokenValidator<Jwt> {
	private OpaqueTokenIntrospector introspector;

	public OpaqueTokenValidator(OpaqueTokenIntrospector introspector) {
		this.introspector = introspector;
	}

	@Override
	public OAuth2TokenValidatorResult validate(Jwt token) {
		try {
			introspector.introspect(token.getTokenValue());
		} catch (Exception e) {
			OAuth2Error oAuth2Error = createOAuth2Error("Token is not activate");
			return OAuth2TokenValidatorResult.failure(oAuth2Error);
		}
		return OAuth2TokenValidatorResult.success();
	}

	private OAuth2Error createOAuth2Error(String reason) {
		return new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, reason, "https://datatracker.ietf.org/doc/html/rfc7009");
	}

}
