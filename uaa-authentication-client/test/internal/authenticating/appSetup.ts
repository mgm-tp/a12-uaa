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
import {
	TokenConfiguration,
	UaaClientConfiguration,
	UaaLdapConfiguration,
	UaaLocalConfiguration,
	UaaOidcConfiguration
} from "../../../src/internal/interfaces/configuration.js";
import {
	AuthenticationType,
	SelfConfigure,
	TokenManagement,
	UaaFilters,
	UaaSamlConfiguration,
	UaaSelectors
} from "../../../src/index.js";

import createUaaStore from "./reduxSetup.js";

export const WINDOW_ORIGIN = "origin";

const DEFAULT_OIDC_TOKEN_SELF_CONFIGURE: TokenConfiguration = {
	authorizationHeaderName: "Authorization",
	tokenType: "Bearer",
	allowCredentials: false
};

// const serverURL = `${window.location.protocol}//${window.location.hostname}${window.location.port ? `:${window.location.port}` : ""}`;
export const serverURLCommon = "urlCommon";

export const localConfiguration: UaaLocalConfiguration = {
	serverURL: "localURL",
	automaticallyLogin: true,
	logoutMethod: "POST",
	tokenType: "UAABEARER"
};
export const ldapConfiguration: UaaLdapConfiguration = {
	serverURL: "ldapURL",
	automaticallyLogin: true,
	logoutRelativeUrl: "user/logout",
	logoutMethod: "POST"
};
export const samlConfiguration: UaaSamlConfiguration = {
	serverURL: "samlURL",
	loginRelativeUrl: "saml2/authenticate/uaa",
	logoutRelativeUrl: "user/logout"
};

export const oidcConfiguration: UaaOidcConfiguration = {
	serverURL: "oidcURL",
	authority: `idpURL/realms/UAARealm`,
	client_id: `uaa_spa_client`,
	redirect_uri: `${WINDOW_ORIGIN}/callback`,
	post_logout_redirect_uri: `${WINDOW_ORIGIN}/logout`,
	silent_redirect_uri: `${WINDOW_ORIGIN}/silent_renew.html`,
	automaticallyLogin: true,
	loadUserInfo: true,
	logoutIdp: true,
	popupAuthentication: true
};

export const offlineSelfConfigure: SelfConfigure = {
	applicationBaseUrl: "http://localhost:8080",
	uaaBaseUrl: "http://localhost:8080",
	tokens: [
		{
			authorizationHeaderName: "wrong authorization",
			tokenType: "UAABEARER",
			generatedTokenHeaderName: "wrong token name",
			generatedTokenExpirationHeaderName: "wrong token expiration",
			allowCredentials: false
		}
	],
	saml: {
		loginRelativeUrl: "wrong login",
		logoutRelativeUrl: "wrong logout",
		logoutMethod: "POST",
		tokenType: "UAABEARER"
	},
	local: {
		loginRelativeUrl: "user/local/login",
		logoutRelativeUrl: "user/logout",
		logoutMethod: "wrong method",
		tokenType: "wrong type"
	},
	activeDirectoryLdap: {
		loginRelativeUrl: "user/active_directory_ldap/login",
		logoutRelativeUrl: "wrong logout",
		logoutMethod: "wrong method",
		tokenType: "UAABEARER"
	},
	oidc: {
		tokenType: "BEARER",
		clientId: "uaa-spa-client",
		realmName: "UAARealm",
		idpBaseUrl: "idpURL",
		loginRedirectRelativeUrl: "callback",
		logoutRedirectRelativeUrl: "logout",
		silentRedirectRelativeUrl: "silent_renew.html",
		logoutIdp: true,
		popupAuthentication: false
	}
};

const tokenConfigure = TokenManagement.getInstance().getTokenConfiguration(
	AuthenticationType.LOCAL
);
const requestFilter = [
	new UaaFilters.AuthorizationHeaderFilter(
		() => createUaaStore().getState() as { uaa: UaaSelectors.UaaSlice },
		tokenConfigure
	)
];
const responseFilters = [
	new UaaFilters.TokenResponseFilter(tokenConfigure),
	new UaaFilters.ResponseFilter401()
];

