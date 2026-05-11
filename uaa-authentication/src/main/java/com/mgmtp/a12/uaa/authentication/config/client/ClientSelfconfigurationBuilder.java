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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.config.client.properties.CacheProperties;
import com.mgmtp.a12.uaa.authentication.config.client.properties.OidcProperties;
import com.mgmtp.a12.uaa.authentication.config.client.properties.SamlProperties;
import com.mgmtp.a12.uaa.authentication.config.client.properties.SsoProperties;
import com.mgmtp.a12.uaa.authentication.internal.TokenType;

public class ClientSelfconfigurationBuilder {

	private static final String LOGIN_TEMPLATE_RELATIVE_URL = "user/%s/login";
	private static final String URL_LOGOUT_RELATIVE_URL = "user/logout";
	private boolean isLocalSelected;
	private boolean isLdapSelected;
	private boolean isSamlSelected;
	private boolean isOauth2Selected;
	private boolean isCertificateSelected;

	private AuthenticationProperties authenticationProperties;

	private ClientSelfconfigurationBuilder(AuthenticationProperties authenticationProperties) {
		this.authenticationProperties = authenticationProperties;
	}

	public static ClientSelfconfigurationBuilder withConfiguration(AuthenticationProperties authenticationProperties) {
		return new ClientSelfconfigurationBuilder(authenticationProperties);
	}

