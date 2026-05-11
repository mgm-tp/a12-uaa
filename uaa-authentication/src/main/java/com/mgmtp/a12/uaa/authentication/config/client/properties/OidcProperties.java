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
package com.mgmtp.a12.uaa.authentication.config.client.properties;

import com.mgmtp.a12.uaa.authentication.config.common.EnabledProperty;
import com.mgmtp.a12.uaa.authentication.config.common.UrlProperty;

public class OidcProperties {
	private ConfidentialClientProperties confidentialClient = new ConfidentialClientProperties();
	private PublicClientProperties publicClient = new PublicClientProperties();

	private EnabledProperty idpLogout = new EnabledProperty(true);

	public EnabledProperty getIdpLogout() {
		return idpLogout;
	}

	public ConfidentialClientProperties getConfidentialClient() {
		return confidentialClient;
	}

	public void setConfidentialClient(ConfidentialClientProperties confidentialClient) {
		this.confidentialClient = confidentialClient;
	}

	public PublicClientProperties getPublicClient() {
		return publicClient;
	}

	public void setPublicClient(PublicClientProperties publicClient) {
		this.publicClient = publicClient;
	}

	@Override
	public String toString() {
		return "OidcProperties{" +
			"confidentialClient=" + confidentialClient +
			", publicClient=" + publicClient +
			", logoutIdp=" + idpLogout +
			'}';
	}

	public static class PublicClientProperties extends LoginLogoutAwareProperties {

		private String clientId;
		private String realmName;
		private UrlProperty idpBase = new UrlProperty("http://localhost:9090");
		private UrlProperty tokenExchangeRelative = new UrlProperty("token");
		private UrlProperty loginRedirectRelative = new UrlProperty("callback");
		private UrlProperty logoutRedirectRelative = new UrlProperty("logout");
		private UrlProperty silentRedirectRelative = new UrlProperty("silent_renew.html");
		private Boolean enableRefreshTokenGrant = false;
		private Integer tokenRenewThresholdInSeconds = 60;
		private String currentUserUrl;
		private SsoProperties ssoConfiguration = new SsoProperties();

		public PublicClientProperties() {
			setLoginRelative(new UrlProperty("auth"));
			setLogoutRelative(new UrlProperty("logout"));
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

		public UrlProperty getIdpBase() {
			return idpBase;
		}

		public void setIdpBase(UrlProperty idpBase) {
			this.idpBase = idpBase;
		}

		public UrlProperty getTokenExchangeRelative() {
			return tokenExchangeRelative;
		}

		public void setTokenExchangeRelative(UrlProperty tokenExchangeRelative) {
			this.tokenExchangeRelative = tokenExchangeRelative;
		}

		public UrlProperty getLoginRedirectRelative() {
			return loginRedirectRelative;
		}

		public void setLoginRedirectRelative(UrlProperty loginRedirectRelative) {
			this.loginRedirectRelative = loginRedirectRelative;
		}

		public UrlProperty getLogoutRedirectRelative() {
			return logoutRedirectRelative;
		}

		public void setLogoutRedirectRelative(UrlProperty logoutRedirectRelative) {
			this.logoutRedirectRelative = logoutRedirectRelative;
		}

		public UrlProperty getSilentRedirectRelative() {
			return silentRedirectRelative;
		}

		public void setSilentRedirectRelative(UrlProperty silentRedirectRelative) {
			this.silentRedirectRelative = silentRedirectRelative;
		}

		public Boolean getEnableRefreshTokenGrant() {
			return enableRefreshTokenGrant;
		}

		public void setEnableRefreshTokenGrant(Boolean enableRefreshTokenGrant) {
			this.enableRefreshTokenGrant = enableRefreshTokenGrant;
		}

		public Integer getTokenRenewThresholdInSeconds() {
			return tokenRenewThresholdInSeconds;
		}

		public void setTokenRenewThresholdInSeconds(Integer tokenRenewThresholdInSeconds) {
			this.tokenRenewThresholdInSeconds = tokenRenewThresholdInSeconds;
		}

		public String getCurrentUserUrl() {
			return currentUserUrl;
		}

		public void setCurrentUserUrl(String currentUserUrl) {
			this.currentUserUrl = currentUserUrl;
		}

		public SsoProperties getSsoConfiguration() {
			return ssoConfiguration;
		}

		public void setSsoConfiguration(SsoProperties ssoConfiguration) {
			this.ssoConfiguration = ssoConfiguration;
		}

		@Override
		public String toString() {
			return super.toString() + "PublicClientProperties{" +
				"clientId='" + clientId + '\'' +
				", realmName='" + realmName + '\'' +
				", idpBase=" + idpBase +
				", tokenExchangeRelative=" + tokenExchangeRelative +
				", loginRedirectRelative=" + loginRedirectRelative +
				", logoutRedirectRelative=" + logoutRedirectRelative +
				", silentRedirectRelative=" + silentRedirectRelative +
				", enableRefreshTokenGrant=" + enableRefreshTokenGrant +
				", tokenRenewThresholdInSeconds=" + tokenRenewThresholdInSeconds +
				", ssoConfiguration=" + ssoConfiguration +
				'}';
		}
	}

	public static class ConfidentialClientProperties extends LoginLogoutAwareProperties {

		private String clientId;
		private String realmName;
		private Integer tokenRenewThresholdInSeconds = 60;
		private UrlProperty idpBase = new UrlProperty("http://localhost:9090");

		public ConfidentialClientProperties() {
			setLoginRelative(new UrlProperty("token"));
			setLogoutRelative(new UrlProperty("logout"));
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

		public UrlProperty getIdpBase() {
			return idpBase;
		}

		public void setIdpBase(UrlProperty idpBase) {
			this.idpBase = idpBase;
		}

		@Override
		public String toString() {
			return super.toString() + "ConfidentialClientProperties{" +
				"clientId='" + clientId + '\'' +
				", realmName='" + realmName + '\'' +
				", tokenRenewThresholdInSeconds='" + tokenRenewThresholdInSeconds + '\'' +
				", idpBase=" + idpBase +
				'}';
		}
	}
}
