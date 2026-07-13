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
import { LoggerFactory } from "@com.mgmtp.a12.utils/utils-logging";

import * as UaaActions from "../actions.js";
import {
	isUaaUser,
	Role,
	UaaOidcModifiedUser,
	UaaOidcUser,
	UaaSagaDescriptor,
	UaaUser
} from "../interfaces/index.js";
import * as UaaRequest from "../utils/request.js";
import { dispatchAndCheckServerRequest } from "../utils/index.js";

const logger = LoggerFactory.getLogger("UAA/OIDC");

/**
 *
 */
function getCurrentUser(currentUserUrl?: string): Promise<UaaUser> {
	const getUserDataRequest = UaaRequest.buildGetUserRequest(currentUserUrl);
	return dispatchAndCheckServerRequest<UaaUser>(getUserDataRequest, isUaaUser);
}

/**
 *
 * @param user
 */
function hasRoles(user: UaaUser): user is UaaUser & { roles: Role[] } {
	return "roles" in user;
}

/**
 *
 * @param user
 */
function hasAdditionalProperties(
	user: UaaUser
): user is UaaUser & { additionalProperties: Record<string, unknown> } {
	return "additionalProperties" in user;
}

/**
 * @param action
 */
function* modifyUser(action: Action<UaaOidcUser>): SagaGenerator<void> {
	const user = action.payload as UaaOidcUser;

	try {
		if (user.currentUserUrl && !URL.canParse(user.currentUserUrl)) {
			logger.error(`Invalid URL of currentUserURL: ${user.currentUserUrl}`);
			throw {
				status: 400,
				statusText: "Invalid URL of currentUserURL"
			};
		}
		const currentUser = yield* call(getCurrentUser, user.currentUserUrl);
		const uaaOidcModifiedUser: UaaOidcModifiedUser = new UaaOidcModifiedUser(
			user,
			hasRoles(currentUser) ? currentUser.roles : undefined,
			hasAdditionalProperties(currentUser)
				? currentUser.additionalProperties
				: undefined
		);
		yield* put(UaaActions.modifiedOidcUser(uaaOidcModifiedUser));
	} catch (error) {
		const { status, statusText } = error as Response;
		yield* put(
			UaaActions.modifyOidcUserFailed({
				status: status,
				statusText: statusText
			})
		);
	}
}

const oidcClientSaga: UaaSagaDescriptor[] = [
	{
		canHandle: (action: ReduxAction) =>
			UaaActions.modifyingOidcUser.match(action),
		handle: (action: Action<UaaOidcUser>) => modifyUser(action)
	}
];
export default oidcClientSaga;
