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
package com.mgmtp.a12.uaa.authentication.jwt.integration;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.userdetails.UserDetails;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.internal.UAASpringJsonHandler;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenStorage;
import com.mgmtp.a12.uaa.authentication.jwt.RenewTokenStorage;
import com.mgmtp.a12.uaa.authentication.jwt.encryption.internal.BypassingEncoder;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtAuthenticationProvider;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenCleaner;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenGenerator;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenPrincipalCreator;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenVerifier;
import com.mgmtp.a12.uaa.authentication.jwt.internal.SimpleJwtTokenStorage;
import com.mgmtp.a12.uaa.authentication.jwt.internal.renew.RenewTokenCleaner;
import com.mgmtp.a12.uaa.authentication.jwt.internal.renew.RenewTokenService;
import com.mgmtp.a12.uaa.authentication.jwt.internal.renew.SimpleRenewTokenStorage;
import com.mgmtp.a12.uaa.authentication.local.internal.LocalAuthenticationProvider;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalAdapter;
import com.mgmtp.a12.uaa.authentication.principal.internal.AuthenticationPrincipalAdapter;
import com.mgmtp.a12.uaa.authentication.principal.internal.serialization.UAAUserJacksonModule;
import com.mgmtp.a12.uaa.authentication.utils.TokenTester;

public class BaseTestConfig {

	@Bean
	public JwtTokenPrincipalCreator createJwtTokenPrincipalCreator() {
		return new JwtTokenPrincipalCreator();
	}

	@Bean
	public AuthenticationManager authenticationManager(JwtTokenPrincipalCreator jwtTokenPrincipalCreator, JwtTokenVerifier jwtTokenVerifier) {
		return new ProviderManager(new LocalAuthenticationProvider(), new JwtAuthenticationProvider(jwtTokenPrincipalCreator, jwtTokenVerifier));
	}

	@Bean
	public JwtTokenStorage createStorage() {
		return new SimpleJwtTokenStorage();
	}

	@Bean
	public JwtTokenCleaner createJwtTokenCleaner() {
		return new JwtTokenCleaner();
	}

	@Bean
	public UAASpringJsonHandler createJsonHandler() {
		return new UAASpringJsonHandler();
	}

	@Bean
	public UAAUserJacksonModule uaaUserJacksonModule() {
		return new UAAUserJacksonModule();
	}

	@Bean
	public PrincipalAdapter<? extends UserDetails> principalFactory() {
		return new AuthenticationPrincipalAdapter();
	}

	@Bean
	public JwtTokenGenerator createJwtTokenGeneratorSupport() {
		return TokenTester.getJwtTokenGeneratorSupport(new BypassingEncoder(), 100, true);
	}

	@Bean
	public JwtTokenVerifier createJwtTokenVerifierSupport() {
		return TokenTester.getJwtTokenVerifierSupport(new BypassingEncoder(), 100, true);
	}

	@Bean
	public RenewTokenCleaner createRenewTokenCleaner() {
		return new RenewTokenCleaner();
	}

	@Bean
	public RenewTokenStorage createRenewTokenStorage() {
		return new SimpleRenewTokenStorage();
	}

	@Bean
	public RenewTokenService createRenewTokenService() {
		return new RenewTokenService();
	}

	@Bean
	public AuthenticationProperties createAuthenticationProperties() {
		return new AuthenticationProperties();
	}

	@Bean
	public TestExtendedJacksonModule createTestExtendedJacksonModule() {
		return new TestExtendedJacksonModule();
	}

}