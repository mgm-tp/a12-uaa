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

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SamlJwtTokenStorageTest {

	private SamlJwtTokenStorage samlJwtTokenStorage;

	private String SESSION_ID = "sessionID-01";

	private String TOKEN = "tokenValue";

	@BeforeEach
	void setUp() {
		samlJwtTokenStorage = new SamlJwtTokenStorage() {
		};
		SamlJwtTokenData data =
			new SamlJwtTokenData.Builder(TOKEN).withSessionId(SESSION_ID).withExpirationTime(Instant.now().plus(Duration.ofSeconds(5))).build();
		samlJwtTokenStorage.storeJwtToken(SESSION_ID, data);
	}

	@Test
	public void storeToken() {
		checkData(TOKEN, SESSION_ID);
	}

	@Test
	public void storeTokenUpdate() {
		String tokenNew = "tokenValue-NEW";
		SamlJwtTokenData dataNew =
			new SamlJwtTokenData.Builder(tokenNew).withSessionId(SESSION_ID).withExpirationTime(Instant.now().plus(Duration.ofSeconds(5))).build();
		samlJwtTokenStorage.storeJwtToken(SESSION_ID, dataNew);
		Optional<SamlJwtTokenData> dataByOldToken = samlJwtTokenStorage.loadAccessToken(TOKEN);
		Assertions.assertTrue(dataByOldToken.isEmpty(), "Old token is not deleted from token map");
		checkData(tokenNew, SESSION_ID);
	}

	@Test
	public void tokenDelete() {
		samlJwtTokenStorage.deleteAccessToken(SESSION_ID);
		Optional<SamlJwtTokenData> dataBySession = samlJwtTokenStorage.loadAccessTokenBySessionId(SESSION_ID);
		Optional<SamlJwtTokenData> dataByToken = samlJwtTokenStorage.loadAccessToken(TOKEN);
		Assertions.assertTrue(dataBySession.isEmpty());
		Assertions.assertTrue(dataByToken.isEmpty());
	}

	@Test
	public void checkLoadAll() {
		String tokenNew = "tokenValue-NEW";
		String sessionNew = "session-NEW";
		addToStorage(sessionNew, tokenNew);
		samlJwtTokenStorage.loadAll().stream()
			.forEach(data -> {
				checkData(data.getAccessToken(), data.getSessionId());
			});

	}

	private void addToStorage(String sessionId, String token) {
		SamlJwtTokenData data =
			new SamlJwtTokenData.Builder(token).withSessionId(sessionId).withExpirationTime(Instant.now().plus(Duration.ofSeconds(5))).build();
		samlJwtTokenStorage.storeJwtToken(sessionId, data);
	}

	private void checkData(String token, String sessionId) {
		Optional<SamlJwtTokenData> dataBySession = samlJwtTokenStorage.loadAccessTokenBySessionId(sessionId);
		Optional<SamlJwtTokenData> dataByToken = samlJwtTokenStorage.loadAccessToken(token);
		Assertions.assertEquals(dataBySession.get().getSessionId(), sessionId);
		Assertions.assertEquals(dataByToken.get().getSessionId(), sessionId);
		Assertions.assertEquals(dataBySession.get().getAccessToken(), token);
		Assertions.assertEquals(dataByToken.get().getAccessToken(), token);
		Assertions.assertTrue(dataByToken.get() == dataBySession.get());

	}

}
