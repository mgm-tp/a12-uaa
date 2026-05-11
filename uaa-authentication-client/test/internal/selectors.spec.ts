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
import sinon from "sinon";
import * as selectors from "../../src/internal/selectors.js";
import {
	AccessRight,
	AuthenticationState,
	Role,
	UaaSelectors,
	UaaUser
} from "../../src/index.js";

import {
	accessRightsMock,
	mockLocalState,
	mockOidcState,
	rolesMock,
	uaaExtendedUserMock,
	userModifiedUserMock
} from "./authenticating/mockState.js";

const oidcUser: unknown = {
	username: "username",
	displayName: "displayName",
	scope: "scope",
	access_token: "access_token",
	id_token: "id_token",
	token_type: "token_type",
	toStorageString: () => "toStorageString"
};

describe("selectors - user", function () {
	let sandbox: sinon.SinonSandbox;
	let state:
		| Record<string, unknown>
		| sinon.SinonSpy<unknown[], unknown>
		| { uaa: UaaSelectors.UaaSlice }
		| undefined;
	let sliceName: unknown;

	beforeAll(function () {
		sandbox = sinon.createSandbox();
		sliceName = "uaa";
	});

	afterEach(function () {
		sandbox.restore();
	});

	it("user should throw an error when state is null", function () {
		state = undefined;
		expect(() => {
			selectors.user(state as Record<string, unknown>);
		}).to.throw(`State does not contain a ${sliceName} slice.`);
	});

	it("user should throw an error when state don't contain sliceName 'uaa'", function () {
		state = sandbox.spy();
		expect(() => {
			selectors.user(state as Record<string, unknown>);
		}).to.throw(`State does not contain a ${sliceName} slice.`);
	});

	it("user should throw an error when state contain an invalid function isUaaUser: slide is not Object", function () {
		state = {
			uaa: null
		};
		expect(() => {
			selectors.user(state as Record<string, unknown>);
		}).to.throw(`State contains an invalid ${sliceName} slice.`);
	});

	it("user should return correct data of user in uaa", function () {
		state = {
			uaa: mockOidcState.loggedIn
		};
		expect(selectors.user(state)).equal(mockOidcState.loggedIn.user);
	});
});

describe("selectors - username", function () {
	let sandbox: sinon.SinonSandbox;
	let state:
		| Record<string, unknown>
		| undefined
		| sinon.SinonSpy<unknown[], unknown>;
	let sliceName: unknown;

	beforeAll(function () {
		sandbox = sinon.createSandbox();
		sliceName = "uaa";
	});

	afterEach(function () {
		sandbox.restore();
	});

	it("username should throw an error when state is null", function () {
		state = undefined;
		expect(() => {
			selectors.username(state as Record<string, unknown>);
		}).to.throw(`State does not contain a ${sliceName} slice.`);
	});

	it("username should throw an error when state don't contain sliceName 'uaa'", function () {
		state = sandbox.spy();
		expect(() => {
			selectors.username(state as Record<string, unknown>);
		}).to.throw(`State does not contain a ${sliceName} slice.`);
	});

	it("username should throw an error when state contain an invalid function username: slide is not Object", function () {
		state = {
			uaa: null
		};
		expect(() => {
			selectors.username(state as Record<string, unknown>);
		}).to.throw(`State contains an invalid ${sliceName} slice.`);
	});

	it("username should return correct data when logged in type UaaOidcUser ", function () {
		state = {
			uaa: {
				...mockOidcState.loggedIn,
				user: {
					...mockOidcState.loggedIn.user,
					profile: {
						...mockOidcState.loggedIn.user.profile
					}
				}
			}
		};
		expect(selectors.username(state)).equal("admin");
	});

	it("username should return correct data when logged in type UaaUser ", function () {
		state = {
			uaa: {
				...mockLocalState.loggedIn,
				user: {
					...mockLocalState.loggedIn.user
				}
			}
		};
		expect(selectors.username(state)).equal("admin");
	});
});

