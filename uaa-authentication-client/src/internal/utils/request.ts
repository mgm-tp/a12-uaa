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
import { RestRequestPayload } from "@com.mgmtp.a12.utils/utils-connector";

import { AuthenticationType } from "../interfaces/index.js";

const ACCEPT_JSON_HEADER = ["Accept", "application/json"];
const ACCEPT_ALL_HEADER = ["Accept", "*/*"];
const CONTENT_TYPE_JSON_HEADER = [
	"Content-Type",
	"application/json;charset=utf8"
];
const APPLICATION_FORM_URLENCODED_VALUE = [
	"Content-Type",
	"application/x-www-form-urlencoded"
];
export const RELATIVE_LOGOUT_URL = "user/logout";
export const RELATIVE_LOCAL_LOGIN_URL = `user/${AuthenticationType.LOCAL.toLowerCase()}/login`;
export const RELATIVE_ACTIVE_DIRECTORY_LDAP_LOGIN_URL = `user/${AuthenticationType.ACTIVE_DIRECTORY_LDAP.toLowerCase()}/login`;

interface LoginRequest {
	readonly username: string;
	readonly password: string;
}

/**
 * build default request to login
 *
 * @param loginRequest
 * @param type
 * @param relativeUrl
 */
export function buildLoginRequest(
	loginRequest: LoginRequest,
	type: AuthenticationType.LOCAL | AuthenticationType.ACTIVE_DIRECTORY_LDAP,
	relativeUrl?: string
): RestRequestPayload {
	return {
		method: "POST",
		relativeUrl: relativeUrl ?? `user/${type.toLowerCase()}/login`,
		body: JSON.stringify(loginRequest),
		customHeaders: [ACCEPT_JSON_HEADER, CONTENT_TYPE_JSON_HEADER],
		extendedData: {
			unAuthorizeRequest: true,
			sensitiveFields: ["password"]
		}
	};
}

/**
 * build default request to get selfconfigure
 */
export function buildGetSelfconfigure(): RestRequestPayload {
	return {
		method: "GET",
		relativeUrl: "uaa-authentication/selfconfigure",
		customHeaders: [ACCEPT_JSON_HEADER]
	};
}

/**
 * build default request to logout
 *
 * @param logoutRelativeUrl
 * @param method
 */
export function buildLogoutRequest(
	logoutRelativeUrl?: string,
	method = "POST"
): RestRequestPayload {
	return {
		method,
		relativeUrl: logoutRelativeUrl ?? RELATIVE_LOGOUT_URL,
		customHeaders: [ACCEPT_JSON_HEADER]
	};
}

/**
 * build default request to get current login user
 */
export function buildGetUserRequest(
	currentUserUrl?: string
): RestRequestPayload {
	const url = currentUserUrl ? new URL(currentUserUrl) : undefined;
	return {
		method: "GET",
		runtimeBaseUrl: url ? url.origin : undefined,
		relativeUrl: url
			? url.pathname + url.search + url.hash
			: "uaa-authentication/currentUser",
		customHeaders: [ACCEPT_JSON_HEADER]
	};
}

/**
 * build exchangeCode authorize request with included authorization code under browser cookie
 * state and code_challenge are enclosed
 */
export function buildExchangeCodeRequestAuthorize({
	state,
	code_c
}: {
	state: string;
	code_c: string;
}): RestRequestPayload {
	return {
		method: "POST",
		relativeUrl: `uaa-authentication/exchangeAuthorizationCodeToToken/authorize`,
		customHeaders: [ACCEPT_ALL_HEADER, ["Content-Type", "application/json"]],
		body: `{
			"code_challenge": "${code_c}",
			"state": "${state}"
		}`,
		extendedData: {
			unAuthorizeRequest: false
		}
	};
}

/**
 * build exchangeCode request with included authorization code under browser cookie
 * enclosed code_verifier
 */
export function buildExchangeCodeRequest(code_v: string): RestRequestPayload {
	const body = new URLSearchParams();
	body.append("code_verifier", code_v);
	return {
		method: "POST",
		relativeUrl: `uaa-authentication/exchangeAuthorizationCodeToToken`,
		customHeaders: [ACCEPT_ALL_HEADER],
		body,
		extendedData: {
			unAuthorizeRequest: false
		}
	};
}

/**
 * build token valid request
 *
 * @param token
 */
export function buildTokenValidRequest(token: string): RestRequestPayload {
	return {
		method: "POST",
		relativeUrl: "uaa-authentication/tokenValid",
		body: token,
		customHeaders: [ACCEPT_JSON_HEADER],
		extendedData: {
			unAuthorizeRequest: true
		}
	};
}

/**
 * build token valid request
 * @param token
 */
export function buildOauth2TokenValidRequest(
	token: string
): RestRequestPayload {
	return {
		method: "POST",
		relativeUrl: "uaa-authentication/oauth2TokenValid",
		body: token,
		customHeaders: [ACCEPT_JSON_HEADER],
		extendedData: {
			unAuthorizeRequest: true
		}
	};
}

/**
 * build authorize request in sequence request for renewing the token.
 *
 * @param root0
 * @param root0.state
 * @param root0.code_c
 * @param root0.access_token
 */
export function buildAuthorizeRequest({
	state,
	code_c,
	access_token
}: {
	state: string;
	code_c: string;
	access_token: string;
}): RestRequestPayload {
	return {
		method: "POST",
		relativeUrl: `uaa-authentication/authorize`,
		customHeaders: [ACCEPT_JSON_HEADER, ["Content-Type", "application/json"]],
		body: `{
			"code_challenge": "${code_c}",
			"state": "${state}",
			"id_token_hint": "${access_token}"
		}`,
		extendedData: {
			unAuthorizeRequest: true
		}
	};
}

/**
 * build token request in sequence request for renewing the token.
 *
 * @param root0
 * @param root0.code
 * @param root0.code_v
 */
export function buildTokenRequest({
	code,
	code_v
}: {
	code: string;
	code_v: string;
}): RestRequestPayload {
	const urlencoded = new URLSearchParams();
	urlencoded.append("code", code);
	urlencoded.append("code_verifier", code_v);
	return {
		method: "POST",
		relativeUrl: `uaa-authentication/token`,
		customHeaders: [APPLICATION_FORM_URLENCODED_VALUE],
		body: urlencoded,
		extendedData: {
			unAuthorizeRequest: true
		}
	};
}
