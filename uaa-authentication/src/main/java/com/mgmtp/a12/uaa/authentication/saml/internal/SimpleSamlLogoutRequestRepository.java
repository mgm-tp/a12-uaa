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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.saml2.provider.service.authentication.logout.Saml2LogoutRequest;
import org.springframework.util.Assert;

import com.mgmtp.a12.uaa.authentication.internal.AuthenticationTokenLocator;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenData;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenVerifier;
import com.mgmtp.a12.uaa.authentication.saml.LogoutRequestData;
import com.mgmtp.a12.uaa.authentication.saml.UaaSaml2LogoutRequestRepository;

public class SimpleSamlLogoutRequestRepository implements UaaSaml2LogoutRequestRepository, SamlRepositorySupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSamlLogoutRequestRepository.class);

	static Map<String, LogoutRequestData> storage = new HashMap<>();

	private AuthenticationTokenLocator jwtTokenLocator;
	private JwtTokenVerifier jwtTokenVerifier;

	public SimpleSamlLogoutRequestRepository(AuthenticationTokenLocator jwtTokenLocator, JwtTokenVerifier jwtTokenVerifier) {
		this.jwtTokenLocator = jwtTokenLocator;
		this.jwtTokenVerifier = jwtTokenVerifier;
	}

	@Override
	public Saml2LogoutRequest loadLogoutRequest(HttpServletRequest request) {
		String stateParameter = getStateValue(request);
		return Optional
			.ofNullable(storage.get(stateParameter))
			.map(data -> data.getLogoutRequest())
			.orElse(null);
	}

	@Override
	public void saveLogoutRequest(Saml2LogoutRequest logoutRequest, HttpServletRequest request, HttpServletResponse response) {
		String state = getStateValue(logoutRequest);
		Optional<String> token = jwtTokenLocator.locateToken(request);
		Assert.isTrue(token.isPresent(), "JWT token cannot be empty");
		Assert.hasText(state, "logoutRequest.state cannot be empty");
		JwtTokenData jwtTokenData = jwtTokenVerifier.unpackToken(token.get());

		LogoutRequestData data = new LogoutRequestData.Builder(logoutRequest, token.get())
			.withExpirationDate(jwtTokenData.getExpirationTime())
			.build();
		storage.put(state, data);
		LOGGER.info("SAML request [{}] has been stored", state);
	}

	@Override
	public Saml2LogoutRequest removeLogoutRequest(HttpServletRequest request, HttpServletResponse response) {
		return loadLogoutRequest(request);
	}

	@Override
	public Optional<LogoutRequestData> removeLogoutRequestAndGetToken(HttpServletRequest request, HttpServletResponse response) {
		String stateParameter = getStateValue(request);
		LogoutRequestData data = storage.get(stateParameter);
		if (data == null) {
			return Optional.empty();
		}
		delete(data);

		return Optional.of(data);
	}

	@Override
	public Collection<LogoutRequestData> loadAll() {
		return new ArrayList<>(storage.values());
	}

	@Override
	public void delete(LogoutRequestData data) {
		String key = getStateValue(data.getLogoutRequest());
		LOGGER.info("SAML request [{}] has been deleted", key);
		storage.remove(key);

	}
}
