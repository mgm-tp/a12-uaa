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
package com.mgmtp.a12.uaa.client.rest.config.properties;

import com.mgmtp.a12.uaa.client.rest.auth.token.internal.oauth2.ClientType;
import com.mgmtp.a12.uaa.client.rest.config.common.UrlProperty;

public class OidcProperties extends BaseProperties {
	private ClientType clientType = ClientType.CONFIDENTIAL;
	private ConfidentialClientProperties confidentialClient = new ConfidentialClientProperties();
	private PublicClientProperties publicClient = new PublicClientProperties();

	public ClientType getClientType() {
		return clientType;
	}

	public void setClientType(ClientType clientType) {
		this.clientType = clientType;
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
		return super.toString() + "Oauth2Configuration{" +
			", confidentialClient=" + confidentialClient +
			", publicClient=" + publicClient +
			'}';
	}

	public static class PublicClientProperties extends BaseOauth2ClientTypeProperties {
		private UrlProperty tokenExchangeRelative = new UrlProperty("token");
		private UrlProperty loginRedirectRelative = new UrlProperty("callback");
		private UrlProperty silentRedirectRelative = new UrlProperty("silent_renew.html");
		private SsoProperties ssoConfiguration = new SsoProperties();

		public PublicClientProperties() {
			setLoginRelative(new UrlProperty("auth"));
			setLogoutRelative(new UrlProperty("logout"));
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

		public UrlProperty getSilentRedirectRelative() {
			return silentRedirectRelative;
		}

		public void setSilentRedirectRelative(UrlProperty silentRedirectRelative) {
			this.silentRedirectRelative = silentRedirectRelative;
		}

		public SsoProperties getSsoConfiguration() {
			return ssoConfiguration;
		}

		public void setSsoConfiguration(SsoProperties ssoConfiguration) {
			this.ssoConfiguration = ssoConfiguration;
		}

		@Override
		public String toString() {
			return "PublicClientProperties{" +
				"tokenExchangeRelative=" + tokenExchangeRelative +
				", loginRedirectRelative=" + loginRedirectRelative +
				", silentRedirectRelative=" + silentRedirectRelative +
				", ssoConfiguration=" + ssoConfiguration +
				'}';
		}
	}

	public static class ConfidentialClientProperties extends BaseOauth2ClientTypeProperties {
		private String clientSecret;

		public ConfidentialClientProperties() {
			setLoginRelative(new UrlProperty("token"));
			setLogoutRelative(new UrlProperty("logout"));
		}

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

	public static abstract class BaseOauth2ClientTypeProperties {
		private UrlProperty loginRelative;
		private UrlProperty logoutRelative = new UrlProperty("logout");
		private String clientId;
		private String realmName;
		private UrlProperty idpBase = new UrlProperty("http://localhost:9090");
		private Integer tokenRenewThresholdInSeconds = 60;

		public UrlProperty getLoginRelative() {
			return loginRelative;
		}

		public void setLoginRelative(UrlProperty loginRelative) {
			this.loginRelative = loginRelative;
		}

		public UrlProperty getLogoutRelative() {
			return logoutRelative;
		}

		public void setLogoutRelative(UrlProperty logoutRelative) {
			this.logoutRelative = logoutRelative;
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

		public Integer getTokenRenewThresholdInSeconds() {
			return tokenRenewThresholdInSeconds;
		}

		public void setTokenRenewThresholdInSeconds(Integer tokenRenewThresholdInSeconds) {
			this.tokenRenewThresholdInSeconds = tokenRenewThresholdInSeconds;
		}

		@Override
		public String toString() {
			return "BaseOidcClientTypeProperties{" +
				"loginRelative=" + loginRelative +
				", clientId='" + clientId + '\'' +
				", realmName='" + realmName + '\'' +
				", idpBase=" + idpBase +
				", tokenRenewThresholdInSeconds=" + tokenRenewThresholdInSeconds +
				'}';
		}
	}
}
