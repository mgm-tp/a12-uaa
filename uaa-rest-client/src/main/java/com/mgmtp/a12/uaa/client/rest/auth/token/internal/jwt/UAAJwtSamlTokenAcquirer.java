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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.HttpMethod;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.util.NameValuePair;
import org.springframework.web.util.UriComponentsBuilder;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.TokenRefresher;
import com.mgmtp.a12.uaa.client.rest.auth.internal.data.AuthorizeData;
import com.mgmtp.a12.uaa.client.rest.auth.internal.locator.AuthorizationDataLocator;
import com.mgmtp.a12.uaa.client.rest.auth.internal.locator.JWTResponseAuthorizationDataLocator;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.CodeExchangeUtils;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.Request;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.UAALoginPageTokenAcquirer;
import com.mgmtp.a12.uaa.client.rest.config.properties.SsoProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientAuthenticationProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

import tools.jackson.databind.ObjectMapper;

public class UAAJwtSamlTokenAcquirer extends UAALoginPageTokenAcquirer {
	private static final String EXCHANGE_AUTHORIZATION_CODE_TO_TOKEN_PARAM = "exchangeAuthorizationCodeToToken";
	private static final String EXCHANGE_TOKEN_RELATIVE_URL = "uaa-authentication/exchangeAuthorizationCodeToToken";
	private static final String EXCHANGE_TOKEN_AUTHORIZE_RELATIVE_URL = "uaa-authentication/exchangeAuthorizationCodeToToken/authorize";
	private static final String EXCHANGE_TOKEN_AUTHORIZE_BODY = "{\"code_challenge\":\"%s\", \"state\":\"%s\"}";
	private final ObjectMapper mapper = new ObjectMapper();

	public UAAJwtSamlTokenAcquirer(UAARestClientProperties clientConfiguration, AuthorizationDataStore authorizationDataStore, TokenRefresher tokenRefresher) {
		super(clientConfiguration, authorizationDataStore, new JWTResponseAuthorizationDataLocator(clientConfiguration.getGeneratedTokenHeaderName(),
			clientConfiguration.getGeneratedTokenRenewInSecondsHeaderName()), tokenRefresher);
	}

	public UAAJwtSamlTokenAcquirer(UAARestClientProperties clientConfiguration, AuthorizationDataStore authorizationDataStore,
		AuthorizationDataLocator<Page> authorizationDataLocator,
		TokenRefresher tokenRefresher) {
		super(clientConfiguration, authorizationDataStore, authorizationDataLocator, tokenRefresher);
	}

	@Override
	protected Request createLoginRequest() throws MalformedURLException {
		UAARestClientAuthenticationProperties uaaRestClientAuthenticationProperties = uaaRestClientProperties.getAuthenticationConfiguration();
		URL loginUrl = new URL(getFullUrlWithUaaBasePrefix(uaaRestClientAuthenticationProperties.getSaml().getLoginRelative().getUrl()));
		WebRequest request = new WebRequest(loginUrl, HttpMethod.GET);
		request.setAdditionalHeader("Accept", "*/*");
		return new Request.Builder(request).build();
	}

	protected Page exchangeAuthorizationCode(WebClient webClient, Page authenticatedPage, Request loginRequest)
		throws FailingHttpStatusCodeException, IOException {
		// Authorization code is set under cookie before redirect to client
		if (isExchangeAuthorizationCodeRedirectUrl(authenticatedPage.getUrl())) {
			String state = CodeExchangeUtils.generateState();
			String codeVerifier = CodeExchangeUtils.generateCodeVerifier();
			String codeChallenge = CodeExchangeUtils.generateCodeChallenge(codeVerifier);

			URL exchangeTokenAuthorize = new URL(getFullUrlWithUaaBasePrefix(EXCHANGE_TOKEN_AUTHORIZE_RELATIVE_URL));
			var authorizeRequest = new WebRequest(exchangeTokenAuthorize, HttpMethod.POST);
			authorizeRequest.setAdditionalHeader("Content-Type", "application/json");
			authorizeRequest.setCharset(StandardCharsets.UTF_8);
			authorizeRequest.setRequestBody(EXCHANGE_TOKEN_AUTHORIZE_BODY.formatted(codeChallenge, state));
			Page authorizedPage = webClient.getPage(authorizeRequest);

			AuthorizeData authorizeData = mapper.readValue(authorizedPage.getWebResponse().getContentAsString(), AuthorizeData.class);

			if (!state.equals(authorizeData.getState())) {
				throw new RuntimeException("state is different!");
			}

			URL exchangeTokenByAuthorizationUrl = new URL(getFullUrlWithUaaBasePrefix(EXCHANGE_TOKEN_RELATIVE_URL));
			WebRequest request = new WebRequest(exchangeTokenByAuthorizationUrl, HttpMethod.POST);
			request.setRequestParameters(List.of(new NameValuePair("code_verifier", codeVerifier)));

			return webClient.getPage(request);
		}
		return authenticatedPage;
	}

	@Override
	protected SsoProperties getSsoProperties() {
		return uaaRestClientProperties.getAuthenticationConfiguration().getSaml().getSsoConfiguration();
	}

	@Override
	protected LogoutConfig getLogoutConfig() {
		UAARestClientAuthenticationProperties uaaRestClientAuthenticationProperties = uaaRestClientProperties.getAuthenticationConfiguration();
		return new LogoutConfig(uaaRestClientAuthenticationProperties.getSaml().getLogoutRelative().getUrl(), HttpMethod.POST);
	}

	private boolean isExchangeAuthorizationCodeRedirectUrl(URL url) {
		try {
			return UriComponentsBuilder.fromUri(url.toURI()).build().getQueryParams().getFirst(EXCHANGE_AUTHORIZATION_CODE_TO_TOKEN_PARAM)
				.equals(String.valueOf(true));
		} catch (Exception e) {
			return false;
		}
	}
}
