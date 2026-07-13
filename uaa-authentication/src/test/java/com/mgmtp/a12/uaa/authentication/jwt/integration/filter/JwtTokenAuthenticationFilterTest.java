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
package com.mgmtp.a12.uaa.authentication.jwt.integration.filter;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mgmtp.a12.uaa.authentication.internal.AuthenticationTokenLocator;
import com.mgmtp.a12.uaa.authentication.internal.HeaderAuthenticationTokenLocator;
import com.mgmtp.a12.uaa.authentication.internal.TokenType;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenData;
import com.mgmtp.a12.uaa.authentication.jwt.integration.BaseTestConfig;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenAuthenticationFilter;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenGenerator;
import com.mgmtp.a12.uaa.authentication.principal.UAAPrincipal;
import com.mgmtp.a12.uaa.authentication.security.login.internal.UAALoginEntryPoint;
import com.mgmtp.a12.uaa.authentication.utils.TokenTester;
import com.mgmtp.a12.uaa.authentication.utils.UserDataCreator;
import com.mgmtp.a12.uaa.authentication.utils.UserDataCreator.TestExtendedData;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JwtTokenAuthenticationFilterTest {

	@Inject
	private JwtTokenGenerator jwtTokenGenerator;

	@Inject
	private JwtTokenAuthenticationFilter headerJwtAuthenticationFilter;

	@BeforeAll
	public void setUp() {
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	@Test
	public void testHeaderExistSupport() throws ServletException, IOException {
		UAAPrincipal<TestExtendedData> user = UserDataCreator.createUser("test", "N/A");
		JwtTokenData tokenData = jwtTokenGenerator.generateToken(user);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", TokenType.UAABEARER + " " + tokenData.getToken());
		MockHttpServletResponse response = new MockHttpServletResponse();
		PrincipalExistingCheckFilter principalExistingCheckFilter = new PrincipalExistingCheckFilter(user);
		headerJwtAuthenticationFilter.doFilter(request, response, principalExistingCheckFilter);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Assertions.assertNotNull(authentication);
	}

	static class PrincipalExistingCheckFilter implements FilterChain {

		private UAAPrincipal<TestExtendedData> inputUser;

		public PrincipalExistingCheckFilter(UAAPrincipal<TestExtendedData> inputUser) {
			super();
			this.inputUser = inputUser;
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response) {
			@SuppressWarnings("unchecked")
			UAAPrincipal<TestExtendedData> userDetails = (UAAPrincipal<TestExtendedData>) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			//password is erased by spring - ProviderManager
			TokenTester.assertPrincipal(inputUser, userDetails, false, false);
		}
	}

	@Configuration
	static class TestConfig extends BaseTestConfig {
		@Bean
		public AuthenticationTokenLocator createHeaderJwtTokenLocator() {
			return new HeaderAuthenticationTokenLocator("Authorization", TokenType.UAABEARER);
		}

		@Bean
		public JwtTokenAuthenticationFilter createHeaderJwtAuthenticationFilter() {
			return new JwtTokenAuthenticationFilter(createHeaderJwtTokenLocator(),
				authenticationManager(createJwtTokenPrincipalCreator(), createJwtTokenVerifierSupport()), new UAALoginEntryPoint(401));
		}
	}

}
