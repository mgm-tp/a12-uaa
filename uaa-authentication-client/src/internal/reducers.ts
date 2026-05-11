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
import { AnyAction } from "redux";

import {
	isUaaModifiedOidcUser,
	UaaClient,
	UaaOidcModifiedUser,
	UaaOidcUser
} from "../index.js";

import * as UaaActions from "./actions.js";
import { AuthenticationState, AuthenticationType } from "./interfaces/index.js";
import { UaaLoggedInSlice, UaaSlice } from "./selectors.js";

/**
 * Reduces the authentication related actions
 * @param slice
 * @param action
 */
// eslint-disable-next-line complexity
export function AuthenticationReducer(
	slice: UaaSlice = {
		state: AuthenticationState.NOT_AUTHENTICATED,
		authenticationType: undefined
	},
	action: AnyAction
): UaaSlice {
	const newSlice = slice;
	const removeError = function (slide: Record<string, unknown>) {
		const { error, ...restSlide } = slide;
		return restSlide;
	};

	switch (action.type) {
		case UaaActions.loggingInOIDC.type:
			return {
				...removeError(newSlice),
				state: AuthenticationState.AUTHENTICATING,
				authenticationType: AuthenticationType.OAUTH2
			};
		case UaaActions.loggingInSAML.type:
			return {
				...removeError(newSlice),
				state: AuthenticationState.AUTHENTICATING,
				authenticationType: AuthenticationType.SAML
			};
		case UaaActions.loggingInLocal.type:
			return {
				...removeError(newSlice),
				state: AuthenticationState.AUTHENTICATING,
				authenticationType: AuthenticationType.LOCAL
			};
		case UaaActions.loggingInLDAP.type:
			return {
				...removeError(newSlice),
				state: AuthenticationState.AUTHENTICATING,
				authenticationType: AuthenticationType.ACTIVE_DIRECTORY_LDAP
			};
		case UaaActions.loggedIn.type:
			if (
				slice.state !== AuthenticationState.AUTHENTICATED ||
				slice.user !== action.payload.user
			) {
				const { user, type } = action.payload;
				return {
					...removeError(newSlice),
					state: AuthenticationState.AUTHENTICATED,
					user,
					authenticationType: type
				};
			}
			return newSlice;
		case UaaActions.loginFailed.type:
			if (
				slice.state !== AuthenticationState.NOT_AUTHENTICATED ||
				slice.error !== action.payload.errorCode
			) {
				return {
					authenticationType: newSlice.authenticationType,
					state: AuthenticationState.NOT_AUTHENTICATED,
					error: action.payload.errorCode
				};
			}
			return newSlice;
		case UaaActions.oidc_session_terminated.type:
		case UaaActions.oidc_user_signed_out.type:
		case UaaActions.oidc_user_expired.type:
		case UaaActions.oidc_silentRenewError.type:
		case UaaActions.loggedOut.type:
			if (
				slice.state !== AuthenticationState.NOT_AUTHENTICATED &&
				!(
					AuthenticationType.SAML === slice.authenticationType &&
					UaaClient.getSamlConfiguration().logoutIDP
				)
			) {
				return {
					state: AuthenticationState.NOT_AUTHENTICATED
				};
			}
			return newSlice;
		case UaaActions.oidc_userFound.type: {
			const userPayload = action.payload as UaaOidcUser;
			const oldUser = (newSlice as UaaLoggedInSlice)?.user;
			const newUser = isUaaModifiedOidcUser(oldUser)
				? new UaaOidcModifiedUser(
						userPayload,
						oldUser.roles,
						oldUser.additionalProperties
					)
				: userPayload;
			return {
				...newSlice,
				authenticationType: AuthenticationType.OAUTH2,
				state: AuthenticationState.AUTHENTICATED,
				user: newUser
			};
		}
		case UaaActions.modifiedOidcUser.type:
			if (
				slice.state !== AuthenticationState.NOT_AUTHENTICATED ||
				slice.error !== action.payload.errorCode
			) {
				const user = action.payload;
				return {
					...removeError(newSlice),
					state: AuthenticationState.AUTHENTICATED,
					user,
					authenticationType: AuthenticationType.OAUTH2
				};
			}
			return newSlice;
		case UaaActions.updateAccessToken.type:
			return {
				...removeError(newSlice),
				authenticationType:
					action.payload.authenticationType || newSlice.authenticationType,
				state: AuthenticationState.AUTHENTICATED,
				user: (newSlice as UaaLoggedInSlice)?.user,
				access_token: action.payload.access_token
			};
		case UaaActions.updateUserInfo.type:
			return {
				...removeError(newSlice),
				authenticationType: (newSlice as UaaLoggedInSlice)?.authenticationType,
				state: AuthenticationState.AUTHENTICATED,
				user: action.payload,
				access_token: (newSlice as UaaLoggedInSlice)?.access_token
			};
		default:
			return newSlice;
	}
}
