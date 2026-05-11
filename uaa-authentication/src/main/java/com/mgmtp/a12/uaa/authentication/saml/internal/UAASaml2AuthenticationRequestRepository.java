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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.saml2.provider.service.authentication.AbstractSaml2AuthenticationRequest;
import org.springframework.security.saml2.provider.service.web.Saml2AuthenticationRequestRepository;

public interface UAASaml2AuthenticationRequestRepository
	extends Saml2AuthenticationRequestRepository<AbstractSaml2AuthenticationRequest>, SamlRepositorySupport {

	static Map<String, AuthenticationRequestData> storage = new HashMap<>();

	@Override
	default AbstractSaml2AuthenticationRequest loadAuthenticationRequest(HttpServletRequest request) {
		String relayState = getStateValue(request);
		return Optional.ofNullable(storage.get(relayState)).map(AuthenticationRequestData::getSamlAuthenticationRequest).orElse(null);
	}

	@Override
	default AbstractSaml2AuthenticationRequest removeAuthenticationRequest(HttpServletRequest request, HttpServletResponse response) {
		String relayState = getStateValue(request);
		return Optional.ofNullable(storage.remove(relayState)).map(AuthenticationRequestData::getSamlAuthenticationRequest).orElse(null);
	}

	@Override
	default void saveAuthenticationRequest(AbstractSaml2AuthenticationRequest authenticationRequest, HttpServletRequest request, HttpServletResponse response) {
		String relayState = getStateValue(authenticationRequest);
		AuthenticationRequestData requestData =
			AuthenticationRequestData.builder().withSamlauAuthenticationRequest(authenticationRequest).withCreationDate(Instant.now()).build();
		storage.put(relayState, requestData);
	}

	default Collection<AuthenticationRequestData> loadAll() {
		return new ArrayList<AuthenticationRequestData>(storage.values());
	}

	default void delete(AuthenticationRequestData data) {
		String relaySate = getStateValue(data.getSamlAuthenticationRequest());
		storage.remove(relaySate);
	}

}
