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
package com.mgmtp.a12.uaa.authentication.benchmark.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.config.common.EnabledProperty;
import com.mgmtp.a12.uaa.authentication.internal.UAASpringJsonHandler;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenStorage;
import com.mgmtp.a12.uaa.authentication.jwt.RenewTokenStorage;
import com.mgmtp.a12.uaa.authentication.jwt.encryption.internal.BypassingEncoder;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenGenerator;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenPrincipalCreator;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenVerifier;
import com.mgmtp.a12.uaa.authentication.jwt.internal.SimpleJwtTokenStorage;
import com.mgmtp.a12.uaa.authentication.jwt.internal.renew.SimpleRenewTokenStorage;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalAdapter;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalCreator;
import com.mgmtp.a12.uaa.authentication.principal.internal.AuthenticationPrincipalAdapter;
import com.mgmtp.a12.uaa.authentication.principal.internal.serialization.UAAUserJacksonModule;
import com.mgmtp.a12.uaa.authentication.utils.TokenTester;

@Configuration
public class CommonConfiguration {
	@Bean
	public AuthenticationProperties authenticationProperties() {
		AuthenticationProperties.JwtProperties jwtProperties = new AuthenticationProperties.JwtProperties();
		jwtProperties.setTokenSignature(new EnabledProperty(true));
		AuthenticationProperties authenticationProperties = new AuthenticationProperties();
		authenticationProperties.setJwt(jwtProperties);
		return authenticationProperties;
	}

	@Bean("bypassingEncodeJwtTokenGeneratorSupport")
	@Primary
	public JwtTokenGenerator createBypassingEncodeJwtTokenGeneratorSupport() {
		return TokenTester.getJwtTokenGeneratorSupport(new BypassingEncoder(), 315360000, true);
	}

	@Bean("bypassingEncodeJwtTokenVerifierSupport")
	@Primary
	public JwtTokenVerifier createBypassingEncodeJwtTokenVerifierSupport() {
		return TokenTester.getJwtTokenVerifierSupport(new BypassingEncoder(), 315360000, true);
	}

	@Bean
	public UAAUserJacksonModule uaaUserJacksonModule() {
		return new UAAUserJacksonModule();
	}

	@Bean
	public UAASpringJsonHandler uaaSpringJsonHandler() {
		return new UAASpringJsonHandler();
	}

	@Bean
	public JwtTokenStorage createStorage() {
		return new SimpleJwtTokenStorage();
	}

	@Bean
	public RenewTokenStorage renewTokenStorage() {
		return new SimpleRenewTokenStorage();
	}

	@Bean
	public PrincipalCreator<? extends UserDetails> createJwtTokenPrincipalCreator() {
		return new JwtTokenPrincipalCreator();
	}

	@Bean
	public PrincipalAdapter<? extends UserDetails> authenticationPrincipalAdapter() {
		return new AuthenticationPrincipalAdapter();
	}
}
