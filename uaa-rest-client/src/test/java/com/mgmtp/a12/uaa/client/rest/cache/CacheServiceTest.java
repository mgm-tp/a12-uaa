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
package com.mgmtp.a12.uaa.client.rest.cache;

import java.net.URI;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.mock.http.client.MockClientHttpRequest;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.TokenType;
import com.mgmtp.a12.uaa.client.rest.cache.internal.CacheService;
import com.mgmtp.a12.uaa.client.rest.cache.internal.CachedClientData;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CacheServiceTest {
	
	@Mock
	private AuthorizationDataStore authorizationDataStore;
	@Mock
	private CacheManager cacheManager;
	@Mock
	private CacheNameMapper cacheNameMapper;
	@Mock
	private Cache cache;
	@Mock
	private URI uri;
	@Captor
	private ArgumentCaptor<String> keyCaptor;
	
	private CacheService cacheService;
	
	@BeforeEach
	void setUp() {
		cacheService = new CacheService(authorizationDataStore, cacheManager, Arrays.asList(cacheNameMapper));
		AuthorizationData authData = new AuthorizationData("token", TokenType.UAABEARER, "session", "userID", 300);
		Mockito.when(authorizationDataStore.getAuthorizationData()).thenReturn(authData);
		Mockito.when(cacheNameMapper.computeCacheName(Mockito.any())).thenReturn("cacheRegion");
		Mockito.when(cacheNameMapper.match(Mockito.any())).thenReturn(true);
		Mockito.when(cacheManager.getCache(Mockito.any())).thenReturn(cache);
		Mockito.when(uri.getPath()).thenReturn("path");
		Mockito.when(uri.getQuery()).thenReturn("query");
	}
	
	@Test
	public void checkCachePut() {
		MockClientHttpRequest httpRequest = new MockClientHttpRequest();
		httpRequest.setURI(uri);
		CachedClientData cachedData = CachedClientData.builder()
			.withCachedContent(new byte[2])
			.build();
		cacheService.put(httpRequest, cachedData);
		
		Mockito.verify(cacheManager, Mockito.times(1)).getCache(ArgumentMatchers.matches("cacheRegion"));
		Mockito.verify(cache, Mockito.times(1)).putIfAbsent(ArgumentMatchers.matches("userID::path\\?query"), ArgumentMatchers.same(cachedData));
		
	}
	
	@Test
	public void checkCacheGet() {
		Mockito.when(uri.getQuery()).thenReturn("");
		MockClientHttpRequest httpRequest = new MockClientHttpRequest();
		httpRequest.setURI(uri);

		cacheService.get(httpRequest);
		
		Mockito.verify(cacheManager, Mockito.times(1)).getCache(ArgumentMatchers.matches("cacheRegion"));
		Mockito.verify(cache, Mockito.times(1)).get(ArgumentMatchers.matches("userID::path"), ArgumentMatchers.same(CachedClientData.class));
	}
	
	@Test
	public void checkCacheFlush() {
		Mockito.when(cacheNameMapper.computeCacheName(Mockito.any())).thenReturn("path");
		cacheService.flush("path");

		Mockito.verify(cacheManager, Mockito.times(1)).getCache(ArgumentMatchers.matches("path"));
		Mockito.verify(cache, Mockito.times(1)).invalidate();
	}

}
