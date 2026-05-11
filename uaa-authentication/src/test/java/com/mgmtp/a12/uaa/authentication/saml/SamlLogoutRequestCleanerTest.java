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
package com.mgmtp.a12.uaa.authentication.saml;

import java.util.Collection;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.internal.AuthenticationTokenLocator;
import com.mgmtp.a12.uaa.authentication.internal.HeaderAuthenticationTokenLocator;
import com.mgmtp.a12.uaa.authentication.internal.TokenType;
import com.mgmtp.a12.uaa.authentication.jwt.encryption.internal.BypassingEncoder;
import com.mgmtp.a12.uaa.authentication.jwt.integration.BaseTestConfig;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenGenerator;
import com.mgmtp.a12.uaa.authentication.saml.internal.SamlLogoutRequestCleaner;
import com.mgmtp.a12.uaa.authentication.saml.internal.SimpleSamlLogoutRequestRepository;
import com.mgmtp.a12.uaa.authentication.utils.TokenTester;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SamlLogoutRequestCleanerTest {

	@Inject
	private UaaSaml2LogoutRequestRepository saml2LogoutRequestRepository;
	@Inject
	private SamlLogoutRequestCleaner samlLogoutRequestCleaner;
	@Inject
	private JwtTokenGenerator jwtTokenGenerator;

	private SamlLogoutSupportTest samlLogoutSupport;

	@BeforeEach
	void setUp() {
		samlLogoutSupport = new SamlLogoutSupportTest(saml2LogoutRequestRepository, jwtTokenGenerator);
	}

	@AfterAll
	public void cleanUp() throws IllegalArgumentException, IllegalAccessException {
		//we have to clear because static field is shared between tests
		saml2LogoutRequestRepository.loadAll().stream()
			.forEach(saml2LogoutRequestRepository::delete);
	}

	@Test
	public void cleanerTest() throws Exception {
		String relayState = "relayState_XX_007";
		samlLogoutSupport.saveLogoutRequest(relayState);

		Collection<LogoutRequestData> allData = saml2LogoutRequestRepository.loadAll();
		Assertions.assertEquals(1, allData.size());

		samlLogoutRequestCleaner.cleanExpiredAuthorizationCode();
		allData = saml2LogoutRequestRepository.loadAll();

		Assertions.assertEquals(0, allData.size());

	}

	@Configuration
	static class TestConfig extends BaseTestConfig {

		@Bean
		@Primary
		public JwtTokenGenerator createJwtTokenGeneratorSupport() {
			return TokenTester.getJwtTokenGeneratorSupport(new BypassingEncoder(), 0, false);
		}

		@Bean
		public SimpleSamlLogoutRequestRepository createSimpleSamlLogoutRequestRepository() {
			return new SimpleSamlLogoutRequestRepository(createauAuthenticationTokenLocator(), createJwtTokenVerifierSupport());
		}

		@Bean
		public AuthenticationTokenLocator createauAuthenticationTokenLocator() {
			return new HeaderAuthenticationTokenLocator("Authorization", TokenType.UAABEARER);
		}

		@Bean
		public SamlLogoutRequestCleaner createSamlLogoutRequestCleaner() {
			return new SamlLogoutRequestCleaner();
		}

		@Override
		@Bean
		public AuthenticationProperties createAuthenticationProperties() {
			AuthenticationProperties properties = new AuthenticationProperties();
			properties.setJwt(new AuthenticationProperties.JwtProperties());
			properties.getJwt().setExpirationSeconds(0);
			return properties;
		}

	}

}
