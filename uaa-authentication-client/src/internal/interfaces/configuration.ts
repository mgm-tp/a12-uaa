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
import { Dispatch, Middleware, Reducer, Store } from "redux";
import {
	SigninPopupArgs,
	SigninRedirectArgs,
	SignoutResponse
} from "oidc-client-ts";

import {
	RequestFilter,
	ResponseFilter,
	ServerConnector
} from "@com.mgmtp.a12.utils/utils-connector";

import { UaaOidcUser } from "./user.js";
import { SelfConfigure, TokenSelfConfigure } from "./selfConfiguration.js";

/**
 * The central interface for customer project to pass on configuration for all authentication types they have.
 * This is considered as convenient interface to help project centralize the configuration UAA from the outside.
 */
export interface UaaClientConfiguration {
	/**
	 * This is your server domain URL. If serverSelfConfigureUrl is not configured, this will be used to fetch selfConfigure.
	 */
	readonly serverURL: string;
	/**
	 * This will be used to send request fetch self configure from server.
	 */
	readonly serverSelfConfigureUrl?: string;
	/**
	 * Allow projects to create configuration within the front-end itself and does not require server configuration
	 */
	readonly offlineSelfConfigure?: SelfConfigure;
	/**
	 * Normally a project already had an instance of serverConnect therefore they can pass in so UAA will use that.
	 */
	readonly serverConnector?: ServerConnector;
	/**
	 * Help project using default serverConnector creation from UAA but then add additional RequestFilter into it.
	 */
	readonly additionalRequestFilter?: RequestFilter[];
	/**
	 * Help project using default serverConnector creation from UAA but then add additional ResponseFilter into it.
	 */
	readonly additionalResponseFilter?: ResponseFilter[];
	/**
	 * This will enable UAA checking login status automatically and trigger login
	 */
	readonly automaticallyLogin?: boolean;
	/**
	 * Using overrideClientConfigures to override any configurations for any authentication type.
	 * This property has the highest priority in resolving UAA configurations internally.
	 * Saml, Local, Ldap, Oidc and Tokens to override specific part of configuration.
	 */
	readonly overrideClientConfigures?: {
		saml?: UaaSamlConfiguration;
		local?: UaaLocalConfiguration;
		ldap?: UaaLdapConfiguration;
		oidc?: UaaOidcConfiguration;
		tokens?: TokenConfiguration[];
	};
	/**
	 * Store which is created during the application setup.
	 */
	readonly store?: Store;
}

/**
 * The interface expresses the way how the token (UAA Token or JWT Token) is sent to server.
 */
export type TokenConfiguration = TokenSelfConfigure;

/**
 * Central UAAClient which help you to create any specific Client for any of your authentication type.
 * Each authentication type should have it owns Client.
 *
 * The central UaaClientConfiguration can be also passed here via init method.
 */
export interface UaaClient {
	/**
	 * Store which is created during the application setup.
	 */
	readonly setStore: (store: Store) => void;
	/**
	 * Init method so all Clients behind UAAClient can be initialized.
	 */
	readonly init: (selfConfig: UaaClientConfiguration) => void;
	/**
	 * Return UaaLocalClient instance
	 */
	readonly getLocalClient: () => UaaLocalClient;
	/**
	 * Return UaaLdapClient instance
	 */
	readonly getLdapClient: () => UaaLdapClient;
	/**
	 * Return UaaOidcClient instance
	 */
	readonly getOidcClient: () => UaaOidcClient;
	/**
	 * Return UaaSamlClient instance
	 */
	readonly getSamlClient: () => UaaSamlClient;
	/**
	 * Return Local configuration
	 */
	readonly getLocalConfiguration: () => UaaLocalConfiguration;
	/**
	 * Return Ldap configuration
	 */
	readonly getLdapConfiguration: () => UaaLdapConfiguration;
	/**
	 * Return Saml configuration
	 */
	readonly getOidcConfiguration: () => UaaOidcConfiguration;
	/**
	 * Return Oidc configuration
	 */
	readonly getSamlConfiguration: () => UaaSamlConfiguration;
}

type LogLevel = "debug" | "info" | "error" | "none" | "warn";

/**
 * Common client configuration with all authentication protocol.
 */
