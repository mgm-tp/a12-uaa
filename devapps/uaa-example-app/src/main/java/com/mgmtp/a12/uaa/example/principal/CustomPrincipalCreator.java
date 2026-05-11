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
package com.mgmtp.a12.uaa.example.principal;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenData;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalAdapter;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalCreator;
import com.mgmtp.a12.uaa.example.principal.extension.ExtendedPrincipal;

//
//@Component
//@Primary
public class CustomPrincipalCreator implements PrincipalCreator<UserDetails> {
	protected static final Logger LOGGER = LoggerFactory.getLogger(CustomPrincipalCreator.class);
	@Inject
	private PrincipalAdapter<?> principalFactory;

	@Override
	public UserDetails createPrincipal(JwtTokenData tokenData) {
		if (isUaaSilentRenewRequestCalled()) {
			if (tokenData.getPrincipal() instanceof ExtendedPrincipal extendedPrincipal) {
				Map<String, Object> attributes = extendedPrincipal.getAdditionalProperties();
				attributes.put("mepiToken", UUID.randomUUID().toString());
				LOGGER.info("Renew successfully with new mepiToken: {}", attributes.get("mepiToken"));
				extendedPrincipal.setAdditionalProperties(attributes);
				return extendedPrincipal;
			}
		}
		return Optional.ofNullable(tokenData.getPrincipal())
			.orElseGet(() -> principalFactory.createPrincipal(tokenData.getUsername(), tokenData.getAuthorities()));
	}

	private boolean isUaaSilentRenewRequestCalled() {
		return RequestContextHolder.getRequestAttributes() != null &&
			RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes servletRequestAttributes &&
			"/uaa-authentication/token".equals(servletRequestAttributes.getRequest().getRequestURI());
	}
}
