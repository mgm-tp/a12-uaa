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
import sinon from "sinon";
import { Unsubscribe } from "redux";

import {
	AuthenticationState,
	AuthenticationType,
	SessionStorageKeys,
	UaaActions,
	UaaClient,
	UaaProvider
} from "../../../../src/index.js";
import { OidcClient } from "../../../../src/internal/factories/index.js";
import { sessionStorage } from "../../../../src/internal/utils/index.js";

import {
	accessTokenMock,
	clientExtendedUserMock,
	mockOidcState,
	uaaOidcModifiedUser,
	uaaOidcUserMock
} from "../mockState.js";
import { uaaClientConfiguration } from "../appSetup.js";
import createUaaStore from "../reduxSetup.js";

let unsubscribe: Unsubscribe = () => undefined;
let store = createUaaStore();
fetchMock.config.overwriteRoutes = true;
const sandbox = sinon.createSandbox();

const handleChange = (
	orderAuthenticationState: unknown[],
	onComplete?: () => void
) => {
	let resolve: () => void;
	const promise = new Promise<void>(r => {
		resolve = r;
	});
	const listener = () => {
		const expectedSlide = orderAuthenticationState.shift();
		const currentSlide = store.getState().uaa;
		if (expectedSlide) {
			if (currentSlide.state === AuthenticationState.AUTHENTICATING) {
				expect(currentSlide.authenticationType).to.deep.equal(
					AuthenticationType.OAUTH2
				);
			} else if (currentSlide.state === AuthenticationState.AUTHENTICATED) {
				expect(currentSlide.user.access_token).to.deep.equal(accessTokenMock);
			} else {
				expect(currentSlide).to.deep.equal(expectedSlide);
			}
		}

		if (orderAuthenticationState.length === 0) {
			unsubscribe();
			onComplete?.();
			resolve();
		}
	};
	return { listener, done: promise };
};

describe("OIDC authenticating test", function () {
	beforeEach(function () {
		store = createUaaStore();
		UaaClient.init(uaaClientConfiguration);
		OidcClient.userManager.signIn = sandbox.stub();
		OidcClient.userManager.signOut = sandbox.stub();
		render(<UaaProvider store={store} />);
	});

	afterEach(function () {
		// cleanup on exiting
		sessionStorage.clear();
		fetchMock.reset();
		unsubscribe();
		cleanup();
		sandbox.restore();
	});

	it("Default slice", function () {
		expect(store.getState().uaa).to.deep.equal(mockOidcState.default);
	});

	it("Slice at function login called", async function () {
		const { listener, done } = handleChange([mockOidcState.loggingIn]);
		unsubscribe = store.subscribe(listener);
		UaaClient.getOidcClient().login();
		await done;
	});

	it("Checking Oidc-client.UserManager behavior when the uaaOidcClient.login called", function () {
		UaaClient.getOidcClient().login();
		// @ts-ignore: skip check next line
		const spyCall = OidcClient.userManager.signIn.getCall(0);
		expect(spyCall).not.to.equal(null);
	});

	it("Checking Oidc-client.UserManager behavior when the uaaOidcClient.logout called", async function () {
		// The state is not_authenticate
		UaaClient.getOidcClient().logout();
		// @ts-ignore: skip check next line
		expect(OidcClient.userManager.signOut.getCall(0)).to.equal(null);

		// The state is authenticated
		const { listener, done } = handleChange([mockOidcState.loggedIn], () => {
			UaaClient.getOidcClient().logout();
			// @ts-ignore: skip check next line
			expect(OidcClient.userManager.signOut.getCall(0)).not.to.equal(null);
		});
		unsubscribe = store.subscribe(listener);
		store.dispatch(
			UaaActions.loggedIn({
				user: uaaOidcUserMock,
				type: AuthenticationType.OAUTH2
			})
		);
		await done;
	});

	it("Checking sessionStorage after login successfully", async function () {
		const { listener, done } = handleChange(
			[
				mockOidcState.loggingIn,
				mockOidcState.oidc_userFound,
				mockOidcState.updateAccessToken,
				mockOidcState.loggedIn
			],
			() => {
				expect(
					sessionStorage.getItem(SessionStorageKeys.ACCESS_TOKEN)
				).to.equal(accessTokenMock);
				expect(
					sessionStorage.getItem(SessionStorageKeys.AUTHENTICATION_TYPE)
				).to.equal(AuthenticationType.OAUTH2);
			}
		);
		unsubscribe = store.subscribe(listener);
		UaaClient.getOidcClient().login();
		store.dispatch(UaaActions.oidc_userFound(uaaOidcUserMock));
		store.dispatch(
			UaaActions.updateAccessToken({
				access_token: uaaOidcUserMock?.access_token as string,
				authenticationType: AuthenticationType.OAUTH2
			})
		);
		store.dispatch(
			UaaActions.loggedIn({
				user: uaaOidcUserMock,
				type: AuthenticationType.OAUTH2
			})
		);
		await done;
	});

	it("Checking redux store after modify user successfully", async function () {
		const { listener, done } = handleChange(
			[
				mockOidcState.loggingIn,
				mockOidcState.oidc_userFound,
				mockOidcState.updateAccessToken,
				mockOidcState.loggedIn,
				mockOidcState.uaaOidcModifyingUser,
				mockOidcState.uaaOidcModifiedUser
			],
			() => {
				expect(store.getState().uaa.user).to.deep.equal(uaaOidcModifiedUser);
			}
		);
		fetchMock.get("/oidcURL/uaa-authentication/currentUser", {
			status: 200,
			body: clientExtendedUserMock
		});
		unsubscribe = store.subscribe(listener);
		UaaClient.getOidcClient().login();
		store.dispatch(UaaActions.oidc_userFound(uaaOidcUserMock));
		store.dispatch(
			UaaActions.loggedIn({
				user: uaaOidcUserMock,
				type: AuthenticationType.OAUTH2
			})
		);
		await done;
	});
});
