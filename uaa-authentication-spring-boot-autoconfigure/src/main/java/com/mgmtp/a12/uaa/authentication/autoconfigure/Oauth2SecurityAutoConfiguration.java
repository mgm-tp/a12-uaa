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
package com.mgmtp.a12.uaa.authentication.autoconfigure;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.introspection.SpringOpaqueTokenIntrospector;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.AuthenticationProperties.Oauth2Properties.ResourceServer.Tenant;
import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;
import com.mgmtp.a12.uaa.authentication.oauth2.Oauth2GrantedAuthorityConverter;
import com.mgmtp.a12.uaa.authentication.oauth2.internal.DelegatedAuthorityConverter;
import com.mgmtp.a12.uaa.authentication.oauth2.internal.DelegatedJWTDecoder;
import com.mgmtp.a12.uaa.authentication.oauth2.internal.OpaqueTokenDecoder;

@ConditionalOnAuthentication(AuthenticationType.OAUTH2)
public class Oauth2SecurityAutoConfiguration {

	@Inject
	private AuthenticationProperties authenticationProperties;

	/**
	 * Default implementation if project doesn't have its own.
	 */
	@ConditionalOnMissingBean(Oauth2GrantedAuthorityConverter.class)
	@Bean
	public Oauth2GrantedAuthorityConverter defaultGrantedAuthoritiesConverter() {
		return new DelegatedAuthorityConverter();
	}

	private byte[] getKeySpec(String keyValue) {
		keyValue = keyValue.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
		return Base64.getMimeDecoder().decode(keyValue);
	}

	@Bean
	@Conditional(MultiTenantsCondition.class)
	JwtDecoder multiTenantsDecoder() throws Exception {
		List<Tenant> tenants = authenticationProperties.getOauth2().getResourceserver().getTenants();
		List<JwtDecoder> jwtDecoders = new ArrayList<>();
		for (Tenant tenant : tenants) {
			createFromJwt(tenant.getJwt()).ifPresent(jwtDecoders::add);
			createFromOpaqueToken(tenant.getOpaquetoken()).ifPresent(jwtDecoders::add);
		}
		return new DelegatedJWTDecoder(jwtDecoders.toArray(new JwtDecoder[0]));
	}

	private Optional<JwtDecoder> createFromJwt(Tenant.Jwt jwt) throws Exception {
		String issuerUri = jwt.getIssuerUri();
		String jwkSetUri = jwt.getJwkSetUri();
		List<String> audiences = jwt.getAudiences();
		Resource publicKeyResource = jwt.getPublicKeyLocation();
		List<SignatureAlgorithm> algs = jwt.getJwsAlgorithms().stream().map(SignatureAlgorithm::from).toList();
		OAuth2TokenValidator<Jwt> validator = StringUtils.isBlank(issuerUri) ? JwtValidators.createDefault() : JwtValidators.createDefaultWithIssuer(issuerUri);
		if (CollectionUtils.isNotEmpty(audiences)) {
			validator = new DelegatingOAuth2TokenValidator<>(validator,
				new JwtClaimValidator<List<String>>(JwtClaimNames.AUD, aud -> aud != null && !Collections.disjoint(aud, audiences)));
		}
		if (StringUtils.isNotBlank(jwkSetUri)) {
			NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
				.jwsAlgorithms(signatureAlgorithms -> signatureAlgorithms.addAll(algs)).build();
			decoder.setJwtValidator(validator);
			return Optional.of(decoder);
		} else if (StringUtils.isNotBlank(issuerUri)) {
			NimbusJwtDecoder decoder = NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
			decoder.setJwtValidator(validator);
			return Optional.of(decoder);
		} else if (Objects.nonNull(publicKeyResource)) {
			RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
				.generatePublic(new X509EncodedKeySpec(getKeySpec(readPublicKey(jwt.getPublicKeyLocation()))));
			NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey)
				.signatureAlgorithm(SignatureAlgorithm.from(exactlyOneAlgorithm(jwt.getJwsAlgorithms())))
				.build();
			decoder.setJwtValidator(validator); // validator here is always JwtValidators.createDefault() because issuer uri is always null
			return Optional.of(decoder);
		}
		return Optional.empty();
	}

	private String readPublicKey(Resource location) throws IOException {
		String key = "mgmtp.a12.uaa.authentication.oauth2.resourceserver.tenants[].jwt.public-key-location";
		Assert.notNull(location, "PublicKeyLocation must not be null");
		if (!location.exists()) {
			throw new IllegalStateException("Property %s with value '%s' is invalid: Public key location does not exist".formatted(
				key, location));
		}
		try (InputStream inputStream = location.getInputStream()) {
			return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
		}
	}

	private String exactlyOneAlgorithm(List<String> algorithms) {
		int count = (algorithms != null) ? algorithms.size() : 0;
		if (count != 1) {
			throw new IllegalStateException(
				"Creating a JWT decoder using a public key requires exactly one JWS algorithm but " + count
					+ " were configured");
		}
		return algorithms.get(0);
	}

	private Optional<JwtDecoder> createFromOpaqueToken(Tenant.OpaqueToken opaqueToken) {
		String introspectionUri = opaqueToken.getIntrospectionUri();
		String clientId = opaqueToken.getClientId();
		String clientSecret = opaqueToken.getClientSecret();
		if (StringUtils.isNoneBlank(introspectionUri, clientId, clientSecret)) {
			SpringOpaqueTokenIntrospector introspector = new SpringOpaqueTokenIntrospector(introspectionUri, clientId, clientSecret);
			return Optional.of(new OpaqueTokenDecoder(introspector));
		}
		return Optional.empty();
	}

}
