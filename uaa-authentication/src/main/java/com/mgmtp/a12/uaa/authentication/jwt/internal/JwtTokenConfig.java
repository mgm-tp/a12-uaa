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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.AuthenticationProperties.JwtProperties;
import com.mgmtp.a12.uaa.authentication.AuthenticationProperties.Redirect;
import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;
import com.mgmtp.a12.uaa.authentication.ConditionalOnConfiguration;
import com.mgmtp.a12.uaa.authentication.internal.AuthenticationTokenLocator;
import com.mgmtp.a12.uaa.authentication.internal.HeaderAuthenticationTokenLocator;
import com.mgmtp.a12.uaa.authentication.internal.RedirectSupport;
import com.mgmtp.a12.uaa.authentication.internal.RedirectType;
import com.mgmtp.a12.uaa.authentication.internal.RedirectType.Type;
import com.mgmtp.a12.uaa.authentication.internal.TokenType;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenService;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenStorage;
import com.mgmtp.a12.uaa.authentication.jwt.RenewTokenStorage;
import com.mgmtp.a12.uaa.authentication.jwt.encryption.DataEncoder;
import com.mgmtp.a12.uaa.authentication.jwt.internal.renew.CacheableRenewTokenStorage;
import com.mgmtp.a12.uaa.authentication.jwt.internal.renew.RenewTokenCleaner;
import com.mgmtp.a12.uaa.authentication.jwt.internal.renew.RenewTokenService;
import com.mgmtp.a12.uaa.authentication.jwt.internal.renew.SimpleRenewTokenStorage;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalCreator;
import com.mgmtp.a12.uaa.authentication.web.internal.TokenHandlingController;

/**
 * It's kept in separate class to avoid circular dependencies.
 */
@ConditionalOnAuthentication({ AuthenticationType.LOCAL, AuthenticationType.ACTIVE_DIRECTORY_LDAP, AuthenticationType.SAML,
	AuthenticationType.UAA_ACCESS_TOKEN })
@Configuration
@EnableCaching
class JwtTokenConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenConfig.class.getCanonicalName() + ".UAA_Config");

	@Inject
	private Optional<CacheManager> cacheManager;

	@Inject
	private AuthenticationProperties authenticationProperties;

	@Bean
	public JwtTokenCleaner createTokenCleaner() {
		return new JwtTokenCleaner();
	}

	@Bean
	@ConditionalOnConfiguration(configurationKey = "mgmtp.a12.uaa.authentication.jwt.token-endpoints.enabled", configurationValue = "true",
		matchIfMissing = true)
	public RenewTokenCleaner createRenewTokenCleaner() {
		return new RenewTokenCleaner();
	}

	@Bean
	public AuthenticationTokenLocator createJwtTokenLocator() {
		LOGGER.info("JWT token is stored in header named [{}]", authenticationProperties.getJwt().getHeaderName());
		return new HeaderAuthenticationTokenLocator(authenticationProperties.getJwt().getHeaderName(), TokenType.UAABEARER);
	}

	@Bean
	public JwtTokenStorage createJwtTokenStorage() {
		JwtTokenStorage storage = new SimpleJwtTokenStorage();
		if (authenticationProperties.getCachedTokenStorage().isEnabled()) {
			cacheManager.orElseThrow(() -> new RuntimeException("No cache manager is defined. Please enable spring caching."));
			storage = new CacheableJwtTokenStorage(storage, cacheManager.get());
			LOGGER.info("Cached JWT token storage created. Don't forget to configure cache region [tokenCache] for replication");
		}
		return storage;
	}

	@Bean
	public JwtTokenGenerator createJwtTokenGenerator(DataEncoder encoder) {
		JwtTokenGenerator.Builder builder = new JwtTokenGenerator.Builder();
		JwtProperties jwtProperties = authenticationProperties.getJwt();
		return builder
			.withPrivateKeyLocation(jwtProperties.getPrivateKeyLocation())
			.withDataEncoder(encoder)
			.withSecretKey(jwtProperties.getSecret())
			.withExpirationSeconds(jwtProperties.getExpirationSeconds())
			.withStoreUser(jwtProperties.getStoreUserInToken().isEnabled())
			.build();
	}

	@Bean
	public JwtTokenVerifier createJwtTokenVerifier(DataEncoder encoder) {
		JwtTokenVerifier.Builder builder = new JwtTokenVerifier.Builder();
		JwtProperties jwtProperties = authenticationProperties.getJwt();
		return builder
			.withPublicKeyLocation(jwtProperties.getPublicKeyLocation())
			.withDataEncoder(encoder)
			.withSecretKey(jwtProperties.getSecret())
			.withUserLifetimeSeconds(jwtProperties.getUserLifetimeSeconds())
			.withStoreUser(jwtProperties.getStoreUserInToken().isEnabled())
			.build();
	}

	@Bean
	public PrincipalCreator<? extends UserDetails> createJwtTokenPrincipalCreator() {
		return new JwtTokenPrincipalCreator();
	}

	@Bean
	public RenewTokenStorage createRenewTokenStorage() {
		RenewTokenStorage storage = new SimpleRenewTokenStorage();
		if (authenticationProperties.getCachedTokenStorage().isEnabled()) {
			cacheManager.orElseThrow(() -> new RuntimeException("No cache manager is defined. Please enable spring caching."));
			storage = new CacheableRenewTokenStorage(storage, cacheManager.get());
			LOGGER.info(
				"Cached renew token storage created. Don't forget to configure cache region [codeChallengeCache, codeCache, tokenHintCache] for replication");
		}
		return storage;
	}

	@Bean
	public RenewTokenService createRenewTokenService() {
		return new RenewTokenService();
	}

	@Bean
	public JWTLogoutSuccessHandler createJwtLogoutSuccessHandler() {
		return new JWTLogoutSuccessHandler(createLogoutRedirectSupport());
	}

	@Bean
	JwtTokenLogoutHandler createJwtTokenLogoutHandler() {
		return new JwtTokenLogoutHandler(createJwtTokenLocator(), createJwtTokenStorage(), createLogoutRedirectSupport());
	}

	@Bean
	@RedirectType(type = Type.LOGOUT)
	RedirectSupport createLogoutRedirectSupport() {
		return createRedirectSupport(authenticationProperties.getLogout().getRedirect());
	}

	@Bean
	@RedirectType(type = Type.LOGIN)
	RedirectSupport createLoginRedirectSupport() {
		return createRedirectSupport(authenticationProperties.getLogin().getRedirect());
	}

	@Bean
	public JwtTokenService createJwtTokenService() {
		return new JwtTokenService();
	}

	@Bean
	@ConditionalOnConfiguration(configurationKey = "mgmtp.a12.uaa.authentication.jwt.token-endpoints.enabled", configurationValue = "true",
		matchIfMissing = true)
	TokenHandlingController createTokenHandlingController() {
		return new TokenHandlingController();
	}

	private RedirectSupport createRedirectSupport(Redirect redirectConfiguration) {
		Boolean httpOnly = Optional.ofNullable(authenticationProperties)
			.map(properties -> properties.getCookie().getHttpOnly().isEnabled()).orElse(Boolean.FALSE);
		Boolean secured = Optional.ofNullable(authenticationProperties)
			.map(properties -> properties.getCookie().getSecured().isEnabled()).orElse(Boolean.FALSE);
		Integer cookieLifetimeSeconds = authenticationProperties.getCookie().getLifetimeSeconds();

		return new RedirectSupport(redirectConfiguration, httpOnly, secured, cookieLifetimeSeconds);
	}
}
