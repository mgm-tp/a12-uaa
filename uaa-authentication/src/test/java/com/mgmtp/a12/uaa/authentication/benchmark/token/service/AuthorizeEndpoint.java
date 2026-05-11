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

import jakarta.inject.Inject;

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
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenVerifier;
import com.mgmtp.a12.uaa.authentication.jwt.internal.renew.RenewTokenService;
import com.mgmtp.a12.uaa.authentication.web.internal.CodeExchangeRequest;

@Fork(1)
@Threads(10)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class AuthorizeEndpoint {
	private static final String TOKEN = Utils.readFile("token/token.txt");
	private static final String STATE_KEY = "state";
	private static final String CODE_KEY = "code";

	@Benchmark
	public ResponseEntity<?> authorizeEndpoint(TestState state) {
		String codeChallenge = state.codeExchangeRequest.getCodeChallenge();
		String idTokenHint = state.codeExchangeRequest.getIdTokenHint();
		return Optional.of(state.renewTokenService)
			.filter(renewTokenService -> renewTokenService.isRequestAuthorizeValid(codeChallenge, idTokenHint))
			.map(renewTokenService -> {
				String code = renewTokenService.authorize("make sure always true", idTokenHint);
				HashMap<String, Object> body = new HashMap<>();
				body.put(STATE_KEY, state.codeExchangeRequest.getState());
				body.put(CODE_KEY, code);
				return ResponseEntity.status(HttpStatus.OK)
					.body(body);
			}).orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
	}

	@State(Scope.Thread)
	public static class TestState {

		private RenewTokenService renewTokenService;
		private AnnotationConfigApplicationContext context;
		private CodeExchangeRequest codeExchangeRequest = new CodeExchangeRequest();

		@Setup(Level.Trial)
		public void setUp() {
			context = new AnnotationConfigApplicationContext(AuthorizeEndpointConfiguration.class, CommonConfiguration.class);
			renewTokenService = context.getBean("renewTokenService", RenewTokenService.class);
			codeExchangeRequest.setIdTokenHint(TOKEN);
			codeExchangeRequest.setCodeChallenge("code challenge");
			codeExchangeRequest.setState("state");
		}

		@TearDown
		public void tearDown() {
			context.close();
		}

	}

	@Configuration
	static class AuthorizeEndpointConfiguration {
		@Bean("renewTokenService")
		public RenewTokenServiceCustom renewTokenService() {
			return new RenewTokenServiceCustom();
		}
	}

	static class RenewTokenServiceCustom extends RenewTokenService {

		@Inject
		private JwtTokenVerifier jwtTokenVerifier;

		@Override
		public boolean isRequestAuthorizeValid(String codeChallenge, String idTokenHint) {
			return !isCodeChallengeValid(codeChallenge) && jwtTokenVerifier.isTokenValid(idTokenHint) && isTokenRenewalValid(idTokenHint);
		}

		private Boolean isTokenRenewalValid(String token) {
			JwtTokenData tokenData = jwtTokenVerifier.unpackToken(token);
			Instant expiration = tokenData.getExpirationTime().minus(Duration.ofSeconds(15));
			return isBeforeNow(expiration);
		}

		private Boolean isBeforeNow(Instant expiration) {
			return expiration.isBefore(Instant.now()) || true;
		}
	}
}
