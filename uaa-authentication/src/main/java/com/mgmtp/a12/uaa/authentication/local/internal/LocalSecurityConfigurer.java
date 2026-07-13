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
package com.mgmtp.a12.uaa.authentication.local.internal;

import java.util.Optional;

import jakarta.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;
import com.mgmtp.a12.uaa.authentication.internal.ClassNameUtils;
import com.mgmtp.a12.uaa.authentication.internal.ManualRedirectStrategy;
import com.mgmtp.a12.uaa.authentication.internal.RedirectSupport;
import com.mgmtp.a12.uaa.authentication.internal.RedirectType;
import com.mgmtp.a12.uaa.authentication.internal.StandardJsonHandler;
import com.mgmtp.a12.uaa.authentication.local.LocalAuthenticationService;
import com.mgmtp.a12.uaa.authentication.security.UAASecurityConfigurer;
import com.mgmtp.a12.uaa.authentication.security.login.internal.UAAAuthenticationFailureHandler;
import com.mgmtp.a12.uaa.authentication.security.login.internal.UAAAuthenticationFilter;
import com.mgmtp.a12.uaa.authentication.security.login.internal.UAAAuthenticationSuccessHandler;

@Configuration
@ConditionalOnAuthentication(AuthenticationType.LOCAL)
public class LocalSecurityConfigurer extends UAASecurityConfigurer<LocalSecurityConfigurer> {

	@Inject
	private AuthenticationProperties authenticationProperties;
	@Inject
	private StandardJsonHandler standardJsonHandler;
	@Inject
	private Optional<LocalAuthenticationService<?>> localAuthenticationService;
	@Inject
	@RedirectType(type = RedirectType.Type.LOGIN)
	private RedirectSupport loginRedirectSupport;

	@Override
	public void configure(HttpSecurity http) {
		if (authenticationProperties.getJwt().getTokenEndpoints().isEnabled()) {
			http
				.addFilterBefore(createLocalAuthenticationFilter(getAuthenticationManager(http)), UsernamePasswordAuthenticationFilter.class);
		}
		LOGGER.info("LOCAL: Using UAALocalAuthenticationService: [{}]", ClassNameUtils.resolveShortClassName(localAuthenticationService));

	}

	@Override
	public void configureHttpSecurity(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
		authenticationManagerBuilder.eraseCredentials(true);

	}

	@Override
	protected Optional<AuthenticationProvider> createAuthenticationProvider() {
		return Optional.of(new LocalAuthenticationProvider());
	}

	@Bean
	public UAAAuthenticationSuccessHandler createUaaAuthenticationSuccessHandlerLocal() {
		return new UAAAuthenticationSuccessHandler(new ManualRedirectStrategy());
	}

	private UAAAuthenticationFilter createLocalAuthenticationFilter(AuthenticationManager authenticationManager) {
		UAAAuthenticationFilter filter =
			new UAAAuthenticationFilter(authenticationProperties.getContextPath(), loginRedirectSupport, authenticationManager, standardJsonHandler,
				AuthenticationType.LOCAL);
		filter.setAuthenticationSuccessHandler(createUaaAuthenticationSuccessHandlerLocal());
		filter.setAuthenticationFailureHandler(createUAAAuthenticationFailureHandler());
		return filter;
	}

	private AuthenticationFailureHandler createUAAAuthenticationFailureHandler() {
		return new UAAAuthenticationFailureHandler();
	}

}
