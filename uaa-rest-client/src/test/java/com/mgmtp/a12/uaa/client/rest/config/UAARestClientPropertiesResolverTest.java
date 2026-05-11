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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import com.mgmtp.a12.uaa.client.rest.auth.token.internal.oauth2.ClientType;
import com.mgmtp.a12.uaa.client.rest.config.common.UrlProperty;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientAuthenticationProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

public class UAARestClientPropertiesResolverTest {

	private static final String SELFCONFIGURATION_URL = "http://localhost:8080/uaa-authentication/selfconfigure";

	private UAARestClientProperties uaaRestClientProperties;

	@BeforeEach
	void setUp() {
		uaaRestClientProperties = new UAARestClientProperties();
		uaaRestClientProperties.setUaaBase(new UrlProperty("http://localhost:8080"));
		uaaRestClientProperties.setAuthorizationHeaderName("Authorization");
		uaaRestClientProperties.setGeneratedTokenHeaderName("access_token");
		uaaRestClientProperties.setGeneratedTokenRenewInSecondsHeaderName("token_renew_in_seconds");
		UAARestClientAuthenticationProperties authConfiguration = new UAARestClientAuthenticationProperties();
		authConfiguration.setUsername("admin");
		authConfiguration.setPassword("admin");
		uaaRestClientProperties.setAuthenticationType(AuthenticationType.LOCAL);
		uaaRestClientProperties.setAuthenticationConfiguration(authConfiguration);
		uaaRestClientProperties.setSelfconfiguration(new UrlProperty());
	}

	@Test
	void resolveTestWithSelfconfigurationUrl() {
		uaaRestClientProperties.getSelfconfiguration().setUrl(SELFCONFIGURATION_URL);
		try (MockedConstruction<ClientSelfconfigurationReader> mockedConstruction = Mockito.mockConstruction(ClientSelfconfigurationReader.class,
			(reader, context) -> Mockito.when(reader.readSelfconfiguration(SELFCONFIGURATION_URL, null,
					"admin", "admin", AuthenticationType.LOCAL.name(), null, ClientType.CONFIDENTIAL, null, "Authorization"))
				.thenReturn(uaaRestClientProperties))) {

			UAARestClientPropertiesResolver.resolve(uaaRestClientProperties);

			Assertions.assertEquals(1, mockedConstruction.constructed().size());
			Mockito.verify(mockedConstruction.constructed().get(0), Mockito.atLeastOnce())
				.readSelfconfiguration(SELFCONFIGURATION_URL, null, "admin", "admin", AuthenticationType.LOCAL
					.name(), null, ClientType.CONFIDENTIAL, null, "Authorization");
		}
	}

	@Test
	void resolveTestWithEmptySelfconfigurationUrl() {
		uaaRestClientProperties.getSelfconfiguration().setUrl("");
		try (MockedConstruction<ClientSelfconfigurationReader> mockedConstruction = Mockito.mockConstruction(ClientSelfconfigurationReader.class,
			(reader, context) -> Mockito.when(reader.readSelfconfiguration("", null, "admin",
					"admin", AuthenticationType.LOCAL.name(), null, ClientType.PUBLIC, null, "Authorization"))
				.thenReturn(uaaRestClientProperties))) {

			UAARestClientPropertiesResolver.resolve(uaaRestClientProperties);

			Assertions.assertEquals(0, mockedConstruction.constructed().size());
		}
	}

}