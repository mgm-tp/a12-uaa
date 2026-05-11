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

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Generated;

import org.htmlunit.WebRequest;

public class Request {

	private WebRequest request;
	private Map<String, Object> parameters;

	@Generated("SparkTools")
	private Request(Builder builder) {
		this.request = builder.request;
		this.parameters = builder.parameters;
	}

	public WebRequest getRequest() {
		return request;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	/**
	 * Creates a builder to build {@link Request} and initialize it with the given object.
	 *
	 * @param requestData to initialize the builder with
	 * @return created builder
	 */
	@Generated("SparkTools")
	public static Builder builderFrom(Request requestData) {
		return new Builder(requestData);
	}

	/**
	 * Builder to build {@link Request}.
	 */
	@Generated("SparkTools")
	public static final class Builder {
		private WebRequest request;
		private Map<String, Object> parameters = new HashMap<>();

		public Builder(WebRequest request) {
			this.request = request;
		}

		private Builder(Request requestData) {
			this.request = requestData.request;
			this.parameters = requestData.parameters;
		}

		public Builder withParameters(Map<String, Object> parameters) {
			this.parameters = parameters;
			return this;
		}

		public Builder withParameter(String key, Object value) {
			parameters.put(key, value);
			return this;
		}

		public Request build() {
			return new Request(this);
		}
	}

}