describe("selectors - accessToken", function () {
	let sandbox: sinon.SinonSandbox;
	let state:
		| Record<string, unknown>
		| undefined
		| sinon.SinonSpy<unknown[], unknown>;
	let sliceName: unknown;

	beforeAll(function () {
		sandbox = sinon.createSandbox();
		sliceName = "uaa";
	});

	afterEach(function () {
		sandbox.restore();
	});

	it("accessToken should throw an error when state is null", function () {
		state = undefined;
		expect(() => {
			selectors.accessToken(state as Record<string, unknown>);
		}).to.throw(`State does not contain a ${sliceName} slice.`);
	});

	it("accessToken should throw an error when state don't contain sliceName 'uaa'", function () {
		state = sandbox.spy();
		expect(() => {
			selectors.accessToken(state as Record<string, unknown>);
		}).to.throw(`State does not contain a ${sliceName} slice.`);
	});

	it("accessToken should throw an error when state contain an invalid function isAccessToken: slide is not Object", function () {
		state = {
			uaa: null
		};
		expect(() => {
			selectors.accessToken(state as Record<string, unknown>);
		}).to.throw(`State contains an invalid ${sliceName} slice.`);
	});

	it("accessToken should throw an error when state contain an invalid function isAccessToken: slide.user is not Object", function () {
		state = {
			uaa: {
				user: null
			}
		};
		expect(() => {
			selectors.accessToken(state as Record<string, unknown>);
		}).to.throw(`State contains an invalid ${sliceName} slice.`);
	});

	it("accessToken should throw an error when state contain an invalid function isAccessToken: slide.user.access_token is not string", function () {
		state = {
			uaa: {
				user: {
					access_token: null
				}
			}
		};
		expect(() => {
			selectors.accessToken(state as Record<string, unknown>);
		}).to.throw(`State contains an invalid ${sliceName} slice.`);
	});
});

describe("selectors - tokenType", function () {
	let sandbox: sinon.SinonSandbox;
	let state:
		| Record<string, unknown>
		| sinon.SinonSpy<unknown[], unknown>
		| undefined;
	let sliceName: unknown;

	beforeAll(function () {
		sandbox = sinon.createSandbox();
		sliceName = "uaa";
	});

	afterEach(function () {
		sandbox?.restore();
	});

	it("tokenType should throw an error when state is null", function () {
		state = undefined;
		expect(() => {
			selectors.tokenType(state as Record<string, unknown>);
		}).to.throw(`State does not contain a ${sliceName} slice.`);
	});

	it("tokenType should throw an error when state don't contain sliceName 'uaa'", function () {
		state = sandbox.spy();
		expect(() => {
			selectors.tokenType(state as Record<string, unknown>);
		}).to.throw(`State does not contain a ${sliceName} slice.`);
	});

	it("tokenType should throw an error when state contain an invalid function isTokenType: slide is not Object", function () {
		state = {
			uaa: null
		};
		expect(() => {
			selectors.tokenType(state as Record<string, unknown>);
		}).to.throw(`State contains an invalid ${sliceName} slice.`);
	});

	it("tokenType should return undefined if the token_type is invalid", function () {
		state = {
			uaa: {
				...mockOidcState.loggedIn,
				user: {
					...mockOidcState.loggedIn.user,
					token_type: null
				}
			}
		};
		expect(() => {
			selectors.tokenType(state as Record<string, unknown>);
		}).to.throw(`State contains an invalid ${sliceName} slice.`);
	});

	it("tokenType should return correct data of accessToken in uaa", function () {
		state = {
			uaa: {
				...mockOidcState.loggedIn,
				user: oidcUser
			}
		};
		expect(selectors.tokenType(state)).equal("token_type");
		expect(selectors.accessToken(state)).equal("access_token");
	});
});

