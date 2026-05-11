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

import java.util.Optional;

import jakarta.inject.Inject;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.CorsFilter;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;
import com.mgmtp.a12.uaa.authentication.internal.AuthenticationTokenLocator;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalCreator;
import com.mgmtp.a12.uaa.authentication.security.UAASecurityConfigurer;
import com.mgmtp.a12.uaa.authentication.security.login.internal.UAALoginEntryPoint;

@ConditionalOnAuthentication({ AuthenticationType.LOCAL, AuthenticationType.ACTIVE_DIRECTORY_LDAP, AuthenticationType.SAML,
	AuthenticationType.UAA_ACCESS_TOKEN })
@Configuration
class JwtTokenSecurityConfigurer extends UAASecurityConfigurer<JwtTokenSecurityConfigurer> {

	@Inject
	private AuthenticationProperties authenticationProperties;
	@Inject
	private PrincipalCreator<? extends UserDetails> principalCreator;
	@Inject
	private JWTLogoutSuccessHandler jwtLogoutSuccessHandler;
	@Inject
	private JwtTokenLogoutHandler jwtTokenLogoutHandler;
	@Inject
	private AuthenticationTokenLocator authenticationTokenLocator;
	@Inject
	private JwtTokenVerifier jwtTokenVerifier;

	@Override
	public void configureHttpSecurity(final HttpSecurity http) throws Exception {
		if (authenticationProperties.getJwt().getTokenEndpoints().isEnabled()) {
			http
				.logout(configurer -> {
					configurer.logoutSuccessHandler(jwtLogoutSuccessHandler);
					String logoutUrl = authenticationProperties.getContextPath() + "/user/logout";
					LOGGER.info("Registering logout endpoint to [{}]", logoutUrl);
					configurer.logoutRequestMatcher(new AntPathRequestMatcher(logoutUrl, "POST"));
					configurer.addLogoutHandler(jwtTokenLogoutHandler);
				});
		}
	}

	@Override
	public void configure(HttpSecurity builder) throws Exception {
		builder.addFilterAfter(jwtTokenFilter(getAuthenticationManager(builder)), CorsFilter.class);
	}

	@Override
	protected Optional<AuthenticationProvider> createAuthenticationProvider() {
		return Optional.of(new JwtAuthenticationProvider(principalCreator, jwtTokenVerifier));
	}

	private JwtTokenAuthenticationFilter jwtTokenFilter(AuthenticationManager authenticationManager) throws Exception {
		UAALoginEntryPoint loginEntryPoint = new UAALoginEntryPoint(authenticationProperties.getUnauthorizedCode());
		JwtTokenAuthenticationFilter filter = new JwtTokenAuthenticationFilter(authenticationTokenLocator, authenticationManager, loginEntryPoint);
		return filter;
	}

}
