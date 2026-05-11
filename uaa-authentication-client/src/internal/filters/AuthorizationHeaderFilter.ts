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
import {
	RequestFilter,
	RequestFilterPayload,
	RequestFilterResult
} from "@com.mgmtp.a12.utils/utils-connector";

import {
	AuthenticationType,
	SessionStorageKeys,
	TokenConfiguration
} from "../interfaces/index.js";
import * as UaaSelectors from "../selectors.js";
import { sessionStorage } from "../utils/index.js";

/**
 * AuthorizationHeaderFilter is responsible for sending your token
 * with Authorization header which is standard header in token-based authentication.
 */
export class AuthorizationHeaderFilter implements RequestFilter {
	private getAppReduxState: () => Record<string, unknown> | undefined;
	private tokenConfigure?: TokenConfiguration;

	constructor(
		getAppReduxState: () => Record<string, unknown> | undefined,
		tokenConfigure?: TokenConfiguration
	) {
		this.getAppReduxState = getAppReduxState;
		this.tokenConfigure = tokenConfigure;
	}

	canHandleRequest(requestInit: RequestFilterPayload): boolean {
		const extendedData = requestInit.payload.extendedData as {
			[key: string]: boolean;
		};
		if (extendedData?.unAuthorizeRequest) {
			return false;
		}
		return true;
	}

	doRequestFilter(
		requestFilterPayload: RequestFilterPayload
	): RequestFilterResult {
		if (!requestFilterPayload || !requestFilterPayload.request) {
			throw new Error("request init may not be falsy");
		}
		if (!requestFilterPayload.request.headers) {
			requestFilterPayload.request.headers = new Headers();
		}

		if (requestFilterPayload.payload) {
			const tokenFromSessionStorage = sessionStorage.getItem(
				SessionStorageKeys.ACCESS_TOKEN
			);
			const authenticationType = sessionStorage.getItem(
				SessionStorageKeys.AUTHENTICATION_TYPE
			);
			const reduxState = this.getAppReduxState();
			const tokenFromStore = reduxState && UaaSelectors.accessToken(reduxState);
			const access_token =
				tokenFromSessionStorage || tokenFromStore || undefined;
			if (access_token) {
				const tokenType =
					this.tokenConfigure?.tokenType ??
					`${
						authenticationType === AuthenticationType.OAUTH2 ? "" : "UAA"
					}Bearer`;
				const token = `${tokenType} ${access_token}`;
				const { headers } = requestFilterPayload.request;
				if (headers instanceof Headers) {
					headers.append(
						this.tokenConfigure?.authorizationHeaderName ?? "Authorization",
						token
					);
				} else if (Array.isArray(headers)) {
					headers.push([
						this.tokenConfigure?.authorizationHeaderName ?? "Authorization",
						token
					]);
				}
				requestFilterPayload.request.headers = headers;
			}
		}
		return {
			request: requestFilterPayload.request,
			continue: true
		};
	}
}
