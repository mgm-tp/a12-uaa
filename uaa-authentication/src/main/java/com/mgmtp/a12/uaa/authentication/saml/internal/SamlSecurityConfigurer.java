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
package com.mgmtp.a12.uaa.authentication.saml.internal;

import java.lang.reflect.Field;
import java.security.Security;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import jakarta.inject.Inject;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.opensaml.saml.saml2.assertion.SAML2AssertionValidationParameters;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider;
import org.springframework.security.saml2.provider.service.metadata.OpenSamlMetadataResolver;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration.Builder;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.Saml2MetadataFilter;
import org.springframework.security.saml2.provider.service.web.Saml2WebSsoAuthenticationRequestFilter;
import org.springframework.security.saml2.provider.service.web.authentication.OpenSaml5AuthenticationRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.Saml2WebSsoAuthenticationFilter;
import org.springframework.security.saml2.provider.service.web.authentication.logout.OpenSaml5LogoutRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2RelyingPartyInitiatedLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.util.ReflectionUtils;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.AuthenticationProperties.SamlProperties;
import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;
import com.mgmtp.a12.uaa.authentication.internal.AuthenticationTokenLocator;
import com.mgmtp.a12.uaa.authentication.internal.ClassNameUtils;
import com.mgmtp.a12.uaa.authentication.internal.RedirectSupport;
import com.mgmtp.a12.uaa.authentication.internal.RedirectType;
import com.mgmtp.a12.uaa.authentication.internal.RedirectType.Type;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JWTLogoutSuccessHandler;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenVerifier;
import com.mgmtp.a12.uaa.authentication.saml.AuthorizationCodeStorage;
import com.mgmtp.a12.uaa.authentication.saml.RequestContextDataGenerator;
import com.mgmtp.a12.uaa.authentication.saml.RequestExtensionsDataGenerator;
import com.mgmtp.a12.uaa.authentication.saml.SamlAssertionExtractor;
import com.mgmtp.a12.uaa.authentication.saml.SamlGrantedAuthorityConverter;
import com.mgmtp.a12.uaa.authentication.saml.SamlJwtTokenStorage;
import com.mgmtp.a12.uaa.authentication.saml.UaaSaml2LogoutRequestRepository;
import com.mgmtp.a12.uaa.authentication.security.UAASecurityConfigurer;

@Configuration
@ConditionalOnAuthentication(AuthenticationType.SAML)
public class SamlSecurityConfigurer extends UAASecurityConfigurer<SamlSecurityConfigurer> {

	private RelyingPartyRegistrationResolver relyingPartyRegistrationResolver;

	@Inject
	private AuthenticationProperties authenticationProperties;
	@Inject
	private Optional<CacheManager> cacheManager;
	@Inject
	private Optional<RelyingPartyRegistrationRepository> relyingPartyRegistrationRepository;
	@Inject
	private Optional<RequestExtensionsDataGenerator> extensionDataGenerator;
	@Inject
	private Optional<RequestContextDataGenerator> requestContextDataGenerator;
	@Inject
	private Optional<SamlGrantedAuthorityConverter> samlGrantedAuthorityConverter;
	@Inject
	private Optional<SamlAssertionExtractor> samlAssertionExtractor;
	@Inject
	private JWTLogoutSuccessHandler jwtLogoutSuccessHandler;
	@Inject
	private AuthenticationTokenLocator jwtTokenLocator;
	@Inject
	private JwtTokenVerifier jwtTokenVerifier;
	@Inject
	@RedirectType(type = Type.LOGIN)
	private RedirectSupport loginRedirectSupport;

	public SamlSecurityConfigurer() {
		Security.addProvider(new BouncyCastleProvider());
	}

