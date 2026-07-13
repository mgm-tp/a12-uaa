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
package com.mgmtp.a12.uaa.client.rest.config;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.cache.CacheManager;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;

import com.mgmtp.a12.connector.rest.ResponseErrorHandler;
import com.mgmtp.a12.uaa.client.rest.cache.CacheNameMapper;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

public class UAARestClientFactoryBuilder {

	private UAARestClientProperties uaaRestClientProperties;
	private ClientHttpRequestInterceptor[] interceptors;
	private ResponseErrorHandler[] errorHandlers;
	private List<HttpMessageConverter<?>> messageConverters;
	private CacheNameMapper[] cacheNameMappers = new CacheNameMapper[0];
	private CacheManager cacheManager;
	private CloseableHttpClient httpClient;

	private UAARestClientFactoryBuilder(UAARestClientProperties uaaRestClientProperties) {
		this.uaaRestClientProperties = uaaRestClientProperties;
	}

	public static UAARestClientFactoryBuilder withConfiguration(UAARestClientProperties clientConfiguration) {
		return new UAARestClientFactoryBuilder(clientConfiguration);
	}

	public UAARestClientFactoryBuilder withInterceptors(ClientHttpRequestInterceptor... interceptors) {
		this.interceptors = interceptors;
		return this;
	}

	public UAARestClientFactoryBuilder withMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
		this.messageConverters = messageConverters;
		return this;
	}

	public UAARestClientFactoryBuilder withErrorHandlers(ResponseErrorHandler... errorHandlers) {
		this.errorHandlers = errorHandlers;
		return this;
	}

	public UAARestClientFactoryBuilder withCache(CacheNameMapper... cacheNameMappers) {
		this.cacheNameMappers = cacheNameMappers;
		return this;
	}

	public UAARestClientFactoryBuilder withCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
		return this;
	}

	public UAARestClientFactoryBuilder withHttpClient(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
		return this;
	}

	public UAARestClientFactory build()
		throws GeneralSecurityException, IOException {
		UAARestClientProperties finalConfiguration = UAARestClientPropertiesResolver.resolve(uaaRestClientProperties);
		return new UAARestClientFactory(finalConfiguration, httpClient, interceptors, errorHandlers, messageConverters, cacheManager, cacheNameMappers);
	}

	public UAARestClientFactory buildWithInjectedBeans()
		throws GeneralSecurityException, IOException {
		UAARestClientProperties finalConfiguration = UAARestClientPropertiesResolver.resolve(uaaRestClientProperties);
		return new UAARestClientFactory(finalConfiguration, httpClient, interceptors, messageConverters, errorHandlers);
	}

}
