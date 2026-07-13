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
package com.mgmtp.a12.uaa.authentication.apikey.internal;

import java.util.Optional;

import jakarta.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;
import com.mgmtp.a12.uaa.authentication.apikey.APIKeyConverter;
import com.mgmtp.a12.uaa.authentication.apikey.APIKeyValidator;
import com.mgmtp.a12.uaa.authentication.internal.AuthenticationTokenLocator;
import com.mgmtp.a12.uaa.authentication.internal.HeaderAuthenticationTokenLocator;
import com.mgmtp.a12.uaa.authentication.internal.TokenType;
import com.mgmtp.a12.uaa.authentication.security.UAASecurityConfigurer;
import com.mgmtp.a12.uaa.authentication.security.login.internal.UAALoginEntryPoint;

@Configuration
@ConditionalOnAuthentication(AuthenticationType.API_KEY)
public class APIKeySecurityConfigurer extends UAASecurityConfigurer<APIKeySecurityConfigurer> {

	@Inject
	private AuthenticationProperties authenticationProperties;
	@Inject
	private APIKeyConverter apiKeyPrincipalCreator;
	@Inject
	private Optional<APIKeyValidator> apiKeyValidator;

	@Bean
	public RootCAManager rootCAManager() {
		return new RootCAManager(authenticationProperties.getApiKeyAuthorityResources());
	}

	@Override
	public void configure(HttpSecurity http) {
		AuthenticationTokenLocator locator = new HeaderAuthenticationTokenLocator(authenticationProperties.getJwt().getHeaderName(), TokenType.APIKEY);

		UAALoginEntryPoint loginEntryPoint = new UAALoginEntryPoint(authenticationProperties.getUnauthorizedCode());
		APIKeyAuthenticationFilter apiKeyAuthenticationFilter = new APIKeyAuthenticationFilter(locator,
			authenticationProperties.getContextPath(),
			authenticationProperties.getApiKeyWhiteListAccessUrlPatterns(),
			getAuthenticationManager(http), loginEntryPoint);
		http.addFilterBefore(apiKeyAuthenticationFilter, AuthorizationFilter.class);
	}

	@Override
	protected Optional<AuthenticationProvider> createAuthenticationProvider() {
		APIKeyValidator apiKeyValidator = this.apiKeyValidator.orElse(new UAAAPIKeyValidator());
		return Optional.of(new APIKeyAuthenticationProvider(apiKeyPrincipalCreator, rootCAManager(), apiKeyValidator));
	}
}
