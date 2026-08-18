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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;

import jakarta.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenData;
import com.mgmtp.a12.uaa.authentication.jwt.RenewTokenStorage;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenGenerator;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenVerifier;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalCreator;
import com.mgmtp.a12.uaa.authentication.saml.SamlJwtTokenData;
import com.mgmtp.a12.uaa.authentication.saml.SamlJwtTokenStorage;

public class RenewTokenService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RenewTokenService.class);
	private static final Integer CODE_CHARACTER_LENGTH = 128;

	@Inject
	private JwtTokenGenerator jwtTokenGenerator;

	@Inject
	private JwtTokenVerifier jwtTokenVerifier;

	@Inject
	private PrincipalCreator<? extends UserDetails> principalCreator;

	@Inject
	private RenewTokenStorage renewTokenStorage;
	@Inject
	private Optional<SamlJwtTokenStorage> samlJwtTokenStorage;
	@Inject
	private AuthenticationProperties authenticationProperties;

	public boolean isCodeChallengeValid(String codeChallenge) {
		Optional<String> loadValueByCodeChallenge = renewTokenStorage.loadCodeChallenge(codeChallenge);
		return loadValueByCodeChallenge.isPresent() && isAfterNow(Instant.ofEpochMilli(Long.parseLong(loadValueByCodeChallenge.get())));
	}

	public boolean isCodeValid(String code) {
		Optional<String> loadValueByCode = renewTokenStorage.loadCode(code);
		if (loadValueByCode.isPresent() && isAfterNow(Instant.ofEpochMilli(Long.parseLong(loadValueByCode.get())))) {
			return true;
		}
		LOGGER.warn("Code '%s' is invalid".formatted(code));
		return false;
	}

	public boolean isTokenHintValid(String code) {
		Optional<String> loadTokenHintByCode = renewTokenStorage.loadTokenHintByCode(code);
		if (loadTokenHintByCode.isPresent() && jwtTokenVerifier.isTokenValid(loadTokenHintByCode.get())) {
			return true;
		}
		LOGGER.warn("TokenHintValid '%s' is invalid".formatted(code));
		return false;
	}

	public boolean isRequestAuthorizeValid(String codeChallenge, String idTokenHint) {
		boolean isNewCodeChallengeValid = !isCodeChallengeValid(codeChallenge);
		boolean isTokenValid = jwtTokenVerifier.isTokenValid(idTokenHint);
		boolean isTokenRenewalTimeValid = isTokenRenewalValid(idTokenHint);
		if (isNewCodeChallengeValid && isTokenValid && isTokenRenewalTimeValid) {
			return true;
		}
		LOGGER.warn("Authorization for token renewal failed with the result [newCodeChallengeValid: %s, tokenValid: %s, renewalTimeValid: %s]"
			.formatted(isNewCodeChallengeValid, isTokenValid, isTokenRenewalTimeValid));
		return false;
	}

	public String authorize(String codeChallenge, String tokenHint) {
		String code = generateCode();
		String expiration = String.valueOf(calculateExpirationTime());
		renewTokenStorage.storeCodeChallenge(codeChallenge, expiration);
		renewTokenStorage.storeCode(code, expiration);
		renewTokenStorage.storeTokenHint(code, tokenHint);
		return code;
	}

	public boolean isRequestTokenValid(String code, String codeVerifier) {
		String codeChallenge = generateCodeChallenge(codeVerifier);
		boolean isCodeChallengeValid = isCodeChallengeValid(codeChallenge);
		boolean isCodeValid = isCodeValid(code);
		boolean isTokenHintValid = isTokenHintValid(code);
		renewTokenStorage.removeCodeChallenge(codeChallenge);
		renewTokenStorage.removeCode(code);
		if (isCodeChallengeValid && isCodeValid && isTokenHintValid) {
			return true;
		}
		LOGGER.warn("Exchange token failed with the result [codeChallengeValid: %s, codeValid: %s, tokenHintValid: %s]"
			.formatted(isCodeChallengeValid, isCodeValid, isTokenHintValid));
		return false;
	}

	public JwtTokenData generateNewToken(String code) {
		return renewTokenStorage.loadTokenHintByCode(code)
			.map(token -> generateNewToken(token, code))
			.orElse(null);
	}

	private String generateCodeChallenge(String codeVerifier) {
		String codeChallenge = null;
		try {
			byte[] bytes = codeVerifier.getBytes("US-ASCII");
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(bytes, 0, bytes.length);
			byte[] digest = md.digest();
			codeChallenge = Base64.encodeBase64URLSafeString(digest);
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			LOGGER.warn("Code challenge generating failed.");
		}
		return codeChallenge;
	}

	private String generateCode() {
		return RandomStringUtils.random(CODE_CHARACTER_LENGTH, 0, 0, true, true, null, new SecureRandom());
	}

	private Long calculateExpirationTime() {
		return Instant.now().plusSeconds(getTokenRenewThresholdInSeconds()).toEpochMilli();
	}

	private boolean isBeforeNow(Instant expiration) {
		return expiration.isBefore(Instant.now());
	}

	private boolean isAfterNow(Instant expiration) {
		return expiration.isAfter(Instant.now());
	}

	private boolean isTokenRenewalValid(String token) {
		JwtTokenData tokenData = jwtTokenVerifier.unpackToken(token);
		Instant expiration = tokenData.getExpirationTime().minusSeconds(getTokenRenewThresholdInSeconds());
		if (isBeforeNow(expiration)) {
			return true;
		}
		LOGGER.warn("The renewal is only processed in %s seconds before the token is expired".formatted(getTokenRenewThresholdInSeconds()));
		return false;
	}

	private Integer getTokenRenewThresholdInSeconds() {
		return authenticationProperties.getJwt().getTokenRenewThresholdInSeconds();
	}

	private JwtTokenData generateNewToken(String existingToken, String code) {
		JwtTokenData jwtTokenData = jwtTokenVerifier.unpackToken(existingToken);
		UserDetails userDetails = principalCreator.createPrincipal(jwtTokenData);
		// preserve the original login time so user-lifetime-seconds keeps being measured from the first login
		JwtTokenData newTokenData = jwtTokenGenerator.generateToken(userDetails, jwtTokenData.getLoginTime());
		renewTokenStorage.removeTokenHint(code);
		LOGGER.debug("Token expiration renewed: [{}] -> [{}]", jwtTokenData.getExpirationTime().toEpochMilli(),
			newTokenData.getExpirationTime().toEpochMilli());
		//token for the session needs to be updated
		samlJwtTokenStorage.map(storage -> storage.loadAccessToken(existingToken))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.map(SamlJwtTokenData::getSessionId)
			.ifPresent(sessionIndex -> {
				SamlJwtTokenData samlJwtTokenData = new SamlJwtTokenData.Builder(newTokenData.getToken())
					.withExpirationTime(newTokenData.getExpirationTime())
					.withSessionId(sessionIndex)
					.build();
				LOGGER.debug("Token session mapping data replaced [{}]", samlJwtTokenData);
				samlJwtTokenStorage.get().storeJwtToken(sessionIndex, samlJwtTokenData);
			});

		return newTokenData;
	}

}
