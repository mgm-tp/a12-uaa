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
package com.mgmtp.a12.uaa.authentication.jwt.integration.jwt;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.internal.UAASpringJsonHandler;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenStorage;
import com.mgmtp.a12.uaa.authentication.jwt.encryption.internal.BypassingEncoder;
import com.mgmtp.a12.uaa.authentication.jwt.encryption.internal.HuffmanEncoder;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenGenerator;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenVerifier;
import com.mgmtp.a12.uaa.authentication.jwt.internal.SimpleJwtTokenStorage;
import com.mgmtp.a12.uaa.authentication.principal.internal.serialization.UAAUserJacksonModule;
import com.mgmtp.a12.uaa.authentication.utils.TokenTester;

@ExtendWith(SpringExtension.class)
public class JwtTokenSupportTest {

	@Inject
	private JwtTokenStorage jwtTokenStorage;

	@Inject
	@Qualifier(value = "huffmanEncoderJwtTokenGeneratorSupport100false")
	private JwtTokenGenerator huffmanEncoderJwtTokenGenerator100False;

	@Inject
	@Qualifier(value = "huffmanEncoderJwtTokenGeneratorSupport100true")
	private JwtTokenGenerator huffmanEncoderJwtTokenGenerator100True;
	@Inject
	@Qualifier(value = "bypassingEncoderJwtTokenGeneratorSupport1false")
	private JwtTokenGenerator bypassingEncoderJwtTokenGenerator1False;

	@Inject
	@Qualifier(value = "bypassingEncoderJwtTokenGeneratorSupport100true")
	private JwtTokenGenerator bypassingEncoderJwtTokenGenerator100True;

	@Inject
	@Qualifier(value = "bypassingEncoderJwtTokenGeneratorSupport100false")
	private JwtTokenGenerator bypassingEncoderJwtTokenGenerator100False;

	@Inject
	@Qualifier(value = "bypassingEncoderJwtTokenGeneratorSupport25false")
	private JwtTokenGenerator bypassingEncoderJwtTokenGenerator25False;

	@Inject
	@Qualifier(value = "huffmanEncoderJwtTokenVerifierSupport500false")
	private JwtTokenVerifier huffmanEncoderJwtTokenVerifier500False;

	@Inject
	@Qualifier(value = "huffmanEncoderJwtTokenVerifierSupport500true")
	private JwtTokenVerifier huffmanEncoderJwtTokenVerifier500True;

	@Inject
	@Qualifier(value = "bypassingEncoderJwtTokenVerifierSupport500true")
	private JwtTokenVerifier bypassingEncoderJwtTokenVerifier500True;

	@Inject
	@Qualifier(value = "bypassingEncoderJwtTokenVerifierSupport500false")
	private JwtTokenVerifier bypassingEncoderJwtTokenVerifier500False;

	@Inject
	@Qualifier(value = "bypassingEncoderJwtTokenVerifierSupport45false")
	private JwtTokenVerifier bypassingEncoderJwtTokenVerifier45False;

	@Test
	public void huffmanEncoderWithNoUserStoringDeserialization() {
		TokenTester.checkTokenData(huffmanEncoderJwtTokenGenerator100False, huffmanEncoderJwtTokenVerifier500False, false);
	}

	@Test
	public void huffmanEncoderWithUserStoringDeserialization() throws InterruptedException {
		TokenTester.checkTokenData(huffmanEncoderJwtTokenGenerator100True, huffmanEncoderJwtTokenVerifier500True, true);
		TokenTester.checkCreationTimestampForNewToken(huffmanEncoderJwtTokenGenerator100True);
		// Special character at beginning
		boolean isTokenValid = huffmanEncoderJwtTokenVerifier500True.isTokenValid(
			"eyJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0.." +
				"@3rPMd5krCHUQ_R7I.$Y98f0jwQBHV8B-yKS9WNrF8cg15DNs4cQVvaxJXXklbFcNzC2JTwdpvl_dsvvvTcYw.=5oGmLN2LisgV3NeHoj_hng");
		Assertions.assertFalse(isTokenValid);
		// Special character at middle
		isTokenValid = huffmanEncoderJwtTokenVerifier500True.isTokenValid(
			"eyJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0.." +
				"3r$.PMd5krCHUQ_R7I.Y9@8f0jwQBHV8B-yKS9WNrF8cg15DNs4cQVvaxJXXklbFcNzC2JTwdpvl_dsvvvTcYw.5=oGmLN2LisgV3NeHoj_hng");
		Assertions.assertFalse(isTokenValid);
		// Special character at last
		isTokenValid = huffmanEncoderJwtTokenVerifier500True.isTokenValid(
			"eyJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0.." +
				"3rPMd5krCHUQ_R7I.Y98f0jwQBHV8B-yKS9WNrF8cg15DNs4cQVvaxJXXklbFcNzC2JTwdpvl_dsvvvTcYw.5oGmLN2LisgV3NeHoj_hng=");
		Assertions.assertFalse(isTokenValid);
		// null is token
		isTokenValid = huffmanEncoderJwtTokenVerifier500True.isTokenValid(null);
		Assertions.assertFalse(isTokenValid);
	}

	@Test
	public void bypassingEncoderWithNoUserStoringDeserialization() {
		TokenTester.checkTokenData(bypassingEncoderJwtTokenGenerator100False, bypassingEncoderJwtTokenVerifier500False, false);
	}

