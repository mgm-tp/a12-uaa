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
package com.mgmtp.a12.uaa.authentication.jwt.integration.oauth;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.CollectionUtils;

import com.mgmtp.a12.uaa.authentication.jwt.integration.BaseTestConfig;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenGenerator;
import com.mgmtp.a12.uaa.authentication.oauth2.Oauth2ClaimsExtractor;
import com.mgmtp.a12.uaa.authentication.oauth2.Oauth2GrantedAuthorityConverter;
import com.mgmtp.a12.uaa.authentication.oauth2.internal.DelegatedAuthorityConverter;
import com.mgmtp.a12.uaa.authentication.oauth2.internal.Oauth2JwtAuthenticationToken;
import com.mgmtp.a12.uaa.authentication.oauth2.internal.Oauth2JwtAuthenticationTokenConverter;
import com.mgmtp.a12.uaa.authentication.principal.UAAPrincipal;
import com.mgmtp.a12.uaa.authentication.utils.TokenTester;
import com.mgmtp.a12.uaa.authentication.utils.UserDataCreator;
import com.mgmtp.a12.uaa.authentication.utils.UserDataCreator.TestExtendedData;

@ExtendWith(SpringExtension.class)
public class UaaJwtConverterTest {

	@Inject
	Oauth2JwtAuthenticationTokenConverter uaaJwtConverter;

	@Inject
	JwtTokenGenerator jwtTokenGenerator;

	@Test
	public void testUaaJwtConvertSupport() {
		Map<String, Object> claims = new HashMap<>();
		claims.put("scope", "role1 role2");

		Map<String, Object> header = new HashMap<>();
		header.put("key", "value");

		String token = jwtTokenGenerator.generateToken(UserDataCreator.createUser("admin", "admin")).getToken();
		Jwt jwt = new Jwt(token, Instant.parse("2020-07-17T18:35:24.00Z"), Instant.parse("2020-07-18T19:35:24.00Z"),
			header, claims);

		Oauth2JwtAuthenticationToken uaaJwtAuthenticationToken = uaaJwtConverter.convert(jwt);
		Assertions.assertTrue(CollectionUtils.containsAny(uaaJwtAuthenticationToken.getAuthorities(),
			Arrays.asList(new SimpleGrantedAuthority("SCOPE_role1"),
				new SimpleGrantedAuthority("SCOPE_role2"))));
		TokenTester.assertPrincipal(UserDataCreator.createUser("admin", "admin"),
			(UAAPrincipal<TestExtendedData>) uaaJwtAuthenticationToken.getPrincipal(), true, false);
	}

	@Configuration
	static class TestConfig extends BaseTestConfig {

		@Bean
		public Oauth2GrantedAuthorityConverter createGrantedAuthorityConverter() {
			return new DelegatedAuthorityConverter();
		}

		@Bean
		public Oauth2ClaimsExtractor createOauth2ClaimsExtractor() {
			return token -> UserDataCreator.createUser("admin", "admin");
		}

		@Bean
		public Oauth2JwtAuthenticationTokenConverter createUaaJwtConverter() {
			return new Oauth2JwtAuthenticationTokenConverter();
		}

	}
}
