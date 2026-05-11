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
package com.mgmtp.a12.uaa.authentication.security;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import com.mgmtp.a12.uaa.authentication.AuthenticationType;

/**
 * Base class for UAA configuration DSL. Each {@link AuthenticationType} has one configurer.
 * The configurer is divided into two main sections.
 * Standard  which contains configuration needed after the httpSecurity is built.
 * <p>
 * UAA specific {@link UAASecurityConfigurer#configureHttpSecurity(HttpSecurity)} which contains configuration
 * which is required before the {@link HttpSecurity} is built.
 */
public abstract class UAASecurityConfigurer<T extends UAASecurityConfigurer<T>> extends AbstractHttpConfigurer<T, HttpSecurity> {

	protected static final Logger LOGGER = LoggerFactory.getLogger(UAASecurityConfigurer.class.getCanonicalName() + ".UAA_Config");

	private AuthenticationProvider authenticationProvider;

	@Override
	public void init(HttpSecurity builder) {
		Optional.ofNullable(authenticationProvider).or(() -> createAuthenticationProvider())
			.ifPresent(provider -> {
				postProcess(provider);
				builder.authenticationProvider(provider);
				controlSecurityHeader(builder);
				authenticationProvider = provider;
			});
	}

	/**
	 * Implementor should implement this method if an authentication type has it's own {@link AuthenticationProvider}
	 */
	protected Optional<AuthenticationProvider> createAuthenticationProvider() {
		return Optional.empty();
	}

	protected AuthenticationProvider getAuthenticationProvider() {
		return authenticationProvider;
	}

	@SuppressWarnings("unused")
	public void configureHttpSecurity(HttpSecurity http) throws Exception {
	}

	protected AuthenticationManager getAuthenticationManager(HttpSecurity http) {
		return http.getSharedObject(AuthenticationManager.class);
	}

	/**
	 * This bean will avoid the default inMemoryUserDetail bean is created by
	 * org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
	 *
	 * @param http instance
	 * @return AuthenticationManagerResolver bean
	 */
	@Bean
	protected AuthenticationManagerResolver<?> getAuthenticationManagerResolver(HttpSecurity http) {
		return http.getSharedObject(AuthenticationManagerResolver.class);
	}

	/**
	 * Central security header control. We need to remove xssProtection header due to A12UAA-2278
	 * The X-XSS-Protection is not supported anymore.
	 * <p>
	 * Sometimes, you have situation that Spring enables a certain header and during penentration test
	 * the pentester does not allow these headers for security reason.
	 * UAA needs to have better control what should and should not shown in the response header of all request.
	 *
	 * @param builder: builder for all authentication types in UAA.
	 */
	private void controlSecurityHeader(HttpSecurity builder) {
		try {
			builder.headers(customizer -> {
				customizer.xssProtection(xXssConfig -> {
					xXssConfig.disable();
				});
			});
		} catch (Exception e) {
			LOGGER.warn("Control security header exception ", e);
		}
	}
}