	@Test
	public void bypassingEncoderWithUserStoringDeserialization() {
		TokenTester.checkTokenData(bypassingEncoderJwtTokenGenerator100True, bypassingEncoderJwtTokenVerifier500True, true);
	}

	@Test
	public void bypassingEncoderWithNoUserStoringWithDeserializationException() {
		//should ignore silently
		TokenTester.checkTokenData(bypassingEncoderJwtTokenGenerator100False, bypassingEncoderJwtTokenVerifier500False, false);
	}

	@Test
	public void bypassingEncoderNoUserStoringWithTokenExpiration() throws InterruptedException {
		TokenTester.checkTokenValid(bypassingEncoderJwtTokenGenerator1False, bypassingEncoderJwtTokenVerifier500False,
			jwtTokenStorage, 5, false, false);
	}

	@Test
	public void bypassingEncoderNoUserStoringWithTokenLifeTimeExpiration() throws InterruptedException {
		TokenTester.checkTokenValid(bypassingEncoderJwtTokenGenerator25False, bypassingEncoderJwtTokenVerifier45False,
			jwtTokenStorage, 50, false, false);
	}

	@Test
	public void tokenIsValid() throws InterruptedException {
		TokenTester.checkTokenValid(bypassingEncoderJwtTokenGenerator100False, bypassingEncoderJwtTokenVerifier500False,
			jwtTokenStorage, 2, false, true);
	}

	@Test
	public void tokenIsNotValidSinceBlackList() throws InterruptedException {
		TokenTester.checkTokenValid(bypassingEncoderJwtTokenGenerator100False, bypassingEncoderJwtTokenVerifier500False,
			jwtTokenStorage, 2, true, false);
	}

	@Configuration
	static class TestConfig {

		@Bean("huffmanEncoderJwtTokenGeneratorSupport100false")
		public JwtTokenGenerator createHuffmanEncoderJwtTokenGeneratorSupport100false() {
			return TokenTester.getJwtTokenGeneratorSupport(new HuffmanEncoder(), 100, false);
		}

		@Bean("huffmanEncoderJwtTokenVerifierSupport500false")
		public JwtTokenVerifier createHuffmanEncoderJwtTokenVerifierSupport500false() {
			return TokenTester.getJwtTokenVerifierSupport(new HuffmanEncoder(), 500, false);
		}

		@Bean("huffmanEncoderJwtTokenGeneratorSupport100true")
		public JwtTokenGenerator createHuffmanEncoderJwtTokenGeneratorSupport100true() {
			return TokenTester.getJwtTokenGeneratorSupport(new HuffmanEncoder(), 100, true);
		}

		@Bean("huffmanEncoderJwtTokenVerifierSupport500true")
		public JwtTokenVerifier createHuffmanEncoderJwtTokenVerifierSupport500true() {
			return TokenTester.getJwtTokenVerifierSupport(new HuffmanEncoder(), 500, true);
		}

		@Bean("bypassingEncoderJwtTokenGeneratorSupport1false")
		public JwtTokenGenerator createBypassingEncoderJwtTokenGeneratorSupport1false() {
			return TokenTester.getJwtTokenGeneratorSupport(new BypassingEncoder(), 1, false);
		}

		@Bean("bypassingEncoderJwtTokenGeneratorSupport100true")
		public JwtTokenGenerator createBypassingEncoderJwtTokenGeneratorSupport100true() {
			return TokenTester.getJwtTokenGeneratorSupport(new BypassingEncoder(), 100, true);
		}

		@Bean("bypassingEncoderJwtTokenVerifierSupport500true")
		public JwtTokenVerifier createNBypassingEncoderJwtTokenVerifierSupport500true() {
			return TokenTester.getJwtTokenVerifierSupport(new BypassingEncoder(), 500, true);
		}

		@Bean("bypassingEncoderJwtTokenGeneratorSupport100false")
		public JwtTokenGenerator createBypassingEncoderJwtTokenGeneratorSupport100false() {
			return TokenTester.getJwtTokenGeneratorSupport(new BypassingEncoder(), 100, false);
		}

		@Bean("bypassingEncoderJwtTokenVerifierSupport500false")
		public JwtTokenVerifier createBypassingEncoderJwtTokenVerifierSupport500false() {
			return TokenTester.getJwtTokenVerifierSupport(new BypassingEncoder(), 500, false);
		}

		@Bean("bypassingEncoderJwtTokenGeneratorSupport25false")
		public JwtTokenGenerator createBypassingEncoderJwtTokenGeneratorSupport25false() {
			return TokenTester.getJwtTokenGeneratorSupport(new BypassingEncoder(), 25, false);
		}

		@Bean("bypassingEncoderJwtTokenVerifierSupport45false")
		public JwtTokenVerifier createBypassingEncoderJwtTokenVerifierSupport45false() {
			return TokenTester.getJwtTokenVerifierSupport(new BypassingEncoder(), 45, false);
		}

		@Bean
		public JwtTokenStorage createStorage() {
			return new SimpleJwtTokenStorage();
		}

		@Bean
		public UAAUserJacksonModule uaaUserJacksonModule() {
			return new UAAUserJacksonModule();
		}

		@Bean
		public UAASpringJsonHandler createJsonHandler() {
			return new UAASpringJsonHandler();
		}

		@Bean
		public AuthenticationProperties createAuthenticationProperties() {
			return new AuthenticationProperties();
		}

	}
}
