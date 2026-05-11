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
package com.mgmtp.a12.uaa.authentication.benchmark.token;

import java.util.concurrent.TimeUnit;

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
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;

import com.mgmtp.a12.uaa.authentication.benchmark.Utils;
import com.mgmtp.a12.uaa.authentication.benchmark.config.CommonConfiguration;
import com.mgmtp.a12.uaa.authentication.jwt.encryption.internal.HuffmanEncoder;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenGenerator;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenVerifier;
import com.mgmtp.a12.uaa.authentication.utils.TokenTester;
import com.mgmtp.a12.uaa.authentication.utils.UserDataCreator;

@Fork(1)
@Threads(10)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class JwtTokenSupportBenchmark {

	private static final String TOKEN = Utils.readFile("token/token.txt");
	private static final String COMPRESS_TOKEN = Utils.readFile("token/compress_token.txt");

	@Benchmark
	public void bypassingGenerateToken(TestState state, Blackhole bh) {
		bh.consume(state.bypassingEncodeJwtTokenGenerator.generateToken(userDetails()));
	}

	@Benchmark
	public void bypassingUnpackToken(TestState state, Blackhole bh) {
		bh.consume(state.bypassingEncodeJwtTokenVerifier.unpackToken(TOKEN));
	}

	@Benchmark
	public void huffmanGenerateToken(TestState state, Blackhole bh) {
		bh.consume(state.huffmanEncodeJwtTokenGenerator.generateToken(userDetails()));
	}

	@Benchmark
	public void huffmanUnpackToken(TestState state, Blackhole bh) {
		bh.consume(state.huffmanEncodeJwtTokenVerifier.unpackToken(COMPRESS_TOKEN));
	}

	public UserDetails userDetails() {
		return UserDataCreator.createUser("admin", "password");
	}

	@State(Scope.Thread)
	public static class TestState {
		private JwtTokenGenerator bypassingEncodeJwtTokenGenerator;
		private JwtTokenVerifier bypassingEncodeJwtTokenVerifier;
		private JwtTokenGenerator huffmanEncodeJwtTokenGenerator;
		private JwtTokenVerifier huffmanEncodeJwtTokenVerifier;
		private AnnotationConfigApplicationContext context;

		@Setup(Level.Trial)
		public void setUp() {
			context = new AnnotationConfigApplicationContext(BenchmarkConfiguration.class, CommonConfiguration.class);
			bypassingEncodeJwtTokenGenerator = context.getBean("bypassingEncodeJwtTokenGeneratorSupport", JwtTokenGenerator.class);
			bypassingEncodeJwtTokenVerifier = context.getBean("bypassingEncodeJwtTokenVerifierSupport", JwtTokenVerifier.class);
			huffmanEncodeJwtTokenGenerator = context.getBean("huffmanEncodeJwtTokenGeneratorSupport", JwtTokenGenerator.class);
			huffmanEncodeJwtTokenVerifier = context.getBean("huffmanEncodeJwtTokenVerifierSupport", JwtTokenVerifier.class);
		}

		@TearDown
		public void tearDown() {
			context.close();
		}

	}

	@Configuration
	static class BenchmarkConfiguration {
		@Bean("huffmanEncodeJwtTokenGeneratorSupport")
		public JwtTokenGenerator createHuffmanEncodeJwtTokenGeneratorSupport() {
			return TokenTester.getJwtTokenGeneratorSupport(new HuffmanEncoder(), 315360000, true);
		}

		@Bean("huffmanEncodeJwtTokenVerifierSupport")
		public JwtTokenVerifier createHuffmanEncodeJwtTokenVerifierSupport() {
			return TokenTester.getJwtTokenVerifierSupport(new HuffmanEncoder(), 315360000, true);
		}
	}
}
