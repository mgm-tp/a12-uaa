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
import java.io.InputStream;
import java.util.Base64;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

public class UAAAPIKeyTokenAcquirerTest {

	private static final String API_KEY_RESOURCE = "classpath:/apikey/client.crt";
	private static final String API_KEY_RESOURCE_NOT_FOUND = "classpath:/apikey/fileNotFound.crt";

	private UAARestClientProperties uaaRestClientConfiguration = new UAARestClientProperties();
	private UAACertificateTokenAcquirer uaaAPIKeyTokenAcquirer;

	@Test
	void acquireTokenTestSuccessful() throws IOException {
		uaaRestClientConfiguration.setApiKeyResource(API_KEY_RESOURCE);
		uaaAPIKeyTokenAcquirer = new UAACertificateTokenAcquirer(uaaRestClientConfiguration.getApiKeyResource(), TokenType.API_KEY);
		AuthorizationData authorizationData = uaaAPIKeyTokenAcquirer.acquireToken();

		try (InputStream inputStream = new DefaultResourceLoader().getResource(API_KEY_RESOURCE).getInputStream()) {
			byte[] bytes = IOUtils.toByteArray(inputStream);
			String certificate = Base64.getEncoder().encodeToString(bytes);
			Assertions.assertEquals(certificate, authorizationData.getAuthenticationToken());
			Assertions.assertEquals(TokenType.API_KEY, authorizationData.getAuthenticationTokenType());
			Assertions.assertNull(authorizationData.getSessionId());
		}
	}

	@Test
	void acquireTokenTestFail() {
		uaaRestClientConfiguration.setApiKeyResource(API_KEY_RESOURCE_NOT_FOUND);
		uaaAPIKeyTokenAcquirer = new UAACertificateTokenAcquirer(uaaRestClientConfiguration.getApiKeyResource(), TokenType.API_KEY);
		RuntimeException runtimeException = Assertions.assertThrows(RuntimeException.class, () -> uaaAPIKeyTokenAcquirer.acquireToken());

		Assertions.assertEquals("Cannot get the key store", runtimeException.getMessage());
	}

}