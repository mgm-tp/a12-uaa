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
package com.mgmtp.a12.uaa.authentication.web.internal;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.config.client.ClientSelfconfiguration;
import com.mgmtp.a12.uaa.authentication.config.client.properties.ClientProperties;
import com.mgmtp.a12.uaa.authentication.config.client.properties.OidcProperties;
import com.mgmtp.a12.uaa.authentication.config.client.properties.SamlProperties;
import com.mgmtp.a12.uaa.authentication.config.client.properties.SsoProperties;
import com.mgmtp.a12.uaa.authentication.config.common.UrlProperty;
import com.mgmtp.a12.uaa.authentication.junit.SerializationUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthenticationControllerTest {

	private static final String TOKEN_KEY = "access_token";
	private static final String TOKEN_EXPIRATION_KEY = "access_token_expiration";
	private static final String TOKEN_RENEW_IN_SECONDS = "token_renew_in_seconds";
	public static final String APPLICATION_BASE_URL = "application_base_url";
	public static final String UAA_BASE_URL = "uaa_base_url";
	public static final String AUTHORIZATION_DATA_STORE = "authorization_data_store";
	public static final String CONTEXT_1 = "context1";
	public static final String CONTEXT_2 = "context2";
	public static final String LOGIN_URL = "login_url";
	public static final String USERNAME_XPATH = "username_xpath";
	public static final String PASSWORD_XPATH = "password_xpath";
	public static final String LOGIN_BUTTON_XPATH = "login_button_xpath";

	@InjectMocks
	private final AuthenticationController authenticationController = new AuthenticationController();

	@Mock
	private AuthenticationProperties authenticationProperties;

	@BeforeEach
	public void init() {
		AuthenticationProperties.Cors cors = new AuthenticationProperties.Cors();
		cors.setExposedHeaders(List.of(TOKEN_KEY));
		cors.setExposedHeaders(List.of(TOKEN_EXPIRATION_KEY));
		cors.setExposedHeaders(List.of(TOKEN_RENEW_IN_SECONDS));
		Mockito.when(authenticationProperties.getCors()).thenReturn(cors);
	}

	@Test
	public void selfConfigurationSuccess_shouldReturnClientSelfconfiguration_whenGivenAuthTypeOauth2WithLogoutIdpFalse() throws IOException {

		ClientProperties clientProperties = mockCommonClientSelfconfiguration();
		OidcProperties oidcProperties = new OidcProperties();
		oidcProperties.getIdpLogout().setEnabled(false);
		clientProperties.setOidc(oidcProperties);

		Mockito.when(authenticationProperties.getTypes()).thenReturn(List.of(AuthenticationType.OAUTH2));
		Mockito.when(authenticationProperties.getClientSelfconfiguration()).thenReturn(clientProperties);

		ClientSelfconfiguration clientSelfconfiguration = authenticationController.selfConfiguration();
		SerializationUtils.assertSerialization("/selfconfiguration/oauth2_logout_idp_false.json", clientSelfconfiguration);
	}

	@Test
	public void selfConfigurationSuccess_shouldReturnClientSelfconfiguration_whenGivenAuthTypeOauth2WithLogoutIdpTrue() throws IOException {
		ClientProperties clientProperties = mockCommonClientSelfconfiguration();
		OidcProperties oidcProperties = new OidcProperties();
		oidcProperties.getIdpLogout().setEnabled(true);
		clientProperties.setOidc(oidcProperties);

		Mockito.when(authenticationProperties.getTypes()).thenReturn(List.of(AuthenticationType.OAUTH2));
		Mockito.when(authenticationProperties.getClientSelfconfiguration()).thenReturn(clientProperties);

		ClientSelfconfiguration clientSelfconfiguration = authenticationController.selfConfiguration();
		SerializationUtils.assertSerialization("/selfconfiguration/oauth2_logout_idp_true.json", clientSelfconfiguration);
	}

	@Test
	public void selfConfigurationSuccess_shouldReturnClientSelfconfiguration_whenGivenAuthTypeOauth2WithDefaultLogoutIdp() throws IOException {
		ClientProperties clientProperties = mockCommonClientSelfconfiguration();
		OidcProperties oidcProperties = new OidcProperties();
		clientProperties.setOidc(oidcProperties);

		Mockito.when(authenticationProperties.getTypes()).thenReturn(List.of(AuthenticationType.OAUTH2));
		Mockito.when(authenticationProperties.getClientSelfconfiguration()).thenReturn(clientProperties);

		ClientSelfconfiguration clientSelfconfiguration = authenticationController.selfConfiguration();
		SerializationUtils.assertSerialization("/selfconfiguration/oauth2_logout_idp_true.json", clientSelfconfiguration);
	}

	@Test
	public void selfConfigurationSuccess_shouldReturnClientSelfconfiguration_whenGivenAuthTypeLocal() throws IOException {
		ClientProperties clientProperties = mockCommonClientSelfconfiguration();

		Mockito.when(authenticationProperties.getTypes()).thenReturn(List.of(AuthenticationType.LOCAL));
		Mockito.when(authenticationProperties.getClientSelfconfiguration()).thenReturn(clientProperties);

		ClientSelfconfiguration clientSelfconfiguration = authenticationController.selfConfiguration();
		SerializationUtils.assertSerialization("/selfconfiguration/local.json", clientSelfconfiguration);
	}

	@Test
	public void selfConfigurationSuccess_shouldReturnClientSelfconfiguration_whenGivenAuthTypeSaml() throws IOException {
		ClientProperties clientProperties = mockCommonClientSelfconfiguration();
		SamlProperties samlProperties = new SamlProperties();
		samlProperties.setLoginRelative(new UrlProperty(LOGIN_URL));
		SsoProperties ssoProperties = new SsoProperties();
		samlProperties.setSsoConfiguration(ssoProperties);
		ssoProperties.setUsernameXpath(USERNAME_XPATH);
		ssoProperties.setPasswordXpath(PASSWORD_XPATH);
		ssoProperties.setLoginButtonXpath(LOGIN_BUTTON_XPATH);
		clientProperties.setSaml(samlProperties);

		Mockito.when(authenticationProperties.getTypes()).thenReturn(List.of(AuthenticationType.SAML));
		Mockito.when(authenticationProperties.getClientSelfconfiguration()).thenReturn(clientProperties);

		ClientSelfconfiguration clientSelfconfiguration = authenticationController.selfConfiguration();
		SerializationUtils.assertSerialization("/selfconfiguration/saml.json", clientSelfconfiguration);
	}

	@Test
	public void selfConfigurationSuccess_shouldReturnClientSelfconfiguration_whenGivenAuthTypeLdap() throws IOException {
		ClientProperties clientProperties = mockCommonClientSelfconfiguration();

		Mockito.when(authenticationProperties.getTypes()).thenReturn(List.of(AuthenticationType.ACTIVE_DIRECTORY_LDAP));
		Mockito.when(authenticationProperties.getClientSelfconfiguration()).thenReturn(clientProperties);

		ClientSelfconfiguration clientSelfconfiguration = authenticationController.selfConfiguration();
		SerializationUtils.assertSerialization("/selfconfiguration/ldap.json", clientSelfconfiguration);
	}

	private static ClientProperties mockCommonClientSelfconfiguration() {
		ClientProperties clientProperties = new ClientProperties();
		clientProperties.setApplicationBase(new UrlProperty(APPLICATION_BASE_URL));
		clientProperties.setUaaBase(new UrlProperty(UAA_BASE_URL));
		clientProperties.setAuthorizationDataStore(AUTHORIZATION_DATA_STORE);
		clientProperties.setExcludedDelegatedContexts(new String[] { CONTEXT_1, CONTEXT_2 });
		return clientProperties;
	}

}
