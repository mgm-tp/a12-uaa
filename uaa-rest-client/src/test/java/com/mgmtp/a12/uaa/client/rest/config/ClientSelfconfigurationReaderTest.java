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
package com.mgmtp.a12.uaa.client.rest.config;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.mgmtp.a12.uaa.client.rest.auth.token.internal.TokenType;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.oauth2.ClientType;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientAuthenticationProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;
import com.mgmtp.a12.uaa.client.rest.config.selfconfiguration.ClientSelfconfiguration;
import com.mgmtp.a12.uaa.client.rest.config.selfconfiguration.OidcConfiguration;
import com.mgmtp.a12.uaa.client.rest.config.selfconfiguration.SsoConfiguration;
import com.mgmtp.a12.uaa.client.rest.config.selfconfiguration.TokenConfiguration;

public class ClientSelfconfigurationReaderTest {

	private static final String SELF_CONFIGURE_URL = "http://localhost:8080/uaa-authentication/selfconfigure";
	private static final String BASE_URL = "http://localhost:8080";

	private ClientSelfconfigurationReader selfconfigurationReader;
	private ClientSelfconfiguration clientSelfconfiguration;

	@BeforeEach
	void setUp() {
		clientSelfconfiguration = new ClientSelfconfiguration();
		clientSelfconfiguration.setApplicationBaseUrl(BASE_URL);
		clientSelfconfiguration.setUaaBaseUrl(BASE_URL);
		clientSelfconfiguration.setExcludedDelegatedContexts(new String[] { "/api" });
		OidcConfiguration oidcConfiguration = new OidcConfiguration();
		oidcConfiguration.setTokenType(TokenType.BEARER);
		SsoConfiguration ssoConfiguration = new SsoConfiguration();
		ssoConfiguration.setUsernameXpath("//input[@name='username']");
		ssoConfiguration.setPasswordXpath("//input[@name='password']");
		ssoConfiguration.setLoginButtonXpath("//button[@name='login']");

		OidcConfiguration.PublicClientConfiguration publicClientConfiguration = new OidcConfiguration.PublicClientConfiguration();
		publicClientConfiguration.setIdpBaseUrl("http://localhost:9090");
		publicClientConfiguration.setClientId("clientIdTest");
		publicClientConfiguration.setRealmName("realmNameTest");

		publicClientConfiguration.setSsoConfiguration(ssoConfiguration);
		publicClientConfiguration.setLoginRedirectRelativeUrl("/auth");
		publicClientConfiguration.setTokenExchangeRelativeUrl("/token");
		publicClientConfiguration.setLoginRedirectRelativeUrl("/callback");
		oidcConfiguration.setPublicClient(publicClientConfiguration);
		clientSelfconfiguration.setOidc(oidcConfiguration);

		TokenConfiguration oauth2Token = new TokenConfiguration();
		oauth2Token.setTokenType(TokenType.BEARER);
		oauth2Token.setAuthorizationHeaderName("Authorization");
		List<TokenConfiguration> tokenConfigurations = new ArrayList<>();
		tokenConfigurations.add(oauth2Token);
		clientSelfconfiguration.setTokens(tokenConfigurations);
		selfconfigurationReader = new ClientSelfconfigurationReader();
	}

	@Test
	void readSelfconfigurationTestSuccessful() {
		RestClient restClient = Mockito.mock(RestClient.class);
		RestClient.RequestHeadersUriSpec requestHeadersUriSpec = Mockito.mock(RestClient.RequestHeadersUriSpec.class);
		RestClient.RequestHeadersSpec requestHeadersSpec = Mockito.mock(RestClient.RequestHeadersSpec.class);
		RestClient.ResponseSpec responseSpec = Mockito.mock(RestClient.ResponseSpec.class);

		try (MockedStatic<RestClient> mockedStatic = Mockito.mockStatic(RestClient.class)) {
			mockedStatic.when(RestClient::create).thenReturn(restClient);
			Mockito.when(restClient.get()).thenReturn(requestHeadersUriSpec);
			Mockito.when(requestHeadersUriSpec.uri(Mockito.anyString())).thenReturn(requestHeadersSpec);
			Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
			Mockito.when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
			Mockito.when(responseSpec.body(ClientSelfconfiguration.class)).thenReturn(clientSelfconfiguration);

			UAARestClientProperties uaaRestClientProperties = selfconfigurationReader.readSelfconfiguration(
				SELF_CONFIGURE_URL,
				BASE_URL,
				"admin",
				"admin",
				AuthenticationType.OAUTH2.name(),
				"abc123",
				ClientType.PUBLIC,
				null,
				"Authorization");
			Assertions.assertEquals(BASE_URL, uaaRestClientProperties.getUaaBase().getUrl());
			UAARestClientAuthenticationProperties authenticationConfiguration = uaaRestClientProperties.getAuthenticationConfiguration();
			Assertions.assertEquals("admin", authenticationConfiguration.getUsername());
			Assertions.assertEquals("admin", authenticationConfiguration.getPassword());
			Assertions.assertEquals(
				clientSelfconfiguration.getOidc().getPublicClient().getSsoConfiguration().getPasswordXpath(),
				authenticationConfiguration.getOidc().getPublicClient().getSsoConfiguration().getPasswordXpath());
		}
	}

	@Test
	void readSelfconfigurationTestFail() {
		RestClient restClient = Mockito.mock(RestClient.class);
		RestClient.RequestHeadersUriSpec requestHeadersUriSpec = Mockito.mock(RestClient.RequestHeadersUriSpec.class);
		RestClient.RequestHeadersSpec requestHeadersSpec = Mockito.mock(RestClient.RequestHeadersSpec.class);
		RestClient.ResponseSpec responseSpec = Mockito.mock(RestClient.ResponseSpec.class);

		try (MockedStatic<RestClient> mockedStatic = Mockito.mockStatic(RestClient.class)) {
			mockedStatic.when(RestClient::create).thenReturn(restClient);
			Mockito.when(restClient.get()).thenReturn(requestHeadersUriSpec);
			Mockito.when(requestHeadersUriSpec.uri(Mockito.anyString())).thenReturn(requestHeadersSpec);
			Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
			Mockito.when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
			Mockito.when(responseSpec.body(ClientSelfconfiguration.class)).thenThrow(RestClientException.class);

			RuntimeException runtimeException = Assertions.assertThrows(RuntimeException.class, () -> selfconfigurationReader
				.readSelfconfiguration(SELF_CONFIGURE_URL, BASE_URL, "admin", "admin", AuthenticationType.OAUTH2.name(),
					"abc123", ClientType.PUBLIC, null, "Authorization"));
		}
	}

}