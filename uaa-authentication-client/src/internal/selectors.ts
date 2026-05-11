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
import * as Reselect from "reselect";

import {
	AccessRight,
	AuthenticationState,
	AuthenticationType,
	isUaaExtendedUser,
	isUaaModifiedOidcUser,
	isUaaOidcUser,
	Role,
	UaaExtendedUser,
	UaaOidcModifiedUser,
	UaaOidcUser,
	UaaUser
} from "./interfaces/index.js";
import {
	ConfigurableSelector,
	createSliceSelector,
	Selector
} from "./utils/index.js";

export type UaaLoggedInSlice = {
	readonly user: UaaUser | UaaOidcUser | UaaExtendedUser | UaaOidcModifiedUser;
	readonly state: AuthenticationState.AUTHENTICATED;
	readonly authenticationType: AuthenticationType;
	readonly access_token?: string;
};

export type UaaNotLoggedSlice = {
	readonly error?: string;
	readonly state:
		| AuthenticationState.AUTHENTICATING
		| AuthenticationState.NOT_AUTHENTICATED;
	readonly authenticationType?: AuthenticationType;
};

export type UaaSlice = UaaLoggedInSlice | UaaNotLoggedSlice;

/**
 * @param x
 * @internal
 */
function justReturn<T>(x: T): T {
	return x;
}

/**
 * @param slice
 * @internal
 */
function isUaaSlice(slice: unknown): slice is UaaSlice {
	return (
		isLogged(slice as Record<string, unknown>) ||
		isNotLogged(slice as Record<string, unknown>)
	);
}

/**
 * @param slice
 * @internal
 */
function isLogged(slice: unknown): slice is UaaLoggedInSlice {
	return (
		(slice as Record<string, unknown>) &&
		"user" in (slice as Record<string, unknown>) &&
		"state" in (slice as Record<string, unknown>) &&
		"authenticationType" in (slice as Record<string, unknown>)
	);
}

/**
 * @param slice
 * @internal
 */
function isNotLogged(slice: unknown): slice is UaaNotLoggedSlice {
	return (
		(slice as Record<string, unknown>) &&
		"state" in (slice as Record<string, unknown>) &&
		((slice as Record<string, unknown>).state ===
			AuthenticationState.AUTHENTICATING ||
			(slice as Record<string, unknown>).state ===
				AuthenticationState.NOT_AUTHENTICATED)
	);
}

/**
 *
 * @param selector Selector to be extended with default value
 * @returns Selector with default value
 */
function injectWithConfig<T>(selector: Selector<T>): ConfigurableSelector<T> {
	return Object.assign(selector, {
		withConfig: (params: { defaultValue: T }) =>
			((state: { [x: string]: unknown }) => {
				try {
					return selector(state);
				} catch (error) {
					if (
						error instanceof Error &&
						(error.message.startsWith("State contains an invalid") ||
							error.message.startsWith("State does not contain a"))
					) {
						return params.defaultValue;
					}
					throw error;
				}
			}) as Selector<T>
	});
}

/**
 * Get authorization state from uaaSlice
 */
export const state = injectWithConfig(
	Reselect.createSelector(
		createSliceSelector(
			"uaa",
			isUaaSlice,
			slice => slice?.state ?? AuthenticationState.NOT_AUTHENTICATED
		),
		justReturn
	) as Selector<AuthenticationState>
);

/**
 * Get user login from uaaSlice
 */
export const user = injectWithConfig(
	Reselect.createSelector(
		createSliceSelector("uaa", isLogged, slice => slice.user),
		justReturn
	) as Selector<UaaUser | UaaOidcUser | UaaExtendedUser | UaaOidcModifiedUser>
);

/**
 * Get username from uaaSlice
 */
export const username = injectWithConfig(
	Reselect.createSelector(
		createSliceSelector("uaa", isUaaSlice, slice => {
			if (isLogged(slice) && slice.user !== undefined) {
				if (isUaaOidcUser(slice.user)) {
					return slice.user.profile.preferred_username;
				}
				return slice.user.username;
			}
			return "";
		}),
		justReturn
	) as Selector<string | undefined>
);

/**
 * Utility function to extract roles from the user object
 * @param user
 */
const extractRoles = (user: UaaUser | UaaOidcUser) => {
	if (isUaaModifiedOidcUser(user) || isUaaExtendedUser(user)) {
		return user.roles;
	}
	return undefined;
};

/**
 * Selector to get roles from uaaSlice
 */
export const roles = injectWithConfig(
	Reselect.createSelector(
		createSliceSelector("uaa", isLogged, slice => extractRoles(slice.user)),
		justReturn
	) as Selector<Role[] | undefined>
);

/**
 * Selector to get access rights from uaaSlice
 */
export const accessRights = injectWithConfig(
	Reselect.createSelector(
		createSliceSelector("uaa", isLogged, slice => {
			const roles = extractRoles(slice.user);
			if (!roles) {
				return undefined;
			}
			const accessRightsList = roles.flatMap(role => role.accessRights) || [];

			const accessRightsNames = new Set<string>();
			return accessRightsList.filter(accessRight => {
				if (accessRightsNames.has(accessRight.name)) {
					return false;
				}
				accessRightsNames.add(accessRight.name);
				return true;
			});
		}),
		justReturn
	) as Selector<AccessRight[] | undefined>
);

/**
 * Get error from uaaSlice
 */
export const error = injectWithConfig(
	Reselect.createSelector(
		createSliceSelector("uaa", isUaaSlice, slice => {
			if (isNotLogged(slice)) {
				return slice?.error ?? undefined;
			}
			return undefined;
		}),
		justReturn
	) as Selector<string | undefined>
);

/**
 * Get authenticationType from uaaSlice with current user login
 */
export const authenticationType = injectWithConfig(
	Reselect.createSelector(
		createSliceSelector(
			"uaa",
			isUaaSlice,
			slice => slice?.authenticationType ?? undefined
		),
		justReturn
	) as Selector<AuthenticationType | undefined>
);

/**
 * Get access token from uaaSlice with current user login
 */
export const accessToken = injectWithConfig(
	Reselect.createSelector(
		createSliceSelector("uaa", isUaaSlice, slice => {
			if (isLogged(slice)) {
				if (isUaaOidcUser(slice.user)) {
					return slice.user.access_token;
				}

				return slice.access_token;
			}
			return "";
		}),
		justReturn
	) as Selector<string>
);

/**
 * @param slice
 * @internal
 */
function isTokenType(slice: UaaSlice | unknown): slice is UaaLoggedInSlice {
	return (
		isLogged(slice) &&
		isUaaOidcUser(slice.user) &&
		typeof slice.user.token_type === "string"
	);
}

/**
 * Get token type from uaaSlice with current user login
 */
export const tokenType = injectWithConfig(
	Reselect.createSelector(
		createSliceSelector("uaa", isTokenType, slice => {
			if (isUaaOidcUser(slice.user)) {
				return slice.user.token_type;
			}
			return "";
		}),
		justReturn
	) as Selector<string>
);
