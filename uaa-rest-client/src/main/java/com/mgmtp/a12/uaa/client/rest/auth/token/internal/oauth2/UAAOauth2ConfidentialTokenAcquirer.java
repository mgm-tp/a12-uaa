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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.htmlunit.HttpMethod;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.util.NameValuePair;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.TokenRefresher;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.Request;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.UAAHttpTokenAcquirer;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.URLUtils;
import com.mgmtp.a12.uaa.client.rest.config.properties.OidcProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientAuthenticationProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

public class UAAOauth2ConfidentialTokenAcquirer extends UAAHttpTokenAcquirer {

	private static final String VALUE_CONTENT_TYPE = "application/x-www-form-urlencoded";
	private static final String PARAM_CLIENT_ID = "client_id";
	private static final String PARAM_CLIENT_SECRET = "client_secret";
	private static final String PARAM_USERNAME = "username";
	private static final String PARAM_PASSWORD = "password";
	private static final String PARAM_GRANT_TYPE = "grant_type";
	private static final String VALUE_PASSWORD = "password";
	private static final String PARAM_REFRESH_TOKEN = "refresh_token";

	public UAAOauth2ConfidentialTokenAcquirer(UAARestClientProperties uaaRestClientProperties, AuthorizationDataStore authorizationDataStore,
		TokenRefresher tokenRefresher) {
		super(uaaRestClientProperties, authorizationDataStore,
			new Oauth2ResponseAuthorizationDataLocator(uaaRestClientProperties.getAuthenticationConfiguration()), tokenRefresher);
	}

	protected WebClient createWebClient() {
		WebClient webClient = super.createWebClient();
		webClient.getOptions().setRedirectEnabled(false);
		return webClient;
	}

	@Override
	protected Request createLoginRequest() throws MalformedURLException {
		UAARestClientAuthenticationProperties uaaRestClientAuthenticationProperties = uaaRestClientProperties.getAuthenticationConfiguration();
		UriComponentsBuilder uriComponentsBuilder =
			UriComponentsBuilder.fromUriString(getFullUrlWithIdpBasePrefix(getOauth2ConfidentialProperties().getLoginRelative().getUrl()));

		String loginUrl = uriComponentsBuilder.build().toUriString();
		WebRequest request = new WebRequest(new URL(loginUrl), HttpMethod.POST);
		request.setAdditionalHeader(HttpHeaders.CONTENT_TYPE, VALUE_CONTENT_TYPE);
		NameValuePair clientId = new NameValuePair(PARAM_CLIENT_ID, URLUtils.urlEncode((getOauth2ConfidentialProperties().getClientId())));
		NameValuePair grantType = new NameValuePair(PARAM_GRANT_TYPE, VALUE_PASSWORD);
		NameValuePair clientSecret = new NameValuePair(PARAM_CLIENT_SECRET, getOauth2ConfidentialProperties().getClientSecret());
		NameValuePair userName = new NameValuePair(PARAM_USERNAME, URLUtils.urlEncode(uaaRestClientAuthenticationProperties.getUsername()));
		NameValuePair password = new NameValuePair(PARAM_PASSWORD, URLUtils.urlEncode(uaaRestClientAuthenticationProperties.getPassword()));
		String body = "%s&%s&%s&%s&%s".formatted(clientId, grantType, clientSecret, userName, password);
		request.setRequestBody(body);

		return new Request.Builder(request).build();
	}

	@Override
	protected LogoutConfig getLogoutConfig() {
		return new LogoutConfig(getOauth2ConfidentialProperties().getLogoutRelative().getUrl(), HttpMethod.POST);
	}

	@Override
	protected Request createLogoutRequest(AuthorizationData authorizationData) throws MalformedURLException {
		LogoutConfig logoutConfig = getLogoutConfig();
		URL logoutUrl = new URL(getFullUrlWithIdpBasePrefix(logoutConfig.getRelativeLogoutUrl()));
		WebRequest logoutRequest = new WebRequest(logoutUrl, logoutConfig.getMethod());
		logoutRequest.setCharset(StandardCharsets.UTF_8);
		NameValuePair clientId = new NameValuePair(PARAM_CLIENT_ID, URLUtils.urlEncode(getOauth2ConfidentialProperties().getClientId()));
		NameValuePair grantType = new NameValuePair(PARAM_GRANT_TYPE, VALUE_PASSWORD);
		NameValuePair clientSecret = new NameValuePair(PARAM_CLIENT_SECRET, getOauth2ConfidentialProperties().getClientSecret());
		NameValuePair refreshToken = new NameValuePair(PARAM_REFRESH_TOKEN, authorizationData.getRefreshToken());
		String body = "%s&%s&%s&%s".formatted(clientId, grantType, clientSecret, refreshToken);
		logoutRequest.setRequestBody(URLUtils.urlEncode(body));

		return new Request.Builder(logoutRequest).build();
	}

	private String getFullUrlWithIdpBasePrefix(String relativeUrl) {
		String idpBaseUrl = URLUtils.getIdpBaseUrl(getOauth2ConfidentialProperties());
		return URLUtils.getFullUrl(idpBaseUrl, relativeUrl);
	}

	private OidcProperties.ConfidentialClientProperties getOauth2ConfidentialProperties() {
		return uaaRestClientProperties.getAuthenticationConfiguration().getOidc().getConfidentialClient();
	}

}
