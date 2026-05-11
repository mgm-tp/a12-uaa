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
package com.mgmtp.a12.uaa.authentication.config.self.calculate;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.config.client.ClientSelfconfiguration;
import com.mgmtp.a12.uaa.authentication.config.client.ClientSelfconfigurationBuilder;
import com.mgmtp.a12.uaa.authentication.config.client.properties.CacheMappingProperties;
import com.mgmtp.a12.uaa.authentication.config.client.properties.CacheProperties;
import com.mgmtp.a12.uaa.authentication.config.client.properties.ClientProperties;
import com.mgmtp.a12.uaa.authentication.config.client.properties.OidcProperties;
import com.mgmtp.a12.uaa.authentication.config.client.properties.SamlProperties;
import com.mgmtp.a12.uaa.authentication.config.client.properties.SsoProperties;
import com.mgmtp.a12.uaa.authentication.config.common.UrlProperty;
import com.mgmtp.a12.uaa.authentication.internal.TokenType;

public class ClientSelfconfigurationBuilderTest {

	private AuthenticationProperties authenticationProperties;

	@BeforeEach
	void setUp() {
		authenticationProperties = new AuthenticationProperties();
		AuthenticationProperties.JwtProperties jwtProperties = new AuthenticationProperties.JwtProperties();
		jwtProperties.setHeaderName("Authorization");
		authenticationProperties.setJwt(jwtProperties);
		authenticationProperties.setTypes(Arrays.asList(AuthenticationType.LOCAL, AuthenticationType.ACTIVE_DIRECTORY_LDAP,
			AuthenticationType.SAML, AuthenticationType.OAUTH2, AuthenticationType.OAUTH2_CLIENT));
		authenticationProperties.getLogout().setRedirect(new AuthenticationProperties.Redirect());
		authenticationProperties.getLogout().getRedirect().getSuccess().setUrl("http://localhost:8080");
		AuthenticationProperties.SamlProperties samlProperties = new AuthenticationProperties.SamlProperties();
		samlProperties.setLogin(new UrlProperty("http://localhost:8080/saml2/authenticate/uaa"));
		authenticationProperties.setSaml(samlProperties);

		AuthenticationProperties.LdapProperties ldapProperties = new AuthenticationProperties.LdapProperties();
		authenticationProperties.setLdap(ldapProperties);

		AuthenticationProperties.Oauth2Properties oauth2Properties = new AuthenticationProperties.Oauth2Properties();
		oauth2Properties.setPostLogout(new UrlProperty("http://localhost:8080"));
		authenticationProperties.setOauth2(oauth2Properties);

		// Build client self-configuration
		ClientProperties clientProperties = new ClientProperties();
		UrlProperty baseUrl = new UrlProperty();
		baseUrl.setUrl("http://localhost:8080");
		clientProperties.setApplicationBase(baseUrl);
		clientProperties.setUaaBase(baseUrl);

		SamlProperties clientSaml = new SamlProperties();
		UrlProperty samlRelativeLogin = new UrlProperty();
		samlRelativeLogin.setUrl("saml2/authenticate/uaa");
		clientSaml.setLoginRelative(samlRelativeLogin);
		SsoProperties ssoProperties = new SsoProperties();
		ssoProperties.setLoginButtonXpath("//button[@name='login']");
		ssoProperties.setPasswordXpath("//input[@name='password']");
		ssoProperties.setUsernameXpath("//input[@name='username']");
		clientSaml.setSsoConfiguration(ssoProperties);
		clientProperties.setSaml(clientSaml);

		OidcProperties clientOauth2 = new OidcProperties();
		UrlProperty idpUrl = new UrlProperty();
		idpUrl.setUrl("http://localhost:9090");

		OidcProperties.PublicClientProperties publicClient = new OidcProperties.PublicClientProperties();
		publicClient.setSsoConfiguration(ssoProperties);
		publicClient.setRealmName("UAARealm");
		publicClient.setIdpBase(idpUrl);
		publicClient.setClientId("uaa-spa-client");

		UrlProperty relativeLoginRedirect = new UrlProperty();
		relativeLoginRedirect.setUrl("callback");
		publicClient.setLoginRedirectRelative(relativeLoginRedirect);

		UrlProperty oauth2RelativeLogin = new UrlProperty();
		relativeLoginRedirect.setUrl("auth");
		publicClient.setLoginRelative(oauth2RelativeLogin);

		UrlProperty oauth2RelativeLogout = new UrlProperty("logout");
		publicClient.setLogoutRelative(oauth2RelativeLogout);

		UrlProperty relativeTokenExchange = new UrlProperty();
		relativeTokenExchange.setUrl("token");
		publicClient.setTokenExchangeRelative(relativeTokenExchange);

		UrlProperty relativeSilentRedirect = new UrlProperty();
		relativeSilentRedirect.setUrl("silent_redirect.html");
		publicClient.setSilentRedirectRelative(relativeSilentRedirect);

		publicClient.setTokenRenewThresholdInSeconds(60);

		UrlProperty relativeLogoutRedirect = new UrlProperty();
		relativeLogoutRedirect.setUrl("logout");
		publicClient.setLogoutRedirectRelative(relativeLogoutRedirect);

		publicClient.setEnableRefreshTokenGrant(false);

		clientOauth2.setPublicClient(publicClient);

		OidcProperties.ConfidentialClientProperties confidentialClient = new OidcProperties.ConfidentialClientProperties();
		confidentialClient.setRealmName("UAARealm");
		confidentialClient.setIdpBase(idpUrl);
		confidentialClient.setClientId("uaa-auth-client");
		oauth2RelativeLogin = new UrlProperty();
		relativeLoginRedirect.setUrl("auth");
		confidentialClient.setLoginRelative(oauth2RelativeLogin);
		confidentialClient.setLogoutRelative(oauth2RelativeLogout);
		clientOauth2.setConfidentialClient(confidentialClient);

		CacheProperties cache = new CacheProperties();
		cache.setEnabled(true);
		CacheMappingProperties mappingProps = new CacheMappingProperties();
		mappingProps.setCachePathPattern("/cachePath/endpoint");
		mappingProps.setRegionPattern("/cachePath/*");
		mappingProps.setStaticName("staticName");
		cache.setMapping(Arrays.asList(mappingProps));
		clientProperties.setCache(cache);

		clientProperties.setOidc(clientOauth2);

		authenticationProperties.setClientSelfconfiguration(clientProperties);

	}

