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
package com.mgmtp.a12.uaa.authentication.jwt.internal.renew;

import java.util.Map;
import java.util.Optional;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import com.mgmtp.a12.uaa.authentication.internal.CacheStorageType;
import com.mgmtp.a12.uaa.authentication.jwt.RenewTokenStorage;

public class CacheableRenewTokenStorage implements RenewTokenStorage {

	private RenewTokenStorage renewTokenStorage;

	public CacheableRenewTokenStorage(RenewTokenStorage renewTokenStorage, CacheManager cacheManager) {
		this.renewTokenStorage = renewTokenStorage;
		verifyCache(cacheManager, CacheStorageType.EXCHANGE_CODE_CHALLENGE);
		verifyCache(cacheManager, CacheStorageType.EXCHANGE_CODE);
		verifyCache(cacheManager, CacheStorageType.EXCHANGE_TOKEN_HINT);
	}

	private void verifyCache(CacheManager cacheManager, String cacheName) {
		Cache cache = cacheManager.getCache(cacheName);
		Optional.ofNullable(cache).orElseThrow(() -> new RuntimeException("Please define cache with name [%s].".formatted(cacheName)));
	}

	@CachePut(value = CacheStorageType.EXCHANGE_CODE_CHALLENGE, key = "#codeChallenge")
	@Override
	public String storeCodeChallenge(String codeChallenge, String expiration) {
		return renewTokenStorage.storeCodeChallenge(codeChallenge, expiration);
	}

	@Cacheable(value = CacheStorageType.EXCHANGE_CODE_CHALLENGE, key = "#codeChallenge", unless = "#result == null")
	@Override
	public Optional<String> loadCodeChallenge(String codeChallenge) {
		return renewTokenStorage.loadCodeChallenge(codeChallenge);
	}

	@CacheEvict(value = CacheStorageType.EXCHANGE_CODE_CHALLENGE, key = "#codeChallenge", beforeInvocation = true)
	@Override
	public void removeCodeChallenge(String codeChallenge) {
		renewTokenStorage.removeCodeChallenge(codeChallenge);
	}

	@Override
	public Map<String, String> getCodeChallengeStored() {
		return renewTokenStorage.getCodeChallengeStored();
	}

	@CachePut(value = CacheStorageType.EXCHANGE_CODE, key = "#code")
	@Override
	public String storeCode(String code, String expiration) {
		return renewTokenStorage.storeCode(code, expiration);
	}

	@Cacheable(value = CacheStorageType.EXCHANGE_CODE, key = "#code", unless = "#result == null")
	@Override
	public Optional<String> loadCode(String code) {
		return renewTokenStorage.loadCode(code);
	}

	@CacheEvict(value = CacheStorageType.EXCHANGE_CODE, key = "#code", beforeInvocation = true)
	@Override
	public void removeCode(String code) {
		renewTokenStorage.removeCode(code);
	}

	@Override
	public Map<String, String> getCodeStored() {
		return renewTokenStorage.getCodeStored();
	}

	@CachePut(value = CacheStorageType.EXCHANGE_TOKEN_HINT, key = "#code")
	@Override
	public String storeTokenHint(String code, String tokenHint) {
		return renewTokenStorage.storeTokenHint(code, tokenHint);
	}

	@Cacheable(value = CacheStorageType.EXCHANGE_TOKEN_HINT, key = "#code", unless = "#result == null")
	@Override
	public Optional<String> loadTokenHintByCode(String code) {
		return renewTokenStorage.loadTokenHintByCode(code);
	}

	@CacheEvict(value = CacheStorageType.EXCHANGE_TOKEN_HINT, key = "#code", beforeInvocation = true)
	@Override
	public void removeTokenHint(String code) {
		renewTokenStorage.removeTokenHint(code);
	}

	@Override
	public Map<String, String> getTokenHintStored() {
		return renewTokenStorage.getTokenHintStored();
	}

}
