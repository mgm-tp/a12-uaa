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
import { Unsubscribe } from "redux";
import sinon, { SinonSandbox } from "sinon";

import {
	AuthenticationType,
	UaaActions,
	UaaClient,
	UaaProvider,
	UaaUser
} from "../../../../src/index.js";
import {
	sessionStorage,
	reduxStore
} from "../../../../src/internal/utils/index.js";

import {
	accessTokenExpirationMock as access_token_expiration,
	accessTokenMock as access_token,
	mockSamlState,
	uaaUserMock
} from "../mockState.js";
import createUaaStore from "../reduxSetup.js";
import { uaaClientConfiguration } from "../appSetup.js";

let unsubscribe: Unsubscribe = () => undefined;
let store = createUaaStore();
fetchMock.config.overwriteRoutes = true;
const locationFunc = window.location;

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
			if (currentSlide.state === "authenticating") {
				expect(currentSlide.authenticationType).to.deep.equal(
					AuthenticationType.SAML
				);
				expect(currentSlide.user).to.deep.equal(undefined);
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

describe("SAML authenticating test", function () {
	let sandbox: SinonSandbox;

	beforeEach(function () {
		store = createUaaStore();
		sandbox = sinon.createSandbox();
		UaaClient.init(uaaClientConfiguration);
		render(<UaaProvider store={store} />);
	});

	afterEach(function () {
		// cleanup on exiting
		sessionStorage.clear();
		fetchMock.reset();
		unsubscribe();
		cleanup();
		sandbox.restore();
		// @ts-expect-error
		window.location = locationFunc;
	});

	it("Default slice", function () {
		expect(store.getState().uaa).to.deep.equal(mockSamlState.default);
	});

	it.skip("Restore token successfully", async function () {
		const { listener, done } = handleChange([
			mockSamlState.restoreProcessing,
			mockSamlState.updateIdToken,
			mockSamlState.loggingIn,
			mockSamlState.loggedIn
		]);
		sessionStorage.setItem("access_token", access_token);
		sessionStorage.setItem("authenticationType", AuthenticationType.SAML);
		fetchMock.post("uaa-authentication/tokenValid", {
			status: 200,
			body: true
		});
		fetchMock.mock("uaa-authentication/currentUser", {
			status: 200,
			body: {
				...uaaUserMock
			}
		});
		// faking case the UAAProvider component was not rendered
		sandbox.replace(reduxStore, "getStore", sinon.stub().returns(undefined));
		UaaClient.getSamlClient().restoreAuthenticationState(store.dispatch);

		unsubscribe = store.subscribe(listener);
		await done;
	});

	it.skip("Redirect to Login_url when calling one API with nonAuthorization", async function () {
		let resolve: () => void;
		const promise = new Promise<void>(r => {
			resolve = r;
		});
		sessionStorage.setItem("authenticationType", AuthenticationType.SAML);
		sessionStorage.setItem(
			"selfConfigure",
			JSON.stringify({
				uaaBaseUrl: "http://localhost:8080",
				loginRelativeUrl: "saml2/authenticate/uaa"
			})
		);

		delete (window as unknown as { location?: Record<string, unknown> })
			.location;
		window.location = {
			...locationFunc,
			// @ts-expect-error
			replace: (url: string) => {
				// Check login url
				expect(url).to.deep.equal(
					"http://localhost:8080/saml2/authenticate/uaa"
				);
				resolve();
			}
		};
		fetchMock.get("uaa-authentication/currentUser", {
			status: 401
		});
		UaaClient.getSamlClient().login();
		await promise;
	});

	it.skip("Update id token when the user login successfully", async function () {
		const { listener, done } = handleChange(
			[mockSamlState.loggingIn, mockSamlState.updateIdToken],
			() => {
				expect(sessionStorage.getItem("access_token")).to.deep.equal(
					access_token
				);
			}
		);
		sessionStorage.setItem("authenticationType", AuthenticationType.SAML);
		sessionStorage.setItem(
			"selfConfigure",
			JSON.stringify({
				uaaBaseUrl: "http://localhost:8080",
				loginRelativeUrl: "saml2/authenticate/uaa"
			})
		);

		delete (window as unknown as { location?: Record<string, unknown> })
			.location;
		window.location = {
			...locationFunc,
			// @ts-expect-error
			replace: (url: string) => {
				// Check login url
				expect(url).to.deep.equal(
					"http://localhost:8080/saml2/authenticate/uaa"
				);
				window.location.href =
					"http://localhost:3000/pathSuccessUrl/?authorizationCode=authorizationCodeSample";
				fetchMock.get(
					"uaa-authentication/exchangeAuthorizationCodeToToken?authorizationCode=authorizationCodeSample",
					{
						status: 200,
						headers: { access_token, access_token_expiration }
					}
				);
				cleanup();
				render(<UaaProvider store={store} />);
			}
		};
		fetchMock.get("uaa-authentication/currentUser", {
			status: 401
		});
		unsubscribe = store.subscribe(listener);
		UaaClient.getSamlClient().login();
		await done;
	});

	it("Slice at logout successfully", async function () {
		const { listener, done } = handleChange([
			mockSamlState.updateIdToken,
			mockSamlState.loggedIn,
			//UAA/LOGGING_OUT
			mockSamlState.loggedIn,
			mockSamlState.loggedOut
		]);
		unsubscribe = store.subscribe(listener);
		fetchMock.post("samlURL/user/logout", 200);
		store.dispatch(
			UaaActions.updateAccessToken({
				access_token: access_token,
				authenticationType: AuthenticationType.SAML
			})
		);
		store.dispatch(
			UaaActions.loggedIn({
				user: {
					username: "admin",
					displayName: "admin",
					customData: "here is custom data"
				} as UaaUser,
				type: AuthenticationType.SAML
			})
		);
		UaaClient.getSamlClient().logout();
		await done;
	});
});
