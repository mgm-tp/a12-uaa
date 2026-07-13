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
import { call, put, SagaGenerator } from "typed-redux-saga";
import type { Action as ReduxAction } from "redux";

import type { Action } from "@com.mgmtp.a12.client/typescript-fsa-redux-5-compat";

import { UaaClient } from "../../index.js";

import * as UaaActions from "../actions.js";
import {
	AuthenticationType,
	isUaaUser,
	UaaExtendedUser,
	UaaSagaDescriptor,
	UaaUser
} from "../interfaces/index.js";
import * as UaaRequest from "../utils/request.js";
import { dispatchAndCheckServerRequest } from "../utils/index.js";
import { AUTH_KEYS } from "../locale/index.js";
import { uaaClient } from "../factories/index.js";

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

/**
 * @param action
 */
function* restoreSuccessSaga(
	action: Action<UaaActions.RestorePayload>
): SagaGenerator<void> {
	if (action.payload.authenticationType === AuthenticationType.SAML) {
		try {
			const user: UaaUser | UaaExtendedUser = yield* call(getUser);
			yield* put(UaaActions.loggedIn({ user, type: AuthenticationType.SAML }));
		} catch (error) {
			const { status, statusText } = error as Response;
			const errorCode = AUTH_KEYS.auth.error.authenticationfailed;
			yield* put(UaaActions.loginFailed({ errorCode, status, statusText }));
		}
	}
}

/**
 * @param action
 */
function* userLoggingInSaga(): SagaGenerator<void> {
	try {
		const { loginRelativeUrl } = uaaClient.getSamlConfiguration();
		const baseUrl =
			UaaClient.getClientConfiguration(AuthenticationType.SAML)?.serverURL ||
			"";
		let loginUrl = `${baseUrl}${loginRelativeUrl}`;
		if (baseUrl && baseUrl.slice(-1) !== "/") {
			loginUrl = `${baseUrl}/${loginRelativeUrl}`;
		}
		if (loginRelativeUrl) {
			window.location.href = loginUrl;
		} else {
			const user: UaaUser | UaaExtendedUser = yield* call(getUser);
			yield* put(UaaActions.loggedIn({ user, type: AuthenticationType.SAML }));
		}
	} catch (error) {
		const { status, statusText } = error as Response;
		const errorCode = AUTH_KEYS.auth.error.authenticationfailed;
		yield* put(UaaActions.loginFailed({ errorCode, status, statusText }));
	}
}

/**
 *
 */

const samlClientSaga: UaaSagaDescriptor[] = [
	{
		canHandle: (action: ReduxAction) => UaaActions.loggingInSAML.match(action),
		handle: () => userLoggingInSaga()
	},
	{
		canHandle: (action: ReduxAction) => UaaActions.restoreSuccess.match(action),
		handle: (action: Action<UaaActions.RestorePayload>) =>
			restoreSuccessSaga(action)
	}
];
export default samlClientSaga;