	@Test
	void buildTest() {
		ClientSelfconfiguration build = ClientSelfconfigurationBuilder.withConfiguration(authenticationProperties).build();
		Assertions.assertEquals("http://localhost:8080", build.getApplicationBaseUrl());
		Assertions.assertEquals(2, build.getTokens().size());

		Assertions.assertEquals("user/local/login", build.getLocal().getLoginRelativeUrl());
		Assertions.assertEquals("user/logout", build.getLocal().getLogoutRelativeUrl());
		Assertions.assertEquals(TokenType.UAABEARER, build.getLocal().getTokenType());

		Assertions.assertEquals("user/active_directory_ldap/login", build.getActiveDirectoryLdap().getLoginRelativeUrl());
		Assertions.assertEquals("user/logout", build.getActiveDirectoryLdap().getLogoutRelativeUrl());
		Assertions.assertEquals(TokenType.UAABEARER, build.getActiveDirectoryLdap().getTokenType());

		Assertions.assertEquals("saml2/authenticate/uaa", build.getSaml().getLoginRelativeUrl());
		Assertions.assertEquals(TokenType.UAABEARER, build.getSaml().getTokenType());
		Assertions.assertEquals("//input[@name='username']", build.getSaml().getSsoConfiguration().getUsernameXpath());
		Assertions.assertEquals("//input[@name='password']", build.getSaml().getSsoConfiguration().getPasswordXpath());
		Assertions.assertEquals("//button[@name='login']", build.getSaml().getSsoConfiguration().getLoginButtonXpath());
		Assertions.assertEquals("user/logout", build.getSaml().getLogoutRelativeUrl());

		Assertions.assertEquals("uaa-spa-client", build.getOidc().getPublicClient().getClientId());
		Assertions.assertEquals("http://localhost:9090", build.getOidc().getPublicClient().getIdpBaseUrl());
		Assertions.assertEquals(TokenType.BEARER, build.getOidc().getTokenType());

		Assertions.assertEquals("uaa-auth-client", build.getOidc().getConfidentialClient().getClientId());
		Assertions.assertEquals("http://localhost:9090", build.getOidc().getConfidentialClient().getIdpBaseUrl());
		Assertions.assertEquals(TokenType.BEARER, build.getOidc().getTokenType());

		Assertions.assertEquals(true, build.getCache().isEnabled());
		Assertions.assertEquals("/cachePath/endpoint", build.getCache().getMapping().get(0).getCachePathPattern());
		Assertions.assertEquals("/cachePath/*", build.getCache().getMapping().get(0).getRegionPattern());
		Assertions.assertEquals("staticName", build.getCache().getMapping().get(0).getStaticName());
	}

}
