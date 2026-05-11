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
package com.mgmtp.a12.uaa.client.rest.config.selfconfiguration;

import com.mgmtp.a12.uaa.client.rest.auth.token.internal.oauth2.ClientType;

public class OidcConfiguration extends BaseConfiguration {
	private ClientType clientType = ClientType.CONFIDENTIAL;
	private ConfidentialClientConfiguration confidentialClient;
	private PublicClientConfiguration publicClient;

	public ClientType getClientType() {
		return clientType;
	}

	public void setClientType(ClientType clientType) {
		this.clientType = clientType;
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
		return super.toString() + "Oauth2Configuration{" +
			", confidentialClient=" + confidentialClient +
			", publicClient=" + publicClient +
			'}';
	}

	public static class PublicClientConfiguration extends BaseOidcClientTypeConfiguration {
		private String tokenExchangeRelativeUrl;
		private String loginRedirectRelativeUrl;
		private String logoutRedirectRelativeUrl;
		private String silentRedirectRelativeUrl;
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

		public SsoConfiguration getSsoConfiguration() {
			return ssoConfiguration;
		}

		public void setSsoConfiguration(SsoConfiguration ssoConfiguration) {
			this.ssoConfiguration = ssoConfiguration;
		}

		@Override
		public String toString() {
			return super.toString() + "PublicClientProperties{" +
				", tokenExchangeRelativeUrl='" + tokenExchangeRelativeUrl + '\'' +
				", loginRedirectRelativeUrl='" + loginRedirectRelativeUrl + '\'' +
				", logoutRedirectRelativeUrl='" + logoutRedirectRelativeUrl + '\'' +
				", silentRedirectRelativeUrl='" + silentRedirectRelativeUrl + '\'' +
				", ssoConfiguration=" + ssoConfiguration +
				'}';
		}
	}

	public static class ConfidentialClientConfiguration extends BaseOidcClientTypeConfiguration {
		private String clientSecret;

		public String getClientSecret() {
			return clientSecret;
		}

		public void setClientSecret(String clientSecret) {
			this.clientSecret = clientSecret;
		}

		@Override
		public String toString() {
			return super.toString() + "ConfidentialClientConfiguration{" +
				"clientSecret='" + "*****" + '\'' +
				'}';
		}
	}

	public static abstract class BaseOidcClientTypeConfiguration {
		private String loginRelativeUrl;
		private String logoutRelativeUrl;
		private String clientId;
		private String realmName;
		private String idpBaseUrl;
		private Integer tokenRenewThresholdInSeconds;

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

		public String getIdpBaseUrl() {
			return idpBaseUrl;
		}

		public void setIdpBaseUrl(String idpBaseUrl) {
			this.idpBaseUrl = idpBaseUrl;
		}

		public Integer getTokenRenewThresholdInSeconds() {
			return tokenRenewThresholdInSeconds;
		}

		public void setTokenRenewThresholdInSeconds(Integer tokenRenewThresholdInSeconds) {
			this.tokenRenewThresholdInSeconds = tokenRenewThresholdInSeconds;
		}

		@Override
		public String toString() {
			return "BaseOidcClientTypeConfiguration{" +
				"loginRelativeUrl='" + loginRelativeUrl + '\'' +
				"logoutRelativeUrl='" + logoutRelativeUrl + '\'' +
				", clientId='" + clientId + '\'' +
				", realmName='" + realmName + '\'' +
				", idpBaseUrl='" + idpBaseUrl + '\'' +
				", tokenRenewThresholdInSeconds=" + tokenRenewThresholdInSeconds +
				'}';
		}
	}
}
