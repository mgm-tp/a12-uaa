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
import java.util.concurrent.ConcurrentHashMap;

import com.mgmtp.a12.uaa.authentication.jwt.RenewTokenStorage;

public class SimpleRenewTokenStorage implements RenewTokenStorage {

	private final Map<String, String> codeChallengeStored = new ConcurrentHashMap<>();
	private final Map<String, String> codeStored = new ConcurrentHashMap<>();
	private final Map<String, String> tokenHintStored = new ConcurrentHashMap<>();

	@Override
	public String storeCodeChallenge(String codeChallenge, String expiration) {
		codeChallengeStored.put(codeChallenge, expiration);
		return expiration;
	}

	@Override
	public Optional<String> loadCodeChallenge(String codeChallenge) {
		return Optional.ofNullable(codeChallengeStored.get(codeChallenge));
	}

	@Override
	public Map<String, String> getCodeChallengeStored() {
		return codeChallengeStored;
	}

	@Override
	public void removeCodeChallenge(String codeChallenge) {
		codeChallengeStored.remove(codeChallenge);
	}

	@Override
	public String storeCode(String code, String expiration) {
		codeStored.put(code, expiration);
		return expiration;
	}

	@Override
	public Optional<String> loadCode(String code) {
		return Optional.ofNullable(codeStored.get(code));
	}

	@Override
	public void removeCode(String code) {
		codeStored.remove(code);
	}

	@Override
	public Map<String, String> getCodeStored() {
		return codeStored;
	}

	@Override
	public String storeTokenHint(String code, String tokenHint) {
		tokenHintStored.put(code, tokenHint);
		return tokenHint;
	}

	@Override
	public Optional<String> loadTokenHintByCode(String code) {
		return Optional.ofNullable(tokenHintStored.get(code));
	}

	@Override
	public void removeTokenHint(String code) {
		tokenHintStored.remove(code);
	}

	@Override
	public Map<String, String> getTokenHintStored() {
		return tokenHintStored;
	}

}
