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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import jakarta.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.a12.uaa.authentication.jwt.RenewTokenStorage;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenVerifier;
import com.mgmtp.a12.uaa.authentication.saml.AuthorizationCodeStorage;

public class SamlTokenExchangeService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SamlTokenExchangeService.class);
	private static final Integer CODE_CHALLENGE_EXPIRATION_SECONDS = 15;

	@Inject
	private JwtTokenVerifier jwtTokenVerifier;

	@Inject
	private RenewTokenStorage renewTokenStorage;

	@Inject
	private AuthorizationCodeStorage samlAuthorizationCodeStorage;

	public void authorize(String codeChallenge) {
		String expiration = String.valueOf(calculateExpirationTime());
		renewTokenStorage.storeCodeChallenge(codeChallenge, expiration);
	}

	public Optional<String> verifyAndClaimToken(String codeVerifier, String authCode) {
		String codeChallenge = generateCodeChallenge(codeVerifier);
		Optional<String> jwtToken = samlAuthorizationCodeStorage.loadAccessTokenByAuthorizationCode(authCode);
		boolean isCodeChallengeValid = isCodeChallengeValid(codeChallenge);
		boolean isTokenPresent = jwtToken.isPresent();
		boolean isTokenValid = jwtToken.map(token -> jwtTokenVerifier.isTokenValid(token)).orElse(false);
		boolean isValid = isCodeChallengeValid && isTokenPresent && isTokenValid;
		renewTokenStorage.removeCodeChallenge(codeChallenge);
		if (!isValid) {
			LOGGER.warn("Cannot claim token: [isCodeChallengeValid: %s, isTokenPresent: %s, isTokenValid: %s]"
				.formatted(isCodeChallengeValid, isTokenPresent, isTokenValid));
		}
		return isValid ? jwtToken : Optional.empty();
	}

	private Long calculateExpirationTime() {
		Instant now = Instant.now();
		return now.plus(Duration.ofSeconds(CODE_CHALLENGE_EXPIRATION_SECONDS)).toEpochMilli();
	}

	public Boolean isCodeChallengeValid(String codeChallenge) {
		Optional<String> loadValueByCodeChallenge = renewTokenStorage.loadCodeChallenge(codeChallenge);
		return loadValueByCodeChallenge.isPresent() && Instant.ofEpochMilli(Long.parseLong(loadValueByCodeChallenge.get())).isAfter(Instant.now());
	}

	private String generateCodeChallenge(String codeVerifier) {
		String codeChallenge = null;
		try {
			byte[] bytes = codeVerifier.getBytes(StandardCharsets.US_ASCII);
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(bytes, 0, bytes.length);
			byte[] digest = md.digest();
			codeChallenge = Base64.encodeBase64URLSafeString(digest);
		} catch (NoSuchAlgorithmException e) {
			LOGGER.debug("Code challenge generating failed.");
		}
		return codeChallenge;
	}
}
