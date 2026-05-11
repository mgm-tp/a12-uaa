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

import java.io.Serializable;

import jakarta.annotation.Generated;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class CachedClientData implements Serializable {
	private HttpHeaders headers;
	private HttpStatus statusCode;
	private String statusText;
	private byte[] cachedContent;

	@Generated("SparkTools")
	private CachedClientData(Builder builder) {
		this.headers = builder.headers;
		this.statusCode = builder.statusCode;
		this.statusText = builder.statusText;
		this.cachedContent = builder.cachedContent;
	}

	/**
	 * Creates builder to build {@link CachedClientData}.
	 * @return created builder
	 */
	@Generated("SparkTools")
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Creates a builder to build {@link CachedClientData} and initialize it with the given object.
	 * @param cachedClientData to initialize the builder with
	 * @return created builder
	 */
	@Generated("SparkTools")
	public static Builder builderFrom(CachedClientData cachedClientData) {
		return new Builder(cachedClientData);
	}

	public HttpHeaders getHeaders() {
		return headers;
	}

	public void setHeaders(HttpHeaders headers) {
		this.headers = headers;
	}

	public HttpStatus getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(HttpStatus statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatusText() {
		return statusText;
	}

	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}

	public byte[] getCachedContent() {
		return cachedContent;
	}

	public void setCachedContent(byte[] cachedContent) {
		this.cachedContent = cachedContent;
	}

	@Override
	public String toString() {
		return "CachedClientData [headers=" + headers + ", statusCode=" + statusCode + ", statusText=" + statusText + ", cachedContent="
			+ new String(cachedContent) + "]";
	}

	/**
	 * Builder to build {@link CachedClientData}.
	 */
	@Generated("SparkTools")
	public static final class Builder {
		private HttpHeaders headers;
		private HttpStatus statusCode;
		private String statusText;
		private byte[] cachedContent;

		private Builder() {
		}

		private Builder(CachedClientData cachedClientData) {
			this.headers = cachedClientData.headers;
			this.statusCode = cachedClientData.statusCode;
			this.statusText = cachedClientData.statusText;
			this.cachedContent = cachedClientData.cachedContent;
		}

		public Builder withHeaders(HttpHeaders headers) {
			this.headers = headers;
			return this;
		}

		public Builder withStatusCode(HttpStatus statusCode) {
			this.statusCode = statusCode;
			return this;
		}

		public Builder withStatusText(String statusText) {
			this.statusText = statusText;
			return this;
		}

		public Builder withCachedContent(byte[] cachedContent) {
			this.cachedContent = cachedContent;
			return this;
		}

		public CachedClientData build() {
			return new CachedClientData(this);
		}
	}
}
