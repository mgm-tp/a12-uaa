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

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.AuthenticationProperties.JwtProperties;
import com.mgmtp.a12.uaa.authentication.internal.UAASpringJsonHandler;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenData;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenStorage;
import com.mgmtp.a12.uaa.authentication.jwt.encryption.DataEncoder;
import com.mgmtp.a12.uaa.authentication.principal.internal.serialization.UAAJacksonModule;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

public class JwtTokenVerifier {

	private static final Pattern TOKEN_BASE64_URL_PATTERN = Pattern.compile("^[a-zA-Z0-9-._]*$");

	private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenVerifier.class);
	private static final String CLAIM_USER_DETAILS = "userDetails";
	private static final String CLAIM_AUTHORITIES = "authorities";
	private static final String CLAIM_AUTH_TIME = "auth_time";
	private Integer userLifetimeSeconds;
	private RSASSAVerifier verifier;
	private DirectDecrypter decrypter;
	private boolean storeUser;
	private DataEncoder dataEncoder;
	@Inject
	private JwtTokenStorage jwtTokenStorage;
	@Inject
	private UAASpringJsonHandler uaaSpringJsonHandler;
	@Inject
	private AuthenticationProperties authenticationProperties;

	private JwtTokenVerifier(boolean storeUser, DataEncoder dataEncoder, Integer userLifetimeSeconds, RSASSAVerifier verifier,
		DirectDecrypter decrypter) {
		this.storeUser = storeUser;
		this.dataEncoder = dataEncoder;
		this.userLifetimeSeconds = userLifetimeSeconds;
		this.verifier = verifier;
		this.decrypter = decrypter;
	}

	public JwtTokenData unpackToken(String token) {
		JWTClaimsSet claims = extractClaims(token);
		JwtProperties jwtProperties = authenticationProperties.getJwt();
		return new JwtTokenData.Builder(claims.getSubject())
			.withToken(token)
			.withAuthorities(getAuthorities(claims))
			.withPrincipal(getPrincipal(claims))
			.withIssuedTime(claims.getIssueTime().toInstant())
			.withLoginTime(extractLoginTime(claims))
			.withExpirationSeconds(jwtProperties.getExpirationSeconds())
			.withTokenRenewThresholdInSeconds(jwtProperties.getTokenRenewThresholdInSeconds())
			.build();
	}

	/**
	 * Reads the preserved original authentication time from the token. Falls back to the standard issue time for tokens
	 * issued before the {@code auth_time} claim existed, so that the {@code user-lifetime-seconds} cap remains enforced.
	 */
	private Instant extractLoginTime(JWTClaimsSet claims) {
		try {
			Long authTimeMillis = claims.getLongClaim(CLAIM_AUTH_TIME);
			if (authTimeMillis != null) {
				return Instant.ofEpochMilli(authTimeMillis);
			}
		} catch (Exception e) {
			LOGGER.warn("Unable to read the authentication time claim, falling back to the issue time." + e);
		}
		return claims.getIssueTime().toInstant();
	}

	private UserDetails getPrincipal(JWTClaimsSet claims) {
		String userDetailsString = Objects.toString(claims.getClaim(CLAIM_USER_DETAILS), null);
		return Optional.ofNullable(userDetailsString)
			.map(user -> dataEncoder.decrypt(user))
			.map((user) -> deserializeFromJson(user))
			.orElse(null);
	}

	@SuppressWarnings("unchecked")
	private Collection<? extends GrantedAuthority> getAuthorities(JWTClaimsSet claims) {
		String authoritiesString = Objects.toString(claims.getClaim(CLAIM_AUTHORITIES), null);
		return Optional.ofNullable(authoritiesString)
			.map(authorities -> dataEncoder.decrypt(authorities))
			.map(authorities -> deserializeFromJson(authorities, Collection.class))
			.orElse(null);
	}

	public Boolean isTokenExpired(String token) {
		return isTokenExpired(extractClaims(token));
	}

	public Boolean isTokenExpired(JWTClaimsSet tokenClaims) {
		try {
			Instant expiration = tokenClaims.getExpirationTime().toInstant();
			if (expiration.isBefore(Instant.now())) {
				LOGGER.warn("Token is expired.");
				return true;
			}
			return false;
		} catch (Exception e) {
			LOGGER.warn("Token parsing failed." + e);
			return true;
		}
	}

	private Boolean isTokenLifetimeExpired(JWTClaimsSet tokenClaims) {
		try {
			Instant loginTime = extractLoginTime(tokenClaims);
			return Optional.ofNullable(userLifetimeSeconds).map(lifetime -> {
				Instant expirationTime = loginTime.plus(Duration.ofSeconds(lifetime));
				if (Instant.now().isAfter(expirationTime)) {
					LOGGER.warn("Token lifetime is expired.");
					return true;
				}
				return false;
			}).orElse(false);
		} catch (Exception e) {
			LOGGER.warn("Token parsing failed." + e);
			return true;
		}
	}

	public Boolean isTokenValid(String token) {
		try {
			Matcher matcher = TOKEN_BASE64_URL_PATTERN.matcher(token);
			if (!matcher.matches()) {
				LOGGER.warn("UAABearer token is malformed (not a base64).");
				return false;
			}

			JWTClaimsSet tokenClaims = extractClaims(token);
			boolean isTokenLifetimeExpired = isTokenLifetimeExpired(tokenClaims);
			boolean isTokenExpired = isTokenExpired(tokenClaims);
			boolean isTokenLoggedOut = jwtTokenStorage.loadToken(token).isPresent();
			if (!isTokenLifetimeExpired && !isTokenExpired && !isTokenLoggedOut) {
				return true;
			}
			LOGGER.warn("Token is invalid with result checked [tokenLifetimeExpired: %s, tokenExpired: %s, tokenLoggedOut: %s]".formatted(
				isTokenLifetimeExpired, isTokenExpired, isTokenLoggedOut));
			return false;
		} catch (Exception e) {
			LOGGER.error("Token parsing failed." + e);
			return false;
		}
	}

	private JWTClaimsSet extractClaims(String token) {
		try {
			// Convert String to JWE object
			EncryptedJWT encryptedJWT = EncryptedJWT.parse(token);
			// Decrypt JWE to get JWT by using secret key
			encryptedJWT.decrypt(decrypter);
			if (authenticationProperties.getJwt().getTokenSignature().isEnabled()) {
				SignedJWT parsedJWT = encryptedJWT.getPayload().toSignedJWT();
				// Verify JWT signature
				if (!parsedJWT.verify(verifier)) {
					throw new RuntimeException("Invalid token signature");
				}
				// Get Claims for using
				return parsedJWT.getJWTClaimsSet();
			}
			return encryptedJWT.getJWTClaimsSet();
		} catch (Exception e) {
			throw new RuntimeException("Unable to extract the token claims", e);
		}
	}

	private UserDetails deserializeFromJson(String json) {
		if (!storeUser) {
			throw new RuntimeException("No user is stored in the token");
		}
		UserWrapper userWrapper = deserializeFromJson(json, UserWrapper.class);
		return userWrapper.getUser();
	}

	private <T> T deserializeFromJson(String json, Class<T> type) {
		try {
			return uaaSpringJsonHandler.convertFromJson(json, type);
		} catch (IOException e) {
			throw new RuntimeException("Unable to deserialize UserDetails from the token", e);
		}
	}

	public static class Builder {
		private String secretKey;
		private boolean storeUser;
		private DataEncoder dataEncoder;
		private Resource publicKeyLocation;
		private Integer userLifetimeSeconds;
		private List<UAAJacksonModule> jacksonModules;

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

		public Builder withPublicKeyLocation(Resource publicKeyLocation) {
			this.publicKeyLocation = publicKeyLocation;
			return this;
		}

		public Builder withUserLifetimeSeconds(Integer userLifetimeSeconds) {
			this.userLifetimeSeconds = userLifetimeSeconds;
			return this;
		}

		public JwtTokenVerifier build() {
			RSAPublicKey publicKey;
			if (publicKeyLocation != null) {
				publicKey = readPublicKeyLocation(publicKeyLocation);
			} else {
				publicKey = (RSAPublicKey) KeyPairUtils.getDefaultKeyPair().getPublic();
			}
			RSASSAVerifier verifier = new RSASSAVerifier(publicKey);
			byte[] secretByteArr = Base64.getDecoder().decode(secretKey);
			DirectDecrypter decrypter = null;
			try {
				decrypter = new DirectDecrypter(secretByteArr);
			} catch (KeyLengthException e) {
				LOGGER.error("The secret key length is not compatible." + e.getMessage());
			}
			return new JwtTokenVerifier(storeUser, dataEncoder, userLifetimeSeconds, verifier, decrypter);
		}

		private RSAPublicKey readPublicKeyLocation(Resource location) {
			try (InputStream inputStream = location.getInputStream()) {
				return RsaKeyConverters.x509().convert(inputStream);
			} catch (Exception e) {
				throw new IllegalArgumentException("Can not read the public key location", e);
			}
		}
	}

}
