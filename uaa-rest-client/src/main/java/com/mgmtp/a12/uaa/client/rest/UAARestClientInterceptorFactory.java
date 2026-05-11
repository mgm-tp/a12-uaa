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
package com.mgmtp.a12.uaa.client.rest;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.cache.CacheManager;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import com.mgmtp.a12.connector.rest.AcceptHeaderInterceptor;
import com.mgmtp.a12.uaa.client.rest.auth.AuthenticationHandler;
import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.TokenAcquirer;
import com.mgmtp.a12.uaa.client.rest.auth.TokenRefresher;
import com.mgmtp.a12.uaa.client.rest.auth.TokenValidator;
import com.mgmtp.a12.uaa.client.rest.auth.internal.AuthorizationInterceptor;
import com.mgmtp.a12.uaa.client.rest.auth.internal.DelegatedAuthenticationHandler;
import com.mgmtp.a12.uaa.client.rest.auth.internal.SingleThreadAuthenticationHandler;
import com.mgmtp.a12.uaa.client.rest.auth.internal.delegated.AuthorizationDataHolder;
import com.mgmtp.a12.uaa.client.rest.auth.internal.store.AtomicAuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.internal.store.DelegatingAuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.internal.store.PersistingAuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.internal.store.ThreadLocalAuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.TokenType;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.UAACertificateTokenAcquirer;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.jwt.JwtTokenRefresher;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.jwt.JwtTokenValidator;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.jwt.LoginEndpointTokenAcquirer;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.jwt.UAAJwtSamlTokenAcquirer;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.oauth2.ClientType;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.oauth2.Oauth2TokenRefresher;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.oauth2.Oauth2TokenValidator;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.oauth2.UAAOauth2ConfidentialTokenAcquirer;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.oauth2.UAAOauth2PublicTokenAcquirer;
import com.mgmtp.a12.uaa.client.rest.cache.CacheNameMapper;
import com.mgmtp.a12.uaa.client.rest.cache.internal.CacheInterceptor;
import com.mgmtp.a12.uaa.client.rest.cache.internal.CacheService;
import com.mgmtp.a12.uaa.client.rest.config.AuthenticationType;
import com.mgmtp.a12.uaa.client.rest.config.properties.OidcProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

public final class UAARestClientInterceptorFactory {

	public static ClientHttpRequestInterceptor createAuthorizationInterceptor(UAARestClientProperties configuration) {
		return createAuthorizationInterceptorWithCaching(configuration, null, new CacheNameMapper[] {}).getAuthorizationInterceptor();

	}

