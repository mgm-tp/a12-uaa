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

import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.htmlunit.HttpMethod;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.util.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.TokenRefresher;
import com.mgmtp.a12.uaa.client.rest.auth.internal.WebClientFactory;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.URLUtils;
import com.mgmtp.a12.uaa.client.rest.config.properties.OidcProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

public class Oauth2TokenRefresher implements TokenRefresher {

	private static final Logger LOGGER = LoggerFactory.getLogger(Oauth2TokenRefresher.class);

	private static final String VALUE_CONTENT_TYPE = "application/x-www-form-urlencoded";
	private static final String TOKEN_URL = "/token";
	private static final String KEY_CLIENT_ID = "client_id";
	private static final String KEY_GRANT_TYPE = "grant_type";
	private static final String KEY_REFRESH_TOKEN = "refresh_token";
	private static final String KEY_CLIENT_SECRET = "client_secret";

	private AuthorizationDataStore authorizationDataStore;
	private Oauth2ResponseAuthorizationDataLocator authorizationDataLocator;
	private OidcProperties oidcProperties;
	private WebClient webClient;

	public Oauth2TokenRefresher(UAARestClientProperties uaaRestClientProperties, AuthorizationDataStore authorizationDataStore) {
		this.oidcProperties = uaaRestClientProperties.getAuthenticationConfiguration().getOidc();
		this.authorizationDataLocator = new Oauth2ResponseAuthorizationDataLocator(uaaRestClientProperties.getAuthenticationConfiguration());
		this.authorizationDataStore = authorizationDataStore;
		this.webClient = WebClientFactory.createWebClient();
	}

	@Override
	public AuthorizationData refreshAuthorizationData() throws Exception {
		AuthorizationData authorizationData = authorizationDataStore.getAuthorizationData();
		String clientIdStr;
		String clientSecretStr = null;
		String baseUrlStr;
		if (oidcProperties.getClientType() == ClientType.PUBLIC) {
			clientIdStr = oidcProperties.getPublicClient().getClientId();
			baseUrlStr = URLUtils.getIdpBaseUrl(oidcProperties.getPublicClient());
		} else {
			clientIdStr = oidcProperties.getConfidentialClient().getClientId();
			clientSecretStr = oidcProperties.getConfidentialClient().getClientSecret();
			baseUrlStr = URLUtils.getIdpBaseUrl(oidcProperties.getConfidentialClient());
		}
		WebRequest tokenRequest = new WebRequest(new URL(baseUrlStr + TOKEN_URL), HttpMethod.POST);
		tokenRequest.setAdditionalHeader(HttpHeaders.CONTENT_TYPE, VALUE_CONTENT_TYPE);
		tokenRequest.setCharset(StandardCharsets.UTF_8);
		NameValuePair clientId = new NameValuePair(KEY_CLIENT_ID, URLUtils.urlEncode(clientIdStr));
		NameValuePair grantType = new NameValuePair(KEY_GRANT_TYPE, KEY_REFRESH_TOKEN);
		NameValuePair refreshToken = new NameValuePair(KEY_REFRESH_TOKEN, authorizationData.getRefreshToken());
		NameValuePair clientSecret = new NameValuePair(KEY_CLIENT_SECRET, StringUtils.trimToEmpty(clientSecretStr));
		String body = "%s&%s&%s&%s".formatted(clientId, grantType, refreshToken, clientSecret);
		tokenRequest.setRequestBody(body);

		Page page = webClient.getPage(tokenRequest);
		AuthorizationData newAuthorizationData = authorizationDataLocator.convert(page);
		newAuthorizationData.setUniqueUserIdentification(authorizationData.getUniqueUserIdentification());

		Assert.isTrue(newAuthorizationData.isValid(), "The authorization data is not valid.");
		authorizationDataStore.setAuthorizationData(newAuthorizationData);
		LOGGER.info("Oauth2 token has been refreshed. It will be refreshed in [%s]".formatted(newAuthorizationData.getTokenRenewInSeconds()));
		return newAuthorizationData;
	}

}
