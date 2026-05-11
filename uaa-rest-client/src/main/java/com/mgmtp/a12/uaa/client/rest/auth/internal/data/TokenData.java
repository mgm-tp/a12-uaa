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
package com.mgmtp.a12.uaa.client.rest.auth.internal.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Store the response data from Token Renewal's REST API (/token).
 */
public class TokenData {

	@JsonProperty("access_token")
	private String accessToken;
	@Deprecated(since = "8.2.2", forRemoval = true)
	@JsonProperty("access_token_expiration")
	private String accessTokenExpiration;
	@JsonProperty("token_renew_in_seconds")
	private String tokenRenewInSeconds;

	public TokenData() {
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	@Deprecated(since = "8.2.2", forRemoval = true)
	public String getAccessTokenExpiration() {
		return accessTokenExpiration;
	}

	@Deprecated(since = "8.2.2", forRemoval = true)
	public void setAccessTokenExpiration(String accessTokenExpiration) {
		this.accessTokenExpiration = accessTokenExpiration;
	}

	public String getTokenRenewInSeconds() {
		return tokenRenewInSeconds;
	}

	public void setTokenRenewInSeconds(String tokenRenewInSeconds) {
		this.tokenRenewInSeconds = tokenRenewInSeconds;
	}
}
