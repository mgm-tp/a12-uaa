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
package com.mgmtp.a12.uaa.client.rest.cache.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

public class CacheClientHttpResponse implements ClientHttpResponse {

	private static final Logger LOGGER = LoggerFactory.getLogger(CacheClientHttpResponse.class);

	private CachedClientData cachedClientData;
	private InputStream dataStream;

	public CacheClientHttpResponse(CachedClientData cachedData) {
		this.cachedClientData = cachedData;
	}

	@Override
	public InputStream getBody() throws IOException {
		dataStream = new ByteArrayInputStream(cachedClientData.getCachedContent());
		return dataStream;
	}

	@Override
	public HttpHeaders getHeaders() {
		return cachedClientData.getHeaders();
	}

	@Override
	public HttpStatus getStatusCode() throws IOException {
		return cachedClientData.getStatusCode();
	}

	@Override
	public int getRawStatusCode() throws IOException {
		return cachedClientData.getStatusCode().value();
	}

	@Override
	public String getStatusText() throws IOException {
		return cachedClientData.getStatusText();
	}

	@Override
	public void close() {
		Optional.ofNullable(dataStream).ifPresent((stream) -> {
			try {
				stream.close();
			} catch (IOException e) {
				LOGGER.error("Unable to close the input stream", e);
			}
		});
	}
}
