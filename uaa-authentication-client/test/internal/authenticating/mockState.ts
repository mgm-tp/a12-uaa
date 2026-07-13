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
	Role,
	UaaExtendedUser,
	UaaOidcModifiedUser
} from "../../../src/internal/interfaces/user.js";
import { AccessRight, UaaOidcUser, UaaUser } from "../../../src/index.js";
import {
	AuthenticationState,
	AuthenticationType
} from "../../../src/internal/interfaces/authentication.js";

export const accessTokenMock = "here is access_token";
export const tokenRenewInSecondsMock = "300";
export const accessRightsMock: AccessRight[] = [
	{
		name: "Access right 1",
		description: "description AR1"
	},
	{
		name: "Access right 2",
		description: "description AR2"
	},
	{
		name: "Access right 3",
		description: "description AR3"
	}
];

export const rolesMock: Role[] = [
	{
		name: "Role 1",
		accessRights: accessRightsMock,
		description: "description R1"
	},
	{
		name: "Role 2",
		accessRights: accessRightsMock,
		description: "description R2"
	}
];
export const uaaUserMock: UaaUser = {
	username: "admin",
	displayName: "admin",
	customData: "here is custom data"
} as UaaUser;

export const uaaOidcUserMock: UaaOidcUser = {
	id_token: "here is id_token",
	session_state: "ec97fa47-647d-482f-9348-368303aa10f1",
	access_token: "here is access_token",
	refresh_token: "",
	token_type: "Bearer",
	scope: "openid email profile",
	profile: {
		auth_time: 1617783832,
		jti: "826ba8c2-e162-4c67-92fc-44c5c6bdc1ad",
		sub: "4d8aa7ef-9cfc-47a8-8836-8ab557b04398",
		typ: "ID",
		azp: "uaa-spa-client",
		session_state: "ec97fa47-647d-482f-9348-368303aa10f1",
		acr: "1",
		email_verified: false,
		nationality: "VN",
		realm_access: {
			roles: ["Manager", "Admin"]
		},
		preferred_username: "admin"
	} as unknown,
	expires_at: 1617784133,
	toStorageString: () => "toStorageString"
} as UaaOidcUser;

export const userModifiedUserMock: UaaOidcModifiedUser =
	new UaaOidcModifiedUser(uaaOidcUserMock, rolesMock);

export const roleMock: Role = {
	name: "default",
	description: "this is description",
	accessRights: [
		{
			name: "mockAdmin",
			description: "description of mockAdmin access"
		} as AccessRight
	]
};

export const uaaOidcModifiedUser: UaaOidcModifiedUser = new UaaOidcModifiedUser(
	uaaOidcUserMock,
	[roleMock]
);

export interface clientExtendedUser extends UaaExtendedUser {
	readonly clientAdditionalProps: string;
}

export const uaaExtendedUserMock: UaaExtendedUser = {
	username: "username",
	displayName: "displayName",
	firstName: "first name",
	lastName: "last name",
	email: "email",
	roles: rolesMock
};

export const clientExtendedUserMock: clientExtendedUser = {
	username: "admin",
	displayName: "admin",
	lastName: "lastName",
	firstName: "firstName",
	email: "admin@mgm-tp.com",
	roles: [roleMock],
	customData: "here is custom data",
	clientAdditionalProps: "here is custom props"
} as clientExtendedUser;

const defaultState = {
	state: AuthenticationState.NOT_AUTHENTICATED,
	authenticationType: undefined
};
const loggingInState = {
	state: AuthenticationState.AUTHENTICATING,
	authenticationType: AuthenticationType.LOCAL
};
const loginFailedState = {
	state: AuthenticationState.NOT_AUTHENTICATED,
	authenticationType: AuthenticationType.LOCAL,
	error: "auth.error.authenticationfailed"
};
const updateAccessTokenState = {
	state: AuthenticationState.AUTHENTICATED,
	authenticationType: AuthenticationType.LOCAL,
	access_token: accessTokenMock,
	user: undefined
};
const loggedInState = {
	state: AuthenticationState.AUTHENTICATED,
	authenticationType: AuthenticationType.LOCAL,
	user: uaaUserMock,
	access_token: accessTokenMock
};
const loggedOutState = {
	state: AuthenticationState.NOT_AUTHENTICATED
};

export const mockLdapState = {
	default: { ...defaultState },
	loggingIn: {
		...loggingInState,
		authenticationType: AuthenticationType.ACTIVE_DIRECTORY_LDAP
	},
	loginFailed: {
		...loginFailedState,
		authenticationType: AuthenticationType.ACTIVE_DIRECTORY_LDAP
	},
	updateAccessToken: {
		...updateAccessTokenState,
		authenticationType: AuthenticationType.ACTIVE_DIRECTORY_LDAP
	},
	loggedIn: {
		...loggedInState,
		authenticationType: AuthenticationType.ACTIVE_DIRECTORY_LDAP
	},
	loggedOut: { ...loggedOutState },
	restoreProcessing: { ...defaultState }
};

export const mockSamlState = {
	default: { ...defaultState },
	loggingIn: {
		...loggingInState,
		authenticationType: AuthenticationType.SAML
	},
	loginFailed: {
		...loginFailedState,
		authenticationType: AuthenticationType.SAML
	},
	updateIdToken: {
		...updateAccessTokenState,
		authenticationType: AuthenticationType.SAML
	},
	loggedIn: {
		...loggedInState,
		authenticationType: AuthenticationType.SAML
	},
	loggedOut: { ...loggedOutState },
	restoreProcessing: { ...defaultState }
};

const { ...loggedInOIDCState } = {
	...loggedInState,
	authenticationType: AuthenticationType.OAUTH2,
	user: uaaOidcUserMock
};

export const mockOidcState = {
	default: { ...defaultState },
	loggingIn: {
		...loggingInState,
		authenticationType: AuthenticationType.OAUTH2
	},
	loginFailed: {
		...loginFailedState,
		authenticationType: AuthenticationType.OAUTH2
	},
	updateAccessToken: {
		...updateAccessTokenState,
		authenticationType: AuthenticationType.OAUTH2,
		user: uaaOidcUserMock
	},
	loggedIn: loggedInOIDCState,
	loggedOut: { ...loggedOutState },
	oidc_userFound: {
		authenticationType: AuthenticationType.OAUTH2,
		state: AuthenticationState.AUTHENTICATED,
		user: uaaOidcUserMock
	},
	uaaOidcModifyingUser: loggedInOIDCState,
	uaaOidcModifiedUser: {
		authenticationType: AuthenticationType.OAUTH2,
		state: AuthenticationState.AUTHENTICATED,
		abc: "abdc",
		user: { ...uaaOidcModifiedUser }
	}
};

export const mockLocalState = {
	default: { ...defaultState },
	loggingIn: { ...loggingInState },
	loginFailed: { ...loginFailedState },
	updateAccessToken: { ...updateAccessTokenState },
	loggedIn: { ...loggedInState },
	loggedOut: { ...loggedOutState }
};
