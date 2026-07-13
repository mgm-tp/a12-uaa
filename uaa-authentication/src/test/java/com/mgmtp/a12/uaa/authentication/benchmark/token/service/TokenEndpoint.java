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
package com.mgmtp.a12.uaa.authentication.benchmark.token.service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.mgmtp.a12.uaa.authentication.benchmark.Utils;
import com.mgmtp.a12.uaa.authentication.benchmark.config.CommonConfiguration;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenData;
import com.mgmtp.a12.uaa.authentication.jwt.RenewTokenStorage;
import com.mgmtp.a12.uaa.authentication.jwt.internal.renew.RenewTokenService;
import com.mgmtp.a12.uaa.authentication.web.internal.TokenFormData;

@Fork(1)
@Threads(10)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class TokenEndpoint {
	private static final String TOKEN = Utils.readFile("token/token.txt");
	private static final String TOKEN_KEY = "access_token";
	private static final String TOKEN_RENEW_IN_SECONDS = "token_renew_in_seconds";
	private static final Integer FIFTEEN_SECONDS = 15;

	@Benchmark
	public ResponseEntity<?> tokenEndpoint(TestState state) {
		String code = StringUtils.trimToEmpty(state.tokenFormData.getCode());
		String codeVerifier = StringUtils.trimToEmpty(state.tokenFormData.getCodeVerifier());
		return Optional.of(state.renewTokenService)
			.filter(renewTokenService -> renewTokenService.isRequestTokenValid(code, codeVerifier))
			.map(renewTokenService -> {
				JwtTokenData tokenData = renewTokenService.generateNewToken(code);
				state.renewTokenStorage.storeTokenHint("code", TOKEN);
				String tokenRenewInSeconds = String.valueOf(tokenData.getExpirationSeconds() - tokenData.getTokenRenewThresholdInSeconds());
				HashMap<String, String> responseBody = new HashMap<>();
				responseBody.put(TOKEN_KEY, tokenData.getToken());
				responseBody.put(TOKEN_RENEW_IN_SECONDS, tokenRenewInSeconds);
				return ResponseEntity.status(HttpStatus.OK).body(responseBody);
			}).orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
	}

	@State(Scope.Thread)
	public static class TestState {

		private RenewTokenService renewTokenService;
		private RenewTokenStorage renewTokenStorage;
		private AnnotationConfigApplicationContext context;
		private TokenFormData tokenFormData;

		@Setup(Level.Trial)
		public void setUp() {
			context = new AnnotationConfigApplicationContext(TokenEndpointConfiguration.class, CommonConfiguration.class);
			renewTokenService = context.getBean("renewTokenService", RenewTokenService.class);
			renewTokenStorage = context.getBean("renewTokenStorage", RenewTokenStorage.class);
			renewTokenStorage.storeTokenHint("code", TOKEN);
			tokenFormData = new TokenFormData("code", "code verifier");
		}

		@TearDown
		public void tearDown() {
			context.close();
		}

	}

	@Configuration
	static class TokenEndpointConfiguration {
		@Bean("renewTokenService")
		public RenewTokenServiceCustom renewTokenService() {
			return new RenewTokenServiceCustom();
		}
	}

	static class RenewTokenServiceCustom extends RenewTokenService {

		@Override
		public boolean isCodeChallengeValid(String codeChallenge) {
			Optional<String> loadValueByCodeChallenge = Optional.of(String.valueOf(Instant.now().plus(Duration.ofSeconds(FIFTEEN_SECONDS)).toEpochMilli()));
			Instant expiration = Instant.ofEpochMilli(Long.parseLong(loadValueByCodeChallenge.get()));
			return isAfterNow(expiration);
		}

		public boolean isCodeValid(String code) {
			Optional<String> loadValueByCode = Optional.of(String.valueOf(Instant.now().plus(Duration.ofSeconds(FIFTEEN_SECONDS)).toEpochMilli()));
			Instant expiration = Instant.ofEpochMilli(Long.parseLong(loadValueByCode.get()));
			return isAfterNow(expiration);
		}

		private Boolean isAfterNow(Instant expiration) {
			return expiration.isAfter(Instant.now());
		}
	}
}