	@Override
	public void configureHttpSecurity(HttpSecurity http) throws Exception {
		http.requestCache(customizer -> {
			customizer.requestCache(new HttpSessionRequestCache());
		});

		http.logout(logoutConfigurer -> {
			LogoutSuccessHandler logoutSuccessHandler = logoutConfigurer.getLogoutSuccessHandler();
			logoutConfigurer.logoutSuccessHandler(
				new SamlInvalidatingLogoutSuccessHandler(logoutSuccessHandler, createLogoutRequestRepository()));
		});

		// we need to ensure that we use post binding. There is no way to do it by
		// configuration. We need to re-instantiate regiatration repository
		List<RelyingPartyRegistration> empty = Collections.emptyList();
		Iterable<RelyingPartyRegistration> iterable = () -> relyingPartyRegistrationRepository
			.map((repository) -> ((InMemoryRelyingPartyRegistrationRepository) repository).iterator())
			.orElse(empty.iterator());
		List<RelyingPartyRegistration> updatedRegistrations = StreamSupport.stream(iterable.spliterator(), false)
			.map(registration -> {
				RelyingPartyRegistration existingRegistration = relyingPartyRegistrationRepository.get()
					.findByRegistrationId(registration.getRegistrationId());
				// the update is only for spring based registration.
				Builder builder = existingRegistration.mutate()
					.assertingPartyMetadata(party -> {
						party.signingAlgorithms((alg) -> alg.add(authenticationProperties.getSaml().getSigningAlgorithm().getUrl()));
					});
				RelyingPartyRegistration updatedRegistration = builder.build();

				return updatedRegistration;
			}).collect(Collectors.toList());

		RelyingPartyRegistrationRepository relyingRepository = new InMemoryRelyingPartyRegistrationRepository(
			updatedRegistrations);

		relyingPartyRegistrationResolver = new DefaultRelyingPartyRegistrationResolver(
			relyingRepository);

		Saml2MetadataFilter metadataFilter = new Saml2MetadataFilter(relyingPartyRegistrationResolver,
			new OpenSamlMetadataResolver());

		OpenSaml5AuthenticationRequestResolver authenticationRequestResolver = new OpenSaml5AuthenticationRequestResolver(
			relyingPartyRegistrationResolver);
		boolean isForceAuth = authenticationProperties.getSaml().getForceAuth().isEnabled();
		authenticationRequestResolver.setAuthnRequestCustomizer(
			new UAAAuthnRequestConsumer(isForceAuth, extensionDataGenerator, requestContextDataGenerator));
		http
			.saml2Login(saml2 -> {
				saml2.successHandler(createSamlAuthenticationSuccessHandler());
				saml2.relyingPartyRegistrationRepository(relyingRepository);
				saml2.failureHandler(createSamlAuthenticationFailureHandler());
				saml2.authenticationRequestResolver(authenticationRequestResolver);
			})
			.saml2Logout(saml2 -> {
				OpenSaml5LogoutRequestResolver logoutRequestResolver = new OpenSaml5LogoutRequestResolver(
					relyingPartyRegistrationResolver);
				saml2.relyingPartyRegistrationRepository(relyingRepository);
				saml2.logoutUrl("/user/logout");
				saml2.logoutRequest(customizer -> {
					customizer.logoutRequestRepository(createLogoutRequestRepository());
				});
				saml2.addObjectPostProcessor(new ObjectPostProcessor<LogoutFilter>() {
					public LogoutFilter postProcess(LogoutFilter filter) {
						Field successHandlerField = ReflectionUtils.findField(LogoutFilter.class, "logoutSuccessHandler");
						successHandlerField.setAccessible(true);
						LogoutSuccessHandler logoutSuccessHandler = jwtLogoutSuccessHandler;
						if (authenticationProperties.getSaml().getIdpLogout().isEnabled()) {
							Saml2RelyingPartyInitiatedLogoutSuccessHandler successHandler = (Saml2RelyingPartyInitiatedLogoutSuccessHandler) ReflectionUtils
								.getField(successHandlerField, filter);
							successHandler.setLogoutRequestRepository(createLogoutRequestRepository());
							logoutSuccessHandler = new SamlDelegatedLogoutSuccessHandler(logoutRequestResolver,
								jwtLogoutSuccessHandler, successHandler);
						}
						ReflectionUtils.setField(successHandlerField, filter, logoutSuccessHandler);
						return filter;
					}
				});
			}).addFilterBefore(metadataFilter, Saml2WebSsoAuthenticationFilter.class)
			.addFilterBefore(
				new UAASamlAuthenticationRequestFilter(authenticationProperties.getContextPath(), loginRedirectSupport),
				Saml2WebSsoAuthenticationRequestFilter.class)
			.addFilterBefore(new SameSiteFilter(authenticationProperties.getCookie().getSameSite()),
				UAASamlAuthenticationRequestFilter.class);
		LOGGER.info("SAML: Using SamlGrantedAuthorityConverter: [{}]",
			ClassNameUtils.resolveShortClassName(samlGrantedAuthorityConverter));
		LOGGER.info("SAML: Using SamlAssertionExtractor: [{}]",
			ClassNameUtils.resolveShortClassName(samlAssertionExtractor));
	}

	@Override
	public void configure(HttpSecurity builder) throws Exception {
		builder.addFilterBefore(
			new Saml2LogoutRequestAuthenticatorFilter(relyingPartyRegistrationResolver, createSamlJwtTokenStorage(), getAuthenticationManager(builder)),
			CsrfFilter.class);
	}

