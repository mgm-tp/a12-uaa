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
package com.mgmtp.a12.uaa.client.rest.config;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.DefaultResourceLoader;

import com.mgmtp.a12.uaa.client.rest.auth.token.internal.TokenType;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.oauth2.ClientType;
import com.mgmtp.a12.uaa.client.rest.config.common.UrlProperty;
import com.mgmtp.a12.uaa.client.rest.config.properties.OidcProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.SamlProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.SsoProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientAuthenticationProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties.CacheConfiguration;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties.CacheMappingConfiguration;
import com.mgmtp.a12.uaa.client.rest.config.selfconfiguration.ClientSelfconfiguration;
import com.mgmtp.a12.uaa.client.rest.config.selfconfiguration.OidcConfiguration;
import com.mgmtp.a12.uaa.client.rest.config.selfconfiguration.SamlConfiguration;
import com.mgmtp.a12.uaa.client.rest.config.selfconfiguration.SsoConfiguration;
import com.mgmtp.a12.uaa.client.rest.config.selfconfiguration.TokenConfiguration;

public class UAARestClientPropertiesBuilder {

	private ClientSelfconfiguration clientSelfconfiguration;
	private String baseUrl;
	private String username;
	private String password;
	private String[] excludedContexts;
	private String authType;
	private String oauth2ClientSecret;
	private ClientType oauth2ClientType;
	private String apiKeyResource;

	private String authorizationHeaderName;

	private UAARestClientPropertiesBuilder(ClientSelfconfiguration clientSelfconfiguration) {
		this.clientSelfconfiguration = clientSelfconfiguration;
	}

