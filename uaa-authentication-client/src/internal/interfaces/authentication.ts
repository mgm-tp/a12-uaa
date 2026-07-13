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
import type { Action as ReduxAction } from "redux";
import { SagaGenerator } from "typed-redux-saga";

/* eslint-disable no-unused-vars */
export enum AuthenticationState {
	AUTHENTICATING = "authenticating",
	AUTHENTICATED = "authenticated",
	NOT_AUTHENTICATED = "not_authenticate"
}

export enum AuthenticationType {
	LOCAL = "LOCAL",
	SAML = "SAML",
	OAUTH2 = "OAUTH2",
	ACTIVE_DIRECTORY_LDAP = "ACTIVE_DIRECTORY_LDAP"
}

export interface AuthenticationFailedPayload {
	readonly timestamp: string;
	readonly status: number;
	readonly error: string;
	readonly message: string;
	readonly path: string;
}

/**
 * @param obj
 */
export function isAuthenticationFailedPayload(
	obj: unknown
): obj is AuthenticationFailedPayload {
	if (typeof obj === "object" && obj) {
		return (
			obj &&
			"timestamp" in obj &&
			"error" in obj &&
			"status" in obj &&
			"message" in obj &&
			"path" in obj &&
			typeof obj.timestamp === "string" &&
			typeof obj.error === "string" &&
			typeof obj.status === "number" &&
			typeof obj.message === "string" &&
			typeof obj.path === "string"
		);
	}
	return false;
}

export interface Token {
	readonly token: string;
}

/**
 * @param obj
 */
export function isToken(obj: Token | Record<string, unknown>): obj is Token {
	return obj && "token" in obj && typeof obj.token === "string";
}

export type UaaSagaDescriptor = {
	canHandle(action: ReduxAction): boolean;
	handle(action: ReduxAction): SagaGenerator<void>;
};

export enum SessionStorageKeys {
	TOKEN_EXPIRATION_IN_SECONDS = "token_expiration_in_seconds",
	TOKEN_EXPIRATION_TIMESTAMP = "token_expiration_timestamp",
	ACCESS_TOKEN = "access_token",
	TOKEN_RENEW_TIMESTAMP = "token_renew_timestamp",
	AUTHENTICATION_TYPE = "authenticationType",
	SELF_CONFIGURE = "selfConfigure"
}
