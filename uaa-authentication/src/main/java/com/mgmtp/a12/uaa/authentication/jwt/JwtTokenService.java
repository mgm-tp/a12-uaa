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
package com.mgmtp.a12.uaa.authentication.jwt;

import jakarta.inject.Inject;

import org.springframework.security.core.userdetails.UserDetails;

import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenGenerator;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenVerifier;

/**
 * Service for operations with UAA Access Token
 *
 */
public class JwtTokenService {

	@Inject
	private JwtTokenGenerator jwtTokenGeneratorSupport;

	@Inject
	private JwtTokenVerifier jwtTokenVerifier;

	@Inject
	private JwtTokenStorage jwtTokenStorage;

	/**
	 * Generate JWT token from given principal object.
	 *
	 * @param userDetails principal data
	 */
	public JwtTokenData generateToken(UserDetails userDetails) {
		return jwtTokenGeneratorSupport.generateToken(userDetails);
	}

	/**
	 * Using internal uaa token support to check token is valid or not
	 * (check token expired and token blacklist)
	 *
	 * @param token token string
	 * @return true/false
	 */
	public Boolean isTokenValid(String token) {
		return jwtTokenVerifier.isTokenValid(token);
	}

	/**
	 * Remove the token by storing to blacklist
	 *
	 * @param token token string
	 */
	public void invalidToken(String token) {
		jwtTokenStorage.storeToken(token);
	}

	/**
	 * Unpack given token content.
	 */
	public JwtTokenData unpackToken(String token) {
		return jwtTokenVerifier.unpackToken(token);
	}

}
