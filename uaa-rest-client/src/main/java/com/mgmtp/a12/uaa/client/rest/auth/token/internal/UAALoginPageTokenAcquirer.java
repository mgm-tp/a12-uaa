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
import java.util.List;
import java.util.Optional;

import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.TokenRefresher;
import com.mgmtp.a12.uaa.client.rest.auth.internal.locator.AuthorizationDataLocator;
import com.mgmtp.a12.uaa.client.rest.config.properties.SsoProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientAuthenticationProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

public abstract class UAALoginPageTokenAcquirer extends UAAHttpTokenAcquirer {

	public UAALoginPageTokenAcquirer(UAARestClientProperties uaaRestClientProperties, AuthorizationDataStore authorizationDataStore,
		AuthorizationDataLocator<Page> authorizationDataLocator,
		TokenRefresher tokenRefresher) {
		super(uaaRestClientProperties, authorizationDataStore, authorizationDataLocator, tokenRefresher);
	}

	@Override
	protected Page acquireTokenResponse(WebClient webClient, Request loginRequest) throws FailingHttpStatusCodeException, IOException {
		Page loginResponsePage = super.acquireTokenResponse(webClient, loginRequest);
		Page authenticatedPage = handleIDPLoginPage(webClient, loginResponsePage, loginRequest);
		Page tokenExchangePage = exchangeAuthorizationCode(webClient, authenticatedPage, loginRequest);

		if (HttpStatus.OK.value() != tokenExchangePage.getWebResponse().getStatusCode()) {
			throw new RuntimeException("Unable to token exchange response %s:%s".formatted(tokenExchangePage.getWebResponse().getStatusCode(),
				tokenExchangePage.getWebResponse().getStatusMessage()));
		}
		return tokenExchangePage;
	}

	protected Page handleIDPLoginPage(WebClient webClient, Page loginResponsePage, Request loginRequest) throws IOException {
		SsoProperties ssoProperties = getSsoProperties();
		if (ssoProperties == null) {
			throw new RuntimeException("The ssoConfiguration is null then can not process the idp login page");
		}

		HtmlInput username = getElement(loginResponsePage, ssoProperties.getUsernameXpath());
		HtmlInput password = getElement(loginResponsePage, ssoProperties.getPasswordXpath());
		HtmlElement loginInButton = getElement(loginResponsePage, ssoProperties.getLoginButtonXpath());

		if (loginInButton == null) {
			throw new RuntimeException("Unable to find login button in XPATH: %s".formatted(ssoProperties.getLoginButtonXpath()));
		}
		UAARestClientAuthenticationProperties uaaRestClientAuthenticationProperties = uaaRestClientProperties.getAuthenticationConfiguration();
		Optional.ofNullable(username).ifPresent(u -> u.setValue(uaaRestClientAuthenticationProperties.getUsername()));
		Optional.ofNullable(password).ifPresent(p -> p.setValue(uaaRestClientAuthenticationProperties.getPassword()));

		return loginInButton.click();

	}

	protected abstract Page exchangeAuthorizationCode(WebClient webClient, Page authenticatedPage, Request loginRequest)
		throws FailingHttpStatusCodeException, IOException;

	protected abstract SsoProperties getSsoProperties();

	private <T> T getElement(Page page, String xpath) {
		if (page.isHtmlPage()) {
			List<Object> elements = ((HtmlPage) page).getByXPath(xpath);
			if (!CollectionUtils.isEmpty(elements)) {
				return (T) elements.get(0);
			}
		}
		return null;
	}

}
