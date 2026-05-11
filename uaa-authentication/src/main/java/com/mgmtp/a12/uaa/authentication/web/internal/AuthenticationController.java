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
package com.mgmtp.a12.uaa.authentication.web.internal;

import java.util.Optional;

import jakarta.inject.Inject;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.config.client.ClientSelfconfiguration;
import com.mgmtp.a12.uaa.authentication.config.client.ClientSelfconfigurationBuilder;
import com.mgmtp.a12.uaa.authentication.principal.ExternalPrincipal;
import com.mgmtp.a12.uaa.authentication.principal.internal.PrincipalConverterService;
import com.mgmtp.a12.uaa.authentication.principal.internal.UAAExternalUserDetailsImpl;

@RestController
@ResponseBody
@RequestMapping("#{T(org.apache.commons.lang3.StringUtils).removeEnd('${mgmtp.a12.uaa.authentication.context-path:/}', '/')}/uaa-authentication")
public class AuthenticationController {

	@Inject
	private PrincipalConverterService principalConverterService;

	@Inject
	private AuthenticationProperties authenticationProperties;

	@GetMapping(value = "selfconfigure", produces = MediaType.APPLICATION_JSON_VALUE)
	public ClientSelfconfiguration selfConfiguration() {
		return ClientSelfconfigurationBuilder
			.withConfiguration(authenticationProperties)
			.build();
	}

	@GetMapping(value = "currentUser", produces = MediaType.APPLICATION_JSON_VALUE)
	public ExternalPrincipal currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Optional.ofNullable(authentication).orElseThrow(() -> new IllegalStateException("No user in the security context"));
		Object principal = authentication.getPrincipal();
		if (principal instanceof UserDetails details) {
			return principalConverterService.convertPrincipal(details);
		}
		return new UAAExternalUserDetailsImpl(principal.toString(), principal.toString());
	}

}
