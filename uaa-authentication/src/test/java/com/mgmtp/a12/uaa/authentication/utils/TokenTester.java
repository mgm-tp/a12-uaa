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
package com.mgmtp.a12.uaa.authentication.utils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collection;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.core.GrantedAuthority;

import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenData;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenStorage;
import com.mgmtp.a12.uaa.authentication.jwt.encryption.DataEncoder;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenGenerator;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenVerifier;
import com.mgmtp.a12.uaa.authentication.principal.UAAPrincipal;

public class TokenTester {

	private static KeyPair rsaKeyPair;

	static {
		rsaKeyPair = generateKeyPair();
	}

	public static JwtTokenGenerator getJwtTokenGeneratorSupport(DataEncoder dataEncoder, Integer expirationSeconds, boolean isStoredUser) {
		String privateKeyString = Base64.getEncoder().encodeToString(rsaKeyPair.getPrivate().getEncoded());
		privateKeyString = "-----BEGIN PRIVATE KEY-----\n%s\n-----END PRIVATE KEY-----".formatted(privateKeyString);
		return new JwtTokenGenerator.Builder()
			.withPrivateKeyLocation(new ByteArrayResource(privateKeyString.getBytes()))
			.withDataEncoder(dataEncoder)
			.withSecretKey("bXlTZWNyZXRLZXkxMjM0NW15U2VjcmV0S2V5MTIzNDU=")
			.withExpirationSeconds(expirationSeconds)
			.withStoreUser(isStoredUser)
			.build();
	}

	public static JwtTokenVerifier getJwtTokenVerifierSupport(DataEncoder dataEncoder, Integer userLifetimeSeconds, boolean isStoredUser) {
		String publicKeyString = Base64.getEncoder().encodeToString(rsaKeyPair.getPublic().getEncoded());
		publicKeyString = "-----BEGIN PUBLIC KEY-----\n%s\n-----END PUBLIC KEY-----".formatted(publicKeyString);
		return new JwtTokenVerifier.Builder()
			.withPublicKeyLocation(new ByteArrayResource(publicKeyString.getBytes()))
			.withDataEncoder(dataEncoder)
			.withSecretKey("bXlTZWNyZXRLZXkxMjM0NW15U2VjcmV0S2V5MTIzNDU=")
			.withUserLifetimeSeconds(userLifetimeSeconds)
			.withStoreUser(isStoredUser)
			.build();
	}

	public static void checkTokenData(JwtTokenGenerator jwtTokenGenerator, JwtTokenVerifier jwtTokenVerifier,
		boolean isStoredUser) {
		UAAPrincipal<UserDataCreator.TestExtendedData> principal = UserDataCreator.createUser("test1", "password");
		JwtTokenData tokenData = jwtTokenGenerator.generateToken(principal);
		JwtTokenData unpackedTokenData = jwtTokenVerifier.unpackToken(tokenData.getToken());
		assertTokenData(tokenData, isStoredUser);
		assertTokenData(unpackedTokenData, isStoredUser);
		Assertions.assertEquals(principal.getUsername(), tokenData.getUsername());
		Assertions.assertEquals(principal.getUsername(), unpackedTokenData.getUsername());
		if (isStoredUser) {
			@SuppressWarnings("unchecked")
			UAAPrincipal<UserDataCreator.TestExtendedData> deserializedUser =
				(UAAPrincipal<UserDataCreator.TestExtendedData>) unpackedTokenData.getPrincipal();
			assertPrincipal(principal, deserializedUser);
			Assertions.assertEquals(UAAPrincipal.class, deserializedUser.getClass());
		}

		Assertions.assertEquals(0,
			tokenData.getIssuedTime().truncatedTo(ChronoUnit.SECONDS).compareTo(unpackedTokenData.getIssuedTime().truncatedTo(ChronoUnit.SECONDS)));
	}

	private static void assertTokenData(JwtTokenData tokenData, boolean isStoredUser) {
		Assertions.assertNotNull(tokenData.getExpirationTime());
		Assertions.assertNotNull(tokenData.getIssuedTime());
		Assertions.assertNotNull(tokenData.getToken());
		Assertions.assertNotNull(tokenData.getUsername());
		if (isStoredUser) {
			Assertions.assertNull(tokenData.getAuthorities());
			Assertions.assertNotNull(tokenData.getPrincipal());
		} else {
			Assertions.assertNotNull(tokenData.getAuthorities());
			Assertions.assertNull(tokenData.getPrincipal());
		}
	}

