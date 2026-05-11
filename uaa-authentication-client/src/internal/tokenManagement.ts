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
import { LoggerFactory } from "@com.mgmtp.a12.utils/utils-logging";
import { RestRequestPayload } from "@com.mgmtp.a12.utils/utils-connector";

import * as UaaSelectors from "./selectors.js";
import * as UaaActions from "./actions.js";
import * as UaaRequest from "./utils/request.js";
import { UAAServiceWorker } from "./serviceWorkerRegistration.js";
import {
	CodeExchange,
	dispatchAndCheckServerRequest,
	fetchServerRequest,
	generateCodeExchange,
	reduxStore,
	sessionStorage
} from "./utils/index.js";
import { isNotAuthorizedError } from "./filters/index.js";
import {
	AuthenticationType,
	isUaaUser,
	SessionStorageKeys,
	TokenConfiguration,
	UaaExtendedUser,
	UaaUser
} from "./interfaces/index.js";
import { OidcClient, uaaClient } from "./factories/index.js";

const logger = LoggerFactory.getLogger("UAA/AutoRenewToken");
const MAX_RETRY_TIME = 3;
const RETRY_INTERVAL_IN_SECOND = 5;

const DEFAULT_OIDC_TOKEN_SELF_CONFIGURE: TokenConfiguration = {
	authorizationHeaderName: "Authorization",
	tokenType: "Bearer",
	allowCredentials: false
};
const DEFAULT_UAA_TOKEN_SELF_CONFIGURE: TokenConfiguration = {
	authorizationHeaderName: "Authorization",
	generatedTokenExpirationHeaderName:
		SessionStorageKeys.ACCESS_TOKEN_EXPIRATION,
	generatedTokenHeaderName: SessionStorageKeys.ACCESS_TOKEN,
	tokenType: "UAABearer",
	allowCredentials: false
};

export class TokenManagement {
	private static instance: TokenManagement;

	static createInstance(): void {
		this.instance = new TokenManagement();
	}

	static getInstance(): TokenManagement {
		if (!this.instance) {
			this.instance = new TokenManagement();
		}
		return this.instance;
	}

	private code: CodeExchange = {
		state: "",
		code_c: "",
		code_v: ""
	};
	private access_token: string | undefined = "";

	private tokenRenewalTask: unknown;
	private retryTime = 0;
	private isRenewRunning = false;

	private _tokenConfigurations: TokenConfiguration[] | undefined;

	public get tokenConfigurations(): TokenConfiguration[] | undefined {
		return this._tokenConfigurations;
	}

	/**
	 * @internal
	 */
	public set tokenConfigurations(value: TokenConfiguration[] | undefined) {
		this._tokenConfigurations = value;
	}

	private prepareTasks = async (): Promise<void> => {
		this.code = await generateCodeExchange();
		this.isRenewRunning = true;
	};

	private finishTasks = (): void => {
		this.isRenewRunning = false;
		this.addSilentRenewTask();
	};

	private requestAuthorize = (): Promise<Response> | undefined => {
		if (!this.access_token) {
			return undefined;
		}
		const params = {
			state: this.code.state,
			code_c: this.code.code_c,
			access_token: this.access_token
		};
		const authorizeRequest: RestRequestPayload =
			UaaRequest.buildAuthorizeRequest(params);
		return fetchServerRequest(authorizeRequest);
	};

	private requestNewToken = (code: string): Promise<Response> => {
		const params = {
			code,
			code_v: this.code.code_v
		};
		const tokenRequest: RestRequestPayload =
			UaaRequest.buildTokenRequest(params);
		return fetchServerRequest(tokenRequest);
	};

	private requestCurrentUser = (): Promise<UaaUser | UaaExtendedUser> => {
		const getUserDataRequest = UaaRequest.buildGetUserRequest();
		return dispatchAndCheckServerRequest<UaaUser | UaaExtendedUser>(
			getUserDataRequest,
			isUaaUser
		);
	};