interface UaaCommonConfiguration {
	/**
	 * This will be used to send request after successfully login.
	 */
	readonly serverURL?: string;
	/**
	 * Server connector instance which project can pass in to override default from UAA
	 */
	readonly serverConnector?: ServerConnector;
	/**
	 * Additional request filter to default server connector from UAA.
	 */
	readonly additionalRequestFilter?: RequestFilter[];
	/**
	 * Additional response filter to default server connector from UAA.
	 */
	readonly additionalResponseFilter?: ResponseFilter[];
	/**
	 * This will enable UAA checking login status automatically and trigger login.
	 */
	readonly automaticallyLogin?: boolean;
	/**
	 * Token Type. It can be UAABearer or regular Bearer
	 */
	readonly tokenType?: string;
}

/**
 * The client which will be used to manage common functionality for Local, Ldap and Saml authentication protocol.
 */
interface UaaCommonClient {
	/**
	 * This would need to be registered in your store setup in reducer part.
	 */
	readonly reducers: Reducer;
	/**
	 * This would need to be registered in your store setup in middlewares part.
	 */
	readonly middlewares: ReadonlyArray<Middleware>;
	/**
	 * Calling default configuration for serverConnector in UAA.
	 */
	readonly initConnector: () => void;
	/**
	 * Logout method, you need to call when you want to logout.
	 */
	readonly logout: () => void;
}

/**
 * SignoutResponse interface this would be representation of SignoutResponse for UAA
 */
export type UaaSignoutResponse = SignoutResponse;

/**
 * Central Client configuration with OpenIdConnect authentication protocol.
 */
export interface UaaOidcConfiguration extends UaaCommonConfiguration {
	/**
	 * Url to realm in Identity provider.
	 */
	readonly authority?: string;
	/**
	 * The successful redirect url after login.
	 */
	readonly redirect_uri?: string;

	/**
	 * Id of the client used for login.
	 */
	readonly client_id?: string;
	/**
	 * The successful redirect url after logout.
	 */
	readonly post_logout_redirect_uri?: string;
	/**
	 * The silent renew html page or path which in charge of silent_renew project. Normally project would
	 * need to create a separate html and serve it as separate to current application page.
	 */
	readonly silent_redirect_uri?: string;
	/**
	 * Additional scopes which will be sent to Identity Provider.
	 */
	readonly scope?: string;
	/**
	 * Load user information. default value is true.
	 */
	readonly loadUserInfo?: boolean;
	/**
	 * Configure logging level. Default is INFO
	 */
	readonly logLevel?: LogLevel;
	/**
	 * Ask Identify Provide to blacklist the token whenever it's sign out. This will make sure the token
	 * can not be used anymore by any cases after logout is triggered via IDP.
	 */
	readonly revokeTokensOnSignout?: boolean;
	/**
	 * This will trigger logout IDP which means clear the session of the current user in IDP. Default is true.
	 * If you make this one false then there is no request send to IDP to trigger logout. In case you refresh the application
	 * the user is keep login since session from IDP is still kept.
	 */
	readonly logoutIdp?: boolean;
	/**
	 * Use the OAuth 2.0 `refresh_token` grant to renew the access token.
	 * If true the library exchanges the stored refresh token for a new access token and skip the silent renew HTML page.
	 * If false, falls back to the silent-renew page workflow.
	 */
	readonly enableRefreshTokenGrant?: boolean;
	/**
	 * MonitorSession would allow oidc client monitor the changing of the user status in IDP.
	 * Imagine you have 2 applications using 2 clients in 1 realm in your IDP.
	 * You open 2 applications in 2 tabs of browser.
	 * True: you logout in 1 application in one of your tab the other application in other tab will automatically get logout.
	 * False: you logout in 1 application in one of your tab the other application in other tab will remain until you refresh application or token expire.
	 */
	readonly monitorSession?: boolean;
	/**
	 * Determine the UI behavior when process login and logout.
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
	/**
	 * Sets the credentials for fetch requests to OIDC/Oauth2 provider (default: “same-origin�?).
	 * (eg: cookies to the OIDC/OAuth2 provider or if you are using a proxy that requires this cookie)
	 * Additional information please read: https://developer.mozilla.org/en-US/docs/Web/API/Request/credentials
	 */
	readonly fetchRequestCredentialsToOIDCProvider?:
		| "include"
		| "omit"
		| "same-origin";
}

/**
 * The Client which will be used to manage all functionality regarding OIDC/Oauth2 authentication protocol.
 */
