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

import com.mgmtp.a12.uaa.authentication.internal.TokenType;

public class OidcConfiguration {
	private TokenType tokenType;
	private ConfidentialClientConfiguration confidentialClient;
	private PublicClientConfiguration publicClient;
	private boolean logoutIdp;

	public boolean isLogoutIdp() {
		return logoutIdp;
	}

	public void setLogoutIdp(boolean logoutIdp) {
		this.logoutIdp = logoutIdp;
	}

	public TokenType getTokenType() {
		return tokenType;
	}

	public void setTokenType(TokenType tokenType) {
		this.tokenType = tokenType;
	}

	public ConfidentialClientConfiguration getConfidentialClient() {
		return confidentialClient;
	}

	public void setConfidentialClient(ConfidentialClientConfiguration confidentialClient) {
		this.confidentialClient = confidentialClient;
	}

	public PublicClientConfiguration getPublicClient() {
		return publicClient;
	}

	public void setPublicClient(PublicClientConfiguration publicClient) {
		this.publicClient = publicClient;
	}

	@Override
	public String toString() {
		return "OidcConfiguration{" +
			"tokenType=" + tokenType +
			", confidentialClient=" + confidentialClient +
			", publicClient=" + publicClient +
			", logoutIdp=" + logoutIdp +
			'}';
	}

	public static class ConfidentialClientConfiguration {
		private String loginRelativeUrl;
		private String logoutRelativeUrl = "logout";
		private String clientId;
		private String realmName;
		private Integer tokenRenewThresholdInSeconds;
		private String idpBaseUrl;

		public String getLoginRelativeUrl() {
			return loginRelativeUrl;
		}

		public void setLoginRelativeUrl(String loginRelativeUrl) {
			this.loginRelativeUrl = loginRelativeUrl;
		}

		public String getLogoutRelativeUrl() {
			return logoutRelativeUrl;
		}

		public void setLogoutRelativeUrl(String logoutRelativeUrl) {
			this.logoutRelativeUrl = logoutRelativeUrl;
		}

		public String getClientId() {
			return clientId;
		}

		public void setClientId(String clientId) {
			this.clientId = clientId;
		}

		public String getRealmName() {
			return realmName;
		}

		public void setRealmName(String realmName) {
			this.realmName = realmName;
		}

		public Integer getTokenRenewThresholdInSeconds() {
			return tokenRenewThresholdInSeconds;
		}

		public void setTokenRenewThresholdInSeconds(Integer tokenRenewThresholdInSeconds) {
			this.tokenRenewThresholdInSeconds = tokenRenewThresholdInSeconds;
		}

		public String getIdpBaseUrl() {
			return idpBaseUrl;
		}

		public void setIdpBaseUrl(String idpBaseUrl) {
			this.idpBaseUrl = idpBaseUrl;
		}

		@Override
		public String toString() {
			return "ConfidentialClientConfiguration{" +
				"loginRelativeUrl='" + loginRelativeUrl + '\'' +
				"logoutRelativeUrl='" + logoutRelativeUrl + '\'' +
				", clientId='" + clientId + '\'' +
				", realmName='" + realmName + '\'' +
				", tokenRenewThresholdInSeconds=" + tokenRenewThresholdInSeconds +
				", idpBaseUrl='" + idpBaseUrl + '\'' +
				'}';
		}
	}

	public static class PublicClientConfiguration extends ConfidentialClientConfiguration {
		private String tokenExchangeRelativeUrl;
		private String loginRedirectRelativeUrl;
		private String logoutRedirectRelativeUrl;
		private String silentRedirectRelativeUrl;
		private Boolean enableRefreshTokenGrant;
		private String currentUserUrl;
		private SsoConfiguration ssoConfiguration;

		public String getTokenExchangeRelativeUrl() {
			return tokenExchangeRelativeUrl;
		}

		public void setTokenExchangeRelativeUrl(String tokenExchangeRelativeUrl) {
			this.tokenExchangeRelativeUrl = tokenExchangeRelativeUrl;
		}

		public String getLoginRedirectRelativeUrl() {
			return loginRedirectRelativeUrl;
		}

		public void setLoginRedirectRelativeUrl(String loginRedirectRelativeUrl) {
			this.loginRedirectRelativeUrl = loginRedirectRelativeUrl;
		}

		public String getLogoutRedirectRelativeUrl() {
			return logoutRedirectRelativeUrl;
		}

		public void setLogoutRedirectRelativeUrl(String logoutRedirectRelativeUrl) {
			this.logoutRedirectRelativeUrl = logoutRedirectRelativeUrl;
		}

		public String getSilentRedirectRelativeUrl() {
			return silentRedirectRelativeUrl;
		}

		public void setSilentRedirectRelativeUrl(String silentRedirectRelativeUrl) {
			this.silentRedirectRelativeUrl = silentRedirectRelativeUrl;
		}

		public Boolean getEnableRefreshTokenGrant() {
			return enableRefreshTokenGrant;
		}

		public void setEnableRefreshTokenGrant(Boolean enableRefreshTokenGrant) {
			this.enableRefreshTokenGrant = enableRefreshTokenGrant;
		}

		public String getCurrentUserUrl() {
			return currentUserUrl;
		}

		public void setCurrentUserUrl(String currentUserUrl) {
			this.currentUserUrl = currentUserUrl;
		}

		public SsoConfiguration getSsoConfiguration() {
			return ssoConfiguration;
		}

		public void setSsoConfiguration(SsoConfiguration ssoConfiguration) {
			this.ssoConfiguration = ssoConfiguration;
		}

		@Override
		public String toString() {
			return "PublicClientConfiguration{" +
				"clientId='" + getClientId() + '\'' +
				", realmName='" + getRealmName() + '\'' +
				", idpBaseUrl='" + getIdpBaseUrl() + '\'' +
				", loginRelativeUrl='" + getLoginRelativeUrl() + '\'' +
				", logoutRelativeUrl='" + getLogoutRelativeUrl() + '\'' +
				", tokenExchangeRelativeUrl='" + tokenExchangeRelativeUrl + '\'' +
				", loginRedirectRelativeUrl='" + loginRedirectRelativeUrl + '\'' +
				", logoutRedirectRelativeUrl='" + logoutRedirectRelativeUrl + '\'' +
				", logoutRedirectRelativeUrl='" + logoutRedirectRelativeUrl + '\'' +
				", silentRedirectRelativeUrl='" + silentRedirectRelativeUrl + '\'' +
				", enableRefreshTokenGrant='" + enableRefreshTokenGrant + '\'' +
				", tokenRenewThresholdInSeconds='" + super.getTokenRenewThresholdInSeconds() + '\'' +
				", ssoConfiguration=" + ssoConfiguration +
				'}';
		}
	}

}
