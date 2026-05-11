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
import "isomorphic-fetch";
import * as Assert from "node:assert";

// eslint-disable-next-line no-restricted-imports
import { FilterChain } from "@com.mgmtp.a12.utils/utils-connector/lib/main/internal/filter/FilterChain.js";

import {
	UaaExtendedUser,
	UaaFilters,
	UaaOidcModifiedUser,
	UaaUser,
	UaaOidcUser
} from "../../src/index.js";
import { sessionStorage } from "../../src/internal/utils/index.js";
import {
	AuthenticationState,
	AuthenticationType
} from "../../src/internal/interfaces/authentication.js";
import { UaaSlice } from "../../src/internal/selectors.js";

describe("com.mgmtp.a12.connector.request.authorizationHeaderFilter", function () {
	it("test default init authorization header filter", function () {
		sessionStorage.setItem("authenticationType", AuthenticationType.OAUTH2);
		const requestFilterChain = new FilterChain();
		const oidcUser:
			| UaaUser
			| UaaOidcUser
			| UaaExtendedUser
			| UaaOidcModifiedUser = {
			username: "username",
			displayName: "displayname",
			scope: "scope",
			access_token: "access_token String",
			id_token: "id_token String",
			toStorageString: () => "toStorageString",
			token_type: "Bearer"
		};
		const appReduxState = {
			uaa: {
				user: oidcUser,
				state: AuthenticationState.AUTHENTICATED,
				authenticationType: AuthenticationType.OAUTH2
			} as UaaSlice
		};
		const bodyFilter: UaaFilters.AuthorizationHeaderFilter =
			new UaaFilters.AuthorizationHeaderFilter(() => appReduxState);
		requestFilterChain.registerFilterRequest(bodyFilter);
		requestFilterChain.startRequestFilter({
			relativeUrl: "",
			method: "DELETE"
		});
		const requestInit = requestFilterChain.getRequestInit();
		Assert.strictEqual(
			(requestInit?.headers as Headers).get("Authorization"),
			"Bearer access_token String",
			"Bearer access_token String"
		);
	});
});

describe("com.mgmtp.a12.connector.request.authorizationHeaderFilter.not.oidc", function () {
	it("test default init authorization header filter", function () {
		sessionStorage.setItem("authenticationType", AuthenticationType.SAML);
		const requestFilterChain = new FilterChain();
		const extendedUser: UaaExtendedUser = {
			email: "",
			firstName: "",
			lastName: "",
			roles: [],
			username: "username",
			displayName: "displayname"
		};
		const appReduxState = {
			uaa: {
				user: extendedUser,
				state: AuthenticationState.AUTHENTICATED,
				authenticationType: AuthenticationType.SAML,
				access_token: "access_token String"
			} as UaaSlice
		};
		const bodyFilter: UaaFilters.AuthorizationHeaderFilter =
			new UaaFilters.AuthorizationHeaderFilter(() => appReduxState);
		requestFilterChain.registerFilterRequest(bodyFilter);
		requestFilterChain.startRequestFilter({
			relativeUrl: "",
			method: "DELETE"
		});
		const requestInit = requestFilterChain.getRequestInit();
		Assert.strictEqual(
			(requestInit?.headers as Headers).get("Authorization"),
			"UAABearer access_token String",
			"UAABearer access_token String"
		);
	});
});

describe("com.mgmtp.a12.connector.request.authorizationHeaderFilter.undefined.state", function () {
	it("test default init authorization header filter with undefined state should success", function () {
		sessionStorage.setItem("authenticationType", AuthenticationType.SAML);
		const requestFilterChain = new FilterChain();
		const bodyFilter: UaaFilters.AuthorizationHeaderFilter =
			new UaaFilters.AuthorizationHeaderFilter(() => undefined);
		requestFilterChain.registerFilterRequest(bodyFilter);
		requestFilterChain.startRequestFilter({
			relativeUrl: "",
			method: "DELETE"
		});
	});
});
