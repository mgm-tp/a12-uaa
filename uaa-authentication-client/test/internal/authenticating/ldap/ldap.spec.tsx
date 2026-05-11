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
import { render, cleanup } from "@testing-library/react";
import { expect } from "chai";
import fetchMock from "fetch-mock";
import sinon, { SinonSandbox } from "sinon";
import { Unsubscribe } from "redux";

import {
	AuthenticationType,
	UaaClient,
	UaaActions,
	UaaProvider,
	UAAServiceWorker,
	UaaUser
} from "../../../../src/index.js";
import { sessionStorage } from "../../../../src/internal/utils/index.js";

import {
	accessTokenMock as access_token,
	mockLdapState
} from "../mockState.js";
import createUaaStore from "../reduxSetup.js";
import { uaaClientConfiguration } from "../appSetup.js";

let unsubscribe: Unsubscribe = () => undefined;
let store = createUaaStore();

const handleChange = (
	orderAuthenticationState: unknown[],
	onComplete?: () => void
) => {
	let resolve: () => void;
	const promise = new Promise<void>(r => {
		resolve = r;
	});
	const listener = () => {
		const uaa = orderAuthenticationState.shift();
		if (uaa) {
			expect(store.getState().uaa).to.deep.equal(uaa);
		}
		if (orderAuthenticationState.length === 0) {
			unsubscribe();
			onComplete?.();
			resolve();
		}
	};
	return { listener, done: promise };
};

describe("ACTIVE_DIRECTORY_LDAP authenticating test", function () {
	let sandbox: SinonSandbox;

	beforeEach(function () {
		sandbox = sinon.createSandbox();
		store = createUaaStore();
		UaaClient.init(uaaClientConfiguration);
		render(<UaaProvider store={store} />);
		UAAServiceWorker.postToken = sandbox.fake.returns(true);
	});

	afterEach(function () {
		// cleanup on exiting
		sessionStorage.clear();
		fetchMock.reset();
		cleanup();
		unsubscribe();
		sandbox.restore();
	});

	it("Default slice", function () {
		expect(store.getState().uaa).to.deep.equal(mockLdapState.default);
	});

	it("Slice at function login called", async function () {
		const { listener, done } = handleChange([mockLdapState.loggingIn]);
		fetchMock.mock("ldapURL/user/active_directory_ldap/login", 200);
		unsubscribe = store.subscribe(listener);
		UaaClient.getLdapClient().login("admin", "admin");
		await done;
	});

	it("Slice at login successfully", async function () {
		const { listener, done } = handleChange([
			mockLdapState.loggingIn,
			mockLdapState.updateAccessToken,
			mockLdapState.loggedIn
		]);
		fetchMock.post("ldapURL/user/active_directory_ldap/login", {
			body: {
				username: "admin",
				displayName: "admin",
				customData: "here is custom data"
			},
			headers: {
				access_token
			},
			status: 200
		});
		unsubscribe = store.subscribe(listener);
		UaaClient.getLdapClient().login("admin", "admin");
		await done;
	});

	it("Slice at login failed", async function () {
		const { listener, done } = handleChange([
			mockLdapState.loggingIn,
			mockLdapState.loginFailed
		]);
		fetchMock.mock("ldapURL/user/active_directory_ldap/login", 404);
		unsubscribe = store.subscribe(listener);
		UaaClient.getLdapClient().login("admin", "admin");
		await done;
	});

	it("Slice at logout failed", async function () {
		const { listener, done } = handleChange([mockLdapState.loggedIn]);
		store.dispatch(
			UaaActions.loggedIn({
				user: {
					username: "admin",
					displayName: "admin",
					customData: "here is custom data"
				} as UaaUser,
				type: AuthenticationType.ACTIVE_DIRECTORY_LDAP
			})
		);
		store.dispatch(
			UaaActions.updateAccessToken({
				access_token
			})
		);

		unsubscribe = store.subscribe(listener);
		UaaClient.getLdapClient().logout();
		await done;
	});

	it("Slice at logout successfully", async function () {
		const { listener, done } = handleChange([
			mockLdapState.loggedIn,
			mockLdapState.loggedOut
		]);
		store.dispatch(
			UaaActions.loggedIn({
				user: {
					username: "admin",
					displayName: "admin",
					customData: "here is custom data"
				} as UaaUser,
				type: AuthenticationType.ACTIVE_DIRECTORY_LDAP
			})
		);
		store.dispatch(
			UaaActions.updateAccessToken({
				access_token
			})
		);
		sessionStorage.setItem(
			"authenticationType",
			AuthenticationType.ACTIVE_DIRECTORY_LDAP
		);
		// headers
		fetchMock.post("ldapURL/user/logout", 200);
		unsubscribe = store.subscribe(listener);
		UaaClient.getLdapClient().logout();
		await done;
	});
});
