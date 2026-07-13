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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;

import com.mgmtp.a12.connector.rest.ResponseErrorHandler;
import com.mgmtp.a12.connector.rest.RestDeleteConnector;
import com.mgmtp.a12.connector.rest.RestGetConnector;
import com.mgmtp.a12.connector.rest.RestHeadConnector;
import com.mgmtp.a12.connector.rest.RestOptionsConnector;
import com.mgmtp.a12.connector.rest.RestPostConnector;
import com.mgmtp.a12.connector.rest.RestPutConnector;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.RefreshTokenScheduler;
import com.mgmtp.a12.uaa.client.rest.cache.CacheNameMapper;
import com.mgmtp.a12.uaa.client.rest.cache.internal.PathCacheNameMapper;
import com.mgmtp.a12.uaa.client.rest.config.UAARestClientFactory;
import com.mgmtp.a12.uaa.client.rest.config.UAARestClientFactoryBuilder;

/**
 * Configuration for REST server connector. It's kept in separate class to avoid circular dependencies.
 */
@AutoConfigureBefore(name = "com.mgmtp.a12.connector.rest.autoconfigure.RestServerConnectorAutoConfiguration")
public class InfrastructureConfiguration {

	@Inject
	private UAARestClientAutoconfigProperties uaaRestClientAutoconfigProperties;
	@Inject
	private Optional<CacheManager> cacheManager;
	@Inject
	private Optional<List<ClientHttpRequestInterceptor>> interceptors;
	@Inject
	private Optional<List<ResponseErrorHandler>> errorHandlers;
	@Inject
	private Optional<List<HttpMessageConverter<?>>> messageConverters;

	private UAARestClientFactory uaaRestClientFactory;

	@PostConstruct
	void setUp() throws Exception {
		UAARestClientFactoryBuilder uaaRestClientFactoryBuilder = UAARestClientFactoryBuilder
			.withConfiguration(uaaRestClientAutoconfigProperties.getRest())
			.withCache(createCacheMappers())
			.withInterceptors(interceptors.orElseGet(Collections::emptyList).toArray(new ClientHttpRequestInterceptor[0]))
			.withErrorHandlers(errorHandlers.orElse(Collections.emptyList()).toArray(new ResponseErrorHandler[0]))
			.withMessageConverters(messageConverters.orElse(null));

		if (uaaRestClientAutoconfigProperties.getRest().getCache().isEnabled()) {
			uaaRestClientFactoryBuilder.withCacheManager(
				cacheManager.orElseThrow(() -> new RuntimeException("Configure spring caching infrastructure please.")));
		}
		uaaRestClientFactory = uaaRestClientFactoryBuilder.buildWithInjectedBeans();
	}

	private CacheNameMapper[] createCacheMappers() {
		return uaaRestClientAutoconfigProperties.getRest().getCache().getMapping().stream()
			.map(mappingConfig -> new PathCacheNameMapper(mappingConfig.getCachePathPattern(), mappingConfig.getRegionPattern(), mappingConfig.getStaticName()))
			.toArray(size -> new PathCacheNameMapper[size]);
	}

	@Bean
	public RestGetConnector restGetConnector() {
		return uaaRestClientFactory.getGetConnector();
	}

	@Bean
	public RestPostConnector restPostConnector() {
		return uaaRestClientFactory.getPostConnector();
	}

	@Bean
	public RestPutConnector getPutConnector() {
		return uaaRestClientFactory.getPutConnector();
	}

	@Bean
	public RestDeleteConnector getDeleteConnector() {
		return uaaRestClientFactory.getDeleteConnector();
	}

	@Bean
	public RestHeadConnector getHeadConnector() {
		return uaaRestClientFactory.getHeadConnector();
	}

	@Bean
	public RestOptionsConnector getOptionsConnector() {
		return uaaRestClientFactory.getOptionsConnector();
	}

	@Bean
	public ShutdownListener createShutdownListener() {
		return new ShutdownListener();
	}

	static class ShutdownListener {

		@EventListener
		public void contextClosed(@SuppressWarnings("unused") ContextClosedEvent event) {
			RefreshTokenScheduler.stopScheduler();
		}
	}

}
