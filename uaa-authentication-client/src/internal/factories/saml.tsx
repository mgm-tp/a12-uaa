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
	RestRequestPayload,
	RestServerConnector
} from "@com.mgmtp.a12.utils/utils-connector";
import { LoggerFactory } from "@com.mgmtp.a12.utils/utils-logging/lib/factory.js";

import * as UaaActions from "../actions.js";
import * as UaaRequest from "../utils/request.js";
import {
	fetchServerRequest,
	generateCodeExchange,
	reduxStore,
	sessionStorage
} from "../utils/index.js";
import {
	AuthorizationHeaderFilter,
	ResponseFilter401,
	TokenResponseFilter
} from "../filters/index.js";
import { CredentialsFilter } from "../filters/CredentialsFilter.js";
import {
	AuthenticationType,
	SessionStorageKeys,
	UaaSamlClient,
	UaaSamlConfiguration
} from "../interfaces/index.js";
import { TokenManagement } from "../tokenManagement.js";
import { createUaaSamlMiddlewares } from "../middlewares.js";
import { AuthenticationReducer } from "../reducers.js";

import { uaaClient } from "./uaaClient.js";

export class SamlClient implements UaaSamlClient {
	private static _instance: UaaSamlClient;

	static createInstance(config: UaaSamlConfiguration): void {
		this._instance = new SamlClient(config);
	}

	static getInstance(): UaaSamlClient {
		return this._instance;
	}

	public config: UaaSamlConfiguration;

	constructor(config: UaaSamlConfiguration) {
		this.config = {
			...config,
			logoutIDP: config.logoutIDP ?? true
		};
		this.restoreAuthenticationState = createRestoreAuthenticationStateHandler();
	}

	public reducers: Reducer = AuthenticationReducer;

	public middlewares = createUaaSamlMiddlewares();

	public restoreAuthenticationState: (dispatch: Dispatch) => Promise<void>;

	public initConnector = (): void => {
		const {
			serverURL,
			serverConnector,
			additionalRequestFilter,
			additionalResponseFilter
		} = this.config;
		const tokenConfigure = TokenManagement.getInstance().getTokenConfiguration(
			AuthenticationType.SAML
		);
		const requestFilters = [
			new AuthorizationHeaderFilter(
				() => reduxStore.getStore()?.getState(),
				tokenConfigure
			),
			new CredentialsFilter(tokenConfigure),
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

	public login = (callBackURL = ""): void => {
		this.initConnector();
		if (callBackURL === "") {
			sessionStorage.removeItem("callBackURL");
		} else {
			sessionStorage.setItem("callBackURL", callBackURL);
		}
		reduxStore.dispatch(UaaActions.loggingInSAML());
	};

	public tokenValid = async (token: string): Promise<boolean> => {
		this.initConnector();
		const tokenValidRequest = UaaRequest.buildTokenValidRequest(token);
		const response = await fetchServerRequest(tokenValidRequest);
		return response.json();
	};

	/**
	 * @internal
	 */
	public static exchangeAuthorizationCodeToToken = (): void => {
		generateCodeExchange().then(codeExchange => {
			SamlClient.getInstance().initConnector();
			const exchangeAuthCodeAuthorizeRequest: RestRequestPayload =
				UaaRequest.buildExchangeCodeRequestAuthorize(codeExchange);
			fetchServerRequest(exchangeAuthCodeAuthorizeRequest)
				.then(response => {
					if (response.status === 200) {
						return response.json();
					}
					return Promise.reject(new Error("Code exchange failed!"));
				})
				.then(responseData => {
					const { state } = responseData;
					if (codeExchange.state !== state) {
						this.samlErrorExchangeCodeHandler(new Error("state is different!"));
						return;
					}

					const exchangeAuthCodeRequest: RestRequestPayload =
						UaaRequest.buildExchangeCodeRequest(codeExchange.code_v);
					fetchServerRequest(exchangeAuthCodeRequest)
						.then(response => {
							if (response.status === 200) {
								if (!response.redirected) {
									sessionStorage.setItem(
										SessionStorageKeys.AUTHENTICATION_TYPE,
										AuthenticationType.SAML
									);
									sessionStorage.setItem("restoreAfterLogin", "true");
									const callBackURL = sessionStorage.getItem("callBackURL");
									localStorage.removeItem("authorizationCode");
									if (callBackURL) {
										samlShowTargetPageAfterTokenExchange(callBackURL);
									} else {
										samlShowTargetPageAfterTokenExchange(
											window.location.origin + window.location.pathname
										);
									}
								} else {
									// Server redirect to failure url when can not exchange token
									samlShowTargetPageAfterTokenExchange(response.url);
								}
							}
						})
						.catch(error => {
							this.samlErrorExchangeCodeHandler(error);
						});
				})
				.catch(error => {
					this.samlErrorExchangeCodeHandler(error);
				});
		});
	};

	private static samlErrorExchangeCodeHandler = (error: Error) => {
		LoggerFactory.getLogger("UAA/exchangeCode").error(
			"Exchange Code Request has error",
			error
		);
		// un-know error then redirect to current client origin.
		samlShowTargetPageAfterTokenExchange(window.location.origin);
	};
}

function samlShowTargetPageAfterTokenExchange(url: string) {
	const configure = uaaClient.getSamlConfiguration();
	if (configure.showTargetPageAfterTokenExchangeHandler) {
		configure.showTargetPageAfterTokenExchangeHandler(url);
	} else {
		window.location.href = url;
	}
}

/**
 * @param root0
 * @param root0.timeout
 */
function createRestoreAuthenticationStateHandler(): (
	dispatch: Dispatch
) => Promise<void> {
	return async function restoreAuthenticationState(dispatch) {
		const access_token = sessionStorage.getItem(
			SessionStorageKeys.ACCESS_TOKEN
		);
		const type = sessionStorage.getItem(SessionStorageKeys.AUTHENTICATION_TYPE);
		if (access_token && type === AuthenticationType.SAML) {
			SamlClient.getInstance().initConnector();
			dispatch(
				UaaActions.restoreProcessing({
					authenticationType: type
				})
			);
			try {
				if (!(await SamlClient.getInstance().tokenValid?.(access_token))) {
					throw new Error("Token is not valid");
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
export function samlClientSetup(config: UaaSamlConfiguration): UaaSamlClient {
	SamlClient.createInstance(config);
	return SamlClient.getInstance();
}