describe("selectors - roles and access rights", function () {
	let sandbox: sinon.SinonSandbox;
	let state:
		| Record<string, unknown>
		| sinon.SinonSpy<unknown[], unknown>
		| undefined;
	let sliceName: unknown;

	beforeAll(function () {
		sandbox = sinon.createSandbox();
		sliceName = "uaa";
	});

	afterEach(function () {
		sandbox?.restore();
		state = {};
	});

	it("roles and access rights should throw an error when state is null", function () {
		state = undefined;
		expect(() => {
			selectors.roles(state as Record<string, unknown>);
		}).to.throw(`State does not contain a ${sliceName} slice.`);
		expect(() => {
			selectors.accessRights(state as Record<string, unknown>);
		}).to.throw(`State does not contain a ${sliceName} slice.`);
	});

	it("roles and access rights should throw an error when state don't contain sliceName 'uaa'", function () {
		state = sandbox.spy();
		expect(() => {
			selectors.roles(state as Record<string, unknown>);
		}).to.throw(`State does not contain a ${sliceName} slice.`);

		expect(() => {
			selectors.accessRights(state as Record<string, unknown>);
		}).to.throw(`State does not contain a ${sliceName} slice.`);
	});

	it("roles and access rights should return undefined if the roles and access rights is invalid", function () {
		state = {
			uaa: {
				...mockOidcState.loggedIn,
				user: {
					...mockOidcState.loggedIn.user
				}
			}
		};

		expect(selectors.roles(state as Record<string, unknown>)).equal(undefined);
		expect(selectors.accessRights(state as Record<string, unknown>)).equal(
			undefined
		);
	});

	it("roles and access rights should return correct data of roles and access rights in uaa with OidcUser", function () {
		state = {
			uaa: {
				...mockOidcState.loggedIn,
				user: userModifiedUserMock
			}
		};
		const roles = selectors.roles(state);
		expect(
			roles
				?.flatMap(role => role.accessRights)
				.flatMap(accessRight => accessRight).length
		).equal(6);
	});

	it("roles and access rights should return correct data of roles and access rights in uaa with UaaExtendedUser user", function () {
		state = {
			uaa: {
				...mockOidcState.loggedIn,
				user: uaaExtendedUserMock
			}
		};
		const roles = selectors.roles(state);
		expect(
			roles
				?.flatMap(role => role.accessRights)
				.flatMap(accessRight => accessRight).length
		).equal(6);
	});
});

describe("selectors with the default value", function () {
	let sandbox: sinon.SinonSandbox;
	let state:
		| Record<string, unknown>
		| sinon.SinonSpy<unknown[], unknown>
		| undefined;

	beforeAll(function () {
		sandbox = sinon.createSandbox();
	});

	afterEach(function () {
		sandbox?.restore();
		state = {};
	});

	it("state is returned with the default value", function () {
		state = {};

		expect(
			selectors.state.withConfig({
				defaultValue: AuthenticationState.AUTHENTICATING
			})(state as Record<string, unknown>)
		).equal(AuthenticationState.AUTHENTICATING);
		expect(
			selectors.state.withConfig({
				defaultValue: AuthenticationState.NOT_AUTHENTICATED
			})(state as Record<string, unknown>)
		).equal(AuthenticationState.NOT_AUTHENTICATED);
	});

	it("user is returned with the default value", function () {
		state = {
			uaa: {
				state: AuthenticationState.NOT_AUTHENTICATED,
				authenticationType: undefined
			}
		};
		expect(
			(
				selectors.user.withConfig({
					defaultValue: {
						username: "anonymous",
						displayName: "anonymous"
					}
				})(state as Record<string, unknown>) as UaaUser
			).username
		).equal("anonymous");
	});

	it("username is returned with the default value", function () {
		state = {};
		expect(
			selectors.username.withConfig({
				defaultValue: "anonymous"
			})(state as Record<string, unknown>)
		).equal("anonymous");
	});

	it("roles is returned with undefined", function () {
		state = {
			uaa: {
				...mockOidcState.loggedIn,
				user: {
					...mockOidcState.loggedIn.user
				}
			}
		};
		expect(
			selectors.roles.withConfig({
				defaultValue: []
			})(state as Record<string, unknown>)
		).equal(undefined);
	});

	it("roles is returned with default value", function () {
		state = {
			uaa: {}
		};
		expect(
			selectors.roles.withConfig({
				defaultValue: rolesMock
			})(state as Record<string, unknown>)?.length
		).equal(2);
		expect(
			(
				selectors.roles.withConfig({
					defaultValue: rolesMock
				})(state as Record<string, unknown>) as Role[]
			)[0].name
		).equal("Role 1");
	});

	it("accessRights is returned with default value", function () {
		state = {};
		expect(
			selectors.accessRights.withConfig({
				defaultValue: accessRightsMock
			})(state as Record<string, unknown>)?.length
		).equal(3);
		expect(
			(
				selectors.accessRights.withConfig({
					defaultValue: accessRightsMock
				})(state as Record<string, unknown>) as AccessRight[]
			)[1].name
		).equal("Access right 2");
	});
});