	public static void checkTokenValid(JwtTokenGenerator jwtTokenGenerator,
		JwtTokenVerifier jwtTokenVerifier, JwtTokenStorage jwtTokenStorage,
		int waitInSecondTimeBeforeCheck,
		boolean isTokenBlackList,
		boolean isExpectedTokenValid) throws InterruptedException {
		UAAPrincipal<UserDataCreator.TestExtendedData> user = UserDataCreator.createUser("test1", "password");
		JwtTokenData tokenData = jwtTokenGenerator.generateToken(user);

		if (waitInSecondTimeBeforeCheck > 0) {
			Thread.sleep(waitInSecondTimeBeforeCheck * 1000L);
		}
		if (isTokenBlackList) {
			jwtTokenStorage.storeToken(tokenData.getToken());
			Optional<String> tokenRtn = jwtTokenStorage.loadToken(tokenData.getToken());
			Assertions.assertTrue(tokenRtn.isPresent());
		}
		Assertions.assertEquals(jwtTokenVerifier.isTokenValid(tokenData.getToken()), isExpectedTokenValid);
	}

	public static void assertPrincipal(UAAPrincipal<UserDataCreator.TestExtendedData> user, UAAPrincipal<UserDataCreator.TestExtendedData> deserializedUser) {
		assertPrincipal(user, deserializedUser, true, true);
	}

	public static void assertPrincipal(UAAPrincipal<UserDataCreator.TestExtendedData> user, UAAPrincipal<UserDataCreator.TestExtendedData> deserializedUser,
		boolean password, boolean isCompareWithDefaultPassword) {
		Assertions.assertEquals(user.getUsername(), deserializedUser.getUsername());
		if (password) {
			if (isCompareWithDefaultPassword) {
				Assertions.assertEquals("***", deserializedUser.getPassword());
			} else {
				Assertions.assertEquals(user.getPassword(), deserializedUser.getPassword());
			}
		}
		Collection<GrantedAuthority> authorities = deserializedUser.getAuthorities();
		Assertions.assertEquals(user.getAuthorities().size(), authorities.size());
		Assertions.assertTrue(authorities.stream().anyMatch(auth -> auth.getAuthority().equals("role1")));
		Assertions.assertTrue(authorities.stream().anyMatch(auth -> auth.getAuthority().equals("role2")));

		UserDataCreator.TestExtendedData extendedUserData = deserializedUser.getExtendedPrincipalData();
		if (user.getExtendedPrincipalData() != null && extendedUserData != null) {
			Assertions.assertEquals(user.getExtendedPrincipalData().getDataOne(), extendedUserData.getDataOne());
			Assertions.assertEquals(user.getExtendedPrincipalData().getDataTwo(), extendedUserData.getDataTwo());
			Assertions.assertEquals(user.getExtendedPrincipalData().getSubData().getSubOne(), extendedUserData.getSubData().getSubOne());
			Assertions.assertEquals(user.getExtendedPrincipalData().getSubData().getSubTwo(), extendedUserData.getSubData().getSubTwo());
		}

	}

	public static void checkCreationTimestampForNewToken(JwtTokenGenerator jwtTokenGenerator) throws InterruptedException {
		UAAPrincipal<UserDataCreator.TestExtendedData> user = UserDataCreator.createUser("test1", "password");
		JwtTokenData tokenData = jwtTokenGenerator.generateToken(user);
		Instant initialTokenCreationTimestamp = tokenData.getIssuedTime();
		// we need to sleep in order to avoid serialization rounding by JWT lib
		Thread.sleep(1000);
		JwtTokenData regeneratedTokenData = jwtTokenGenerator.generateToken(user);
		Assertions.assertNotEquals(initialTokenCreationTimestamp.toEpochMilli(), regeneratedTokenData.getIssuedTime().toEpochMilli());

	}

	private static KeyPair generateKeyPair() {
		KeyPairGenerator keyPairGenerator;
		try {
			keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			return keyPairGenerator.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

}
