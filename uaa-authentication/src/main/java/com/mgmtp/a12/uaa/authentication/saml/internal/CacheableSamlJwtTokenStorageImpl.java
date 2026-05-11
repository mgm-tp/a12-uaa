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

import java.util.Optional;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import com.mgmtp.a12.uaa.authentication.internal.CacheStorageType;
import com.mgmtp.a12.uaa.authentication.internal.CacheUtils;
import com.mgmtp.a12.uaa.authentication.saml.SamlJwtTokenData;
import com.mgmtp.a12.uaa.authentication.saml.SamlJwtTokenStorage;

public class CacheableSamlJwtTokenStorageImpl implements SamlJwtTokenStorage {

	private SamlJwtTokenStorage samlJwtTokenStorage;

	public CacheableSamlJwtTokenStorageImpl(SamlJwtTokenStorage samlJwtTokenStorage, CacheManager cacheManager) {
		this.samlJwtTokenStorage = samlJwtTokenStorage;
		CacheUtils.verifyCaches(cacheManager, CacheStorageType.SAML_JWT_TOKEN);
	}

	@Override
	@CachePut(value = CacheStorageType.SAML_JWT_TOKEN, key = "#sessionId")
	public SamlJwtTokenData storeJwtToken(String sessionId, SamlJwtTokenData accessTokenData) {
		return samlJwtTokenStorage.storeJwtToken(sessionId, accessTokenData);
	}

	@Override
	@Cacheable(value = CacheStorageType.SAML_JWT_TOKEN, key = "#sessionId", unless = "#result == null")
	public Optional<SamlJwtTokenData> loadAccessTokenBySessionId(String sessionId) {
		return samlJwtTokenStorage.loadAccessTokenBySessionId(sessionId);
	}
	
	@Override
	@Cacheable(value = CacheStorageType.SAML_JWT_TOKEN, key = "#accessToken", unless = "#result == null")
	public Optional<SamlJwtTokenData> loadAccessToken(String accessToken) {
		return samlJwtTokenStorage.loadAccessToken(accessToken);
	}

	@Override
	@CacheEvict(value = CacheStorageType.SAML_JWT_TOKEN, key = "#sessionId", beforeInvocation = true)
	public void deleteAccessToken(String sessionId) {
		samlJwtTokenStorage.deleteAccessToken(sessionId);
	}

}
