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
package com.mgmtp.a12.uaa.authentication.jwt.integration.local;

import java.util.Arrays;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.local.LocalAuthenticationService;
import com.mgmtp.a12.uaa.authentication.local.internal.LocalAuthenticationProvider;
import com.mgmtp.a12.uaa.authentication.principal.UAAPrincipal;
import com.mgmtp.a12.uaa.authentication.security.login.internal.TypedUsernamePasswordAuthenticationToken;
import com.mgmtp.a12.uaa.authentication.utils.UserDataCreator;

@ExtendWith(SpringExtension.class)
public class UAAAuthenticationProviderTest {

	@Inject
	LocalAuthenticationProvider uAAAuthenticationProvider;

	@Test
	public void testBadCredentialsExceptionSupport() {
		UAAPrincipal<UserDataCreator.TestExtededData> userDetails = UserDataCreator.createUser("admin", "admin");
		TypedUsernamePasswordAuthenticationToken auth = new TypedUsernamePasswordAuthenticationToken(
			userDetails, null, AuthenticationType.LOCAL, userDetails.getAuthorities());
		Assertions.assertThrows(BadCredentialsException.class,
			() -> uAAAuthenticationProvider.authenticate(auth));
	}

	@Configuration
	static class TestConfig {
		@Bean
		public LocalAuthenticationProvider createUAAAuthenticationProvider() {
			return new LocalAuthenticationProvider();
		}

		@Bean
		public LocalAuthenticationService createUAALocalAuthenticationService() {
			return (userName, password) -> new UAAPrincipal("test", "password",
				Arrays.asList(new SimpleGrantedAuthority("role1"), new SimpleGrantedAuthority("role2")),
				1 / 0);
		}
	}
}
