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
import { Middleware } from "redux";

import { LoggerFactory } from "@com.mgmtp.a12.utils/utils-logging/lib/factory.js";

import * as UaaActions from "./actions.js";
import { UAAServiceWorker } from "./serviceWorkerRegistration.js";
import { OidcClient } from "./factories/index.js";
import {
	AuthenticationType,
	isAuthenticationFailedPayload,
	SessionStorageKeys,
	UaaSagaDescriptor
} from "./interfaces/index.js";
import { uaaSaga } from "./sagas/uaaSaga.js";
import { TokenManagement } from "./tokenManagement.js";
import { reduxStore, sessionStorage } from "./utils/index.js";

/** @internal */
export function onUaaOidcMiddleware(): Middleware {
	return store => next => action => {
		const result = next(action);
		const {
			uaa: { authenticationType, user }
		} = store.getState();
		switch (action.type) {
			case UaaActions.loggingInOIDC.type:
				OidcClient.userManager.signIn(action.payload);
				break;
			case UaaActions.oidc_silentRenewError.type:
				OidcClient.userManager.signOut();
				break;
			case UaaActions.loggingOut.type:
				if (authenticationType === AuthenticationType.OAUTH2) {
					OidcClient.userManager.signOut();
				}
				break;
			case UaaActions.oidc_userFound.type:
				//Store token after silent renew success.
				reduxStore.dispatch(
					UaaActions.updateAccessToken({
						access_token: user.access_token,
						authenticationType
					})
				);
				break;
			default:
				return result;
		}

		return result;
	};
}

/** @internal */
export function onUaaCommonMiddleware(): Middleware {
	return store => next => action => {
		const result = next(action);
		if (!uaaSaga.task) {
			uaaSaga.init();
		}
		switch (action.type) {
			case UaaActions.loggedOut.type:
				sessionStorage.removeItem(SessionStorageKeys.ACCESS_TOKEN);
				sessionStorage.removeItem(SessionStorageKeys.ACCESS_TOKEN_EXPIRATION);
				sessionStorage.removeItem(SessionStorageKeys.TOKEN_RENEW_IN_SECONDS);
				sessionStorage.removeItem(SessionStorageKeys.TOKEN_RENEW_TIMESTAMP);
				sessionStorage.removeItem(SessionStorageKeys.AUTHENTICATION_TYPE);
				sessionStorage.removeItem(SessionStorageKeys.SELF_CONFIGURE);
				TokenManagement.getInstance().stopService();
				break;
			case UaaActions.loggedIn.type:
				if (action.payload) {
					const { type, user } = action.payload;
					sessionStorage.setItem(SessionStorageKeys.AUTHENTICATION_TYPE, type);
					const access_token =
						action.payload?.access_token || user?.access_token;
					UAAServiceWorker.postToken(access_token, type);
					if (access_token) {
						sessionStorage.setItem(
							SessionStorageKeys.ACCESS_TOKEN,
							access_token
						);
					}
					if (type !== AuthenticationType.OAUTH2) {
						TokenManagement.getInstance().startService();
					} else {
						store.dispatch(UaaActions.modifyingOidcUser(user));
					}
				}
				break;
			case UaaActions.updateAccessToken.type:
				if (action.payload) {
					const { access_token, authenticationType } = action.payload;
					sessionStorage.setItem(SessionStorageKeys.ACCESS_TOKEN, access_token);
					UAAServiceWorker.postToken(access_token, authenticationType);
					if (authenticationType) {
						sessionStorage.setItem(
							SessionStorageKeys.AUTHENTICATION_TYPE,
							authenticationType
						);
					}
				}
				break;
			case UaaActions.restoreFailed.type:
				if (action.payload) {
					const error = action.payload.error;
					if (error === undefined) {
						LoggerFactory.getLogger("UAA/Restore").error(
							"Timeout during restoration of the authentication state."
						);
					} else if (
						!isAuthenticationFailedPayload(error) ||
						error.status !== 403
					) {
						LoggerFactory.getLogger("UAA/Restore").error(
							"Error on restoring the authentication state",
							error
						);
					} else {
						LoggerFactory.getLogger("UAA/Restore").error(
							"Authentication state cannot be restored.",
							error
						);
					}
				}
				break;
			default:
				return result;
		}
		return result;
	};
}

/**
 *
 * @param overrideSagas
 */
export function UaaMiddlewares(
	overrideSagas?: UaaSagaDescriptor[]
): Middleware[] {
	uaaSaga.overrideSagas = overrideSagas ?? [];
	return [onUaaOidcMiddleware(), onUaaCommonMiddleware(), uaaSaga.middleware];
}

/**
 *
 */
export function createUaaOidcMiddlewares(): Middleware[] {
	return [onUaaOidcMiddleware(), onUaaCommonMiddleware(), uaaSaga.middleware];
}

/**
 *
 */
export function createUaaLocalMiddlewares(): Middleware[] {
	return [onUaaCommonMiddleware(), uaaSaga.middleware];
}

/**
 *
 */
export function createUaaSamlMiddlewares(): Middleware[] {
	return [onUaaCommonMiddleware(), uaaSaga.middleware];
}
