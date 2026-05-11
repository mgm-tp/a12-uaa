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
package com.mgmtp.a12.uaa.authentication.jwt.internal;

import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.internal.UAASpringJsonHandler;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenData;
import com.mgmtp.a12.uaa.authentication.jwt.encryption.DataEncoder;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

public class JwtTokenGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenGenerator.class);
	private static final String CLAIM_USER_DETAILS = "userDetails";
	private static final String CLAIM_AUTHORITIES = "authorities";
	private Integer expirationSeconds;
	private Random random = new Random();
	private JWSHeader jwtHeader;
	private JWEHeader jweHeader;
	private JWSSigner signer;
	private DirectEncrypter encrypter;
	private boolean storeUser;
	private DataEncoder dataEncoder;

	@Inject
	private UAASpringJsonHandler uaaSpringJsonHandler;

	@Inject
	private AuthenticationProperties authenticationProperties;

	private JwtTokenGenerator(Integer expirationSeconds, boolean storeUser, DataEncoder dataEncoder,
		JWSHeader jwsHeader, JWEHeader jweHeader, RSASSASigner signer, DirectEncrypter encrypter) {
		this.expirationSeconds = expirationSeconds;
		this.storeUser = storeUser;
		this.dataEncoder = dataEncoder;
		this.jwtHeader = jwsHeader;
		this.jweHeader = jweHeader;
		this.signer = signer;
		this.encrypter = encrypter;
	}

	public JwtTokenData generateToken(UserDetails userDetails) {
		JwtTokenData.Builder tokenBuilder = new JwtTokenData.Builder(userDetails.getUsername());
		Map<String, Object> claims = new HashMap<>();
		if (storeUser) {
			String serialized = serializeToJson(userDetails);
			claims.put(CLAIM_USER_DETAILS, dataEncoder.encrypt(serialized));
			tokenBuilder.withPrincipal(userDetails);
		} else {
			//store authorities only when principal is not serialized
			String serializedAuthorities = serializeToJson(userDetails.getAuthorities());
			claims.put(CLAIM_AUTHORITIES, dataEncoder.encrypt(serializedAuthorities));
			tokenBuilder.withAuthorities(userDetails.getAuthorities());
		}
		Instant issuedTime = Instant.now();
		Instant expirationTime = calculateExpirationDate(issuedTime);
		String token = doGenerateToken(claims, userDetails.getUsername(), issuedTime, expirationTime);

		return tokenBuilder.withToken(token)
			.withIssuedTime(issuedTime)
			.withExpirationSeconds(expirationSeconds)
			.withTokenRenewThresholdInSeconds(authenticationProperties.getJwt().getTokenRenewThresholdInSeconds())
			.build();

	}

	private String doGenerateToken(Map<String, Object> claims, String subject, Instant issuedTime, Instant expirationTime) {
		JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
			.subject(subject)
			.expirationTime(Date.from(expirationTime))
			.issueTime(Date.from(issuedTime));
		claims.forEach(claimsBuilder::claim);
		// Create a JWE from JWS
		try {
			JWEObject jweObject;
			if (authenticationProperties.getJwt().getTokenSignature().isEnabled()) {
				SignedJWT signedJWT = new SignedJWT(jwtHeader, claimsBuilder.build());
				signedJWT.sign(signer);
				jweObject = new JWEObject(jweHeader, new Payload(signedJWT));
			} else {
				jweObject = new EncryptedJWT(jweHeader, claimsBuilder.build());
			}
			jweObject.encrypt(encrypter);
			return jweObject.serialize();
		} catch (Exception e) {
			LOGGER.error("Unable to create JWT token", e);
			return null;
		}

	}

	private Instant calculateExpirationDate(Instant createdDate) {
		return createdDate.plus(Duration.ofSeconds(expirationSeconds));
	}

	private String serializeToJson(UserDetails userDetails) {
		UserWrapper wrapper = new UserWrapper();
		wrapper.setUser(userDetails);
		wrapper.setRandom(random.nextInt());
		return serializeToJson(wrapper);
	}

	private String serializeToJson(Object data) {
		try {
			return uaaSpringJsonHandler.convertToJson(data);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Unable to serialize UserDetails from the token", e);
		}

	}

	public static class Builder {

		private String secretKey;
		private boolean storeUser;
		private DataEncoder dataEncoder;
		private Resource privateKeyLocation;
		private Integer expirationSeconds;

		public Builder withSecretKey(String secretKey) {
			this.secretKey = secretKey;
			return this;
		}

		public Builder withStoreUser(boolean storeUser) {
			this.storeUser = storeUser;
			return this;
		}

		public Builder withDataEncoder(DataEncoder dataEncoder) {
			this.dataEncoder = dataEncoder;
			return this;
		}

		public Builder withPrivateKeyLocation(Resource privateKeyLocation) {
			this.privateKeyLocation = privateKeyLocation;
			return this;
		}

		public Builder withExpirationSeconds(Integer expirationSeconds) {
			this.expirationSeconds = expirationSeconds;
			return this;
		}

		public JwtTokenGenerator build() {
			JWSHeader jwtHeader = new JWSHeader.Builder(JWSAlgorithm.RS256).type(JOSEObjectType.JWT).build();
			JWEHeader jweHeader = new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM).build();
			RSAPrivateKey privateKey;
			if (privateKeyLocation != null) {
				privateKey = readPrivateKeyLocation(privateKeyLocation);
			} else {
				privateKey = (RSAPrivateKey) KeyPairUtils.getDefaultKeyPair().getPrivate();
			}
			byte[] secretBytes = Base64.getDecoder().decode(secretKey);
			DirectEncrypter encrypter = null;
			RSASSASigner signer = new RSASSASigner(privateKey);
			try {
				encrypter = new DirectEncrypter(secretBytes);
			} catch (KeyLengthException e) {
				LOGGER.error("The secret key length is not compatible." + e.getMessage());
			}
			return new JwtTokenGenerator(expirationSeconds, storeUser, dataEncoder, jwtHeader, jweHeader, signer, encrypter);
		}

		private RSAPrivateKey readPrivateKeyLocation(Resource location) {
			try (InputStream inputStream = location.getInputStream()) {
				return RsaKeyConverters.pkcs8().convert(inputStream);
			} catch (Exception e) {
				throw new IllegalArgumentException("Can not read the private key location", e);
			}
		}
	}

}