	@Override
	protected Optional<AuthenticationProvider> createAuthenticationProvider() {
		return Optional.empty();
	}

	// register this bean to prevent the default OpenSaml5AuthenticationProvider from Saml2LoginConfigurer
	@Bean
	public OpenSaml5AuthenticationProvider createOpenSaml5AuthenticationProvider() {
		OpenSaml5AuthenticationProvider authenticationProvider = new OpenSaml5AuthenticationProvider();
		authenticationProvider.setResponseAuthenticationConverter(samlAuthenticationConverter());
		authenticationProvider.setAssertionValidator(OpenSaml5AuthenticationProvider.createDefaultAssertionValidatorWithParameters((parameters) -> {
			parameters.put(SAML2AssertionValidationParameters.CLOCK_SKEW,
				Duration.ofMinutes(authenticationProperties.getSaml().getAssertionLifetimeMinutes()).toMillis());
		}));

		return authenticationProvider;
	}

	@Bean
	public SamlAuthenticationConverter samlAuthenticationConverter() {
		SamlProperties samlProperties = authenticationProperties.getSaml();
		return new SamlAuthenticationConverter(samlProperties.getUniqueElementXpaths(),
			samlProperties.getSameValueElementXpaths());
	}

	@Bean
	public AuthorizationCodeStorage createAuthorizationCodeStorage() {
		AuthorizationCodeStorage storage = new AuthorizationCodeStorage() {
		};
		if (authenticationProperties.getCachedTokenStorage().isEnabled()) {
			cacheManager.orElseThrow(
				() -> new RuntimeException("No cache manager is defined. Please enable spring caching."));
			storage = new CacheableAuthorizationCodeStorageImpl(storage, cacheManager.get());
		}
		return storage;
	}

	@Bean
	public SamlAuthorizationCodeCleaner createSamlStoredCodeCleaner() {
		return new SamlAuthorizationCodeCleaner();
	}

	@Bean
	public SamlJwtTokenCleaner createSamlJwtTokenCleaner() {
		return new SamlJwtTokenCleaner();
	}

	@Bean
	public SamlAuthenticationSuccessHandler createSamlAuthenticationSuccessHandler() {
		return new SamlAuthenticationSuccessHandler(authenticationProperties.getCookie().getHttpOnly().isEnabled(),
			authenticationProperties.getCookie().getSecured().isEnabled(), authenticationProperties.getSaml().getAuthorizationCodeExpirationSeconds());
	}

	@Bean
	public SamlAuthenticationFailureHandler createSamlAuthenticationFailureHandler() {
		return new SamlAuthenticationFailureHandler();
	}

	@Bean
	public SamlLogoutRequestCleaner createLogoutRequestCleaner() {
		return new SamlLogoutRequestCleaner();
	}

	@Bean
	public UaaSaml2LogoutRequestRepository createLogoutRequestRepository() {
		UaaSaml2LogoutRequestRepository repository = new SimpleSamlLogoutRequestRepository(jwtTokenLocator, jwtTokenVerifier);
		if (authenticationProperties.getCachedTokenStorage().isEnabled()) {
			cacheManager.orElseThrow(
				() -> new RuntimeException("No cache manager is defined. Please enable spring caching."));
			repository = new CacheableSamlLogoutRequestRepository(repository, cacheManager.get());
		}
		return repository;
	}

	@Bean
	public UAASaml2AuthenticationRequestRepository createAuthenticationRequestRepository() {
		UAASaml2AuthenticationRequestRepository repository = new UAASaml2AuthenticationRequestRepository() {
		};
		if (authenticationProperties.getCachedTokenStorage().isEnabled()) {
			cacheManager.orElseThrow(
				() -> new RuntimeException("No cache manager is defined. Please enable spring caching."));
			repository = new CacheableSamlAuthenticationRequestRepository(repository, cacheManager.get());
		}
		return repository;
	}

	@Bean
	public SamlJwtTokenStorage createSamlJwtTokenStorage() {
		SamlJwtTokenStorage storage = new SamlJwtTokenStorage() {
		};
		if (authenticationProperties.getCachedTokenStorage().isEnabled()) {
			cacheManager.orElseThrow(
				() -> new RuntimeException("No cache manager is defined. Please enable spring caching."));
			storage = new CacheableSamlJwtTokenStorageImpl(storage, cacheManager.get());
		}
		return storage;
	}

	@Bean
	public SamlAuthenticationRequestCleaner createAuthenticationRequestCleaner() {
		return new SamlAuthenticationRequestCleaner();
	}

	@Bean
	public SamlTokenExchangeService createSamlTokenExchangeService() {
		return new SamlTokenExchangeService();
	}
}
