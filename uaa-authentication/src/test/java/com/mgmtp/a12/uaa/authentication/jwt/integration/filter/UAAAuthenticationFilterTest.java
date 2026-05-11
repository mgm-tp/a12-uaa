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
import java.util.Arrays;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.internal.RedirectSupport;
import com.mgmtp.a12.uaa.authentication.internal.StandardJsonHandler;
import com.mgmtp.a12.uaa.authentication.local.LocalAuthenticationService;
import com.mgmtp.a12.uaa.authentication.local.internal.LocalAuthenticationProvider;
import com.mgmtp.a12.uaa.authentication.principal.UAAPrincipal;
import com.mgmtp.a12.uaa.authentication.principal.internal.serialization.UAAUserJacksonModule;
import com.mgmtp.a12.uaa.authentication.security.login.internal.UAAAuthenticationFilter;
import com.mgmtp.a12.uaa.authentication.utils.UserDataCreator;

@ExtendWith(SpringExtension.class)
public class UAAAuthenticationFilterTest {

	@Inject
	private UAAAuthenticationFilter uAAUserAuthenticationFilter;

	@Test
	public void testAttemptAuthenticationSupport() throws IOException, ServletException {

		String inputLogin = "{\"username\":\"test\", \"password\": \"password\"}";
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setContent(inputLogin.getBytes());
		request.setContentType("JSON");
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setMethod("POST");
		Authentication authentication = uAAUserAuthenticationFilter.attemptAuthentication(request, response);
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Assertions.assertEquals(userDetails.getUsername(), "test");
		Assertions.assertNull(userDetails.getPassword());
	}

	@Configuration
	@EnableWebSecurity
	@ComponentScan({ "com.mgmtp.a12.uaa.authentication.security.login", "com.mgmtp.a12.uaa.authentication.principal" })
	public static class TestConfig extends AbstractHttpConfigurer<TestConfig, HttpSecurity> {

		@Bean
		public StandardJsonHandler createJsonHandler() {
			return new StandardJsonHandler();
		}

		@Bean
		public UAAUserJacksonModule uaaUserJacksonModule() {
			return new UAAUserJacksonModule();
		}

		@Bean
		public UAAAuthenticationFilter createUAAUserAuthenticationFilter(HttpSecurity httpSecurity) throws Exception {
			UAAAuthenticationFilter uAAUserAuthenticationFilter =
				new UAAAuthenticationFilter("/", getLoginRedirectSupport(), httpSecurity.getSharedObject(AuthenticationManagerBuilder.class).build(),
					createJsonHandler(), AuthenticationType.LOCAL);
			return uAAUserAuthenticationFilter;
		}

		@Bean
		public LocalAuthenticationService createUAALocalAuthenticationService() {
			return (userName, password) -> new UAAPrincipal("test", "password",
				Arrays.asList(new SimpleGrantedAuthority("role1"), new SimpleGrantedAuthority("role2")),
				UserDataCreator.createTestSubData());
		}

		@Bean
		public LocalAuthenticationProvider createUAAAuthenticationProvider() {
			return new LocalAuthenticationProvider();
		}

		public RedirectSupport getLoginRedirectSupport() {
			AuthenticationProperties.Redirect redirect = new AuthenticationProperties.Redirect();
			redirect.getSuccess().setUrl("http://localhost:8080/");
			redirect.setUrlPattern("http://localhost:8080/");
			return new RedirectSupport(redirect, false, false, 180);
		}
	}
}
