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

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenData;
import com.mgmtp.a12.uaa.authentication.jwt.RenewTokenStorage;
import com.mgmtp.a12.uaa.authentication.jwt.encryption.internal.BypassingEncoder;
import com.mgmtp.a12.uaa.authentication.jwt.integration.BaseTestConfig;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenGenerator;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenVerifier;
import com.mgmtp.a12.uaa.authentication.jwt.internal.renew.RenewTokenService;
import com.mgmtp.a12.uaa.authentication.principal.UAAPrincipal;
import com.mgmtp.a12.uaa.authentication.utils.TokenTester;
import com.mgmtp.a12.uaa.authentication.utils.UserDataCreator;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RenewTokenServiceTest {

	@Inject
	protected RenewTokenService renewTokenService;

	@Inject
	protected JwtTokenGenerator jwtTokenGenerator;

	@Inject
	protected JwtTokenVerifier jwtTokenVerifier;

	@Inject
	protected RenewTokenStorage renewTokenStorage;

	String codeVerifier;
	String codeChallenge;
	JwtTokenData idTokenHint;

	@BeforeAll
	public void init() throws InterruptedException {
		codeVerifier = generateCodeVerifier();
		codeChallenge = generateCodeChallenge(codeVerifier);
		idTokenHint = generateToken();
		// we need to sleep in order to avoid serialization rounding by JWT lib
		Thread.sleep(1000);
	}

	@Test
	public void renewTokenTest() {

		Assertions.assertTrue(isRequestAuthorizeValid(codeChallenge, idTokenHint.getToken()));
		String code = authorize();
		Assertions.assertTrue(isRequestTokenValid(code, codeVerifier));
		JwtTokenData renewedTokenData = generateNewToken(code);
		Assertions.assertNotEquals(idTokenHint.getToken(), renewedTokenData.getToken());

		Instant initialLoginTime = idTokenHint.getIssuedTime();
		Instant initialLoginTimeFromToken = jwtTokenVerifier.unpackToken(idTokenHint.getToken()).getIssuedTime();
		Instant issuedTimeAfterRenew = renewedTokenData.getIssuedTime();
		Assertions.assertNotEquals(initialLoginTimeFromToken, issuedTimeAfterRenew);

		JwtTokenData freshTokenData = generateToken();
		Instant freshTokenLoginTime = freshTokenData.getIssuedTime();
		Assertions.assertNotEquals(initialLoginTime.toEpochMilli(), freshTokenLoginTime.toEpochMilli());
	}

	protected JwtTokenData generateToken() {
		UAAPrincipal<UserDataCreator.TestExtededData> user = UserDataCreator.createUser("test1", "password");
		return jwtTokenGenerator.generateToken(user);
	}

	protected String generateCodeVerifier() {
		SecureRandom sr = new SecureRandom();
		byte[] code = new byte[32];
		sr.nextBytes(code);
		String verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(code);
		return verifier;
	}

	protected String generateCodeChallenge(String codeVerifier) {
		return ReflectionTestUtils.invokeMethod(renewTokenService, "generateCodeChallenge", codeVerifier);
	}

	protected Boolean isRequestAuthorizeValid(String codeChallenge, String idTokenHint) {
		return renewTokenService.isRequestAuthorizeValid(codeChallenge, idTokenHint);
	}

	protected String authorize() {
		String code = renewTokenService.authorize(codeChallenge, idTokenHint.getToken());
		Assertions.assertTrue(renewTokenStorage.loadCodeChallenge(codeChallenge).isPresent());
		Assertions.assertTrue(renewTokenStorage.loadCode(code).isPresent());
		Assertions.assertTrue(renewTokenStorage.loadTokenHintByCode(code).isPresent());
		return code;
	}

	protected Boolean isRequestTokenValid(String code, String codeVerifier) {
		return renewTokenService.isRequestTokenValid(code, codeVerifier);
	}

	protected JwtTokenData generateNewToken(String code) {
		return renewTokenService.generateNewToken(code);
	}

	@Configuration
	static class TestConfig extends BaseTestConfig {
		@Bean
		@Primary
		public JwtTokenGenerator createJwtTokenGeneratorSupport() {
			return TokenTester.getJwtTokenGeneratorSupport(new BypassingEncoder(), 10, false);
		}

		@Override
		@Bean
		public AuthenticationProperties createAuthenticationProperties() {
			AuthenticationProperties properties = new AuthenticationProperties();
			properties.setJwt(new AuthenticationProperties.JwtProperties());
			properties.getJwt().setExpirationSeconds(10);
			return properties;
		}
	}
}