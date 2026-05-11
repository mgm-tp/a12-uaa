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

import java.util.Arrays;
import java.util.List;

import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

public class DelegatedJWTDecoder implements JwtDecoder {

	private List<JwtDecoder> jwtDecoders;

	public DelegatedJWTDecoder(JwtDecoder... jwtDecoders) {
		super();
		this.jwtDecoders = Arrays.asList(jwtDecoders);
	}

	@Override
	public Jwt decode(String token) throws JwtException {
		return jwtDecoders.stream()
			.map(decoder -> {
				try {
					Jwt jwtToken = decoder.decode(token);
					return jwtToken;
				} catch (Exception e) {
					return null;
				}
			})
			.filter(jwtToken -> jwtToken != null)
			.findFirst().orElseThrow(() -> new BadJwtException("Have not found any proper jwtDecoder which successfully decode for the input token"));
	}
}
