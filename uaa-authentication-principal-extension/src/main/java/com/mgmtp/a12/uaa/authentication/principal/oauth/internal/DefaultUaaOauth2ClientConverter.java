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
package com.mgmtp.a12.uaa.authentication.principal.oauth.internal;

import java.util.Optional;

import jakarta.inject.Inject;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;

import com.mgmtp.a12.uaa.authentication.oauth.client.UaaOauth2ClientAuthenticationToken;
import com.mgmtp.a12.uaa.authentication.oauth.client.UaaOauth2ClientConverter;
import com.mgmtp.a12.uaa.authentication.oauth2.Oauth2ClaimsExtractor;

public class DefaultUaaOauth2ClientConverter implements UaaOauth2ClientConverter {

	@Inject
	private Oauth2ClaimsExtractor claimsExtractor;

	@Inject
	private Optional<OAuth2AuthorizedClientService> authorizedClientService;

	private String issuerUri;

	public DefaultUaaOauth2ClientConverter(String issuerUri) {
		this.issuerUri = issuerUri;
	}

	@Override
	public UaaOauth2ClientAuthenticationToken convert(OAuth2AuthenticationToken source) {
		return createUaaOauth2ClientAuthenticationToken(source, extractUserDetails(source));
	}

	private UaaOauth2ClientAuthenticationToken createUaaOauth2ClientAuthenticationToken(OAuth2AuthenticationToken source, UserDetails userDetails) {
		UaaOauth2ClientAuthenticationToken authenticationToken = new UaaOauth2ClientAuthenticationToken(userDetails,
			source.getAuthorities(), source.getAuthorizedClientRegistrationId(), source.getPrincipal());
		authenticationToken.setAuthenticated(true);
		authenticationToken.setDetails(source.getDetails());
		return authenticationToken;
	}

	private UserDetails extractUserDetails(OAuth2AuthenticationToken source) {
		String tokenValue = createOAuth2AuthorizedClient(source).getAccessToken().getTokenValue();
		return claimsExtractor.extractClaims(getJwtDecoder().decode(tokenValue));
	}

	JwtDecoder getJwtDecoder() {
		return JwtDecoders.fromIssuerLocation(issuerUri);
	}

	private OAuth2AuthorizedClient createOAuth2AuthorizedClient(OAuth2AuthenticationToken source) {
		return authorizedClientService.orElseThrow(IllegalStateException::new)
			.loadAuthorizedClient(source.getAuthorizedClientRegistrationId(), source.getPrincipal().getName());
	}
}
