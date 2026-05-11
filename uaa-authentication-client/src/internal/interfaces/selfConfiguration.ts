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
import { LoggerFactory } from "@com.mgmtp.a12.utils/utils-logging/lib/factory.js";

import { AuthenticationType } from "./index.js";

/**
 * The interface expresses the way how the token (UAA Token or JWT Token) is sent to server.
 */
export interface TokenSelfConfigure {
	/**
	 * The header value which the token has to be attached there and send to resource server.
	 */
	readonly authorizationHeaderName?: string;
	/**
	 * The header which contains value of expiration time of the token.
	 */
	readonly generatedTokenExpirationHeaderName?: string;
	/**
	 * The header which contains token generated.
	 */
	readonly generatedTokenHeaderName?: string;
	/**
	 * This would tell the browser cookies need to be sent.
	 */
	readonly allowCredentials?: boolean;
	/**
	 * The type of the token. It can be UAABearer or Bearer or any other type of the token.
	 */
	readonly tokenType?: string;
}

/**
 * The interface expresses the way how to configure your OAuth2 client to connect to IDP
 * This contains all necessary information to trigger a login request with OIDC Authentication protocol
 * to your Identity Provider.
 */
export interface OidcSelfConfigure {
	/**
	 * The type of the token.
	 */
	readonly tokenType?: string;
	/**
	 * Client ID
	 */
	readonly clientId: string;
	/**
	 * Name of Realm
	 */
	readonly realmName: string;
	/**
	 * Url to Identity Provider
	 */
	readonly idpBaseUrl: string;
	/**
	 * Redirect URL after successfully login
	 */
	readonly loginRedirectRelativeUrl: string;
	/**
	 * Redirect URL after successfully logout
	 */
	readonly logoutRedirectRelativeUrl: string;
	/**
	 * Silent Renew path/URL which used to take care of silent renew process
	 */
	readonly silentRedirectRelativeUrl: string;
	/**
	 * Logout IDP.
	 */
	readonly logoutIdp?: boolean;
	/**
	 * Use the OAuth 2.0 `refresh_token` grant to renew the access token.
	 * If true the library exchanges the stored refresh token for a new access token and skip the silent renew HTML page.
	 * If false, falls back to the silent-renew page workflow.
	 */
	readonly enableRefreshTokenGrant?: boolean;
	/**
	 * Determine the UI behavior when process login and logout
	 */
	readonly popupAuthentication?: boolean;
	/**
	 * The `tokenRenewThresholdInSeconds` property determines how many second before the token’s expiration we begin a silent renewal process.
	 * Once the token’s remaining lifespan equals this threshold, the system attempts to seamlessly refresh it in the background.
	 */
	readonly tokenRenewThresholdInSeconds?: number;
	/**
	 * Custom endpoint for fetching the current user after OIDC login.
	 * If set, the library calls this URL to load the current user.
	 * If omitted, defaults to `${baseUrl}/uaa-authentication/currentUser`.
	 */
	readonly currentUserUrl?: string;
}

/**
 * The interface expresses the way how to configure your LOCAL client to connect to your backend using
 * Authentication Authorization libraries.
 * This contains all necessary information to trigger a login request with Username/password
 * which is provided by UAA
 */
export interface LocalSelfConfigure {
	/**
	 * Logout Method
	 */
	readonly logoutMethod?: string;
	/**
	 * Token Type. It can be UAABearer or regular Bearer
	 */
	readonly tokenType?: string;
	/**
	 * Login Relative url
	 */
	readonly loginRelativeUrl?: string;
	/**
	 * Logout Relative url
	 */
	readonly logoutRelativeUrl?: string;
}

/**
 * The interface expresses the way how to configure your LDAP client to connect to your backend using
 * Authentication Authorization libraries.
 */
export interface LdapSelfConfigure extends LocalSelfConfigure {}

/**
 * The interface expresses the way how to configure your SAML client to connect to your backend using
 * Authentication Authorization libraries.
 */
export interface SAMLSelfConfigure extends LocalSelfConfigure {}

/**
 * The interface expresses the full configuration structure for self configuration feature in the front-end.
 * There are some common properties which should be used by all self configuration types like
 * applicationBasedUrl, uaaBaseUrl properties.
 *
 */
