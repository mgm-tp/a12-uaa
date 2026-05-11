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
package com.mgmtp.a12.uaa.example.authentication.local.basic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jakarta.inject.Inject;

import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;
import com.mgmtp.a12.uaa.authentication.local.LocalAuthenticationService;
import com.mgmtp.a12.uaa.authentication.local.UAAExtendedPrincipalDataLoader;
import com.mgmtp.a12.uaa.example.principal.basic.BasicPrincipal;

@Component
@Profile("!principal")
@ConditionalOnAuthentication(AuthenticationType.LOCAL)
public class BasicLocalAuthenticationService implements LocalAuthenticationService<BasicPrincipal> {

	private static final List<String> VALID_CREDENTIALS = Arrays.asList("admin", "thomas");

	@Inject
	private UAAExtendedPrincipalDataLoader extendedPrincipalDataLoader;

	@Override
	public BasicPrincipal authenticate(String userName, String rawPassword) {
		if (isCredentialsValid(userName, rawPassword)) {
			return new BasicPrincipal(userName, rawPassword, Collections.emptyList(), extendedPrincipalDataLoader.loadExtendedPrincipalData(userName));
		}
		throw new BadCredentialsException("Invalid credentials for %s".formatted(userName));
	}

	private boolean isCredentialsValid(String userName, String rawPassword) {
		return VALID_CREDENTIALS.stream()
			.filter(credential -> credential.equals(userName))
			.anyMatch(credential -> credential.equals(rawPassword));
	}
}
