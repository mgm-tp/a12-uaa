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
	ConnectorLocator,
	RestServerConnector
} from "@com.mgmtp.a12.utils/utils-connector";

import * as UaaActions from "../actions.js";
import * as UaaRequest from "../utils/request.js";
import {
	dispatchAndCheckServerRequest,
	fetchServerRequest,
	reduxStore,
	sessionStorage
} from "../utils/index.js";
import {
	AuthorizationHeaderFilter,
	ResponseFilter401,
	TokenResponseFilter
} from "../filters/index.js";
import {
	AuthenticationType,
	isUaaUser,
	SessionStorageKeys,
	UaaExtendedUser,
	UaaLocalClient,
	UaaLocalConfiguration,
	UaaUser
} from "../interfaces/index.js";
import { TokenManagement } from "../tokenManagement.js";
import { createUaaLocalMiddlewares } from "../middlewares.js";
import { AuthenticationReducer } from "../reducers.js";

export class LocalClient implements UaaLocalClient {
	private static _instance: UaaLocalClient;

	static createInstance(config: UaaLocalConfiguration): void {
		this._instance = new LocalClient(config);
	}

	static getInstance(): UaaLocalClient {
		return this._instance;
	}

	public config: UaaLocalConfiguration;

	constructor(config: UaaLocalConfiguration) {
		this.config = {
			...config
		};
		this.restoreAuthenticationState = createRestoreAuthenticationStateHandler({
			timeout: this.config.timeout
		});
	}

	public reducers: Reducer = AuthenticationReducer;

	public middlewares = createUaaLocalMiddlewares();

	public restoreAuthenticationState: (dispatch: Dispatch) => Promise<void>;

	public initConnector = (): void => {
		const {
			serverURL,
			serverConnector,
			additionalRequestFilter,
			additionalResponseFilter
		} = this.config;
		const tokenConfigure = TokenManagement.getInstance().getTokenConfiguration(
			AuthenticationType.LOCAL
		);
		const requestFilters = [
			new AuthorizationHeaderFilter(
				() => reduxStore.getStore()?.getState(),
				tokenConfigure
			),
			...(additionalRequestFilter ?? [])
		];
		const responseFilters = [
			new TokenResponseFilter(tokenConfigure),
			new ResponseFilter401(),
			...(additionalResponseFilter ?? [])
		];
		const defaultServerConnector =
			serverConnector ??
			new RestServerConnector(serverURL ?? "", requestFilters, responseFilters);
		ConnectorLocator.createInstance(defaultServerConnector);
	};

	public logout = (): void => {
		this.initConnector();
		reduxStore.dispatch(UaaActions.loggingOut());
	};

	public login = (username: string, password: string): void => {
		this.initConnector();
		reduxStore.dispatch(
			UaaActions.loggingInLocal({
				username,
				password,
				loginRelativeUrl:
					this.config?.loginRelativeUrl ?? UaaRequest.RELATIVE_LOCAL_LOGIN_URL
			})
		);
	};

	public tokenValid = async (token: string): Promise<boolean> => {
		this.initConnector();
		const tokenValidRequest = UaaRequest.buildTokenValidRequest(token);
		const response = await fetchServerRequest(tokenValidRequest);
		return response.json();
	};
}

/**
 * @param promise
 * @param timeout
 */
export function withTimeout<T>(
	promise: Promise<T>,
	timeout: number
): Promise<T> {
	return Promise.race([
		promise,
		new Promise<never>((_, reject) => {
			setTimeout(reject, timeout);
		})
	]);
}

/**
 * @param root0
 * @param root0.timeout
 */
function createRestoreAuthenticationStateHandler({
	timeout = 3000
}: {
	readonly timeout?: number;
}): (dispatch: Dispatch) => Promise<void> {
	/**
	 *
	 */
	function getUser(): Promise<UaaUser | UaaExtendedUser> {
		const getUserDataRequest = UaaRequest.buildGetUserRequest();
		return dispatchAndCheckServerRequest<UaaUser | UaaExtendedUser>(
			getUserDataRequest,
			isUaaUser
		);
	}
	return async function restoreAuthenticationState(dispatch) {
		const access_token = sessionStorage.getItem(
			SessionStorageKeys.ACCESS_TOKEN
		);
		const type = sessionStorage.getItem(SessionStorageKeys.AUTHENTICATION_TYPE);
		if (access_token && type === AuthenticationType.LOCAL) {
			LocalClient.getInstance().initConnector();
			dispatch(
				UaaActions.restoreProcessing({
					authenticationType: type
				})
			);
			try {
				if (!(await LocalClient.getInstance().tokenValid?.(access_token))) {
					throw new Error("Unauthorized");
				}
				dispatch(
					UaaActions.updateAccessToken({
						access_token: access_token,
						authenticationType: type
					})
				);
				dispatch(
					UaaActions.restoreSuccess({
						authenticationType: type
					})
				);
				const user = await withTimeout(getUser(), timeout);
				dispatch(
					UaaActions.loggedIn({
						user,
						type
					})
				);
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
export function localClientSetup(
	config: UaaLocalConfiguration
): UaaLocalClient {
	LocalClient.createInstance(config);
	return LocalClient.getInstance();
}
