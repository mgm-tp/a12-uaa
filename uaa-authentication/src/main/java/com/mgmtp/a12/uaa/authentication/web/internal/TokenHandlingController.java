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
package com.mgmtp.a12.uaa.authentication.web.internal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mgmtp.a12.uaa.authentication.internal.RedirectSupport;
import com.mgmtp.a12.uaa.authentication.internal.RedirectType;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenData;
import com.mgmtp.a12.uaa.authentication.jwt.internal.CookieUtil;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenVerifier;
import com.mgmtp.a12.uaa.authentication.jwt.internal.renew.RenewTokenService;
import com.mgmtp.a12.uaa.authentication.saml.internal.SamlTokenExchangeService;
import com.mgmtp.a12.uaa.authentication.security.login.internal.UAAAuthenticationSuccessHandler;

@RestController
@ResponseBody
@RequestMapping("#{T(org.apache.commons.lang3.StringUtils).removeEnd('${mgmtp.a12.uaa.authentication.context-path:/}', '/')}/uaa-authentication")
public class TokenHandlingController {

	private static final Logger LOGGER = LoggerFactory.getLogger(TokenHandlingController.class);

	private static final String STATE_KEY = "state";
	private static final String CODE_KEY = "code";
	private static final String AUTHORIZATION_CODE = "authorizationCode";

	@Inject
	private JwtTokenVerifier jwtTokenVerifier;

	@Inject
	private RenewTokenService renewTokenService;

	@Inject
	@RedirectType(type = RedirectType.Type.LOGIN)
	private Optional<RedirectSupport> loginRedirectSupport;

	@Inject
	private Optional<SamlTokenExchangeService> samlTokenExchangeService;

	@PostMapping(value = "tokenValid", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> isTokenValid(@RequestBody String token) {
		JwtTokenVerifier.TokenValidationResult tokenValidation = jwtTokenVerifier.validateToken(token);
		tokenValidation.doErrorLog(LOGGER, Level.DEBUG, "Token is invalid");
		return ResponseEntity.status(HttpStatus.OK).body(tokenValidation.valid());
	}

	@PostMapping(value = "exchangeAuthorizationCodeToToken/authorize", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> authorizeExchangeAuthorizationCodeToToken(HttpServletRequest request, HttpServletResponse response,
		@RequestBody CodeExchangeRequest codeExchangeRequest) {
		return samlTokenExchangeService
			.filter(samlTokenExchangeService -> !samlTokenExchangeService.isCodeChallengeValid(codeExchangeRequest.getCodeChallenge()))
			.map(samlTokenExchangeService -> {
				samlTokenExchangeService.authorize(codeExchangeRequest.getCodeChallenge());
				HashMap<String, Object> body = new HashMap<>();
				body.put(STATE_KEY, codeExchangeRequest.getState());
				return ResponseEntity.status(HttpStatus.OK).body(body);
			}).orElseGet(() -> {
				codeExchangeFailedRedirect(request, response);
				return null;
			});
	}

	@PostMapping(value = "exchangeAuthorizationCodeToToken", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public ResponseEntity<?> exchangeAuthorizationCodeToToken(HttpServletRequest request, HttpServletResponse response, TokenFormData tokenFormData) {
		String codeVerifier = StringUtils.trimToEmpty(tokenFormData.getCodeVerifier());

		return samlTokenExchangeService
			.flatMap(samlTokenExchangeService -> CookieUtil.locateCookie(request, AUTHORIZATION_CODE)
				.map(authorizationCode -> samlTokenExchangeService.verifyAndClaimToken(codeVerifier, authorizationCode)))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.map(jwtToken -> jwtTokenVerifier.unpackToken(jwtToken))
			.map(jwtTokenData -> {
				String tokenRenewInSeconds = String.valueOf(jwtTokenData.getExpirationSeconds() - jwtTokenData.getTokenRenewThresholdInSeconds());
				String tokenExpirationInSeconds = String.valueOf(jwtTokenData.getExpirationSeconds());
				CookieUtil.removeCookie(AUTHORIZATION_CODE, request, response);
				return ResponseEntity.status(HttpStatus.OK)
					.header(UAAAuthenticationSuccessHandler.TOKEN_KEY, jwtTokenData.getToken())
					.header(UAAAuthenticationSuccessHandler.TOKEN_RENEW_IN_SECONDS, tokenRenewInSeconds)
					.header(UAAAuthenticationSuccessHandler.TOKEN_EXPIRATION_IN_SECONDS, tokenExpirationInSeconds)
					.body("OK");
			}).orElseGet(() -> {
				codeExchangeFailedRedirect(request, response);
				return null;
			});
	}

	@PostMapping(value = "authorize", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> authorize(@RequestBody CodeExchangeRequest codeExchangeRequest) {
		String codeChallenge = codeExchangeRequest.getCodeChallenge();
		String idTokenHint = codeExchangeRequest.getIdTokenHint();
		return Optional.of(renewTokenService)
			.filter(renewTokenService -> renewTokenService.isRequestAuthorizeValid(codeChallenge, idTokenHint))
			.map(renewTokenService -> {
				String code = renewTokenService.authorize(codeChallenge, idTokenHint);
				HashMap<String, Object> body = new HashMap<>();
				body.put(STATE_KEY, codeExchangeRequest.getState());
				body.put(CODE_KEY, code);
				return ResponseEntity.status(HttpStatus.OK)
					.body(body);
			}).orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
	}

	@PostMapping(value = "token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> token(TokenFormData tokenFormData) {
		String code = StringUtils.trimToEmpty(tokenFormData.getCode());
		String codeVerifier = StringUtils.trimToEmpty(tokenFormData.getCodeVerifier());
		return Optional.of(renewTokenService)
			.filter(renewTokenService -> renewTokenService.isRequestTokenValid(code, codeVerifier))
			.map(renewTokenService -> {
				JwtTokenData tokenData = renewTokenService.generateNewToken(code);
				String tokenRenewInSeconds = String.valueOf(tokenData.getExpirationSeconds() - tokenData.getTokenRenewThresholdInSeconds());
				String tokenExpirationInSeconds = String.valueOf(tokenData.getExpirationSeconds());
				HashMap<String, String> responseBody = new HashMap<>();
				responseBody.put(UAAAuthenticationSuccessHandler.TOKEN_KEY, tokenData.getToken());
				responseBody.put(UAAAuthenticationSuccessHandler.TOKEN_RENEW_IN_SECONDS, tokenRenewInSeconds);
				responseBody.put(UAAAuthenticationSuccessHandler.TOKEN_EXPIRATION_IN_SECONDS, tokenExpirationInSeconds);
				return ResponseEntity.status(HttpStatus.OK).body(responseBody);
			}).orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
	}

	private void codeExchangeFailedRedirect(HttpServletRequest request, HttpServletResponse response) {
		loginRedirectSupport.ifPresent(redirectSupport -> {
			try {
				redirectSupport.performFailureRedirect(new DefaultRedirectStrategy(), request, response, null);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