	/**
	 *
	 * @internal
	 */
	startSilentRenew = async () => {
		const tokenFromSessionStorage = sessionStorage.getItem(
			SessionStorageKeys.ACCESS_TOKEN
		);
		const tokenFromStore = UaaSelectors.accessToken(
			reduxStore.getStore()?.getState() as UaaSelectors.UaaSlice
		);
		const typeFromSessionStorage = sessionStorage.getItem(
			SessionStorageKeys.AUTHENTICATION_TYPE
		);
		const typeFromStore =
			AuthenticationType[
				UaaSelectors.authenticationType(
					reduxStore.getStore()?.getState() as UaaSelectors.UaaSlice
				) as AuthenticationType
			];
		const type = typeFromStore || typeFromSessionStorage || undefined;
		this.access_token = tokenFromStore || tokenFromSessionStorage || undefined;
		if (!this.access_token) {
			return;
		}
		await this.prepareTasks();
		await this.requestAuthorize()
			?.then(async response => {
				if (response.status !== 200) {
					return false;
				}

				const { code, state } = await response.json();
				if (this.code.state !== state || code === undefined) {
					return false;
				}
				return this.requestNewToken(code).then(async res => {
					if (res.status === 200) {
						const {
							access_token,
							access_token_expiration,
							token_renew_in_seconds
						} = await res.json();
						if (
							access_token &&
							access_token_expiration &&
							token_renew_in_seconds
						) {
							sessionStorage.setItem(
								SessionStorageKeys.ACCESS_TOKEN,
								access_token
							);
							UAAServiceWorker.postToken(access_token, type);
							sessionStorage.setItem(
								SessionStorageKeys.ACCESS_TOKEN_EXPIRATION,
								access_token_expiration
							);
							sessionStorage.setItem(
								SessionStorageKeys.TOKEN_RENEW_IN_SECONDS,
								token_renew_in_seconds
							);
							const token_renew_timestamp =
								Date.now() + Number(token_renew_in_seconds) * 1000;
							sessionStorage.setItem(
								SessionStorageKeys.TOKEN_RENEW_TIMESTAMP,
								token_renew_timestamp.toString()
							);
							reduxStore.dispatch(
								UaaActions.updateAccessToken({ access_token })
							);
						}
						return true;
					}
					return false;
				});
			})
			.then(isSuccess => {
				if (isSuccess) {
					logger.info("Request for new token was successful.");
					this.retryTime = 0;
				} else {
					return new Error();
				}
				this.finishTasks();
				return isSuccess;
			})
			.then(async () => {
				const currentUserResponse = await this.requestCurrentUser();
				reduxStore.dispatch(UaaActions.updateUserInfo(currentUserResponse));
			})
			.catch(err => {
				if (isNotAuthorizedError(err)) {
					reduxStore.dispatch(UaaActions.loginRequire());
					if (
						this.access_token ===
						sessionStorage.getItem(SessionStorageKeys.ACCESS_TOKEN)
					) {
						sessionStorage.removeItem(SessionStorageKeys.ACCESS_TOKEN);
						sessionStorage.removeItem(
							SessionStorageKeys.ACCESS_TOKEN_EXPIRATION
						);
						sessionStorage.removeItem(
							SessionStorageKeys.TOKEN_RENEW_IN_SECONDS
						);
						sessionStorage.removeItem(SessionStorageKeys.TOKEN_RENEW_TIMESTAMP);
					}
				}

				logger.error("Request Authorize was failed", err);

				if (this.retryTime === MAX_RETRY_TIME) {
					reduxStore.dispatch(UaaActions.silentRenewError());
					return;
				}
				this.retryTime++;
				this.finishTasks();
			});
	};

	private addSilentRenewTask = (): boolean => {
		if (this.isRenewRunning) {
			return false;
		}
		const refreshTime = Number(
			sessionStorage.getItem(SessionStorageKeys.TOKEN_RENEW_TIMESTAMP)
		);
		if (refreshTime) {
			const waitingTime =
				this.retryTime > 0
					? RETRY_INTERVAL_IN_SECOND * 1000
					: refreshTime - Date.now();
			if (waitingTime > 0) {
				this.tokenRenewalTask = setTimeout(() => {
					this.startSilentRenew();
				}, waitingTime);
				logger.info(`requestNewToken will call in ${waitingTime}ms`);
				return true;
			}
			this.startSilentRenew();
			return true;
		}
		return false;
	};

	private oidcSilentRenewTask = (phase: "start" | "stop"): void => {
		if (!OidcClient?.userManager?.uaaInternalUserManager || !reduxStore) {
			return;
		}
		if (phase === "stop") {
			OidcClient.userManager.uaaInternalUserManager.stopSilentRenew();
			this.isRenewRunning = false;
			return;
		}
		const typeFromSessionStorage = sessionStorage.getItem(
			SessionStorageKeys.AUTHENTICATION_TYPE
		);
		const typeFromStore =
			AuthenticationType[
				UaaSelectors.authenticationType(
					reduxStore.getStore()?.getState() as UaaSelectors.UaaSlice
				) as AuthenticationType
			];
		const type = typeFromStore || typeFromSessionStorage || undefined;
		if (type === AuthenticationType.OAUTH2 && phase === "start") {
			OidcClient.userManager.uaaInternalUserManager.startSilentRenew();
			this.isRenewRunning = true;
			return;
		}
		return;
	};

	startService = (): void => {
		this.retryTime = 0;
		this.stopService();
		this.oidcSilentRenewTask("start");
		this.addSilentRenewTask();
	};

	stopService = (): void => {
		this.oidcSilentRenewTask("stop");
		clearTimeout(this.tokenRenewalTask as ReturnType<typeof setTimeout>);
	};

	getTokenConfiguration = (
		type: AuthenticationType
	): TokenConfiguration | undefined => {
		if (this.tokenConfigurations) {
			const config = this.tokenConfigurations.find(
				config =>
					config.tokenType === uaaClient.getClientConfiguration(type)?.tokenType
			);
			if (!config) {
				logger.error(
					`The tokenType ${
						uaaClient.getClientConfiguration(type)?.tokenType
					} is invalid`
				);
			} else {
				return config;
			}
		}
		if (type === AuthenticationType.OAUTH2) {
			return DEFAULT_OIDC_TOKEN_SELF_CONFIGURE;
		}
		return DEFAULT_UAA_TOKEN_SELF_CONFIGURE;
	};
}
