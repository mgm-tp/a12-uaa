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
package com.mgmtp.a12.uaa.client.rest.auth;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.TokenType;

/**
 * Store all authorization related data and optional expiration time.
 */
public class AuthorizationData {

	@JsonProperty
	private String authenticationToken;
	private Integer tokenRenewInSeconds;
	@JsonProperty
	private TokenType authenticationTokenType;
	@JsonProperty
	private String sessionId;
	@JsonProperty
	private String refreshToken;
	@JsonProperty
	private String oauth2IdToken;
	@JsonProperty
	private String uniqueUserIdentification;

	public AuthorizationData() {
	}

	public AuthorizationData(String authenticationToken, TokenType authenticationTokenType, String sessionId, Integer tokenRenewInSeconds) {
		this(authenticationToken, authenticationTokenType, sessionId, UUID.randomUUID().toString(), tokenRenewInSeconds);
	}

	public AuthorizationData(String authenticationToken, TokenType authenticationTokenType, String sessionId, String uniqueUserIdentification,
		Integer tokenRenewInSeconds) {
		this.authenticationToken = authenticationToken;
		this.tokenRenewInSeconds = tokenRenewInSeconds;
		this.authenticationTokenType = authenticationTokenType;
		this.sessionId = sessionId;
		this.uniqueUserIdentification = uniqueUserIdentification;
	}

	public String getAuthenticationToken() {
		return authenticationToken;
	}

	public Integer getTokenRenewInSeconds() {
		return tokenRenewInSeconds;
	}

	public TokenType getAuthenticationTokenType() {
		return authenticationTokenType;
	}

	public String getSessionId() {
		return sessionId;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getOauth2IdToken() {
		return oauth2IdToken;
	}

	public void setOauth2IdToken(String oauth2IdToken) {
		this.oauth2IdToken = oauth2IdToken;
	}

	public String getUniqueUserIdentification() {
		return uniqueUserIdentification;
	}

	public void setUniqueUserIdentification(String uniqueUserIdentification) {
		this.uniqueUserIdentification = uniqueUserIdentification;
	}

	@JsonIgnore
	public boolean isValid() {
		return authenticationTokenType != null && authenticationToken != null;
	}

	public void validate() {
		if (!isValid()) {
			throw new RuntimeException("Missing required cookies/header authenticationToken=null");
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authenticationToken == null) ? 0 : authenticationToken.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AuthorizationData other = (AuthorizationData) obj;
		if (authenticationToken == null) {
			if (other.authenticationToken != null)
				return false;
		} else if (!authenticationToken.equals(other.authenticationToken))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AuthorizationData [authenticationToken=" + "*****" + ", authenticationTokenType=" + authenticationTokenType + "," +
			"sessionId=" + sessionId + ", refreshToken=" + "*****" + "]";
	}

}
