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
package com.mgmtp.a12.uaa.client.rest.cache.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.cache.CacheNameMapper;

public class CacheService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CacheService.class);

	private CacheManager cacheManager;
	private List<CacheNameMapper> cacheNameMappers;
	private AuthorizationDataStore authorizationDataStore;

	public CacheService(AuthorizationDataStore authorizationDataStore, CacheManager cacheManager, List<CacheNameMapper> cacheNameMappers) {
		this.cacheManager = cacheManager;
		this.cacheNameMappers = cacheNameMappers;
		this.authorizationDataStore = authorizationDataStore;
	}

	public void put(HttpRequest request, CachedClientData cacheClientData) {
		Optional<Cache> cache = getCache(request);
		cache.ifPresent(cacheRegion -> {
			Optional<String> keyOptional = calculateCacheKey(request);
			keyOptional.ifPresent(key -> {
				cacheRegion.putIfAbsent(key, cacheClientData);
				LOGGER.debug("Cache PUT for cache [{}] under key=[{}], [{}]", cacheRegion.getName(), key, cacheClientData);
			});
		});
	}

	public Optional<CachedClientData> get(HttpRequest request) {
		Optional<Cache> cache = getCache(request);
		return cache.flatMap(cacheRegion -> {
			Optional<String> keyOptional = calculateCacheKey(request);
			return keyOptional.map(key -> {
				CachedClientData cachedData = cacheRegion.get(key, CachedClientData.class);
				if (cachedData != null) {
					LOGGER.debug("Cache HIT for cache [{}] under key=[{}], [{}]", cacheRegion.getName(), key, cachedData);
				} else {
					LOGGER.debug("Cache MISS for cache [{}] under key=[{}]", cacheRegion.getName(), key);
				}
				return cachedData;
			});
		});
	}

	public void flush(String endpointPath) {

		HttpRequest request = new HttpRequest() {

			@Override
			public HttpHeaders getHeaders() {
				return null;
			}

			@Override
			public URI getURI() {
				try {
					return new URI(endpointPath);
				} catch (URISyntaxException e) {
					throw new RuntimeException("Unable to convert endpoint path [%s] to URI".formatted(endpointPath), e);
				}
			}

			@Override
			public Map<String, Object> getAttributes() {
				return null;
			}

			@Override
			public HttpMethod getMethod() {
				return null;
			}
		};
		Optional<Cache> cache = getCache(request);
		cache.ifPresent(cacheRegion -> {
			cacheRegion.invalidate();
			LOGGER.debug("Rest client FLUSH cache named [%s]".formatted(cacheRegion.getName()));
		});
	}

	private Optional<String> calculateCacheKey(HttpRequest request) {
		URI uri = request.getURI();
		String queryString = Optional.ofNullable(uri.getQuery())
			.map(query -> "?" + query)
			.orElse("");

		AuthorizationData authorizationData = authorizationDataStore.getAuthorizationData();
		if (authorizationData == null) {
			LOGGER.debug("Skip caching - no AuthorizationData available for request [{}]", uri);
			return Optional.empty();
		}
		String userIdentification = Optional.ofNullable(authorizationData.getUniqueUserIdentification())
			.orElse(authorizationData.getAuthenticationToken());

		return Optional.of("%s::%s%s".formatted(userIdentification, uri.getPath(), queryString));
	}

	private Optional<String> calculateCacheName(HttpRequest request) {
		return cacheNameMappers.stream()
			.filter(mapper -> mapper.match(request))
			.findFirst()
			.map(mapper -> mapper.computeCacheName(request));

	}

	private Optional<Cache> getCache(HttpRequest request) {
		Optional<String> cacheName = calculateCacheName(request);
		return cacheName
			.map(cacheManager::getCache);
	}

}
