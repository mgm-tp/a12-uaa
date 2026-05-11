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

import org.springframework.util.Assert;

public class SamlJwtTokenData implements Serializable {

	private String accessToken;
	private Instant expirationTime;
	private String sessionId;

	@Generated("SparkTools")
	private SamlJwtTokenData(Builder builder) {
		this.accessToken = builder.accessToken;
		this.expirationTime = builder.expirationTime;
		this.sessionId = builder.sessionId;
	}

	public Instant getExpirationTime() {
		return expirationTime;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getSessionId() {
		return sessionId;
	}

	@Override
	public String toString() {
		return "JwtTokenData [accessToken=" + accessToken + ", expirationTime=" + expirationTime + ", sessionId=" + sessionId + "]";
	}

	/**
	 * Creates a builder to build {@link SamlJwtTokenData} and initialize it with the given object.
	 * 
	 * @param jwtTokenData to initialize the builder with
	 * @return created builder
	 */
	@Generated("SparkTools")
	public static Builder builderFrom(SamlJwtTokenData jwtTokenData) {
		return new Builder(jwtTokenData);
	}

	/**
	 * Builder to build {@link SamlJwtTokenData}.
	 */
	@Generated("SparkTools")
	public static final class Builder {
		private String accessToken;
		private Instant expirationTime;
		private String sessionId;

		public Builder(String accessToken) {
			this.accessToken = accessToken;
		}

		private Builder(SamlJwtTokenData jwtTokenData) {
			this.accessToken = jwtTokenData.accessToken;
			this.expirationTime = jwtTokenData.expirationTime;
		}

		public Builder withExpirationTime(Instant expirationTime) {
			this.expirationTime = expirationTime;
			return this;
		}

		public Builder withSessionId(String sessionId) {
			this.sessionId = sessionId;
			return this;
		}

		public SamlJwtTokenData build() {
			Assert.notNull(expirationTime, "Please specify expirationTime");
			return new SamlJwtTokenData(this);
		}
	}

}