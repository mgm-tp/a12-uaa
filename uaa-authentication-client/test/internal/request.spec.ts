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
import fetchMock from "fetch-mock";

import {
	isUaaUser,
	UaaExtendedUser,
	UaaUser
} from "../../src/internal/interfaces/user.js";
import { AuthenticationType, UaaClient } from "../../src/index.js";
import { dispatchAndCheckServerRequest } from "../../src/internal/utils/dispatchServerRequest.js";
import { buildLoginRequest } from "../../src/internal/utils/index.js";

describe("Normalize all URLs within connector", function () {
	beforeEach(function () {
		fetchMock.reset();
	});

	afterEach(function () {
		fetchMock.reset();
	});

	it("serverURL have backslash at the end", function () {
		return new Promise<void>(resolve => {
			fetchMock.postOnce((url: string) => {
				if (url === "/uaaServer/user/local/login") {
					resolve();
				}
				return true;
			}, {});

			UaaClient.init({
				serverURL: "uaaServer/"
			});
			const loginRequest = buildLoginRequest(
				{ username: "admin", password: "admin" },
				AuthenticationType.LOCAL
			);
			dispatchAndCheckServerRequest<UaaUser | UaaExtendedUser>(
				loginRequest,
				isUaaUser
			).catch(() => {
				/* Expected: mock response fails isUaaUser check */
			});
		});
	});

	it("serverURL haven't backslash at the end", function () {
		return new Promise<void>(resolve => {
			fetchMock.postOnce((url: string) => {
				if (url === "/uaaServer/user/local/login") {
					resolve();
				}
				return true;
			}, {});

			UaaClient.init({
				serverURL: "uaaServer"
			});
			const loginRequest = buildLoginRequest(
				{ username: "admin", password: "admin" },
				AuthenticationType.LOCAL
			);
			dispatchAndCheckServerRequest<UaaUser | UaaExtendedUser>(
				loginRequest,
				isUaaUser
			).catch(() => {
				/* Expected: mock response fails isUaaUser check */
			});
		});
	});
});
