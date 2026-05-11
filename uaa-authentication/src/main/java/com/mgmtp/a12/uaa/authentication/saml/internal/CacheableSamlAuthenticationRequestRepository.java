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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.saml2.provider.service.authentication.AbstractSaml2AuthenticationRequest;

import com.mgmtp.a12.uaa.authentication.internal.CacheStorageType;
import com.mgmtp.a12.uaa.authentication.internal.CacheUtils;

public class CacheableSamlAuthenticationRequestRepository implements UAASaml2AuthenticationRequestRepository {

	private UAASaml2AuthenticationRequestRepository samlRequestRepository;
	private CacheManager cacheManager;

	public CacheableSamlAuthenticationRequestRepository(UAASaml2AuthenticationRequestRepository samlrequestRepository, CacheManager cacheManager) {
		this.samlRequestRepository = samlrequestRepository;
		this.cacheManager = cacheManager;
		CacheUtils.verifyCaches(cacheManager, CacheStorageType.SAML_AUTHENTICATION_REQUEST);
	}

	@Override
	@Cacheable(value = CacheStorageType.SAML_AUTHENTICATION_REQUEST, key = "#root.target.getStateValue(#request)",
		condition = "#root.target.getStateValue(#request) != null", unless = "#result == null")
	public AbstractSaml2AuthenticationRequest loadAuthenticationRequest(HttpServletRequest request) {
		return samlRequestRepository.loadAuthenticationRequest(request);
	}

	@Override
	@Cacheable(value = CacheStorageType.SAML_AUTHENTICATION_REQUEST, key = "#root.target.getStateValue(#authenticationRequest)",
		condition = "#root.target.getStateValue(#authenticationRequest) != null", unless = "#result == null")
	public void saveAuthenticationRequest(AbstractSaml2AuthenticationRequest authenticationRequest, HttpServletRequest request, HttpServletResponse response) {
		samlRequestRepository.saveAuthenticationRequest(authenticationRequest, request, response);
		Cache cache = cacheManager.getCache(CacheStorageType.SAML_AUTHENTICATION_REQUEST);
		String stateValue = getStateValue(authenticationRequest);
		if (stateValue != null) {
			cache.put(stateValue, authenticationRequest);
		}
	}

	@Override
	@Cacheable(value = CacheStorageType.SAML_AUTHENTICATION_REQUEST, key = "#root.target.getStateValue(#request)",
		condition = "#root.target.getStateValue(#request) != null", unless = "#result == null")
	@CacheEvict(value = CacheStorageType.SAML_AUTHENTICATION_REQUEST, key = "#root.target.getStateValue(#request)",
		condition = "#root.target.getStateValue(#request) != null")
	public AbstractSaml2AuthenticationRequest removeAuthenticationRequest(HttpServletRequest request, HttpServletResponse response) {
		return samlRequestRepository.removeAuthenticationRequest(request, response);
	}

	@Override
	@CacheEvict(value = CacheStorageType.SAML_AUTHENTICATION_REQUEST, key = "#root.target.getStateValue(#data.samlAuthenticationRequest)",
		condition = "#root.target.getStateValue(#data.samlAuthenticationRequest) != null")
	public void delete(AuthenticationRequestData data) {
		samlRequestRepository.delete(data);
	}

}
