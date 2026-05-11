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
import { Dispatch } from "redux";

import {
	ConnectorLocator,
	RestServerConnector
} from "@com.mgmtp.a12.utils/utils-connector";

import * as UaaActions from "../actions.js";
import * as UaaRequest from "../utils/request.js";
import {
	dispatchAndCheckServerRequest,
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
	UaaLdapClient,
	UaaLdapConfiguration,
	UaaUser
} from "../interfaces/index.js";
import { TokenManagement } from "../tokenManagement.js";

import { LocalClient, withTimeout } from "./local.js";

export class LdapClient extends LocalClient {
	private static instance: LdapClient;

	static createInstance(config: UaaLdapConfiguration): void {
		this.instance = new LdapClient(config);
	}

	static getInstance(): LdapClient {
		return this.instance;
	}

	public config: UaaLdapConfiguration;

	constructor(config: UaaLdapConfiguration) {
		super(config);
		this.config = {
			...config
		};
		this.restoreAuthenticationState = createRestoreAuthenticationStateHandler({
			timeout: this.config.timeout
		});
	}

	public initConnector = (): void => {
		const {
			serverURL,
			serverConnector,
			additionalRequestFilter,
			additionalResponseFilter
		} = this.config;
		const tokenConfigure = TokenManagement.getInstance().getTokenConfiguration(
			AuthenticationType.ACTIVE_DIRECTORY_LDAP
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
			UaaActions.loggingInLDAP({
				username,
				password,
				loginRelativeUrl:
					this.config?.loginRelativeUrl ??
					UaaRequest.RELATIVE_ACTIVE_DIRECTORY_LDAP_LOGIN_URL
			})
		);
	};
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
		if (access_token && type === AuthenticationType.ACTIVE_DIRECTORY_LDAP) {
			LdapClient.getInstance().initConnector();
			dispatch(
				UaaActions.restoreProcessing({
					authenticationType: type
				})
			);
			try {
				if (!(await LdapClient.getInstance().tokenValid?.(access_token))) {
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
export function ldapClientSetup(config: UaaLdapConfiguration): UaaLdapClient {
	LdapClient.createInstance(config);
	return LdapClient.getInstance();
}
