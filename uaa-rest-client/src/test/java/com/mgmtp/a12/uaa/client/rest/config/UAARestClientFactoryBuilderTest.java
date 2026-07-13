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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;

import com.mgmtp.a12.connector.rest.ResponseErrorHandler;
import com.mgmtp.a12.uaa.client.rest.config.common.UrlProperty;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientAuthenticationProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

public class UAARestClientFactoryBuilderTest {

	private UAARestClientProperties uaaRestClientProperties;
	private ClientHttpRequestInterceptor[] interceptors;
	private ResponseErrorHandler[] errorHandlers;
	private List<HttpMessageConverter<?>> messageConverters;

	@BeforeEach
	void setUp() {
		uaaRestClientProperties = new UAARestClientProperties();
		uaaRestClientProperties.setSelfconfiguration(new UrlProperty("http://localhost:8080/uaa-authentication/selfconfigure"));
		uaaRestClientProperties.setUaaBase(new UrlProperty("http://localhost:8080"));
		uaaRestClientProperties.setAuthorizationHeaderName("Authorization");
		uaaRestClientProperties.setGeneratedTokenHeaderName("access_token");
		uaaRestClientProperties.setGeneratedTokenRenewInSecondsHeaderName("token_renew_in_seconds");
		UAARestClientAuthenticationProperties authConfiguration = new UAARestClientAuthenticationProperties();
		authConfiguration.setUsername("admin");
		authConfiguration.setPassword("admin");
		uaaRestClientProperties.setAuthenticationType(AuthenticationType.DELEGATED);
		uaaRestClientProperties.setAuthenticationConfiguration(authConfiguration);

		interceptors = new ClientHttpRequestInterceptor[0];
		errorHandlers = new ResponseErrorHandler[0];
		messageConverters = new ArrayList<>();
	}

	@Test
	void buildTest() throws GeneralSecurityException, IOException {
		try (MockedStatic<UAARestClientPropertiesResolver> mockedStatic = Mockito.mockStatic(UAARestClientPropertiesResolver.class)) {
			mockedStatic.when(() -> UAARestClientPropertiesResolver.resolve(Mockito.any())).thenReturn(uaaRestClientProperties);

			UAARestClientFactory uaaRestClientFactory = UAARestClientFactoryBuilder
				.withConfiguration(uaaRestClientProperties)
				.withInterceptors(interceptors)
				.withErrorHandlers(errorHandlers)
				.withMessageConverters(messageConverters)
				.build();

			Assertions.assertNotNull(uaaRestClientFactory.getRestServerConnectorFactory());
			Assertions.assertNotNull(uaaRestClientFactory.getAuthenticationRestClient());
			Assertions.assertNotNull(uaaRestClientFactory.getAuthorizationRestClient());
			Assertions.assertNotNull(uaaRestClientFactory.getGetConnector());
			Assertions.assertNotNull(uaaRestClientFactory.getPostConnector());
			Assertions.assertNotNull(uaaRestClientFactory.getPutConnector());
			Assertions.assertNotNull(uaaRestClientFactory.getDeleteConnector());
			Assertions.assertNotNull(uaaRestClientFactory.getHeadConnector());
			Assertions.assertNotNull(uaaRestClientFactory.getOptionsConnector());
		}
	}

}