	public UAARestClientPropertiesBuilder withBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
		return this;
	}

	public UAARestClientPropertiesBuilder withUsername(String username) {
		this.username = username;
		return this;
	}

	public UAARestClientPropertiesBuilder withPassword(String password) {
		this.password = password;
		return this;
	}

	public UAARestClientPropertiesBuilder withExcludedContexts(String[] excludedContexts) {
		this.excludedContexts = excludedContexts;
		return this;
	}

	public UAARestClientPropertiesBuilder withAuthenticationType(String authenticationType) {
		this.authType = authenticationType;
		return this;
	}

	public UAARestClientPropertiesBuilder withOauth2ClientSecret(String oauth2ClientSecret) {
		this.oauth2ClientSecret = oauth2ClientSecret;
		return this;
	}

	public UAARestClientPropertiesBuilder withOauth2ClientType(ClientType oauthClientType) {
		this.oauth2ClientType = oauthClientType;
		return this;
	}

	public UAARestClientPropertiesBuilder withAPIKeyResource(String apiKeyResource) {
		this.apiKeyResource = apiKeyResource;
		return this;
	}

	public UAARestClientPropertiesBuilder withAuthorizationHeaderName(String authorizationHeaderName) {
		this.authorizationHeaderName = authorizationHeaderName;
		return this;
	}

	public static UAARestClientPropertiesBuilder with(ClientSelfconfiguration clientSelfconfiguration) {
		return new UAARestClientPropertiesBuilder(clientSelfconfiguration);
	}

	public UAARestClientProperties build() {
		UrlProperty uaaBase = new UrlProperty(Optional.ofNullable(baseUrl).orElse(clientSelfconfiguration.getUaaBaseUrl()));
		UAARestClientProperties uaaRestClientProperties = new UAARestClientProperties(uaaBase, AuthenticationType.valueOf(authType));
		uaaRestClientProperties.setDelegatedModeConfiguration(new UAARestClientProperties.DelegatedModeProperties(excludedContexts));
		if (StringUtils.isNotBlank(clientSelfconfiguration.getAuthorizationDataStore())) {
			uaaRestClientProperties.setAuthorizationDataStore(new DefaultResourceLoader().getResource(clientSelfconfiguration.getAuthorizationDataStore()));
		}

		UAARestClientAuthenticationProperties restClientAuthenticationProperties = new UAARestClientAuthenticationProperties();
		restClientAuthenticationProperties.setPassword(password);
		restClientAuthenticationProperties.setUsername(username);
		if (clientSelfconfiguration.getCache() != null) {
			CacheConfiguration cache = uaaRestClientProperties.getCache();
			cache.setEnabled(clientSelfconfiguration.getCache().isEnabled());
			List<CacheMappingConfiguration> mappingConfigList =
				Optional.ofNullable(clientSelfconfiguration.getCache().getMapping()).orElse(Collections.emptyList()).stream()
					.map(selfconfigCacheMapping -> {
						CacheMappingConfiguration mappingConfig = new CacheMappingConfiguration();
						mappingConfig.setCachePathPattern(selfconfigCacheMapping.getCachePathPattern());
						mappingConfig.setRegionPattern(selfconfigCacheMapping.getRegionPattern());
						mappingConfig.setStaticName(selfconfigCacheMapping.getStaticName());
						return mappingConfig;
					})
					.collect(Collectors.toList());
			cache.setMapping(mappingConfigList);
		}
		TokenType tokenType = null;
		switch (AuthenticationType.valueOf(authType)) {
		case LOCAL:
			restClientAuthenticationProperties.setLoginRelative(new UrlProperty(clientSelfconfiguration.getLocal().getLoginRelativeUrl()));
			tokenType = clientSelfconfiguration.getLocal().getTokenType();
			break;
		case ACTIVE_DIRECTORY_LDAP:
			restClientAuthenticationProperties.setLoginRelative(new UrlProperty(clientSelfconfiguration.getActiveDirectoryLdap().getLoginRelativeUrl()));
			tokenType = clientSelfconfiguration.getActiveDirectoryLdap().getTokenType();
			break;
		case SAML:
			SamlConfiguration samlConfiguration = clientSelfconfiguration.getSaml();

			SsoConfiguration ssoConfiguration = samlConfiguration.getSsoConfiguration();
			SsoProperties ssoProperties = new SsoProperties();
			ssoProperties.setUsernameXpath(ssoConfiguration.getUsernameXpath());
			ssoProperties.setPasswordXpath(ssoConfiguration.getPasswordXpath());
			ssoProperties.setLoginButtonXpath(ssoConfiguration.getLoginButtonXpath());

			SamlProperties samlProperties = new SamlProperties();
			samlProperties.setLoginRelative(new UrlProperty(samlConfiguration.getLoginRelativeUrl()));
			samlProperties.setLogoutRelative(new UrlProperty(samlConfiguration.getLogoutRelativeUrl()));
			samlProperties.setLogoutMethod(samlConfiguration.getLogoutMethod());
			samlProperties.setSsoConfiguration(ssoProperties);
			restClientAuthenticationProperties.setSaml(samlProperties);

			tokenType = clientSelfconfiguration.getSaml().getTokenType();
			break;
		case OAUTH2:
			OidcConfiguration oidcConfiguration = clientSelfconfiguration.getOidc();
			OidcConfiguration.ConfidentialClientConfiguration confidentialClient = oidcConfiguration.getConfidentialClient();
			OidcConfiguration.PublicClientConfiguration publicClient = oidcConfiguration.getPublicClient();

			OidcProperties oidcProperties = new OidcProperties();
			OidcProperties.ConfidentialClientProperties confidentialClientProperties = new OidcProperties.ConfidentialClientProperties();
			OidcProperties.PublicClientProperties publicClientProperties = new OidcProperties.PublicClientProperties();

			if (oauth2ClientType == ClientType.CONFIDENTIAL && oidcConfiguration.getConfidentialClient() != null) {
				confidentialClientProperties.setLoginRelative(new UrlProperty(confidentialClient.getLoginRelativeUrl()));
				confidentialClientProperties.setLogoutRelative(new UrlProperty(confidentialClient.getLogoutRelativeUrl()));
				confidentialClientProperties.setClientId(confidentialClient.getClientId());
				confidentialClientProperties.setRealmName(confidentialClient.getRealmName());
				confidentialClientProperties.setIdpBase(new UrlProperty(confidentialClient.getIdpBaseUrl()));
				confidentialClientProperties.setTokenRenewThresholdInSeconds(confidentialClient.getTokenRenewThresholdInSeconds());
				confidentialClientProperties.setClientSecret(oauth2ClientSecret);
				oidcProperties.setConfidentialClient(confidentialClientProperties);
			} else {
				publicClientProperties.setLoginRelative(new UrlProperty(publicClient.getLoginRelativeUrl()));
				publicClientProperties.setLogoutRelative(new UrlProperty(publicClient.getLogoutRelativeUrl()));
				publicClientProperties.setClientId(publicClient.getClientId());
				publicClientProperties.setRealmName(publicClient.getRealmName());
				publicClientProperties.setIdpBase(new UrlProperty(publicClient.getIdpBaseUrl()));
				publicClientProperties.setTokenRenewThresholdInSeconds(publicClient.getTokenRenewThresholdInSeconds());
				publicClientProperties.setTokenExchangeRelative(new UrlProperty(publicClient.getTokenExchangeRelativeUrl()));
				publicClientProperties.setLoginRedirectRelative(new UrlProperty(publicClient.getLoginRedirectRelativeUrl()));
				publicClientProperties.setSilentRedirectRelative(new UrlProperty(publicClient.getSilentRedirectRelativeUrl()));

				SsoConfiguration publicClientSsoConfiguration = publicClient.getSsoConfiguration();
				SsoProperties publicClientSsoProperties = new SsoProperties();
				publicClientSsoProperties.setUsernameXpath(publicClientSsoConfiguration.getUsernameXpath());
				publicClientSsoProperties.setPasswordXpath(publicClientSsoConfiguration.getPasswordXpath());
				publicClientSsoProperties.setLoginButtonXpath(publicClientSsoConfiguration.getLoginButtonXpath());
				publicClientProperties.setSsoConfiguration(publicClientSsoProperties);

				oidcProperties.setPublicClient(publicClientProperties);
			}
			oidcProperties.setClientType(oauth2ClientType);
			restClientAuthenticationProperties.setOidc(oidcProperties);

			tokenType = clientSelfconfiguration.getOidc().getTokenType();
			break;
		case CERTIFICATE:
			return uaaRestClientProperties;
		case API_KEY:
			uaaRestClientProperties.setApiKeyResource(apiKeyResource);
			tokenType = TokenType.API_KEY;
			break;
		case DELEGATED:
			UAARestClientProperties.DelegatedModeProperties delegatedModeProperties =
				new UAARestClientProperties.DelegatedModeProperties(clientSelfconfiguration.getExcludedDelegatedContexts());
			uaaRestClientProperties.setDelegatedModeConfiguration(delegatedModeProperties);
			uaaRestClientProperties.setAuthorizationHeaderName(authorizationHeaderName);
		}

		TokenConfiguration tokenConfiguration = getTokenConfiguration(tokenType);
		if (tokenConfiguration != null) {
			uaaRestClientProperties.setAuthorizationHeaderName(tokenConfiguration.getAuthorizationHeaderName());
			uaaRestClientProperties.setGeneratedTokenHeaderName(tokenConfiguration.getGeneratedTokenHeaderName());
			uaaRestClientProperties.setGeneratedTokenExpirationHeaderName(tokenConfiguration.getGeneratedTokenExpirationHeaderName());
		}

		uaaRestClientProperties.setAuthenticationConfiguration(restClientAuthenticationProperties);
		return uaaRestClientProperties;
	}

	private TokenConfiguration getTokenConfiguration(TokenType tokenType) {
		return clientSelfconfiguration.getTokens()
			.stream()
			.filter(token -> tokenType == token.getTokenType())
			.findFirst()
			.orElse(null);
	}

}
