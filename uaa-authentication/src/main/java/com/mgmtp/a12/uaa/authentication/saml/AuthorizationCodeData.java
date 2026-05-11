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
package com.mgmtp.a12.uaa.authentication.saml;

import java.time.Instant;

import jakarta.annotation.Generated;

public class AuthorizationCodeData {

	private String authorizationCode;
	private String accessToken;
	private Instant generatedTime;

	@Generated("SparkTools")
	private AuthorizationCodeData(Builder builder) {
		this.authorizationCode = builder.authorizationCode;
		this.accessToken = builder.accessToken;
		this.generatedTime = builder.generatedTime;
	}

	public String getAuthorizationCode() {
		return authorizationCode;
	}

	public void setAuthorizationCode(String code) {
		this.authorizationCode = code;
	}

	public Instant getGeneratedTime() {
		return generatedTime;
	}

	public void setGeneratedTime(Instant generated) {
		this.generatedTime = generated;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	/**
	 * Creates a builder to build {@link AuthorizationCodeData} and initialize it with the given object.
	 * @param authorizationCodeData to initialize the builder with
	 * @return created builder
	 */
	@Generated("SparkTools")
	public static Builder builderFrom(AuthorizationCodeData authorizationCodeData) {
		return new Builder(authorizationCodeData);
	}

	/**
	 * Builder to build {@link AuthorizationCodeData}.
	 */
	@Generated("SparkTools")
	public static final class Builder {
		private String authorizationCode;
		private String accessToken;
		private Instant generatedTime;

		public Builder(String authorizationCode, String accessToken) {
			this.authorizationCode = authorizationCode;
			this.accessToken = accessToken;
		}

		private Builder(AuthorizationCodeData authorizationCodeData) {
			this.authorizationCode = authorizationCodeData.authorizationCode;
			this.accessToken = authorizationCodeData.accessToken;
			this.generatedTime = authorizationCodeData.generatedTime;
		}

		public Builder withGeneratedTime(Instant generatedTime) {
			this.generatedTime = generatedTime;
			return this;
		}

		public AuthorizationCodeData build() {
			return new AuthorizationCodeData(this);
		}
	}

}