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
package com.mgmtp.a12.uaa.authentication.config.client;

import org.springframework.http.HttpHeaders;

import com.mgmtp.a12.uaa.authentication.internal.TokenType;
import com.mgmtp.a12.uaa.authentication.security.login.internal.UAAAuthenticationSuccessHandler;

public class TokenConfiguration {
	private String authorizationHeaderName;
	private TokenType tokenType;
	private String generatedTokenHeaderName;
	@Deprecated(since = "8.2.2", forRemoval = true)
	private String generatedTokenExpirationHeaderName;
	private Boolean allowCredentials = false;

	public TokenConfiguration() {
	}

	private TokenConfiguration(final Builder builder) {
		this.authorizationHeaderName = builder.authorizationHeaderName;
		this.tokenType = builder.tokenType;
		this.generatedTokenHeaderName = builder.generatedTokenHeaderName;
		this.generatedTokenExpirationHeaderName = builder.generatedTokenExpirationHeaderName;
		this.allowCredentials = builder.allowCredentials;
	}

	public String getAuthorizationHeaderName() {
		return authorizationHeaderName;
	}

	public void setAuthorizationHeaderName(String authorizationHeaderName) {
		this.authorizationHeaderName = authorizationHeaderName;
	}

	public TokenType getTokenType() {
		return tokenType;
	}

	public void setTokenType(TokenType tokenType) {
		this.tokenType = tokenType;
	}

	public String getGeneratedTokenHeaderName() {
		return generatedTokenHeaderName;
	}

	public void setGeneratedTokenHeaderName(String generatedTokenHeaderName) {
		this.generatedTokenHeaderName = generatedTokenHeaderName;
	}

	public String getGeneratedTokenExpirationHeaderName() {
		return generatedTokenExpirationHeaderName;
	}

	public void setGeneratedTokenExpirationHeaderName(String generatedTokenExpirationHeaderName) {
		this.generatedTokenExpirationHeaderName = generatedTokenExpirationHeaderName;
	}

	public Boolean getAllowCredentials() {
		return allowCredentials;
	}

	public void setAllowCredentials(Boolean allowCredentials) {
		this.allowCredentials = allowCredentials;
	}

	public static TokenConfiguration getDefaultUaaTokenConfiguration() {
		return DEFAULT_UAA_TOKEN_CONFIGURATION;
	}

	public static TokenConfiguration getDefaultBearerTokenConfiguration() {
		return DEFAULT_BEARER_TOKEN_CONFIGURATION;
	}

	public static TokenConfiguration getDefaultAPIKeyTokenConfiguration() {
		return DEFAULT_API_KEY_TOKEN_CONFIGURATION;
	}

	@Override public String toString() {
		return "TokenConfiguration{" +
			"authorizationHeaderName='" + authorizationHeaderName + '\'' +
			", tokenType=" + tokenType +
			", generatedTokenHeaderName='" + generatedTokenHeaderName + '\'' +
			", generatedTokenExpirationHeaderName='" + generatedTokenExpirationHeaderName + '\'' +
			", allowCredentials=" + allowCredentials +
			'}';
	}

	public static final class Builder {
		private String authorizationHeaderName;
		private TokenType tokenType;
		private String generatedTokenHeaderName;
		@Deprecated(since = "8.2.2", forRemoval = true)
		private String generatedTokenExpirationHeaderName;
		private Boolean allowCredentials = false;

		private Builder() {
		}

		public Builder withAuthorizationHeaderName(String authorizationHeaderName) {
			this.authorizationHeaderName = authorizationHeaderName;
			return this;
		}

		public Builder withTokenType(TokenType tokenType) {
			this.tokenType = tokenType;
			return this;
		}

		public Builder withGeneratedTokenHeaderName(String generatedTokenHeaderName) {
			this.generatedTokenHeaderName = generatedTokenHeaderName;
			return this;
		}

		@Deprecated(since = "8.2.2", forRemoval = true)
		public Builder withGeneratedTokenExpirationHeaderName(String generatedTokenExpirationHeaderName) {
			this.generatedTokenExpirationHeaderName = generatedTokenExpirationHeaderName;
			return this;
		}

		public Builder withAllowCredentials(Boolean allowCredentials) {
			this.allowCredentials = allowCredentials;
			return this;
		}

		public TokenConfiguration build() {
			return new TokenConfiguration(this);
		}
	}

	private static final TokenConfiguration DEFAULT_UAA_TOKEN_CONFIGURATION =
		new TokenConfiguration.Builder()
			.withAuthorizationHeaderName(HttpHeaders.AUTHORIZATION)
			.withTokenType(TokenType.UAABEARER)
			.withAllowCredentials(false)
			.withGeneratedTokenHeaderName(UAAAuthenticationSuccessHandler.TOKEN_KEY)
			.withGeneratedTokenExpirationHeaderName(UAAAuthenticationSuccessHandler.TOKEN_EXPIRATION_KEY)
			.build();

	private static final TokenConfiguration DEFAULT_BEARER_TOKEN_CONFIGURATION =
		new TokenConfiguration.Builder()
			.withAuthorizationHeaderName(HttpHeaders.AUTHORIZATION)
			.withTokenType(TokenType.BEARER)
			.build();

	private static final TokenConfiguration DEFAULT_API_KEY_TOKEN_CONFIGURATION =
		new TokenConfiguration.Builder()
			.withAuthorizationHeaderName(HttpHeaders.AUTHORIZATION)
			.withTokenType(TokenType.APIKEY)
			.build();
}