export const uaaClientConfiguration: UaaClientConfiguration = {
	serverURL: serverURLCommon,
	offlineSelfConfigure: offlineSelfConfigure,
	additionalRequestFilter: requestFilter,
	additionalResponseFilter: responseFilters,
	automaticallyLogin: false,
	overrideClientConfigures: {
		local: localConfiguration,
		ldap: ldapConfiguration,
		saml: samlConfiguration,
		oidc: oidcConfiguration,
		tokens: [
			{
				authorizationHeaderName: "Authorization",
				tokenType: "UAABEARER",
				generatedTokenHeaderName: "access_token",
				generatedTokenExpirationHeaderName: "access_token_expiration",
				allowCredentials: true
			},
			{
				authorizationHeaderName: "Authorization",
				tokenType: "BEARER",
				allowCredentials: false
			},
			{
				authorizationHeaderName: "Authorization",
				tokenType: "CERT",
				allowCredentials: false
			}
		]
	}
};

export const uaaClientConfigurationWithoutOverride: UaaClientConfiguration = {
	serverURL: serverURLCommon,
	offlineSelfConfigure: offlineSelfConfigure
};

export const uaaClientOidcSelfConfigure: UaaClientConfiguration = {
	serverURL: serverURLCommon,
	offlineSelfConfigure: {
		oidc: {
			realmName: "wrong realm",
			clientId: "wrong client ID",
			tokenType: "wrong token type",
			idpBaseUrl: "wrong idp url",
			loginRedirectRelativeUrl: "wrong redirect url",
			logoutRedirectRelativeUrl: "wrong logout redirect url",
			silentRedirectRelativeUrl: "wrong silent url",
			logoutIdp: false
		},
		tokens: [DEFAULT_OIDC_TOKEN_SELF_CONFIGURE]
	},
	overrideClientConfigures: {
		oidc: {
			authority: `${serverURLCommon}/realms/UAARealm`,
			tokenType: "BEARER",
			client_id: "uaa_spa_client",
			logoutIdp: true,
			popupAuthentication: false,
			redirect_uri: `${WINDOW_ORIGIN}/callback`,
			silent_redirect_uri: `${WINDOW_ORIGIN}`,
			post_logout_redirect_uri: `${WINDOW_ORIGIN}/logout`
		}
	}
};

export const uaaClientOidcMissingRequiredProperties: UaaClientConfiguration = {
	serverURL: "serverURLCommon",
	overrideClientConfigures: {
		oidc: {
			tokenType: "BEARER",
			logoutIdp: true,
			popupAuthentication: false
		}
	}
};

export const localConfigurationExpect: UaaLocalConfiguration = {
	serverURL: "localURL",
	additionalRequestFilter: requestFilter,
	additionalResponseFilter: responseFilters,
	automaticallyLogin: true,
	serverConnector: undefined,
	logoutRelativeUrl: "user/logout",
	logoutMethod: "POST",
	loginRelativeUrl: "user/local/login",
	tokenType: "UAABEARER"
};

export const ldapConfigurationExpect: UaaLocalConfiguration = {
	serverURL: "ldapURL",
	additionalRequestFilter: requestFilter,
	additionalResponseFilter: responseFilters,
	automaticallyLogin: true,
	serverConnector: undefined,
	loginRelativeUrl: "user/active_directory_ldap/login",
	logoutRelativeUrl: "user/logout",
	logoutMethod: "POST",
	tokenType: "UAABEARER"
};

export const oidcConfigurationExpect: UaaOidcConfiguration = {
	serverURL: "oidcURL",
	additionalRequestFilter: requestFilter,
	additionalResponseFilter: responseFilters,
	serverConnector: undefined,
	tokenType: "BEARER",
	authority: `idpURL/realms/UAARealm`,
	client_id: `uaa_spa_client`,
	redirect_uri: `${WINDOW_ORIGIN}/callback`,
	post_logout_redirect_uri: `${WINDOW_ORIGIN}/logout`,
	silent_redirect_uri: `${WINDOW_ORIGIN}/silent_renew.html`,
	automaticallyLogin: true,
	loadUserInfo: true,
	popupAuthentication: true,
	logoutIdp: true
};

export const samlConfigurationExpect: UaaSamlConfiguration = {
	serverURL: "samlURL",
	additionalRequestFilter: requestFilter,
	additionalResponseFilter: responseFilters,
	serverConnector: undefined,
	automaticallyLogin: false,
	loginRelativeUrl: "saml2/authenticate/uaa",
	logoutRelativeUrl: "user/logout",
	logoutMethod: "POST",
	tokenType: "UAABEARER"
};
