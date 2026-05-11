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
package com.mgmtp.a12.uaa.authentication.saml;

import java.util.Optional;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mgmtp.a12.uaa.authentication.internal.CacheStorageType;
import com.mgmtp.a12.uaa.authentication.saml.internal.CacheableAuthorizationCodeStorageImpl;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SamlCacheablesCodeStorageImplTest {

	@Inject
	AuthorizationCodeStorage cacheablesAuthorizationCodeStorage;

	@Inject
	CacheManager cacheManager;

	@Test
	@Order(1)
	public void storeAuthorizationCodeTest() {
		cacheablesAuthorizationCodeStorage.storeAuthorizationCode("12345", "123456AAA");
		Cache cache = cacheManager.getCache(CacheStorageType.SAML_AUTHORIZATION_CODE);
		Cache.ValueWrapper valueWrapper = cache.get("12345");
		Assertions.assertEquals("123456AAA", valueWrapper.get());
	}

	@Test
	@Order(2)
	public void loadAccessTokenByAuthorizationCodeTest() {
		cacheablesAuthorizationCodeStorage.storeAuthorizationCode("789", "123456BBB");
		Cache cache = cacheManager.getCache(CacheStorageType.SAML_AUTHORIZATION_CODE);
		cache.evictIfPresent("789");
		Cache.ValueWrapper valueWrapper = cache.get("789");
		Assertions.assertNull(valueWrapper);
		final Optional<String> jwtToken = cacheablesAuthorizationCodeStorage.loadAccessTokenByAuthorizationCode("789");
		valueWrapper = cache.get("789");
		Assertions.assertNotNull(valueWrapper);
		Assertions.assertEquals("123456BBB", jwtToken.get());
	}

	@Test
	@Order(3)
	public void loadAllTest() {
		long itemCount = cacheablesAuthorizationCodeStorage
			.loadAll()
			.stream()
			.filter(authorizationCodeData -> "789".equals(authorizationCodeData.getAuthorizationCode())
				|| "12345".equals(authorizationCodeData.getAuthorizationCode()))
			.count();
		Assertions.assertEquals(2, itemCount);
	}

	@Test
	public void deleteTest() {
		cacheablesAuthorizationCodeStorage.storeAuthorizationCode("456", "123456CCC");
		cacheablesAuthorizationCodeStorage.deleteAuthorizationCode("456");
		Cache cache = cacheManager.getCache(CacheStorageType.SAML_AUTHORIZATION_CODE);
		Cache.ValueWrapper valueWrapper = cache.get("456");
		Assertions.assertNull(valueWrapper);
		Optional<String> rtnValue = cacheablesAuthorizationCodeStorage.loadAccessTokenByAuthorizationCode("456");
		Assertions.assertFalse(rtnValue.isPresent());

	}

	@Configuration
	@EnableCaching
	static class TestConfig {
		@Bean
		public AuthorizationCodeStorage createAuthorizationCodeStorage() {
			return new CacheableAuthorizationCodeStorageImpl(new AuthorizationCodeStorage() {
			}, cacheManager());
		}

		@Bean
		public CacheManager cacheManager() {
			return new ConcurrentMapCacheManager(CacheStorageType.SAML_AUTHORIZATION_CODE);
		}
	}
}
