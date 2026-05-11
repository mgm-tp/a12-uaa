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
package com.mgmtp.a12.uaa.authentication.jwt.integration.jwt.renew;

import java.time.Instant;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mgmtp.a12.uaa.authentication.jwt.RenewTokenStorage;
import com.mgmtp.a12.uaa.authentication.jwt.encryption.internal.BypassingEncoder;
import com.mgmtp.a12.uaa.authentication.jwt.integration.BaseTestConfig;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenGenerator;
import com.mgmtp.a12.uaa.authentication.jwt.internal.renew.RenewTokenCleaner;
import com.mgmtp.a12.uaa.authentication.principal.UAAPrincipal;
import com.mgmtp.a12.uaa.authentication.utils.TokenTester;
import com.mgmtp.a12.uaa.authentication.utils.UserDataCreator;

@ExtendWith(SpringExtension.class)
public class RenewTokenCleanerForExpiredTest {

	@Inject
	private RenewTokenCleaner renewTokenCleaner;

	@Inject
	private RenewTokenStorage renewTokenStorage;

	@Inject
	private JwtTokenGenerator jwtTokenGenerator;

	@Test
	public void cleanRenewTokenStorageTest() throws InterruptedException {
		renewTokenStorage.storeCodeChallenge("code_challenge", calculateExpirationTime(2));
		Assertions.assertTrue(renewTokenStorage.loadCodeChallenge("code_challenge").isPresent());

		renewTokenStorage.storeCode("code", calculateExpirationTime(2));
		Assertions.assertTrue(renewTokenStorage.loadCode("code").isPresent());

		UAAPrincipal<UserDataCreator.TestExtededData> user = UserDataCreator.createUser("test1", "password");
		String tokenHint = jwtTokenGenerator.generateToken(user).getToken();
		renewTokenStorage.storeTokenHint("code", tokenHint);
		Assertions.assertTrue(renewTokenStorage.loadTokenHintByCode("code").isPresent());

		Thread.sleep(3 * 1000);
		renewTokenCleaner.cleanRenewTokenStorage();
		Assertions.assertTrue(renewTokenStorage.loadCodeChallenge("code_challenge").isEmpty());
		Assertions.assertTrue(renewTokenStorage.loadCode("code").isEmpty());
		Assertions.assertTrue(renewTokenStorage.loadTokenHintByCode("code").isEmpty());
	}

	private String calculateExpirationTime(Integer renewalSeconds) {
		Instant expiration = Instant.now().plusSeconds(renewalSeconds);
		return String.valueOf(expiration.toEpochMilli());
	}

	@Configuration
	static class TestConfig extends BaseTestConfig {
		@Bean
		@Primary
		public JwtTokenGenerator createJwtTokenGeneratorSupport() {
			return TokenTester.getJwtTokenGeneratorSupport(new BypassingEncoder(), 1, true);
		}
	}
}