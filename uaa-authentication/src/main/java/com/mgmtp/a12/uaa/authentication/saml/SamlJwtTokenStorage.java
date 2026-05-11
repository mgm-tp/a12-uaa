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

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public interface SamlJwtTokenStorage {

	Map<String, SamlJwtTokenData> samlSessionTokenMap = new ConcurrentHashMap<>();
	Map<String, SamlJwtTokenData> samlTokenMap = new ConcurrentHashMap<>();

	default SamlJwtTokenData storeJwtToken(String sessionId, SamlJwtTokenData accessTokenData) {
		removeTokenWhenExists(sessionId);

		samlSessionTokenMap.put(sessionId, accessTokenData);
		samlTokenMap.put(accessTokenData.getAccessToken(), accessTokenData);
		return accessTokenData;
	}

	default Optional<SamlJwtTokenData> loadAccessTokenBySessionId(String sessionId) {
		return Optional.ofNullable(samlSessionTokenMap.get(sessionId));
	}

	default Optional<SamlJwtTokenData> loadAccessToken(String accessToken) {
		return Optional.ofNullable(samlTokenMap.get(accessToken));
	}

	default Collection<SamlJwtTokenData> loadAll() {
		return samlSessionTokenMap.values();
	}

	default void deleteAccessToken(String sessionId) {
		removeTokenWhenExists(sessionId);
		samlSessionTokenMap.remove(sessionId);
	}

	private void removeTokenWhenExists(String sessionId) {
		Optional.ofNullable(samlSessionTokenMap.get(sessionId))
			.ifPresent(data -> samlTokenMap.remove(data.getAccessToken()));
	}
}
