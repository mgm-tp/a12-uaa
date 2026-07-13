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
package com.mgmtp.a12.uaa.client.rest.auth.token.internal.oauth2;

import org.htmlunit.Page;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.internal.locator.AuthorizationDataLocator;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.TokenType;
import com.mgmtp.a12.uaa.client.rest.config.properties.OidcProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientAuthenticationProperties;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public class Oauth2ResponseAuthorizationDataLocator implements AuthorizationDataLocator<Page> {

	private final UAARestClientAuthenticationProperties restClientAuthenticationProperties;

	private ObjectMapper mapper = new ObjectMapper();

	public Oauth2ResponseAuthorizationDataLocator(UAARestClientAuthenticationProperties restClientAuthenticationProperties) {
		this.restClientAuthenticationProperties = restClientAuthenticationProperties;
	}

	@Override
	public AuthorizationData convert(Page page) {
		String response = page.getWebResponse().getContentAsString();
		try {
			TokenResponse tokenResponse = mapper.readValue(response, TokenResponse.class);
			TokenType tokenType = TokenType.fromTypeName(tokenResponse.getTokenType())
				.orElseThrow(() -> new IllegalStateException("Wrong token type received %s".formatted(tokenResponse.getTokenType())));

			AuthorizationData data =
				new AuthorizationData(tokenResponse.getAccessToken(), tokenType, null, tokenResponse.getExpiresIn() - getTokenRenewThresholdInSeconds());
			data.setRefreshToken(tokenResponse.getRefreshToken());
			data.setOauth2IdToken(tokenResponse.getIdToken());
			return data;
		} catch (JacksonException e) {
			throw new IllegalStateException("Unable to convert token response", e);
		}
	}

	private Integer getTokenRenewThresholdInSeconds() {
		OidcProperties oidcProperties = restClientAuthenticationProperties.getOidc();
		return oidcProperties.getClientType() == ClientType.CONFIDENTIAL ?
			oidcProperties.getConfidentialClient().getTokenRenewThresholdInSeconds() :
			oidcProperties.getPublicClient().getTokenRenewThresholdInSeconds();
	}

}
