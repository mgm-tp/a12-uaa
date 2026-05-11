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
package com.mgmtp.a12.uaa.authentication.oauth2.internal;

import java.util.Optional;

import jakarta.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.reactive.function.client.WebClient;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;
import com.mgmtp.a12.uaa.authentication.oauth.client.UaaOauth2ClientConverter;
import com.mgmtp.a12.uaa.authentication.oauth.client.internal.CustomOauth2ClientSuccessHandler;
import com.mgmtp.a12.uaa.authentication.oauth.client.internal.ReverseLogoutOauth2AuthenticationTokenFilter;
import com.mgmtp.a12.uaa.authentication.security.UAASecurityConfigurer;

@Configuration
@ConditionalOnAuthentication(AuthenticationType.OAUTH2_CLIENT)
public class Oauth2ClientSecurityConfigurer extends UAASecurityConfigurer<Oauth2ClientSecurityConfigurer> {

	@Inject
	private AuthenticationProperties authenticationProperties;
	@Inject
	private Optional<UaaOauth2ClientConverter> uaaOauth2ClientConverter;
	@Inject
	private Optional<ClientRegistrationRepository> clientRegistrationRepository;

	@Override
	public void configureHttpSecurity(HttpSecurity http) throws Exception {
		if (uaaOauth2ClientConverter.isPresent()) {
			//For future, if we need to configure about converting to UserDetails or not
			http.oauth2Login(login -> login.successHandler(new CustomOauth2ClientSuccessHandler(uaaOauth2ClientConverter.get())));
			http.addFilterBefore(new ReverseLogoutOauth2AuthenticationTokenFilter(), LogoutFilter.class);
			//Mode Oauth2 Client requires session created.
			http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
			if (authenticationProperties.getOauth2().getIdpLogout().isEnabled()) {
				http.logout(logout -> logout.logoutSuccessHandler(oidcLogoutSuccessHandler()));
			}
		}
	}

	@Bean
	public OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler() {
		OidcClientInitiatedLogoutSuccessHandler logoutHandler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository.get());
		logoutHandler.setPostLogoutRedirectUri(authenticationProperties.getOauth2().getPostLogout().getUrl());
		return logoutHandler;
	}

	//Configuration for refresh token
	@Bean
	public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistration,
		OAuth2AuthorizedClientRepository authorizedClientRepository) {

		OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().refreshToken().build();

		DefaultOAuth2AuthorizedClientManager authorizedClientManager = new DefaultOAuth2AuthorizedClientManager(clientRegistration, authorizedClientRepository);
		authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

		return authorizedClientManager;
	}

	@Bean
	public WebClient webClient(OAuth2AuthorizedClientManager authorizedClientManager) {
		ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client = new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
		return WebClient.builder().apply(oauth2Client.oauth2Configuration()).build();
	}
}
