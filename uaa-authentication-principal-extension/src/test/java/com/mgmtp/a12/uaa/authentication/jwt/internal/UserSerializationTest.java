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
package com.mgmtp.a12.uaa.authentication.jwt.internal;

import java.util.Arrays;
import java.util.Collection;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.config.common.EnabledProperty;
import com.mgmtp.a12.uaa.authentication.internal.UAASpringJsonHandler;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenData;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenStorage;
import com.mgmtp.a12.uaa.authentication.jwt.encryption.internal.BypassingEncoder;
import com.mgmtp.a12.uaa.authentication.principal.ExtendedPrincipal;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalFactory;
import com.mgmtp.a12.uaa.authentication.principal.internal.UAAPrincipalFactory;

@ExtendWith(SpringExtension.class)
public class UserSerializationTest {

	@Inject
	private JwtTokenGenerator jwtTokenGenerator;

	@Inject
	private JwtTokenVerifier jwtTokenVerifier;

	@Test
	public void checkUserSerialization() {
		Collection<GrantedAuthority> grantedAuthorities = Arrays.asList(new SimpleGrantedAuthority("rest_role"));
		String extendedData = "extendedData";
		ExtendedPrincipal principal = new ExtendedPrincipal("test", "pwd", grantedAuthorities, extendedData);
		principal.setEmail("email");
		principal.setFirstname("firstname");
		principal.setLastname("lastname");
		principal.addAdditionalProperty("test", "value");
		principal.setRelayingPartyRegistration("regitration");
		JwtTokenData tokenData = jwtTokenGenerator.generateToken(principal);

		ExtendedPrincipal deserializedPrincipal = (ExtendedPrincipal) jwtTokenVerifier.unpackToken(tokenData.getToken()).getPrincipal();

		Assertions.assertEquals(principal.getUsername(), deserializedPrincipal.getUsername());
		Assertions.assertEquals(principal.getPassword(), deserializedPrincipal.getPassword());
		Assertions.assertEquals(principal.getAuthorities().iterator().next().getAuthority(),
			deserializedPrincipal.getAuthorities().iterator().next().getAuthority());
		Assertions.assertEquals(principal.getEmail(), deserializedPrincipal.getEmail());
		Assertions.assertEquals(principal.getFirstname(), deserializedPrincipal.getFirstname());
		Assertions.assertEquals(principal.getLastname(), deserializedPrincipal.getLastname());
		Assertions.assertEquals(principal.getName(), deserializedPrincipal.getName());
		Assertions.assertEquals(principal.getExtendedPrincipalData(), deserializedPrincipal.getExtendedPrincipalData());
		Assertions.assertEquals(principal.getRelyingPartyRegistrationId(), deserializedPrincipal.getRelyingPartyRegistrationId());
		Assertions.assertEquals(principal.getAdditionalProperties().get("test"), deserializedPrincipal.getAdditionalProperties().get("test"));
	}

	@Configuration
	@ComponentScan({ "com.mgmtp.a12.uaa.authentication.principal.internal.serialization", "com.mgmtp.a12.uaa.authentication.principal.internal.jackson" })
	static class TestConfig {

		@Bean
		public JwtTokenGenerator createJwtTokenGeneratorSupport() {
			return new JwtTokenGenerator.Builder()
				.withDataEncoder(new BypassingEncoder())
				.withSecretKey("bXlTZWNyZXRLZXkxMjM0NW15U2VjcmV0S2V5MTIzNDU=")
				.withExpirationSeconds(100)
				.withStoreUser(true).build();
		}

		@Bean
		public JwtTokenVerifier createJwtTokenVerifierSupport() {
			return new JwtTokenVerifier.Builder()
				.withDataEncoder(new BypassingEncoder())
				.withSecretKey("bXlTZWNyZXRLZXkxMjM0NW15U2VjcmV0S2V5MTIzNDU=")
				.withUserLifetimeSeconds(500)
				.withStoreUser(true)
				.build();
		}

		@Bean
		public JwtTokenStorage createStorage() {
			return new SimpleJwtTokenStorage();
		}

		@Bean
		public UAASpringJsonHandler createJsonHandler() {
			return new UAASpringJsonHandler();
		}

		@Bean
		public PrincipalFactory userFactory() {
			return new UAAPrincipalFactory();
		}

		@Bean
		public AuthenticationProperties createAuthenticationProperties() {
			AuthenticationProperties.JwtProperties jwtProperties = new AuthenticationProperties.JwtProperties();
			jwtProperties.setTokenSignature(new EnabledProperty(false));
			AuthenticationProperties authenticationProperties = new AuthenticationProperties();
			authenticationProperties.setJwt(jwtProperties);
			return authenticationProperties;
		}
	}
}
