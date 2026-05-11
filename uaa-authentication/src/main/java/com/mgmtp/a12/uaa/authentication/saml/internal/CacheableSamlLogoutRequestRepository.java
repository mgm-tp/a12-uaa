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

import java.util.Collection;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.saml2.provider.service.authentication.logout.Saml2LogoutRequest;

import com.mgmtp.a12.uaa.authentication.internal.CacheStorageType;
import com.mgmtp.a12.uaa.authentication.internal.CacheUtils;
import com.mgmtp.a12.uaa.authentication.saml.LogoutRequestData;
import com.mgmtp.a12.uaa.authentication.saml.UaaSaml2LogoutRequestRepository;

public class CacheableSamlLogoutRequestRepository implements UaaSaml2LogoutRequestRepository, SamlRepositorySupport {

	private UaaSaml2LogoutRequestRepository samlRequestRepository;
	private CacheManager cacheManager;

	public CacheableSamlLogoutRequestRepository(UaaSaml2LogoutRequestRepository samlRequestRepository, CacheManager cacheManager) {
		this.samlRequestRepository = samlRequestRepository;
		this.cacheManager = cacheManager;
		CacheUtils.verifyCaches(cacheManager, CacheStorageType.SAML_LOGOUT_REQUEST);
	}

	@Override
	@Cacheable(value = CacheStorageType.SAML_LOGOUT_REQUEST, key = "#root.target.getStateValue(#request)", unless = "#result == null")
	public Saml2LogoutRequest loadLogoutRequest(HttpServletRequest request) {
		return samlRequestRepository.loadLogoutRequest(request);
	}

	@Override
	public void saveLogoutRequest(Saml2LogoutRequest logoutRequest, HttpServletRequest request, HttpServletResponse response) {
		Cache requestCache = cacheManager.getCache(CacheStorageType.SAML_LOGOUT_REQUEST);
		requestCache.put(getStateValue(logoutRequest), logoutRequest);
		samlRequestRepository.saveLogoutRequest(logoutRequest, request, response);
	}

	@Override
	@Cacheable(value = CacheStorageType.SAML_LOGOUT_REQUEST, key = "#root.target.getStateValue(#request)", unless = "#result == null")
	public Saml2LogoutRequest removeLogoutRequest(HttpServletRequest request, HttpServletResponse response) {
		//entry is removed in success handler at the end of the process see #removeLogoutRequestAndGetToken
		return samlRequestRepository.removeLogoutRequest(request, response);
	}

	@Override
	@CacheEvict(value = CacheStorageType.SAML_LOGOUT_REQUEST,
		key = "#root.target.getStateValue(#request)", condition = "#root.target.getStateValue(#request) != null")
	public Optional<LogoutRequestData> removeLogoutRequestAndGetToken(HttpServletRequest request, HttpServletResponse response) {
		return samlRequestRepository.removeLogoutRequestAndGetToken(request, response);
	}

	@Override
	public Collection<LogoutRequestData> loadAll() {
		return samlRequestRepository.loadAll();
	}

	@CacheEvict(value = CacheStorageType.SAML_LOGOUT_REQUEST, key = "#root.target.getStateValue(#data.logoutRequest)")
	@Override
	public void delete(LogoutRequestData data) {
		samlRequestRepository.delete(data);
	}
}
