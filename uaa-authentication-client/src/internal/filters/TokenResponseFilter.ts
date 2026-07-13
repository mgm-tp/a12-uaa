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
	ResponseFilter,
	ResponseFilterResult
} from "@com.mgmtp.a12.utils/utils-connector";

import * as UaaActions from "../actions.js";
import { SessionStorageKeys, TokenConfiguration } from "../interfaces/index.js";
import { reduxStore, sessionStorage } from "../utils/index.js";

/**
 * When you configure UAA with SAML protocol for trying failed request eg: getCurrentUser
 * you will receive a response header "Login" which give you login url.
 * This filter will handle this header and redirect you to login page of SAML server (IDP).
 */
export class TokenResponseFilter implements ResponseFilter {
	private tokenConfigure?: TokenConfiguration;

	constructor(tokenConfigure?: TokenConfiguration) {
		this.tokenConfigure = tokenConfigure;
	}

	doResponseFilter(response: Response | undefined): ResponseFilterResult {
		if (
			response === undefined ||
			response.headers.get(
				this.tokenConfigure?.generatedTokenHeaderName ?? "access_token"
			) === null
		) {
			return {
				response: response,
				continue: true
			};
		}
		const access_token = response.headers.get(
			this.tokenConfigure?.generatedTokenHeaderName ?? "access_token"
		);
		if (access_token) {
			const token_expiration_in_seconds = response.headers.get(
				"token_expiration_in_seconds"
			);
			const token_renew_in_seconds = response.headers.get(
				"token_renew_in_seconds"
			);
			sessionStorage.setItem(SessionStorageKeys.ACCESS_TOKEN, access_token);
			if (token_expiration_in_seconds) {
				sessionStorage.setItem(
					SessionStorageKeys.TOKEN_EXPIRATION_IN_SECONDS,
					token_expiration_in_seconds
				);
				const token_expiration_timestamp =
					Date.now() + Number(token_expiration_in_seconds) * 1000;
				sessionStorage.setItem(
					SessionStorageKeys.TOKEN_EXPIRATION_TIMESTAMP,
					token_expiration_timestamp.toString()
				);
			}
			if (token_renew_in_seconds) {
				const token_renew_timestamp =
					Date.now() + Number(token_renew_in_seconds) * 1000;
				sessionStorage.setItem(
					SessionStorageKeys.TOKEN_RENEW_TIMESTAMP,
					token_renew_timestamp.toString()
				);
			}
			reduxStore.dispatch(UaaActions.updateAccessToken({ access_token }));
		}
		return {} as ResponseFilterResult;
	}

	canHandleResponse(response: Response | undefined): boolean {
		if (!response || response.status === 401) {
			return false;
		}
		return true;
	}
}
