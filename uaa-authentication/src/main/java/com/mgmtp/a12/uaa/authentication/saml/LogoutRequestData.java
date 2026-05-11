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

import java.io.Serializable;
import java.time.Instant;

import jakarta.annotation.Generated;

import org.springframework.security.saml2.provider.service.authentication.logout.Saml2LogoutRequest;

public class LogoutRequestData implements Serializable {

	private Saml2LogoutRequest logoutRequest;
	private String jwtToken;
	private Instant expirationDate;

	@Generated("SparkTools")
	private LogoutRequestData(Builder builder) {
		this.logoutRequest = builder.logoutRequest;
		this.jwtToken = builder.jwtToken;
		this.expirationDate = builder.expirationDate;
	}

	public Saml2LogoutRequest getLogoutRequest() {
		return logoutRequest;
	}

	public String getJwtToken() {
		return jwtToken;
	}

	public Instant getExpirationDate() {
		return expirationDate;
	}

	@Generated("SparkTools")
	public static Builder builderFrom(LogoutRequestData loginRequestData) {
		return new Builder(loginRequestData);
	}

	@Generated("SparkTools")
	public static final class Builder {
		private Saml2LogoutRequest logoutRequest;
		private String jwtToken;
		private Instant expirationDate = Instant.now();

		public Builder(Saml2LogoutRequest logoutRequest, String jwtToken) {
			this.logoutRequest = logoutRequest;
			this.jwtToken = jwtToken;
		}

		private Builder(LogoutRequestData loginRequestData) {
			this.logoutRequest = loginRequestData.logoutRequest;
			this.jwtToken = loginRequestData.jwtToken;
			this.expirationDate = loginRequestData.expirationDate;
		}

		public Builder withExpirationDate(Instant expirationDate) {
			this.expirationDate = expirationDate;
			return this;
		}

		public LogoutRequestData build() {
			return new LogoutRequestData(this);
		}
	}

}