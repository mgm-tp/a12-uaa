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
package com.mgmtp.a12.uaa.client.rest.auth.token.internal.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.mgmtp.a12.connector.rest.UrlBuilderSupport;
import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.TokenRefresher;
import com.mgmtp.a12.uaa.client.rest.auth.internal.data.AuthorizeData;
import com.mgmtp.a12.uaa.client.rest.auth.internal.data.TokenData;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

import static com.mgmtp.a12.uaa.client.rest.auth.token.internal.CodeExchangeUtils.generateCodeChallenge;
import static com.mgmtp.a12.uaa.client.rest.auth.token.internal.CodeExchangeUtils.generateCodeVerifier;
import static com.mgmtp.a12.uaa.client.rest.auth.token.internal.CodeExchangeUtils.generateState;

public class JwtTokenRefresher implements TokenRefresher {

	private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenRefresher.class);

	private static final String CONTEXT = "uaa-authentication";
	private static final String ENDPOINT_AUTHORIZE = "authorize";
	private static final String ENDPOINT_TOKEN = "token";
	private static final String PARAM_CODE = "code";
	private static final String PARAM_CODE_VERIFIER = "code_verifier";
	private static final String EXCHANGE_TOKEN_AUTHORIZE_BODY = "{\"code_challenge\":\"%s\", \"state\":\"%s\", \"id_token_hint\":\"%s\"}";

	private AuthorizationDataStore authorizationDataStore;
	private UrlBuilderSupport urlBuilderSupport;
	private RestTemplate restTemplate;

	public JwtTokenRefresher(UAARestClientProperties clientConfiguration, AuthorizationDataStore authorizationDataStore) {
		this.urlBuilderSupport = UrlBuilderSupport.withBaseUrl(clientConfiguration.getUaaBase().getUrl(), CONTEXT);
		this.authorizationDataStore = authorizationDataStore;
		this.restTemplate = new RestTemplate();
	}

	@Override
	public AuthorizationData refreshAuthorizationData() {
		AuthorizationData authorizationData = authorizationDataStore.getAuthorizationData();
		String state = generateState();
		String codeVerifier = generateCodeVerifier();
		String codeChallenge = generateCodeChallenge(codeVerifier);

		AuthorizeData newAuthorizeData = requestAuthorize(state, codeChallenge, authorizationData.getAuthenticationToken());
		TokenData newTokenData = requestToken(newAuthorizeData.getCode(), codeVerifier);
		authorizationData = new AuthorizationData(newTokenData.getAccessToken(), authorizationData.getAuthenticationTokenType(),
			authorizationData.getSessionId(), authorizationData.getUniqueUserIdentification(), Integer.parseInt(newTokenData.getTokenRenewInSeconds()));

		Assert.isTrue(authorizationData.isValid(), "The authorization data is not valid.");
		authorizationDataStore.setAuthorizationData(authorizationData);
		LOGGER.info("Jwt token has been refreshed. It will be refreshed in [%s].".formatted(authorizationData.getTokenRenewInSeconds()));
		return authorizationData;
	}

	private AuthorizeData requestAuthorize(String state, String codeChallenge, String idTokenHint) {
		Assert.notNull(idTokenHint, "The id token hint is not specified.");

		String url = urlBuilderSupport.createBuilder().pathSegment(ENDPOINT_AUTHORIZE)
			.build()
			.toString();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(EXCHANGE_TOKEN_AUTHORIZE_BODY.formatted(codeChallenge, state, idTokenHint), headers);
		return restTemplate.postForObject(url, entity, AuthorizeData.class);
	}

	private TokenData requestToken(String code, String codeVerifier) {
		Assert.notNull(code, "The authorization code is not specified.");

		String url = urlBuilderSupport.createBuilder().pathSegment(ENDPOINT_TOKEN)
			.build()
			.toString();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
		requestBody.add(PARAM_CODE, code);
		requestBody.add(PARAM_CODE_VERIFIER, codeVerifier);
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

		return restTemplate.postForObject(url, requestEntity, TokenData.class);
	}

}
