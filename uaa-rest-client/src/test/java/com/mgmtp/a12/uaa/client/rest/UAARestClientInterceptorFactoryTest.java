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
package com.mgmtp.a12.uaa.client.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.util.ReflectionTestUtils;

import com.mgmtp.a12.connector.rest.AcceptHeaderInterceptor;
import com.mgmtp.a12.uaa.client.rest.auth.AuthenticationHandler;
import com.mgmtp.a12.uaa.client.rest.auth.TokenAcquirer;
import com.mgmtp.a12.uaa.client.rest.auth.TokenValidator;
import com.mgmtp.a12.uaa.client.rest.auth.internal.AuthorizationInterceptor;
import com.mgmtp.a12.uaa.client.rest.auth.internal.DelegatedAuthenticationHandler;
import com.mgmtp.a12.uaa.client.rest.auth.internal.SingleThreadAuthenticationHandler;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.UAACertificateTokenAcquirer;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.jwt.LoginEndpointTokenAcquirer;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.jwt.UAAJwtSamlTokenAcquirer;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.oauth2.ClientType;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.oauth2.UAAOauth2ConfidentialTokenAcquirer;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.oauth2.UAAOauth2PublicTokenAcquirer;
import com.mgmtp.a12.uaa.client.rest.config.AuthenticationType;
import com.mgmtp.a12.uaa.client.rest.config.common.UrlProperty;
import com.mgmtp.a12.uaa.client.rest.config.properties.OidcProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.SsoProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientAuthenticationProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UAARestClientInterceptorFactoryTest {

	private UAARestClientProperties uaaRestClientProperties;
	@Mock
	private TokenValidator tokenValidator;

	@BeforeEach
	void setUp() {
		uaaRestClientProperties = new UAARestClientProperties();
		uaaRestClientProperties.setUaaBase(new UrlProperty("http://localhost:8080"));
		uaaRestClientProperties.setAuthorizationHeaderName("Authorization");
		uaaRestClientProperties.setGeneratedTokenHeaderName("access_token");
		uaaRestClientProperties.setGeneratedTokenRenewInSecondsHeaderName("token_renew_in_seconds");
		UAARestClientAuthenticationProperties authConfiguration = new UAARestClientAuthenticationProperties();
		authConfiguration.setUsername("admin");
		authConfiguration.setPassword("admin");
		uaaRestClientProperties.setAuthenticationConfiguration(authConfiguration);

		Mockito.when(tokenValidator.isTokenValid(Mockito.anyString())).thenReturn(true);
	}

	@Test
	void createAuthorizationInterceptorTestDELEGATED() {
		uaaRestClientProperties.setAuthenticationType(AuthenticationType.DELEGATED);

		ClientHttpRequestInterceptor clientHttpRequestInterceptor = UAARestClientInterceptorFactory.createAuthorizationInterceptor(uaaRestClientProperties);
		Assertions.assertEquals(AuthorizationInterceptor.class, clientHttpRequestInterceptor.getClass());

		AuthorizationInterceptor authorizationInterceptor = (AuthorizationInterceptor) clientHttpRequestInterceptor;
		AuthenticationHandler authHandler = (AuthenticationHandler) ReflectionTestUtils.getField(authorizationInterceptor, "authenticationHandler");
		Assertions.assertEquals(DelegatedAuthenticationHandler.class, authHandler.getClass());
	}

	@Test
	void createAuthorizationInterceptorTestLOCAL() {
		uaaRestClientProperties.setAuthenticationType(AuthenticationType.LOCAL);

		ClientHttpRequestInterceptor clientHttpRequestInterceptor = UAARestClientInterceptorFactory.createAuthorizationInterceptor(uaaRestClientProperties);

		TokenAcquirer tokenAcquirer = tokenAcquirer(clientHttpRequestInterceptor);

		Assertions.assertEquals(LoginEndpointTokenAcquirer.class, tokenAcquirer.getClass());
	}

	@Test
	void createAuthorizationInterceptorTestACTIVE_DIRECTORY_LDAP() {
		uaaRestClientProperties.setAuthenticationType(AuthenticationType.ACTIVE_DIRECTORY_LDAP);

		ClientHttpRequestInterceptor clientHttpRequestInterceptor = UAARestClientInterceptorFactory.createAuthorizationInterceptor(uaaRestClientProperties);

		TokenAcquirer tokenAcquirer = tokenAcquirer(clientHttpRequestInterceptor);

		Assertions.assertEquals(LoginEndpointTokenAcquirer.class, tokenAcquirer.getClass());
	}

	@Test
	void createAuthorizationInterceptorTestSAML() {
		uaaRestClientProperties.setAuthenticationType(AuthenticationType.SAML);

		ClientHttpRequestInterceptor clientHttpRequestInterceptor = UAARestClientInterceptorFactory.createAuthorizationInterceptor(uaaRestClientProperties);

		TokenAcquirer tokenAcquirer = tokenAcquirer(clientHttpRequestInterceptor);

		Assertions.assertEquals(UAAJwtSamlTokenAcquirer.class, tokenAcquirer.getClass());
	}

	@Test
	void createAuthorizationInterceptorTestOAUTH2UsePublicAccessType() {
		uaaRestClientProperties.setAuthenticationType(AuthenticationType.OAUTH2);
		OidcProperties oidcProperties = new OidcProperties();
		oidcProperties.setClientType(ClientType.PUBLIC);
		OidcProperties.PublicClientProperties publicClientProperties = new OidcProperties.PublicClientProperties();
		publicClientProperties.setLoginRelative(new UrlProperty("/auth"));
		publicClientProperties.setTokenExchangeRelative(new UrlProperty("/token"));
		publicClientProperties.setClientId("clientIdTest");
		publicClientProperties.setRealmName("realmNameTest");
		publicClientProperties.setIdpBase(new UrlProperty("http://localhost:9090"));
		SsoProperties ssoProperties = new SsoProperties();
		ssoProperties.setUsernameXpath("//input[@name='username']");
		ssoProperties.setPasswordXpath("//input[@name='password']");
		ssoProperties.setLoginButtonXpath("//button[@name='login']");
		publicClientProperties.setSsoConfiguration(ssoProperties);
		oidcProperties.setPublicClient(publicClientProperties);
		uaaRestClientProperties.getAuthenticationConfiguration().setOidc(oidcProperties);
		ClientHttpRequestInterceptor clientHttpRequestInterceptor = UAARestClientInterceptorFactory.createAuthorizationInterceptor(uaaRestClientProperties);
		TokenAcquirer tokenAcquirer = tokenAcquirer(clientHttpRequestInterceptor);
		Assertions.assertEquals(UAAOauth2PublicTokenAcquirer.class, tokenAcquirer.getClass());
	}

	@Test
	void createAuthorizationInterceptorTestOAUTH2NotUsePublicAccessType() {
		uaaRestClientProperties.setAuthenticationType(AuthenticationType.OAUTH2);
		OidcProperties oidcProperties = new OidcProperties();
		oidcProperties.setClientType(ClientType.CONFIDENTIAL);
		OidcProperties.ConfidentialClientProperties confidentialClientProperties = new OidcProperties.ConfidentialClientProperties();
		confidentialClientProperties.setLoginRelative(new UrlProperty("/token"));
		confidentialClientProperties.setClientId("clientIdTest");
		confidentialClientProperties.setRealmName("realmNameTest");
		confidentialClientProperties.setIdpBase(new UrlProperty("http://localhost:9090"));
		confidentialClientProperties.setClientSecret("secret");
		oidcProperties.setConfidentialClient(confidentialClientProperties);
		uaaRestClientProperties.getAuthenticationConfiguration().setOidc(oidcProperties);

		ClientHttpRequestInterceptor clientHttpRequestInterceptor = UAARestClientInterceptorFactory.createAuthorizationInterceptor(uaaRestClientProperties);

		TokenAcquirer tokenAcquirer = tokenAcquirer(clientHttpRequestInterceptor);

		Assertions.assertEquals(UAAOauth2ConfidentialTokenAcquirer.class, tokenAcquirer.getClass());
	}

	@Test
	void createAuthorizationInterceptorTestCertificate() {
		uaaRestClientProperties.setAuthenticationType(AuthenticationType.CERTIFICATE);
		uaaRestClientProperties.getAuthenticationConfiguration().getCertificate().setKeyStore("KEYSTORE");

		ClientHttpRequestInterceptor clientHttpRequestInterceptor = UAARestClientInterceptorFactory.createAuthorizationInterceptor(uaaRestClientProperties);

		TokenAcquirer tokenAcquirer = tokenAcquirer(clientHttpRequestInterceptor);

		Assertions.assertEquals(UAACertificateTokenAcquirer.class, tokenAcquirer.getClass());
	}

	@Test
	void createAuthorizationInterceptorTestAPIKey() {
		uaaRestClientProperties.setAuthenticationType(AuthenticationType.CERTIFICATE);
		uaaRestClientProperties.setApiKeyResource("APIKey");

		ClientHttpRequestInterceptor clientHttpRequestInterceptor = UAARestClientInterceptorFactory.createAuthorizationInterceptor(uaaRestClientProperties);

		TokenAcquirer tokenAcquirer = tokenAcquirer(clientHttpRequestInterceptor);

		Assertions.assertEquals(UAACertificateTokenAcquirer.class, tokenAcquirer.getClass());
	}

	@Test
	void createAuthorizationInterceptorTestExceptionWithDataStoreResource() {
		uaaRestClientProperties.setAuthenticationType(AuthenticationType.LOCAL);
		Resource resource = new DefaultResourceLoader().getResource("classpath:/fileNotFound.txt");
		uaaRestClientProperties.setAuthorizationDataStore(resource);

		RuntimeException runtimeException =
			Assertions.assertThrows(RuntimeException.class, () -> UAARestClientInterceptorFactory.createAuthorizationInterceptor(uaaRestClientProperties));
		Assertions.assertEquals("Unable to load data store", runtimeException.getMessage());
	}

	@Test
	void createAcceptHeaderInterceptorTest() {
		ClientHttpRequestInterceptor clientHttpRequestInterceptor = UAARestClientInterceptorFactory.createAcceptHeaderInterceptor();

		Assertions.assertEquals(AcceptHeaderInterceptor.class, clientHttpRequestInterceptor.getClass());
	}

	private SingleThreadAuthenticationHandler singleThreadAuthenticationHandler(ClientHttpRequestInterceptor clientHttpRequestInterceptor) {
		Assertions.assertEquals(AuthorizationInterceptor.class, clientHttpRequestInterceptor.getClass());

		AuthorizationInterceptor authorizationInterceptor = (AuthorizationInterceptor) clientHttpRequestInterceptor;
		AuthenticationHandler authHandler = (AuthenticationHandler) ReflectionTestUtils.getField(authorizationInterceptor, "authenticationHandler");
		Assertions.assertEquals(SingleThreadAuthenticationHandler.class, authHandler.getClass());

		SingleThreadAuthenticationHandler singleThreadAuthenticationHandler = (SingleThreadAuthenticationHandler) authHandler;
		return singleThreadAuthenticationHandler;
	}

	private TokenAcquirer tokenAcquirer(ClientHttpRequestInterceptor clientHttpRequestInterceptor) {
		SingleThreadAuthenticationHandler singleThreadAuthenticationHandler = singleThreadAuthenticationHandler(clientHttpRequestInterceptor);
		TokenAcquirer tokenAcquirer = (TokenAcquirer) ReflectionTestUtils.getField(singleThreadAuthenticationHandler, "tokenAcquirer");
		return tokenAcquirer;
	}

}