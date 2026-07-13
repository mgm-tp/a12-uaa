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
package com.mgmtp.a12.uaa.authentication.security.internal;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.internal.LambdaUtils;
import com.mgmtp.a12.uaa.authentication.log.internal.LoggingFilter;
import com.mgmtp.a12.uaa.authentication.security.FilterRegistration;
import com.mgmtp.a12.uaa.authentication.security.UAASecurityConfigurer;
import com.mgmtp.a12.uaa.authentication.security.login.internal.UAALoginEntryPoint;

@EnableWebSecurity
@Configuration
public class UAAGlobalSecurityConfiguration {

	static final Logger LOGGER = LoggerFactory.getLogger(UAAGlobalSecurityConfiguration.class.getCanonicalName() + ".UAA_Config");

	@Inject
	protected AuthenticationProperties authenticationProperties;

	@Inject
	private Optional<List<UAASecurityConfigurer<?>>> uaaSecurityConfigurers;

	@Inject
	private Optional<UriAuthorizationFilterRegistration> uriAuthorizationFilterRegistration;

	@Inject
	private Optional<List<FilterRegistration>> filterRegistrations;

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> {
			List<String> unsecuredUrls = authenticationProperties.getUnsecured().getUrls();
			LOGGER.info("Unsecured URLs [{}]. No UAA infrastructure applied.", StringUtils.join(unsecuredUrls, ","));
			if (!CollectionUtils.isEmpty(unsecuredUrls)) {
				unsecuredUrls.forEach(unsecuredUrl -> web.ignoring().requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(unsecuredUrl)));
			}
		};
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.anonymous(anonymous -> anonymous.disable());

		LOGGER.info("Configuring spring security with authentication: {}", StringUtils.join(authenticationProperties.getTypes(), ","));
		List<String> securedContexts = new LinkedList<>(authenticationProperties.getSecuredContexts());
		securedContexts.add(contextPath());
		securedContexts.add(authenticationProperties.getContextPath() + "/uaa-authorization/**");
		//Setting stateless to make sure no modes needs session
		http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		configureStandardFilters(http);

		uaaSecurityConfigurers
			.map(List::stream)
			.orElse(Stream.empty())
			.forEach(LambdaUtils.uncheckedConsumer(configurer -> http.with(configurer, Customizer.withDefaults())));

		uaaSecurityConfigurers
			.map(List::stream)
			.orElse(Stream.empty())
			.forEach(LambdaUtils.uncheckedConsumer(configurer -> {
				LOGGER.info("Configuring http security by [{}]", configurer.getClass().getName());
				configurer.configureHttpSecurity(http);
			}));

		List<String> permitAllUrls = new LinkedList<>(Arrays.asList(
			authenticationProperties.getContextPath() + "/uaa-authentication/selfconfigure",
			authenticationProperties.getContextPath() + "/uaa-authentication/tokenValid",
			authenticationProperties.getContextPath() + "/uaa-authentication/oauth2TokenValid",
			authenticationProperties.getContextPath() + "/uaa-authentication/authorize",
			authenticationProperties.getContextPath() + "/uaa-authentication/token",
			authenticationProperties.getContextPath() + "/uaa-authentication/exchangeAuthorizationCodeToToken/authorize",
			authenticationProperties.getContextPath() + "/uaa-authentication/exchangeAuthorizationCodeToToken"));
		LOGGER.info("PermitAll URLs [{}]. UAA infrastructure still applied but bypassed.", StringUtils.join(permitAllUrls, ","));
		http.authorizeHttpRequests(
			(authorizedHttpRequest) -> {
				permitAllUrls.forEach(
					permitAllUrl -> authorizedHttpRequest.requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(permitAllUrl)).permitAll());
				authorizedHttpRequest.anyRequest().authenticated();
			});

		securedContexts.forEach(
			(securedContext) -> http.securityMatchers(customizer -> {
				customizer.requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(securedContext));
			}));
		http
			.exceptionHandling(handling -> handling.accessDeniedHandler(uaaAccessDeniedHandler())
				.authenticationEntryPoint(new UAALoginEntryPoint(authenticationProperties.getUnauthorizedCode())))
			.requestCache(cache -> cache.disable())
			.csrf(csrf -> csrf.disable())
			.rememberMe(me -> me.disable());

		if (authenticationProperties.getCors().isEnabled()) {
			http.cors(cors -> cors.configurationSource(createUrlBasedCorsConfigurationSource()));
		} else {
			http.cors(cors -> cors.disable());
		}

		// Register uriAuthorizationFilterRegistration first then filterRegistrations
		uriAuthorizationFilterRegistration.ifPresent(obj -> obj.getFilterRegistrations().forEach(registration -> registerFilter(http, registration)));
		filterRegistrations.ifPresent(registrations -> registrations.forEach(registration -> registerFilter(http, registration)));

		http.addFilterAfter(new UAAHeaderFilter(authenticationProperties.getHeaderConfiguration()), AuthorizationFilter.class);

		LOGGER.info("Securing contexts [{}] with UAA", StringUtils.join(securedContexts, ","));

		return http.build();
	}

	private void registerFilter(HttpSecurity http, FilterRegistration registration) {
		LOGGER.info("Registering filter {} for context {}", registration.getFilter().getClass().getName(), registration.getContext());
		http.addFilterAfter(new DelegatingPathFilter(registration.getContext(), registration.getFilter()), AuthorizationFilter.class);
	}

	private ForbiddenAccessDeniedHandler uaaAccessDeniedHandler() {
		return new ForbiddenAccessDeniedHandler();
	}

	private UrlBasedCorsConfigurationSource createUrlBasedCorsConfigurationSource() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration configuration = createCorConfiguration();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	private CorsConfiguration createCorConfiguration() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(authenticationProperties.getCors().getAllowedOrigins());
		configuration.setAllowedMethods(authenticationProperties.getCors().getAllowedMethods());
		configuration.setAllowedHeaders(authenticationProperties.getCors().getAllowedHeaders());
		configuration.setExposedHeaders(authenticationProperties.getCors().getExposedHeaders());
		configuration.setAllowCredentials(authenticationProperties.getCors().getAllowCredentials());
		LOGGER.info("Configuring CORS [{}]", authenticationProperties.getCors());
		return configuration;
	}

	private String contextPath() {
		return "%s/**".formatted(authenticationProperties.getContextPath());
	}

	protected void configureStandardFilters(HttpSecurity http) {
		http
			.addFilterAfter(new LoggingFilter(), AuthorizationFilter.class);
	}

}
