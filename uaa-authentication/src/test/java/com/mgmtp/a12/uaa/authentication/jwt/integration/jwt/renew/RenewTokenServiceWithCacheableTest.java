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
package com.mgmtp.a12.uaa.authentication.jwt.integration.jwt.renew;

import java.time.Instant;
import java.util.Optional;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mgmtp.a12.uaa.authentication.internal.CacheStorageType;
import com.mgmtp.a12.uaa.authentication.jwt.RenewTokenStorage;
import com.mgmtp.a12.uaa.authentication.jwt.internal.renew.CacheableRenewTokenStorage;
import com.mgmtp.a12.uaa.authentication.jwt.internal.renew.SimpleRenewTokenStorage;

@ExtendWith(SpringExtension.class)
public class RenewTokenServiceWithCacheableTest extends RenewTokenServiceTest {

	private static final Integer DEFAULT_RENEWAL_SECONDS = 15;

	@Inject
	CacheManager cacheManager;

	String expiration = null;
	String codeVerifier = null;
	String codeChallenge = null;
	String code = null;
	String idTokenHint = null;

	@BeforeEach
	public void initBeforeEach() {
		expiration = calculateExpirationTime();
		codeVerifier = generateCodeVerifier();
		codeChallenge = generateCodeChallenge(codeVerifier);
		code = generateCode();
		idTokenHint = generateToken().getToken();

		renewTokenStorage.storeCodeChallenge(codeChallenge, expiration);
		renewTokenStorage.storeCode(code, expiration);
		renewTokenStorage.storeTokenHint(code, idTokenHint);
	}

	@Test
	public void testStore() {
		verifyStoreCache(CacheStorageType.EXCHANGE_CODE_CHALLENGE, codeChallenge, expiration);
		verifyStoreCache(CacheStorageType.EXCHANGE_CODE, code, expiration);
		verifyStoreCache(CacheStorageType.EXCHANGE_TOKEN_HINT, code, idTokenHint);
	}

	@Test
	public void testRead() {
		Optional<String> codeChallengeOptional = renewTokenStorage.loadCodeChallenge(codeChallenge);
		Optional<String> codeOptional = renewTokenStorage.loadCode(code);
		Optional<String> tokenHintOptional = renewTokenStorage.loadTokenHintByCode(code);

		Assertions.assertEquals(expiration, codeChallengeOptional.get());
		Assertions.assertEquals(expiration, codeOptional.get());
		Assertions.assertEquals(idTokenHint, tokenHintOptional.get());
	}

	@Test
	public void testRemove() {
		renewTokenStorage.removeCodeChallenge(codeChallenge);
		renewTokenStorage.removeCode(code);
		renewTokenStorage.removeTokenHint(code);
		verifyRemoveCache(CacheStorageType.EXCHANGE_CODE_CHALLENGE, codeChallenge);
		verifyRemoveCache(CacheStorageType.EXCHANGE_CODE, code);
		verifyRemoveCache(CacheStorageType.EXCHANGE_TOKEN_HINT, code);
		Assertions.assertFalse(renewTokenStorage.loadCodeChallenge(codeChallenge).isPresent());
		Assertions.assertFalse(renewTokenStorage.loadCodeChallenge(code).isPresent());
		Assertions.assertFalse(renewTokenStorage.loadTokenHintByCode(code).isPresent());
	}

	protected String generateCode() {
		return ReflectionTestUtils.invokeMethod(renewTokenService, "generateCode");
	}

	protected String calculateExpirationTime() {
		Instant expiration = Instant.now().plusSeconds(DEFAULT_RENEWAL_SECONDS);
		return String.valueOf(expiration.toEpochMilli());
	}

	protected void verifyStoreCache(String cacheName, String key, String value) {
		Cache cache = cacheManager.getCache(cacheName);
		Cache.ValueWrapper valueWrapper = cache.get(key);
		Assertions.assertEquals(value, valueWrapper.get());
	}

	protected void verifyRemoveCache(String cacheName, String key) {
		Cache cache = cacheManager.getCache(cacheName);
		Cache.ValueWrapper valueWrapper = cache.get(key);
		Assertions.assertNull(valueWrapper);
	}

	@Configuration
	@EnableCaching
	static class TestConfig extends RenewTokenServiceTest.TestConfig {

		@Bean
		@Primary
		public RenewTokenStorage createRenewTokenStorage() {
			return new CacheableRenewTokenStorage(new SimpleRenewTokenStorage(), cacheManager());
		}

		@Bean
		public CacheManager cacheManager() {
			return new ConcurrentMapCacheManager(CacheStorageType.EXCHANGE_CODE_CHALLENGE, CacheStorageType.EXCHANGE_CODE,
				CacheStorageType.EXCHANGE_TOKEN_HINT);
		}

	}

}