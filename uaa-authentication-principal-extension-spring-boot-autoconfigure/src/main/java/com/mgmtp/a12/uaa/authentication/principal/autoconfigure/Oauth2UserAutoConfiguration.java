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
package com.mgmtp.a12.uaa.authentication.principal.autoconfigure;

import jakarta.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;

import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;
import com.mgmtp.a12.uaa.authentication.autoconfigure.Oauth2SecurityAutoConfiguration;
import com.mgmtp.a12.uaa.authentication.oauth.client.UaaOauth2ClientConverter;
import com.mgmtp.a12.uaa.authentication.oauth2.Oauth2ClaimsExtractor;
import com.mgmtp.a12.uaa.authentication.oauth2.Oauth2GrantedAuthorityConverter;
import com.mgmtp.a12.uaa.authentication.principal.PropertyExtractor;
import com.mgmtp.a12.uaa.authentication.principal.autoconfigure.AuthenticationPrincipalExtensionProperties.Oauth2Config;
import com.mgmtp.a12.uaa.authentication.principal.oauth.internal.DefaultUaaOauth2ClientConverter;
import com.mgmtp.a12.uaa.authentication.principal.oauth.internal.JwtTokenPropertyExtractor;
import com.mgmtp.a12.uaa.authentication.principal.oauth.internal.UAAGrantedAuthorityConverter;
import com.mgmtp.a12.uaa.authentication.principal.oauth.internal.UAAOauth2ClaimsExtractor;

@ConditionalOnAuthentication(AuthenticationType.OAUTH2)
@AutoConfigureBefore(Oauth2SecurityAutoConfiguration.class)
public class Oauth2UserAutoConfiguration {

	@Inject
	private AuthenticationPrincipalExtensionProperties authenticationUserProperties;
	@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
	private String issuerUri;

	@Bean
	@ConditionalOnMissingBean(Oauth2GrantedAuthorityConverter.class)
	public Oauth2GrantedAuthorityConverter grantedAuthorityConverter() {
		Oauth2Config claimsConfig = authenticationUserProperties.getOauth2Config();
		return new UAAGrantedAuthorityConverter(claimsConfig.getRealmAccessMap(), claimsConfig.getRoles());
	}

	@Bean
	@ConditionalOnMissingBean(Oauth2ClaimsExtractor.class)
	public Oauth2ClaimsExtractor claimsExtractor() {
		Oauth2Config claimsConfig = authenticationUserProperties.getOauth2Config();
		return new UAAOauth2ClaimsExtractor(claimsConfig.getUserName());
	}

	@Bean
	UaaOauth2ClientConverter defaultUaaOauth2ClientConverter() {
		return new DefaultUaaOauth2ClientConverter(issuerUri);
	}

	@Bean
	public PropertyExtractor<Jwt> jwtUserPropertyExtractor() {
		return new JwtTokenPropertyExtractor();
	}

}
