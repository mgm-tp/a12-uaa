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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.HttpMethod;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.util.NameValuePair;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.TokenRefresher;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.Request;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.UAALoginPageTokenAcquirer;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.URLUtils;
import com.mgmtp.a12.uaa.client.rest.config.properties.OidcProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.SsoProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

public class UAAOauth2PublicTokenAcquirer extends UAALoginPageTokenAcquirer {

	private static final String VALUE_CONTENT_TYPE = "application/x-www-form-urlencoded";
	private static final String PARAM_VERIFIER = "verifier";
	private static final String PARAM_CLIENT_ID = "client_id";
	private static final String PARAM_CODE = "code";
	private static final String PARAM_RESPONSE_TYPE = "response_type";
	private static final String PARAM_SCOPE = "scope";
	private static final String PARAM_STATE = "state";
	private static final String PARAM_CODE_CHALLENGE = "code_challenge";
	private static final String PARAM_CODE_CHALLENGE_METHOD = "code_challenge_method";

	private static final String PARAM_REDIRECT_URI = "redirect_uri";
	private static final String PARAM_CODE_VERIFIER = "code_verifier";
	private static final String PARAM_GRANT_TYPE = "grant_type";
	private static final String VALUE_AUTHORIZATION_CODE = "authorization_code";
	private static final String VALUE_CODE = "code";
	private static final String VALUE_OPENID = "openid";
	private static final String VALUE_S256 = "S256";
	private static final String PARAM_ID_TOKEN_HINT = "id_token_hint";

	public UAAOauth2PublicTokenAcquirer(UAARestClientProperties clientConfiguration, AuthorizationDataStore authorizationDataStore,
		TokenRefresher tokenRefresher) {
		super(clientConfiguration, authorizationDataStore, new Oauth2ResponseAuthorizationDataLocator(clientConfiguration.getAuthenticationConfiguration()),
			tokenRefresher);
	}

	@Override
	protected WebClient createWebClient() {
		WebClient webClient = super.createWebClient();
		webClient.getOptions().setRedirectEnabled(false);
		return webClient;
	}

	@Override
	protected Request createLoginRequest() throws MalformedURLException {
		String verifier = generateVerifier();
		String state = generateState();
		String challenge = generateChallenge(verifier);

		// Main login url
		UriComponentsBuilder uriComponentsBuilder =
			UriComponentsBuilder
				.fromHttpUrl(getFullUrlWithIdpBasePrefix(getOauth2PublicProperties().getLoginRelative().getUrl()))
				.queryParam(PARAM_CLIENT_ID, URLUtils.urlEncode(getOauth2PublicProperties().getClientId()))
				.queryParam(PARAM_REDIRECT_URI, "{redirectUri}")
				.queryParam(PARAM_RESPONSE_TYPE, VALUE_CODE)
				.queryParam(PARAM_SCOPE, VALUE_OPENID)
				.queryParam(PARAM_STATE, state)
				.queryParam(PARAM_CODE_CHALLENGE, challenge)
				.queryParam(PARAM_CODE_CHALLENGE_METHOD, VALUE_S256);

		// Append login redirect url
		String loginUrl = uriComponentsBuilder
			.buildAndExpand(URLUtils.urlEncode(getFullUrlWithUaaBasePrefix(getOauth2PublicProperties().getLoginRedirectRelative().getUrl()))).toUriString();
		WebRequest request = new WebRequest(new URL(loginUrl), HttpMethod.GET);

		return new Request.Builder(request)
			.withParameter(PARAM_VERIFIER, verifier)
			.withParameter(PARAM_STATE, state)
			.build();
	}

