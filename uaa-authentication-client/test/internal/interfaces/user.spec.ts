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
import * as Assert from "node:assert";

import {
	isUaaExtendedUser,
	isUaaOidcUser,
	isUaaUser,
	isUaaModifiedOidcUser
} from "../../../src/index.js";

import {
	uaaExtendedUserMock,
	userModifiedUserMock
} from "../authenticating/mockState.js";

describe("user interface", function () {
	it("isUaaUser return true which correct type", function () {
		const uaaUser = {
			username: "username",
			displayName: "displayName"
		};
		Assert.strictEqual(isUaaUser(uaaUser), true);
	});

	it("isUaaUser return false which incorrect type", function () {
		const notUaaUser = "not UAA user";
		Assert.strictEqual(isUaaUser(notUaaUser), false);
	});

	it("isUaaExtendedUser return true which correct type", function () {
		Assert.strictEqual(isUaaExtendedUser(uaaExtendedUserMock), true);
	});

	it("isUaaExtendedUser return false which incorrect type", function () {
		const uaaUser = {
			username: "username",
			displayName: "displayName",
			lastName: "last name",
			email: "email"
		};
		Assert.strictEqual(isUaaExtendedUser(uaaUser), false);
	});

	it("isUaaOidcUser return true which correct type", function () {
		const uaaUser = {
			access_token: "access token",
			id_token: "id token",
			token_type: "token type"
		};
		Assert.strictEqual(isUaaOidcUser(uaaUser), true);
	});

	it("isUaaOidcUser return false which incorrect type", function () {
		const uaaUser = {
			access_token: "access token",
			token_type: "token type"
		};
		Assert.strictEqual(isUaaOidcUser(uaaUser), false);
	});

	it("isUaaModifiedOidcUser return false which incorrect type", function () {
		const uaaUser = {
			id_token: "id token",
			access_token: "access token",
			token_type: "token type"
		};
		Assert.strictEqual(isUaaModifiedOidcUser(uaaUser), false);
	});

	it("isUaaModifiedOidcUser return true which correct type", function () {
		Assert.strictEqual(isUaaModifiedOidcUser(userModifiedUserMock), true);
	});
});
