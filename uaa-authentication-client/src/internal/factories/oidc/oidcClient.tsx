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
import { Dispatch, Reducer } from "redux";
import {
	SigninPopupArgs,
	SigninRedirectArgs,
	SignoutResponse,
	User as OidcUser
} from "oidc-client-ts";

import {
	ConnectorLocator,
	RestServerConnector
} from "@com.mgmtp.a12.utils/utils-connector";

import { createUaaOidcMiddlewares } from "../../middlewares.js";
import { AuthenticationReducer } from "../../reducers.js";
import {
	fetchServerRequest,
	reduxStore,
	sessionStorage
} from "../../utils/index.js";
import {
	AuthenticationType,
	SessionStorageKeys,
	UaaOidcClient,
	UaaOidcConfiguration,
	UaaOidcUser,
	UaaSignoutResponse
} from "../../interfaces/index.js";
import * as UaaActions from "../../actions.js";
import {
	AuthorizationHeaderFilter,
	ResponseFilter401
} from "../../filters/index.js";
import { TokenManagement } from "../../tokenManagement.js";
import * as UaaRequest from "../../utils/request.js";

import { createOidcUserManager, OidcUserManager } from "./oidcUserManager.js";

export class OidcClient implements UaaOidcClient {
	static userManager: OidcUserManager;
	private static _instance: UaaOidcClient;

	static createInstance(config: UaaOidcConfiguration): void {
		this._instance = new OidcClient(config);
	}

	static getInstance(): UaaOidcClient {
		return this._instance;
	}

	public config: UaaOidcConfiguration;

	constructor(config: UaaOidcConfiguration) {
		this.config = config;
		OidcClient.userManager = createOidcUserManager(config);
		OidcClient.userManager.registerEvent();
	}

	public initConnector = (): void => {
		const {
			serverURL,
			serverConnector,
			additionalRequestFilter,
			additionalResponseFilter
		} = this.config;
		const tokenConfigure = TokenManagement.getInstance().getTokenConfiguration(
			AuthenticationType.OAUTH2
		);
		const requestFilters = [
			new AuthorizationHeaderFilter(
				() => reduxStore.getStore()?.getState(),
				tokenConfigure
			),
			...(additionalRequestFilter ?? [])
		];
		const responseFilters = [
			new ResponseFilter401(),
			...(additionalResponseFilter ?? [])
		];
		const defaultServerConnector =
			serverConnector ??
			new RestServerConnector(serverURL ?? "", requestFilters, responseFilters);
		ConnectorLocator.createInstance(defaultServerConnector);
	};

	public reducers: Reducer = AuthenticationReducer;
	public middlewares = createUaaOidcMiddlewares();

	public login = (signinArgs?: SigninRedirectArgs | SigninPopupArgs): void => {
		this.initConnector();
		reduxStore.dispatch(UaaActions.loggingInOIDC(signinArgs));
	};

	public logout = (): void => {
		this.initConnector();
		reduxStore.dispatch(UaaActions.loggingOut());
	};

	public processLoginCallbackPopup = (): Promise<void> =>
		OidcClient.userManager.uaaInternalUserManager.signinPopupCallback();

	public processLogoutCallbackPopup = (): Promise<void> =>
		OidcClient.userManager.uaaInternalUserManager.signoutPopupCallback();

	public processLoginCallback = (): Promise<UaaOidcUser> => {
		const successCallback = (user: OidcUser): UaaOidcUser => {
			const uaaOidcUser = user as UaaOidcUser;
			uaaOidcUser.currentUserUrl = this.config.currentUserUrl;
			reduxStore.dispatch(
				UaaActions.loggedIn({
					user: uaaOidcUser,
					type: AuthenticationType.OAUTH2
				})
			);
			return uaaOidcUser;
		};
		return OidcClient.userManager.uaaInternalUserManager
			.signinRedirectCallback()
			.then(successCallback);
	};

	public processLogoutCallback = (): Promise<UaaSignoutResponse> => {
		const successCallback = (response: SignoutResponse): UaaSignoutResponse => {
			reduxStore.dispatch(UaaActions.loggedOut());
			return response;
		};
		return OidcClient.userManager.uaaInternalUserManager
			.signoutRedirectCallback()
			.then(successCallback);
	};

	public tokenValid = async (token: string): Promise<boolean> => {
		const tokenValidRequest = UaaRequest.buildOauth2TokenValidRequest(token);
		const response = await fetchServerRequest(tokenValidRequest);
		return response.json();
	};

	public restoreAuthenticationState = async (
		dispatch: Dispatch
	): Promise<void> => {
		const access_token = sessionStorage.getItem(
			SessionStorageKeys.ACCESS_TOKEN
		);
		const type = sessionStorage.getItem(SessionStorageKeys.AUTHENTICATION_TYPE);
		if (access_token && type === AuthenticationType.OAUTH2) {
			OidcClient.getInstance().initConnector();
			dispatch(
				UaaActions.restoreProcessing({
					authenticationType: type
				})
			);
			try {
				if (!(await this.tokenValid?.(access_token))) {
					throw new Error("Unauthorized");
				}
				dispatch(
					UaaActions.updateAccessToken({
						access_token: access_token,
						authenticationType: type
					})
				);
				const user =
					((await OidcClient.userManager.uaaInternalUserManager.getUser()) ||
						sessionStorage.getItem(
							`oidc.user:${this.config.authority}:${this.config.client_id}`
						)) as OidcUser;
				if (user) {
					const uaaOidcUser = user as UaaOidcUser;
					uaaOidcUser.currentUserUrl = this.config.currentUserUrl;
					dispatch(
						UaaActions.restoreSuccess({
							authenticationType: type
						})
					);
					dispatch(
						UaaActions.loggedIn({
							user: uaaOidcUser,
							type
						})
					);
				} else {
					throw new Error("Restore failed");
				}
			} catch (error) {
				dispatch(
					UaaActions.restoreFailed({
						authenticationType: type,
						error: error as Error
					})
				);
				reduxStore.dispatch(UaaActions.loggedOut());
			}
		}
	};
}

/**
 * @param config
 */
export function oidcClientSetup(config: UaaOidcConfiguration): UaaOidcClient {
	OidcClient.createInstance(config);
	return OidcClient.getInstance();
}
