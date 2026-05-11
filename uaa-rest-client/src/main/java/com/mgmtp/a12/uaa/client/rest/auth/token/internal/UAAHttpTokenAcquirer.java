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
package com.mgmtp.a12.uaa.client.rest.auth.token.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.HttpMethod;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.TokenAcquirer;
import com.mgmtp.a12.uaa.client.rest.auth.TokenRefresher;
import com.mgmtp.a12.uaa.client.rest.auth.internal.WebClientFactory;
import com.mgmtp.a12.uaa.client.rest.auth.internal.locator.AuthorizationDataLocator;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

public abstract class UAAHttpTokenAcquirer implements TokenAcquirer {

	private static final Logger LOGGER = LoggerFactory.getLogger(UAAHttpTokenAcquirer.class);
	private static final Integer MAXIMUM_RETRIES = 2;

	private AuthorizationDataStore authorizationDataStore;
	private AuthorizationDataLocator<Page> authorizationDataLocator;
	private TokenRefresher tokenRefresher;

	protected UAARestClientProperties uaaRestClientProperties;

	public UAAHttpTokenAcquirer(UAARestClientProperties uaaRestClientProperties, AuthorizationDataStore authorizationDataStore,
		AuthorizationDataLocator<Page> authorizationDataLocator,
		TokenRefresher tokenRefresher) {
		this.uaaRestClientProperties = uaaRestClientProperties;
		this.authorizationDataStore = authorizationDataStore;
		this.authorizationDataLocator = authorizationDataLocator;
		this.tokenRefresher = tokenRefresher;
	}

	protected WebClient createWebClient() {
		// Breaking change adapting following https://github.com/HtmlUnit/htmlunit/issues/627
		return WebClientFactory.createWebClient();
	}

	protected abstract Request createLoginRequest() throws MalformedURLException;

	protected abstract LogoutConfig getLogoutConfig();

	protected Request createLogoutRequest(AuthorizationData authorizationData) throws MalformedURLException {
		LogoutConfig logoutConfig = getLogoutConfig();
		if (logoutConfig == null) {
			return null;
		}
		URL logoutUrl = new URL(getFullUrlWithUaaBasePrefix(logoutConfig.getRelativeLogoutUrl()));
		WebRequest logoutRequest = new WebRequest(logoutUrl, logoutConfig.getMethod());
		logoutRequest.setAdditionalHeader(HttpHeaders.AUTHORIZATION,
			"%s %s".formatted(authorizationData.getAuthenticationTokenType().getTypeName(), authorizationData.getAuthenticationToken()));
		logoutRequest.setAdditionalHeader("Accept", "*/*");
		return new Request.Builder(logoutRequest).build();
	}

	@Override
	public AuthorizationData acquireToken() {
		try (WebClient webClient = createWebClient()) {
			Request loginRequest = createLoginRequest();
			LOGGER.info("Logging to server by URL [{}]", loginRequest.getRequest().getUrl().toExternalForm());
			Page loginResponse = acquireTokenResponse(webClient, loginRequest);
			AuthorizationData authorizationData = extractAuthorizationData(loginResponse);
			RefreshTokenScheduler.scheduleTokenRenewal(tokenRefresher, authorizationDataStore, authorizationData.getTokenRenewInSeconds(), MAXIMUM_RETRIES);
			return authorizationData;
		} catch (FailingHttpStatusCodeException | IOException e) {
			throw new RuntimeException("Unable to login", e);
		}
	}

	@Override
	public void releaseToken(AuthorizationData authorizationData, HttpHeaders headers) {

		try (WebClient webClient = createWebClient()) {
			LogoutConfig logoutConfig = getLogoutConfig();
			if (logoutConfig == null) {
				return;
			}
			Request logoutRequest = createLogoutRequest(authorizationData);
			LOGGER.info("Logout to server by URL [{}]", logoutRequest.getRequest().getUrl());
			webClient.getPage(logoutRequest.getRequest());
		} catch (FailingHttpStatusCodeException | IOException e) {
			throw new RuntimeException("Unable to logout", e);
		}

	}

	protected Page acquireTokenResponse(WebClient webClient, Request loginRequest) throws FailingHttpStatusCodeException, IOException {
		Page loginResponsePage = webClient.getPage(loginRequest.getRequest());
		if (HttpStatus.OK.value() != loginResponsePage.getWebResponse().getStatusCode()) {
			throw new RuntimeException("Unable to login response %s:%s".formatted(loginResponsePage.getWebResponse().getStatusCode(),
				loginResponsePage.getWebResponse().getStatusMessage()));
		}
		return loginResponsePage;
	}

	protected AuthorizationData extractAuthorizationData(Page loginResponsePage) {
		AuthorizationData authorizationData = authorizationDataLocator.convert(loginResponsePage);
		Assert.isTrue(authorizationData.isValid(), "The authorization data is not valid.");
		LOGGER.info("Login successful and token has been acquired.");
		return authorizationData;
	}

	protected String getFullUrlWithUaaBasePrefix(String relativeUrl) {
		String uaaBaseUrl = uaaRestClientProperties.getUaaBase().getUrl();
		return URLUtils.getFullUrl(uaaBaseUrl, relativeUrl);
	}

	public static class LogoutConfig {

		private String relativeLogoutUrl;
		private HttpMethod method;

		public LogoutConfig(String relativeLogoutUrl, HttpMethod method) {
			this.relativeLogoutUrl = relativeLogoutUrl;
			this.method = method;
		}

		public String getRelativeLogoutUrl() {
			return relativeLogoutUrl;
		}

		public HttpMethod getMethod() {
			return method;
		}

	}
}
