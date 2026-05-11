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
package com.mgmtp.a12.uaa.authentication.oauth2.internal;

import java.util.Optional;

import jakarta.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;
import com.mgmtp.a12.uaa.authentication.ConditionalOnOpaqueToken;
import com.mgmtp.a12.uaa.authentication.internal.ClassNameUtils;
import com.mgmtp.a12.uaa.authentication.oauth2.Oauth2ClaimsExtractor;
import com.mgmtp.a12.uaa.authentication.oauth2.Oauth2GrantedAuthorityConverter;
import com.mgmtp.a12.uaa.authentication.security.UAASecurityConfigurer;

@Configuration
@ConditionalOnAuthentication(AuthenticationType.OAUTH2)
public class Oauth2SecurityConfigurer extends UAASecurityConfigurer<Oauth2SecurityConfigurer> {

	@Inject
	private Optional<Oauth2ClaimsExtractor> oauth2ClaimsExtractor;
	@Inject
	private Oauth2GrantedAuthorityConverter grantedAuthorityConverter;
	@Inject
	private Optional<OpaqueTokenIntrospector> introspector;

	@Override
	public void configureHttpSecurity(HttpSecurity http) throws Exception {
		http.oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer.jwt(jwt -> {
			jwt.jwtAuthenticationConverter(authenticationConverter());
		}));
		LOGGER.info("OAUTH2: Using Oauth2ClaimsExtractor: [{}]", ClassNameUtils.resolveShortClassName(oauth2ClaimsExtractor));
		LOGGER.info("OAUTH2: Using Oauth2GrantedAuthorityConverter: [{}]", ClassNameUtils.resolveShortClassNameFromObject(grantedAuthorityConverter));
	}

	@Bean
	public Converter<Jwt, Oauth2JwtAuthenticationToken> authenticationConverter() {
		return new Oauth2JwtAuthenticationTokenConverter();
	}

	@Bean
	@ConditionalOnOpaqueToken
	public JwtDecoder opaqueJwtDecoder() {
		return new OpaqueTokenDecoder(introspector.get());
	}

}
