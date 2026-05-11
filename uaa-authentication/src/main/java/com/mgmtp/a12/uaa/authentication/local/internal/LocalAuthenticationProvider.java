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
package com.mgmtp.a12.uaa.authentication.local.internal;

import java.util.Collections;
import java.util.Objects;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.local.LocalAuthenticationService;
import com.mgmtp.a12.uaa.authentication.security.login.internal.TypedUsernamePasswordAuthenticationToken;

/**
 * Check If the current user has given access right.
 *
 */
public class LocalAuthenticationProvider implements AuthenticationProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(LocalAuthenticationProvider.class);

	@Inject
	private LocalAuthenticationService<?> localAuthenticationService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		TypedUsernamePasswordAuthenticationToken<?> localToken = (TypedUsernamePasswordAuthenticationToken<?>) authentication;
		if (localToken.getType() != AuthenticationType.LOCAL) {
			return null;
		}
		String userName = Objects.toString(authentication.getPrincipal());
		String rawPassword = Objects.toString(authentication.getCredentials());
		//load local user and validate pwd 
		try {
			Object principal = localAuthenticationService.authenticate(userName, rawPassword);
			TypedUsernamePasswordAuthenticationToken<?> tokenOut =
				new TypedUsernamePasswordAuthenticationToken<Void>(principal, authentication.getCredentials(), AuthenticationType.LOCAL,
					Collections.emptyList());
			return tokenOut;
		} catch (Exception e) {
			LOGGER.warn("Unable to authenticate", e);
			throw new BadCredentialsException("Wrong credentials", e);
		}

	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(TypedUsernamePasswordAuthenticationToken.class);
	}

}
