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
package com.mgmtp.a12.uaa.client.rest.autoconfigure;

import java.util.Optional;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import com.mgmtp.a12.uaa.client.rest.UAARestClientAuthorizationConfig;
import com.mgmtp.a12.uaa.client.rest.UAARestClientInterceptorFactory;
import com.mgmtp.a12.uaa.client.rest.cache.CacheNameMapper;
import com.mgmtp.a12.uaa.client.rest.cache.internal.CacheService;
import com.mgmtp.a12.uaa.client.rest.cache.internal.PathCacheNameMapper;
import com.mgmtp.a12.uaa.client.rest.internal.UAAResponseErrorHandler;

public class UAAInterceptorsConfiguration {

	@Inject
	private UAARestClientAutoconfigProperties uaaRestClientAutoconfigProperties;
	@Inject
	private Optional<CacheManager> cacheManager;

	private UAARestClientAuthorizationConfig authorizationSetup;

	@PostConstruct
	void setUp() throws Exception {
		createInterceptors();
	}

	private void createInterceptors() {
		CacheManager restClientCacheManager = null;
		if (uaaRestClientAutoconfigProperties.getRest().getCache().isEnabled()) {
			restClientCacheManager = cacheManager.orElseThrow(() -> new RuntimeException("Configure spring caching infrastructure please."));
		}
		CacheNameMapper[] cacheMappers = createCacheMappers();
		authorizationSetup =
			UAARestClientInterceptorFactory.createAuthorizationInterceptorWithCaching(uaaRestClientAutoconfigProperties.getRest(), restClientCacheManager,
				cacheMappers);
	}

	private CacheNameMapper[] createCacheMappers() {
		return uaaRestClientAutoconfigProperties.getRest().getCache().getMapping().stream()
			.map(mappingConfig -> new PathCacheNameMapper(mappingConfig.getCachePathPattern(), mappingConfig.getRegionPattern(), mappingConfig.getStaticName()))
			.toArray(size -> new PathCacheNameMapper[size]);
	}

	@Bean
	public ClientHttpRequestInterceptor authorizationInterceptor() {
		return authorizationSetup.getAuthorizationInterceptor();
	}

	@Bean
	public ClientHttpRequestInterceptor restCacheInterceptor() {
		return authorizationSetup.getCacheInterceptor();
	}

	@Bean
	public ClientHttpRequestInterceptor acceptInterceptor() {
		return UAARestClientInterceptorFactory.createAcceptHeaderInterceptor();
	}

	@Bean
	public UAAResponseErrorHandler uaaResponseErrorHandler() {
		return new UAAResponseErrorHandler();
	}

	@Bean
	public CacheService cacheService() {
		return authorizationSetup.getCacheService();
	}
}