export interface UaaOidcClient extends UaaCommonClient {
	/**
	 * Login method, you eed to call when you want to login.
	 */
	readonly login: (signinArgs?: SigninRedirectArgs | SigninPopupArgs) => void;
	/**
	 * This has to be called after the redirection callback from IDP after successfully login.
	 */
	readonly processLoginCallback: () => Promise<UaaOidcUser>;
	/**
	 * This has to be called after the redirection callback from IDP after successfully logout.
	 */
	readonly processLogoutCallback: () => Promise<UaaSignoutResponse>;
	/**
	 * Call this method if you try to login via a popup
	 */
	readonly processLoginCallbackPopup?: () => Promise<void>;
	/**
	 * Call this method if you try to logout via a popup
	 */
	readonly processLogoutCallbackPopup?: () => Promise<void>;
	/**
	 * Trying to restore login. This method can be called before you render the login page.
	 */
	readonly restoreAuthenticationState?: (dispatch: Dispatch) => Promise<void>;
	/**
	 * Validate Oauth2 Token
	 */
	readonly tokenValid?: (token: string) => Promise<boolean>;
}

/**
 * Central Client configuration with LOCAL authentication protocol.
 */
export interface UaaLocalConfiguration extends UaaCommonConfiguration {
	readonly timeout?: number;
	/**
	 * Logout Method
	 */
	readonly logoutMethod?: string;
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
 * The Client which will be used to manage all functionality regarding LOCAL authentication protocol.
 */
export interface UaaLocalClient extends UaaCommonClient {
	/**
	 * Trying to restore login. This method can be called before you render the login page.
	 */
	readonly restoreAuthenticationState: (dispatch: Dispatch) => Promise<void>;
	/**
	 * Login method, you need to call when you want to login.
	 */
	readonly login: (username: string, password: string) => void;
	/**
	 * Validate UAA Token
	 */
	readonly tokenValid?: (token: string) => Promise<boolean>;
}

/**
 * Central Client configuration with LDAP authentication protocol.
 */
export interface UaaLdapConfiguration extends UaaCommonConfiguration {
	readonly timeout?: number;
	/**
	 * Logout Method
	 */
	readonly logoutMethod?: string;
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
 * The Client which will be used to manage all functionality regarding LDAP authentication protocol.
 */
export interface UaaLdapClient extends UaaCommonClient {
	/**
	 * Trying to restore login. This method can be call before you render the login page.
	 */
	readonly restoreAuthenticationState: (dispatch: Dispatch) => Promise<void>;
	/**
	 * Login method, you need to call when you want to login.
	 */
	readonly login: (username: string, password: string) => void;
	/**
	 * Validate UAA Token
	 */
	readonly tokenValid?: (token: string) => Promise<boolean>;
}

/**
 * Central Client configuration with SAML authentication protocol.
 */
export interface UaaSamlConfiguration extends UaaCommonConfiguration {
	readonly timeout?: number;
	/**
	 * Logout Method
	 */
	readonly logoutMethod?: string;
	/**
	 * Login Relative url
	 */
	readonly loginRelativeUrl?: string;
	/**
	 * Logout Relative url
	 */
	readonly logoutRelativeUrl?: string;
	/**
	 * This will trigger logout IDP which means clear the session of the current user in IDP. Default is true.
	 * If you make this one false then there is no request send to IDP to trigger logout. In case you refresh the application
	 * the user is keep login since session from IDP is still kept.
	 */
	readonly logoutIDP?: boolean;
	/**
	 * After saml token exchange request get response from server.
	 * By default, the browser will be redirected to target url by using window.location.href.
	 * For whatever reason you just want to show the target url without trigger browser redirect you can provide the implementation for this function.
	 *
	 */
	showTargetPageAfterTokenExchangeHandler?(url: string): void;
}

/**
 * The Client which will be used to manage all functionality regarding SAML authentication protocol.
 */
export interface UaaSamlClient extends UaaCommonClient {
	/**
	 * Trying to restore login. This method can be called before you render the login page.
	 */
	readonly restoreAuthenticationState: (dispatch: Dispatch) => Promise<void>;
	/**
	 * Login method, you need to call when you want to login.
	 */
	readonly login: (callBackURL?: string) => void;
	/**
	 * Validate UAA Token
	 */
	readonly tokenValid?: (token: string) => Promise<boolean>;
}
