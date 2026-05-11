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
import { actionCreatorFactory } from "typescript-fsa";
import { SigninPopupArgs, SigninRedirectArgs } from "oidc-client-ts";

import {
	AuthenticationType,
	UaaExtendedUser,
	UaaOidcModifiedUser,
	UaaOidcUser,
	UaaUser
} from "./interfaces/index.js";

const factory = actionCreatorFactory("UAA");
const factorySAML = actionCreatorFactory("UAA/SAML");
const factoryOIDC = actionCreatorFactory("UAA/OIDC");
const factoryLocal = actionCreatorFactory("UAA/LOCAL");
const factoryLdap = actionCreatorFactory("UAA/LDAP");

export const loggingInSAML = factorySAML("LOGGING_IN");
export const loggingInOIDC = factoryOIDC<
	SigninRedirectArgs | SigninPopupArgs | void
>("LOGGING_IN");
export interface UserLoggingInPayload {
	/**
	 * The username of the user to be logged in
	 */
	readonly username: string;

	/**
	 * The users password
	 */
	readonly password: string;

	/**
	 * The users password
	 */
	readonly loginRelativeUrl?: string;
}

export const loggingInLDAP = factoryLdap<UserLoggingInPayload>("LOGGING_IN");
export const loggingInLocal = factoryLocal<UserLoggingInPayload>("LOGGING_IN");

// Action to trackable restore authentication process.
export interface RestorePayload {
	readonly authenticationType: AuthenticationType;
	readonly error?: Error;
}

export const restoreProcessing = factory<RestorePayload>("RESTORE_PROCESSING");
export const restoreFailed = factory<RestorePayload>("RESTORE_FAILED");
export const restoreSuccess = factory<RestorePayload>("RESTORE_SUCCESS");

export interface UserLoggedInPayload {
	/**
	 * The user, that has logged in
	 */
	readonly user: UaaOidcUser | UaaUser | UaaExtendedUser | UaaOidcModifiedUser;

	/**
	 * The authenticate type that used to login
	 */
	readonly type: AuthenticationType;

	/**
	 * The access_token that used to login
	 */
	readonly access_token?: string;
}

export const loggedIn = factory<UserLoggedInPayload>("LOGGED_IN");
export const logoutRequested = factory("LOGOUT_REQUESTED");
export const logoutIdp = factory("LOGOUT_IDP");
export const loggingOut = factory("LOGGING_OUT");
export const loggedOut = factory("LOGGED_OUT");
export const updateUserInfo = factory<
	UaaOidcUser | UaaUser | UaaExtendedUser | UaaOidcModifiedUser
>("UPDATE_USER_INFO");

export interface UserLoginFailedPayload {
	/**
	 * The error code of the login error
	 */
	readonly errorCode: string;
	/**
	 * The status text of the response error
	 */
	readonly statusText?: string;
	/**
	 * The status code of the response error
	 */
	readonly status?: number;
}

export const loginFailed = factory<UserLoginFailedPayload>("LOGIN_FAILED");

export interface UserLogoutFailedPayload {
	/**
	 * The error code of the login error
	 */
	readonly errorCode: string;
	/**
	 * The status text of the response error
	 */
	readonly statusText?: string;
	/**
	 * The status code of the response error
	 */
	readonly status?: number;
}

export const logoutFailed = factory<UserLogoutFailedPayload>("LOGOUT_FAILED");

// OAuth2/OIDC
export interface SilenRenewErrorPayload {
	readonly error: Error;
}

const USER_EXPIRED = "redux-oidc/USER_EXPIRED";
const SILENT_RENEW_ERROR = "redux-oidc/SILENT_RENEW_ERROR";
const SESSION_TERMINATED = "redux-oidc/SESSION_TERMINATED";
const USER_EXPIRING = "redux-oidc/USER_EXPIRING";
const USER_FOUND = "redux-oidc/USER_FOUND";
const USER_SIGNED_OUT = "redux-oidc/USER_SIGNED_OUT";

export const oidc_silentRenewError =
	actionCreatorFactory()<SilenRenewErrorPayload>(SILENT_RENEW_ERROR);
export const oidc_userFound = actionCreatorFactory()<UaaOidcUser>(USER_FOUND);
export const oidc_user_expired = actionCreatorFactory()(USER_EXPIRED);
export const oidc_session_terminated =
	actionCreatorFactory()(SESSION_TERMINATED);
export const oidc_user_expiring = actionCreatorFactory()(USER_EXPIRING);
export const oidc_user_signed_out = actionCreatorFactory()(USER_SIGNED_OUT);

export interface UpdateIdTokenPayload {
	readonly access_token: string;
	readonly authenticationType?: AuthenticationType;
}

export const updateAccessToken = factory<UpdateIdTokenPayload>(
	"UPDATE_ACCESS_TOKEN"
);
export const silentRenewError = factory("SILENT_RENEW_ERROR");
export const loginRequire = factory("LOGIN_REQUIRE");
export const modifyingOidcUser = factory<UaaOidcUser>("MODIFYING_OIDC_USER");
export const modifiedOidcUser =
	factory<UaaOidcModifiedUser>("MODIFIED_OIDC_USER");

export interface ModifyUserFailed {
	/**
	 * The status text of the response error
	 */
	readonly statusText?: string;
	/**
	 * The status code of the response error
	 */
	readonly status?: number;
}

export const modifyOidcUserFailed = factory<ModifyUserFailed>(
	"MODIFY_OIDC_USER_FAILED"
);

export interface UnauthorizedPayload {
	readonly url: string;
}

export const unauthorized = factory<UnauthorizedPayload>("UNAUTHORIZED");

export const sessionStorageSharingData = factory(
	"SESSION_STORAGE_SHARING_DATA"
);
export const sessionStorageSharedData = factory("SESSION_STORAGE_SHARED_DATA");
