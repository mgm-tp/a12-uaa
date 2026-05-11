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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mgmtp.a12.uaa.client.rest.auth.token.internal.URLUtils;
import com.mgmtp.a12.uaa.client.rest.config.common.UrlProperty;
import com.mgmtp.a12.uaa.client.rest.config.properties.OidcProperties;

public class Oauth2UtilsTest {

	private OidcProperties.ConfidentialClientProperties confidentialClientProperties;

	@BeforeEach
	void setUp() {
		confidentialClientProperties = new OidcProperties.ConfidentialClientProperties();
		confidentialClientProperties.setClientId("clientIdTest");
		confidentialClientProperties.setRealmName("realmNameTest");
		confidentialClientProperties.setIdpBase(new UrlProperty("http://localhost:9090"));
		confidentialClientProperties.setClientSecret("secret");
	}

	@Test
	void getBaseUrlTest() {
		String baseUrl = URLUtils.getIdpBaseUrl(confidentialClientProperties);
		Assertions.assertEquals("http://localhost:9090/realms/realmNameTest/protocol/openid-connect", baseUrl);
	}

}