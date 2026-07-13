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
	UaaActions,
	UaaClient,
	UaaProvider,
	UaaSelectors,
	UaaUser
} from "../../../../src/index.js";
import { TokenManagement } from "../../../../src/internal/tokenManagement.js";
import {
	sessionStorage,
	reduxStore
} from "../../../../src/internal/utils/index.js";

import {
	accessTokenMock as access_token,
	mockLocalState,
	uaaUserMock
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

describe("LOCAL authenticating test", function () {
	let sandbox: SinonSandbox;

	const regex_authorize_url = /\/authorize/;
	const regex_token_url = /\/token/;

	beforeEach(function () {
		sandbox = sinon.createSandbox();
		store = createUaaStore();
		UaaClient.init(uaaClientConfiguration);
		render(<UaaProvider store={store} />);
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
		expect(store.getState().uaa).to.deep.equal(mockLocalState.default);
	});

	it("Slice at function login called", async function () {
		const { listener, done } = handleChange([mockLocalState.loggingIn]);
		fetchMock.mock("localURL/user/local/login", 200);
		unsubscribe = store.subscribe(listener);
		UaaClient.getLocalClient().login("admin", "admin");
		await done;
	});

	it("Slice at login successfully", async function () {
		const { listener, done } = handleChange([
			mockLocalState.loggingIn,
			mockLocalState.updateAccessToken,
			mockLocalState.loggedIn
		]);
		fetchMock.post("localURL/user/local/login", {
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
		UaaClient.getLocalClient().login("admin", "admin");
		await done;
	});

	it("Slice at login failed", async function () {
		const { listener, done } = handleChange([
			mockLocalState.loggingIn,
			mockLocalState.loginFailed
		]);
		fetchMock.mock("localURL/user/local/login", 404);
		unsubscribe = store.subscribe(listener);
		UaaClient.getLocalClient().login("admin", "admin");
		await done;
	});

	it("Slice at logout failed", async function () {
		const { listener, done } = handleChange([mockLocalState.loggedIn]);
		store.dispatch(
			UaaActions.loggedIn({
				user: {
					username: "admin",
					displayName: "admin",
					customData: "here is custom data"
				} as UaaUser,
				type: AuthenticationType.LOCAL
			})
		);
		store.dispatch(
			UaaActions.updateAccessToken({
				access_token: access_token
			})
		);

		unsubscribe = store.subscribe(listener);
		UaaClient.getLocalClient().logout();
		await done;
	});

	it("Slice at logout successfully", async function () {
		const { listener, done } = handleChange([
			mockLocalState.loggedIn,
			mockLocalState.loggedOut
		]);
		store.dispatch(
			UaaActions.loggedIn({
				user: {
					username: "admin",
					displayName: "admin",
					customData: "here is custom data"
				} as UaaUser,
				type: AuthenticationType.LOCAL
			})
		);
		store.dispatch(
			UaaActions.updateAccessToken({
				access_token: access_token
			})
		);
		sessionStorage.setItem("authenticationType", AuthenticationType.LOCAL);
		// headers
		fetchMock.post("localURL/user/logout", 200);

		unsubscribe = store.subscribe(listener);
		UaaClient.getLocalClient().logout();
		await done;
	});

	it("Restore token successfully", async function () {
		const { listener, done } = handleChange([mockLocalState.loggedIn]);
		sessionStorage.setItem("access_token", access_token);
		sessionStorage.setItem("authenticationType", AuthenticationType.LOCAL);
		fetchMock.mock("localURL/uaa-authentication/tokenValid", {
			status: 200,
			body: true
		});
		fetchMock.mock("localURL/uaa-authentication/currentUser", {
			status: 200,
			body: {
				...uaaUserMock
			}
		});
		// faking case the UAAProvider component was not rendered
		sandbox.replace(reduxStore, "getStore", sinon.stub().returns(undefined));
		UaaClient.getLocalClient().restoreAuthenticationState(store.dispatch);

		unsubscribe = store.subscribe(listener);
		await done;
	});

	// TODO: This test never completed its async flow (mocha version called done() synchronously)
	it.skip("Silent renew token successfully", async function () {
		sessionStorage.setItem("access_token", access_token);
		sessionStorage.setItem(
			"token_renew_timestamp",
			String(Date.now() + 10000000)
		);
		fetchMock.mock(
			url => {
				const results = url.match(regex_authorize_url);
				return Boolean(results);
			},
			(
				url: string,
				options: Record<string, unknown>
				// eslint-disable-next-line @typescript-eslint/no-invalid-void-type
			): void | Record<string, unknown> => {
				const { body } = options;
				const results = url.match(regex_token_url);
				if (results && body) {
					const state = (body as FormData).get("state");
					const id_token_hint = (body as FormData).get("id_token_hint");
					expect(id_token_hint).to.equal(
						sessionStorage.getItem("access_token")
					);

					return {
						status: 200,
						body: {
							state,
							code: "here is authorization code"
						}
					};
				}
				return {};
			}
		);

		fetchMock.mock(
			url => {
				const results = url.match(regex_token_url);
				return Boolean(results);
			},
			(
				url: string,
				options: Record<string, unknown>
				// eslint-disable-next-line @typescript-eslint/no-invalid-void-type
			): void | Record<string, unknown> => {
				const { body } = options;
				const results = url.match(regex_token_url);
				if (results && body) {
					const code = (body as FormData).get("code");
					expect(code).to.equal("here is authorization code");
					return {
						status: 200,
						body: {
							access_token: "new access_token",
							token_renew_in_seconds: 1000
						}
					};
				}
				return {};
			}
		);

		const management = TokenManagement.getInstance();
		sandbox.spy(management);
		const { listener, done } = handleChange([], () => {
			const new_access_token = UaaSelectors.accessToken(store.getState());
			expect(new_access_token).to.equal("new access_token");

			expect(
				(management.startSilentRenew as unknown as { called: boolean }).called
			).to.equal(true);
			management.stopService();
		});
		unsubscribe = store.subscribe(listener);
		management.startService();
		await done;
	});
});