export interface SelfConfigure {
	/**
	 * Application based url
	 */
	readonly applicationBaseUrl?: string;
	/**
	 * UAA Base Url
	 */
	readonly uaaBaseUrl?: string;
	/**
	 * Self Configure for Oauth2/OIDC
	 */
	readonly oidc?: OidcSelfConfigure;
	/**
	 * Self Configure for Local
	 */
	readonly local?: LocalSelfConfigure;
	/**
	 * Self Configure for SAML
	 */
	readonly saml?: SAMLSelfConfigure;
	/**
	 * Self Configure for activeDirectoryLDAP
	 */
	readonly activeDirectoryLdap?: LdapSelfConfigure;
	/**
	 * Multiple token configuration
	 */
	readonly tokens?: TokenSelfConfigure[];
}

/**
 * The typing checking to guarantee that you have a valid self configure return either from server configuration
 * or from offline configuration as json object.
 *
 * @param obj
 */
export function isSelfConfigure(
	obj: SelfConfigure | Record<string, unknown>
): obj is SelfConfigure {
	const isPropertyNameValid = (
		type: AuthenticationType | string,
		testValue: Record<string, unknown>,
		defaultValue: LocalSelfConfigure | OidcSelfConfigure | TokenSelfConfigure
	): boolean => {
		const validProperties = Object.keys(defaultValue);
		const invalidValues = Object.keys(testValue).filter(value =>
			validProperties.every(item => item !== value)
		);
		if (invalidValues.length > 0) {
			LoggerFactory.getLogger("UAA/Selfconfigure").error(
				`The ${type} configure have invalid property ${invalidValues.toString()}. \n The correct properties must be in [${Object.keys(
					defaultValue
				).toString()}]`
			);
			return false;
		}
		return true;
	};
	const defaultValue: LocalSelfConfigure = {
		logoutMethod: "",
		tokenType: "",
		loginRelativeUrl: "",
		logoutRelativeUrl: ""
	};
	const defaultValueOidc: OidcSelfConfigure = {
		tokenType: "",
		clientId: "",
		realmName: "",
		idpBaseUrl: "",
		loginRedirectRelativeUrl: "",
		logoutRedirectRelativeUrl: "",
		silentRedirectRelativeUrl: "",
		tokenRenewThresholdInSeconds: 0,
		currentUserUrl: "",
		logoutIdp: true,
		enableRefreshTokenGrant: false,
		popupAuthentication: false
	};

	if ("tokens" in obj) {
		const tokenDefaultValue: TokenSelfConfigure = {
			authorizationHeaderName: "",
			generatedTokenExpirationHeaderName: "",
			generatedTokenHeaderName: "",
			tokenType: "",
			allowCredentials: false
		};
		if (Array.isArray(obj.tokens)) {
			// This check will be removed in next breaking change.
			if (obj.tokens.length === 0) {
				LoggerFactory.getLogger("UAA/Selfconfigure").error(
					"The tokens must not empty"
				);
				return false;
			}

			if (
				!isPropertyNameValid(
					"token",
					obj.tokens[0] as unknown as Record<string, unknown>,
					tokenDefaultValue
				)
			) {
				return false;
			}
		} else {
			LoggerFactory.getLogger("UAA/Selfconfigure").error(
				"Property tokens is array"
			);
			return false;
		}
	}

	if ("local" in obj && typeof obj.local === "object") {
		if (
			!isPropertyNameValid(
				AuthenticationType.LOCAL,
				obj.local as unknown as Record<string, unknown>,
				defaultValue
			)
		) {
			return false;
		}
	}

	if (
		"activeDirectoryLdap" in obj &&
		typeof obj.activeDirectoryLdap === "object"
	) {
		if (
			!isPropertyNameValid(
				AuthenticationType.ACTIVE_DIRECTORY_LDAP,
				obj.activeDirectoryLdap as unknown as Record<string, unknown>,
				defaultValue
			)
		) {
			return false;
		}
	}

	if ("saml" in obj && typeof obj.saml === "object") {
		if (
			!isPropertyNameValid(
				AuthenticationType.SAML,
				obj.saml as unknown as Record<string, unknown>,
				defaultValue
			)
		) {
			return false;
		}
	}

	if ("oidc" in obj && typeof obj.oidc === "object") {
		if (
			!isPropertyNameValid(
				AuthenticationType.OAUTH2,
				obj.oidc as unknown as Record<string, unknown>,
				defaultValueOidc
			)
		) {
			return false;
		}
	}

	return true;
}