	public ClientSelfconfiguration build() {
		getAuthenticationTypesAreConfigured();

		ClientSelfconfiguration clientConfiguration = new ClientSelfconfiguration();
		clientConfiguration.setApplicationBaseUrl(authenticationProperties.getClientSelfconfiguration().getApplicationBase().getUrl());
		clientConfiguration.setUaaBaseUrl(authenticationProperties.getClientSelfconfiguration().getUaaBase().getUrl());
		clientConfiguration.setAuthorizationDataStore(authenticationProperties.getClientSelfconfiguration().getAuthorizationDataStore());
		clientConfiguration.setExcludedDelegatedContexts(authenticationProperties.getClientSelfconfiguration().getExcludedDelegatedContexts());
		if (authenticationProperties.getClientSelfconfiguration().getCache() != null) {
			CacheProperties cacheProperties = authenticationProperties.getClientSelfconfiguration().getCache();
			CacheConfiguration cache = new CacheConfiguration();
			cache.setEnabled(cacheProperties.isEnabled());
			List<CacheMappingConfiguration> mappingConfigurationList =
				Optional.ofNullable(cacheProperties.getMapping()).map(List::stream).orElse(Stream.empty())
					.map(mappingProperties -> {
						CacheMappingConfiguration mappingConfiguration = new CacheMappingConfiguration();
						mappingConfiguration.setCachePathPattern(mappingProperties.getCachePathPattern());
						mappingConfiguration.setRegionPattern(mappingProperties.getRegionPattern());
						mappingConfiguration.setStaticName(mappingProperties.getStaticName());
						return mappingConfiguration;
					}).collect(Collectors.toList());
			cache.setMapping(mappingConfigurationList);
			clientConfiguration.setCache(cache);
		}

		List<TokenConfiguration> tokenConfigurations = authenticationProperties.getClientSelfconfiguration().getTokenConfigurations();

		if (tokenConfigurations.isEmpty()) {
			if (authenticationProperties.getJwt() != null) {
				TokenConfiguration uaaBearerToken = TokenConfiguration.getDefaultUaaTokenConfiguration();
				uaaBearerToken.setAuthorizationHeaderName(authenticationProperties.getJwt().getHeaderName());
				uaaBearerToken.setAllowCredentials(
					Optional.ofNullable(authenticationProperties.getCors()).map(cors -> cors.getAllowCredentials()).filter(Objects::nonNull)
						.orElse(Boolean.FALSE)
				);
				tokenConfigurations.add(uaaBearerToken);
			}
			if (isOauth2Selected) {
				tokenConfigurations.add(TokenConfiguration.getDefaultBearerTokenConfiguration());
			}
			if (isCertificateSelected) {
				TokenConfiguration certificateToken = TokenConfiguration.getDefaultAPIKeyTokenConfiguration();
				certificateToken.setAuthorizationHeaderName(authenticationProperties.getJwt().getHeaderName());
				tokenConfigurations.add(certificateToken);
			}
		}

		clientConfiguration.setTokens(tokenConfigurations);

		if (isLocalSelected) {
			LocalConfiguration local = new LocalConfiguration();
			local.setLoginRelativeUrl(
				LOGIN_TEMPLATE_RELATIVE_URL.formatted(StringUtils.lowerCase(AuthenticationType.LOCAL.name())));
			local.setTokenType(TokenType.UAABEARER);
			local.setLogoutRelativeUrl(URL_LOGOUT_RELATIVE_URL);
			clientConfiguration.setLocal(local);
		}
		if (isLdapSelected) {
			LdapConfiguration ldap = new LdapConfiguration();
			ldap.setLoginRelativeUrl(LOGIN_TEMPLATE_RELATIVE_URL.formatted(StringUtils.lowerCase(AuthenticationType.ACTIVE_DIRECTORY_LDAP.name())));
			ldap.setTokenType(TokenType.UAABEARER);
			ldap.setLogoutRelativeUrl(URL_LOGOUT_RELATIVE_URL);
			clientConfiguration.setActiveDirectoryLdap(ldap);
		}
		if (isSamlSelected) {
			SamlProperties samlProperties = authenticationProperties.getClientSelfconfiguration().getSaml();
			SamlConfiguration saml = new SamlConfiguration();
			saml.setTokenType(TokenType.UAABEARER);
			saml.setLoginRelativeUrl(samlProperties.getLoginRelative().getUrl());
			String logoutUrl = Optional.ofNullable(samlProperties.getLogoutRelative())
				.map(logout -> logout.getUrl())
				.orElse(URL_LOGOUT_RELATIVE_URL);
			saml.setLogoutRelativeUrl(logoutUrl);
			Optional.ofNullable(samlProperties.getLogoutMethod()).ifPresent(method -> saml.setLogoutMethod(method));

			SsoProperties ssoProperties = samlProperties.getSsoConfiguration();
			SsoConfiguration sso = new SsoConfiguration();
			sso.setUsernameXpath(ssoProperties.getUsernameXpath());
			sso.setPasswordXpath(ssoProperties.getPasswordXpath());
			sso.setLoginButtonXpath(ssoProperties.getLoginButtonXpath());

			saml.setSsoConfiguration(sso);
			clientConfiguration.setSaml(saml);
		}
		if (isOauth2Selected) {
			OidcProperties oauth2 = authenticationProperties.getClientSelfconfiguration().getOidc();
			if (oauth2 != null) {
				OidcConfiguration oidcConfiguration = new OidcConfiguration();
				oidcConfiguration.setTokenType(TokenType.BEARER);
				oidcConfiguration.setLogoutIdp(oauth2.getIdpLogout().isEnabled());
				if (oauth2.getConfidentialClient() != null) {
					OidcConfiguration.ConfidentialClientConfiguration confidentialClient = new OidcConfiguration.ConfidentialClientConfiguration();
					OidcProperties.ConfidentialClientProperties confidentialProperties = oauth2.getConfidentialClient();
					confidentialClient.setLoginRelativeUrl(confidentialProperties.getLoginRelative().getUrl());
					confidentialClient.setLogoutRelativeUrl(confidentialProperties.getLogoutRelative().getUrl());
					confidentialClient.setClientId(confidentialProperties.getClientId());
					confidentialClient.setRealmName(confidentialProperties.getRealmName());
					confidentialClient.setTokenRenewThresholdInSeconds(confidentialProperties.getTokenRenewThresholdInSeconds());
					confidentialClient.setIdpBaseUrl(confidentialProperties.getIdpBase().getUrl());
					oidcConfiguration.setConfidentialClient(confidentialClient);
				}
				if (oauth2.getPublicClient() != null) {
					OidcConfiguration.PublicClientConfiguration publicClient = new OidcConfiguration.PublicClientConfiguration();
					OidcProperties.PublicClientProperties publicClientProperties = oauth2.getPublicClient();
					publicClient.setLoginRelativeUrl(publicClientProperties.getLoginRelative().getUrl());
					publicClient.setLogoutRelativeUrl(publicClientProperties.getLogoutRelative().getUrl());
					publicClient.setLoginRedirectRelativeUrl(publicClientProperties.getLoginRedirectRelative().getUrl());
					publicClient.setSilentRedirectRelativeUrl(publicClientProperties.getSilentRedirectRelative().getUrl());
					publicClient.setEnableRefreshTokenGrant(publicClientProperties.getEnableRefreshTokenGrant());
					publicClient.setTokenRenewThresholdInSeconds(publicClientProperties.getTokenRenewThresholdInSeconds());
					publicClient.setCurrentUserUrl(publicClientProperties.getCurrentUserUrl());
					publicClient.setLogoutRedirectRelativeUrl(publicClientProperties.getLogoutRedirectRelative().getUrl());
					publicClient.setTokenExchangeRelativeUrl(publicClientProperties.getTokenExchangeRelative().getUrl());
					publicClient.setClientId(publicClientProperties.getClientId());
					publicClient.setRealmName(publicClientProperties.getRealmName());
					publicClient.setIdpBaseUrl(publicClientProperties.getIdpBase().getUrl());

					SsoProperties ssoProperties = publicClientProperties.getSsoConfiguration();
					SsoConfiguration sso = new SsoConfiguration();
					sso.setUsernameXpath(ssoProperties.getUsernameXpath());
					sso.setPasswordXpath(ssoProperties.getPasswordXpath());
					sso.setLoginButtonXpath(ssoProperties.getLoginButtonXpath());

					publicClient.setSsoConfiguration(sso);
					oidcConfiguration.setPublicClient(publicClient);
				}
				clientConfiguration.setOidc(oidcConfiguration);
			}
		}

		return clientConfiguration;
	}

	private void getAuthenticationTypesAreConfigured() {
		authenticationProperties.getTypes().stream()
			.forEach(authenticationType -> {
				switch (authenticationType) {
				case LOCAL:
					isLocalSelected = true;
					break;
				case ACTIVE_DIRECTORY_LDAP:
					isLdapSelected = true;
					break;
				case SAML:
					isSamlSelected = true;
					break;
				case OAUTH2:
					isOauth2Selected = true;
					break;
				case CERTIFICATE:
					isCertificateSelected = true;
					break;
				}
			});
	}
}
