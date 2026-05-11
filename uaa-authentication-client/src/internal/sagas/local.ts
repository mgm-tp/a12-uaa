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
import { SagaIterator } from "redux-saga";
import { Action, AnyAction } from "typescript-fsa";
import { call, put } from "typed-redux-saga";

import * as UaaActions from "../actions.js";
import * as UaaRequest from "../utils/request.js";
import { AUTH_KEYS } from "../locale/index.js";
import { fetchServerRequest } from "../utils/index.js";
import {
	AuthenticationType,
	isUaaUser,
	UaaExtendedUser,
	UaaSagaDescriptor,
	UaaUser
} from "../interfaces/index.js";
import { UserLoginFailedPayload } from "../actions.js";

/**
 * Handles user authentication for both local and LDAP login
 * @param action - The login action containing user credentials
 */
function* userLoggingInSaga(
	action: Action<UaaActions.UserLoggingInPayload>
): SagaIterator<void> {
	const { username, password, loginRelativeUrl } = action.payload;
	const type =
		action.type === UaaActions.loggingInLocal.type
			? AuthenticationType.LOCAL
			: AuthenticationType.ACTIVE_DIRECTORY_LDAP;

	try {
		const loginRequest = UaaRequest.buildLoginRequest(
			{ username, password },
			type,
			loginRelativeUrl
		);

		const response: Response = yield* call(fetchServerRequest, loginRequest);
		const data = yield* call(() => response.json());
		const user: UaaUser | UaaExtendedUser = validateUserResponse(data);
		yield* put(UaaActions.loggedIn({ user, type: type }));
		handlePostLoginRedirect(response);
	} catch (error) {
		const errorPayload = createErrorPayload(error);
		yield* put(UaaActions.loginFailed(errorPayload));
	}
}

function validateUserResponse(data: unknown): UaaUser | UaaExtendedUser {
	if (!isUaaUser(data)) {
		throw new Error("The server response cannot be interpreted!");
	}
	return data;
}

/**
 * Handles post-login redirect if a location header is present
 */
function handlePostLoginRedirect(response: Response): void {
	const redirectUrl = response.headers.get("Location");
	if (redirectUrl) {
		window.location.href = redirectUrl;
	}
}

/**
 * Creates an error payload from the caught error
 */
function createErrorPayload(error: unknown): UserLoginFailedPayload {
	const errorCode = AUTH_KEYS.auth.error.authenticationfailed;

	if (error instanceof Response) {
		return {
			errorCode,
			status: error.status,
			statusText: error.statusText
		};
	}

	if (error instanceof Error) {
		return {
			errorCode,
			statusText: error.message
		};
	}

	return {
		errorCode,
		statusText: "An unexpected error occurred"
	};
}

/**
 *
 */
const localClientSaga: UaaSagaDescriptor[] = [
	{
		canHandle: (action: AnyAction) =>
			UaaActions.loggingInLocal.match(action) ||
			UaaActions.loggingInLDAP.match(action),
		handle: (action: Action<UaaActions.UserLoggingInPayload>) =>
			userLoggingInSaga(action)
	}
];

export default localClientSaga;
