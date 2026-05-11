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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.http.HttpHeaders;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.TokenAcquirer;

public class UAACertificateTokenAcquirer implements TokenAcquirer {

	private static final Logger LOGGER = LoggerFactory.getLogger(UAACertificateTokenAcquirer.class);

	private String pathResource;
	private TokenType tokenType;

	public UAACertificateTokenAcquirer(String pathResource) {
		this.pathResource = pathResource;
	}

	public UAACertificateTokenAcquirer(String pathResource, TokenType tokenType) {
		this.pathResource = pathResource;
		this.tokenType = tokenType;
	}

	@Override
	public AuthorizationData acquireToken() {
		try (InputStream inputStream = new DefaultResourceLoader().getResource(pathResource).getInputStream()) {
			byte[] bytes = IOUtils.toByteArray(inputStream);
			String keyStore = Base64.getEncoder().encodeToString(bytes);
			LOGGER.info("The certificate has been acquired from [{}]", pathResource);
			AuthorizationData authorizationData = new AuthorizationData(keyStore, tokenType, null, (Integer) null);
			//for Certificate and API_KEY we use static key store for caching
			authorizationData.setUniqueUserIdentification(null);
			return authorizationData;
		} catch (IOException e) {
			throw new RuntimeException("Cannot get the key store", e);
		}
	}

	@Override
	public void releaseToken(AuthorizationData authorizationData, HttpHeaders hedars) {
		// nothing to do
	}

}