	@Override
	protected Page exchangeAuthorizationCode(WebClient webClient, Page authenticatedPage, Request loginRequest)
		throws FailingHttpStatusCodeException, IOException {
		String responseHeaderValue = authenticatedPage.getWebResponse().getResponseHeaderValue(HttpHeaders.LOCATION);
		UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(responseHeaderValue).build();
		String state = uriComponents.getQueryParams().get(PARAM_STATE).get(0);
		String generatedState = loginRequest.getParameters().get(PARAM_STATE).toString();
		if (!StringUtils.equals(generatedState, state)) {
			throw new RuntimeException("State codes doesn't match. Generated=[%s], retrieved=[%s]".formatted(generatedState, state));
		}
		String code = uriComponents.getQueryParams().get(PARAM_CODE).get(0);

		WebRequest tokenRequest = new WebRequest(
			new URL(getFullUrlWithIdpBasePrefix(getOauth2PublicProperties().getTokenExchangeRelative().getUrl())), HttpMethod.POST);
		tokenRequest.setAdditionalHeader(HttpHeaders.CONTENT_TYPE, VALUE_CONTENT_TYPE);
		tokenRequest.setCharset(StandardCharsets.UTF_8);
		NameValuePair pair1 = new NameValuePair(PARAM_CLIENT_ID, URLUtils.urlEncode(getOauth2PublicProperties().getClientId()));
		NameValuePair pair2 = new NameValuePair(PARAM_CODE, code);
		NameValuePair pair3 =
			new NameValuePair(PARAM_REDIRECT_URI,
				URLUtils.urlEncode(getFullUrlWithUaaBasePrefix(getOauth2PublicProperties().getLoginRedirectRelative().getUrl())));
		NameValuePair pair4 = new NameValuePair(PARAM_CODE_VERIFIER, loginRequest.getParameters().get(PARAM_VERIFIER).toString());
		NameValuePair pair5 = new NameValuePair(PARAM_GRANT_TYPE, VALUE_AUTHORIZATION_CODE);
		String body = "%s&%s&%s&%s&%s".formatted(pair1, pair2, pair3, pair4, pair5);
		tokenRequest.setRequestBody(body);

		return webClient.getPage(tokenRequest);

	}

	@Override
	protected SsoProperties getSsoProperties() {
		return getOauth2PublicProperties().getSsoConfiguration();
	}

	@Override
	protected LogoutConfig getLogoutConfig() {
		return new LogoutConfig(getOauth2PublicProperties().getLogoutRelative().getUrl(), HttpMethod.GET);
	}

	@Override
	protected Request createLogoutRequest(AuthorizationData authorizationData) throws MalformedURLException {
		LogoutConfig logoutConfig = getLogoutConfig();
		UriComponentsBuilder logoutUriComponentsBuilder =
			UriComponentsBuilder
				.fromHttpUrl(getFullUrlWithIdpBasePrefix(logoutConfig.getRelativeLogoutUrl()))
				.queryParam(PARAM_ID_TOKEN_HINT, authorizationData.getOauth2IdToken());
		WebRequest logoutRequest = new WebRequest(new URL(logoutUriComponentsBuilder.toUriString()), logoutConfig.getMethod());
		return new Request
			.Builder(logoutRequest)
			.build();
	}

	private String generateVerifier() {
		return generateString(32);
	}

	private String generateState() {
		return generateString(20);
	}

	private String generateString(int len) {
		SecureRandom sr = new SecureRandom();
		byte[] code = new byte[len];
		sr.nextBytes(code);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(code);
	}

	private String generateChallenge(String verifier) {
		byte[] bytes = verifier.getBytes(StandardCharsets.US_ASCII);
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(bytes, 0, bytes.length);
			byte[] digest = md.digest();
			String challenge = org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(digest);
			return challenge;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unable to generate OAUTH2 challenge", e);
		}
	}

	private String getFullUrlWithIdpBasePrefix(String relativeUrl) {
		String idpBaseUrl = URLUtils.getIdpBaseUrl(getOauth2PublicProperties());
		return URLUtils.getFullUrl(idpBaseUrl, relativeUrl);
	}

	private OidcProperties.PublicClientProperties getOauth2PublicProperties() {
		return uaaRestClientProperties.getAuthenticationConfiguration().getOidc().getPublicClient();
	}

}