	public static UAARestClientAuthorizationConfig createAuthorizationInterceptorWithCaching(UAARestClientProperties configuration, CacheManager cacheManager,
		CacheNameMapper... cacheNameMappers) {
		AuthorizationInterceptor authorizationInterceptor;
		CacheInterceptor cacheInterceptor = null;
		CacheService cacheService = null;

		try {
			AuthenticationHandler authHandler;
			AuthorizationDataStore authorizationDataStore;
			if (configuration.getAuthenticationType() == AuthenticationType.DELEGATED) {
				authHandler = configureDelegatedAuthentication();
				authorizationDataStore = AuthorizationDataHolder.getCredentialContext();
			} else {
				DelegatingAuthorizationDataStore delegatingAuthorizationDataStore = createAuthorizationDataStore(configuration);
				RegularAuthenticationHolder regularAuthentication = configureRegularAuthentication(configuration, delegatingAuthorizationDataStore);
				authHandler = regularAuthentication.authenticationHandler;
				regularAuthentication.persistingDataStore.ifPresent(delegatingAuthorizationDataStore::addAuthorizationStore);
				authorizationDataStore = delegatingAuthorizationDataStore;
			}

			authorizationInterceptor =
				new AuthorizationInterceptor(configuration.getAuthenticationType(), authHandler, configuration.getUaaBase().getUrl(),
					configuration.getAuthorizationHeaderName());
			if ((cacheNameMappers.length > 0) && (cacheManager != null)) {
				cacheService = new CacheService(authorizationDataStore, cacheManager, Arrays.asList(cacheNameMappers));
				cacheInterceptor = new CacheInterceptor(cacheService);
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to load data store", e);
		}
		return new UAARestClientAuthorizationConfig(authorizationInterceptor, cacheInterceptor, cacheService);
	}

	private static AuthenticationHandler configureDelegatedAuthentication() {
		return new DelegatedAuthenticationHandler();
	}

	private static DelegatingAuthorizationDataStore createAuthorizationDataStore(UAARestClientProperties uaaRestClientProperties) {
		List<AuthorizationDataStore> stores = new LinkedList<>();
		AuthenticationType authType = uaaRestClientProperties.getAuthenticationType();
		boolean isCertificateOrAPIKey =
			authType == AuthenticationType.CERTIFICATE ||
				authType == AuthenticationType.API_KEY;

		if (isCertificateOrAPIKey) {
			stores.add(new ThreadLocalAuthorizationDataStore());
		} else {
			stores.add(new AtomicAuthorizationDataStore());
		}

		return new DelegatingAuthorizationDataStore(stores);

	}

	private static RegularAuthenticationHolder configureRegularAuthentication(UAARestClientProperties uaaRestClientProperties,
		AuthorizationDataStore authorizationDataStore) throws IOException {
		TokenAcquirer tokenAcquirer;
		TokenRefresher tokenRefresher;
		TokenValidator tokenValidator;
		Optional<PersistingAuthorizationDataStore> persistingDataStore = Optional.empty();
		switch (uaaRestClientProperties.getAuthenticationType()) {
		case SAML:
			tokenRefresher = new JwtTokenRefresher(uaaRestClientProperties, authorizationDataStore);
			tokenAcquirer = new UAAJwtSamlTokenAcquirer(uaaRestClientProperties, authorizationDataStore, tokenRefresher);
			tokenValidator = new JwtTokenValidator(uaaRestClientProperties.getUaaBase().getUrl());
			persistingDataStore = createPersistingDataStore(uaaRestClientProperties, tokenRefresher, tokenValidator);
			break;
		case OAUTH2:
			tokenRefresher = new Oauth2TokenRefresher(uaaRestClientProperties, authorizationDataStore);
			OidcProperties oidcProperties = uaaRestClientProperties.getAuthenticationConfiguration().getOidc();
			if (uaaRestClientProperties.getAuthenticationConfiguration().getOidc().getClientType() == ClientType.PUBLIC) {
				tokenAcquirer = new UAAOauth2PublicTokenAcquirer(uaaRestClientProperties, authorizationDataStore, tokenRefresher);
				tokenValidator = new Oauth2TokenValidator(oidcProperties.getPublicClient());
			} else {
				tokenAcquirer = new UAAOauth2ConfidentialTokenAcquirer(uaaRestClientProperties, authorizationDataStore, tokenRefresher);
				tokenValidator = new Oauth2TokenValidator(oidcProperties.getConfidentialClient());
			}
			persistingDataStore = createPersistingDataStore(uaaRestClientProperties, tokenRefresher, tokenValidator);
			break;
		case CERTIFICATE:
			tokenAcquirer = new UAACertificateTokenAcquirer(uaaRestClientProperties.getAuthenticationConfiguration().getCertificate().getKeyStore());
			break;
		case API_KEY:
			tokenAcquirer = new UAACertificateTokenAcquirer(uaaRestClientProperties.getApiKeyResource(), TokenType.API_KEY);
			break;
		default:
			tokenRefresher = new JwtTokenRefresher(uaaRestClientProperties, authorizationDataStore);
			tokenAcquirer = new LoginEndpointTokenAcquirer(uaaRestClientProperties, authorizationDataStore, tokenRefresher);
			tokenValidator = new JwtTokenValidator(uaaRestClientProperties.getUaaBase().getUrl());
			persistingDataStore = createPersistingDataStore(uaaRestClientProperties, tokenRefresher, tokenValidator);
		}

		AuthenticationHandler authHandler = new SingleThreadAuthenticationHandler(uaaRestClientProperties, authorizationDataStore, tokenAcquirer);
		return new RegularAuthenticationHolder(authHandler, persistingDataStore);
	}

	private static Optional<PersistingAuthorizationDataStore> createPersistingDataStore(UAARestClientProperties configuration, TokenRefresher tokenRefresher,
		TokenValidator tokenValidator) throws IOException {

		return Optional
			.of(configuration)
			.filter(config -> config.getAuthorizationDataStore() != null)
			.map(config -> {
				try {
					return config.getAuthorizationDataStore().getFile().toPath();
				} catch (Exception e) {
					throw new RuntimeException("Unable to load data store", e);
				}
			})
			.map(path -> new PersistingAuthorizationDataStore(path, tokenRefresher, tokenValidator));
	}

	public static ClientHttpRequestInterceptor createAcceptHeaderInterceptor() {
		return new AcceptHeaderInterceptor();
	}

	private static class RegularAuthenticationHolder {
		private AuthenticationHandler authenticationHandler;
		private Optional<PersistingAuthorizationDataStore> persistingDataStore;

		public RegularAuthenticationHolder(AuthenticationHandler authenticationHandler, Optional<PersistingAuthorizationDataStore> persistingDataStore) {
			this.authenticationHandler = authenticationHandler;
			this.persistingDataStore = persistingDataStore;
		}

	}
}
