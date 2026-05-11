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

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import com.mgmtp.a12.uaa.authentication.jwt.RenewTokenStorage;

public class RenewTokenCleaner {

	private static final Logger LOGGER = LoggerFactory.getLogger(RenewTokenCleaner.class);

	@Inject
	private RenewTokenStorage renewTokenStorage;

	@Inject
	private RenewTokenService renewTokenService;

	@Scheduled(cron = "0 0 1 * * *")
	public void cleanRenewTokenStorage() {
		long deletedCodeChallenges = renewTokenStorage.getCodeChallengeStored().keySet().stream()
			.filter(this::deleteCodeChallengeInvalid)
			.count();
		long deletedCodes = renewTokenStorage.getCodeStored().keySet().stream()
			.filter(this::deleteCodeInvalid)
			.count();
		long deletedTokenHints = renewTokenStorage.getTokenHintStored().keySet().stream()
			.filter(this::deleteTokenHintInvalid)
			.count();
		LOGGER.info("Renew token cleanup was done. {} codeChallenges, {} codes, {} tokenHints has been deleted",
			deletedCodeChallenges,
			deletedCodes,
			deletedTokenHints);
	}

	private boolean deleteCodeChallengeInvalid(String key) {
		boolean isInvalid = !renewTokenService.isCodeChallengeValid(key);
		if (isInvalid) {
			renewTokenStorage.removeCodeChallenge(key);
		}
		return isInvalid;
	}

	private boolean deleteCodeInvalid(String key) {
		boolean isInvalid = !renewTokenService.isCodeValid(key);
		if (isInvalid) {
			renewTokenStorage.removeCode(key);
		}
		return isInvalid;
	}

	private boolean deleteTokenHintInvalid(String key) {
		boolean isInvalid = !renewTokenService.isTokenHintValid(key);
		if (isInvalid) {
			renewTokenStorage.removeTokenHint(key);
		}
		return isInvalid;
	}
}
