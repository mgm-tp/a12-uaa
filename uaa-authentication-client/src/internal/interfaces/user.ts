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
	SigninPopupArgs,
	SigninRedirectArgs,
	User as OidcUser
} from "oidc-client-ts";

/**
 * @param obj
 */
export function isUaaUser(obj: unknown): obj is UaaUser {
	return (
		!!obj &&
		typeof obj === "object" &&
		"username" in obj &&
		"displayName" in obj &&
		typeof obj.username === "string" &&
		typeof obj.displayName === "string"
	);
}

export interface UaaExtendedUser extends UaaUser {
	readonly lastName: string;
	readonly firstName: string;
	readonly email: string;
	readonly customProperties?: Record<string, unknown>;
	readonly roles: Role[];
}

/**
 * @param obj
 */
export function isUaaExtendedUser(obj: unknown): obj is UaaExtendedUser {
	return (
		isUaaUser(obj) &&
		"lastName" in obj &&
		"firstName" in obj &&
		"email" in obj &&
		"roles" in obj &&
		Array.isArray(obj.roles)
	);
}

export interface Role {
	readonly name: string;
	readonly description: string;
	readonly accessRights: AccessRight[];
}

export interface AccessRight {
	readonly name: string;
	readonly description: string;
}

export interface UaaUser {
	readonly username: string;
	readonly displayName: string;
}

// Hide OidcUser type
export type UaaOidcUser = OidcUser & {
	currentUserUrl?: string;
};

export class UaaOidcModifiedUser extends OidcUser {
	private readonly _roles?: Role[];
	private readonly _additionalProperties?: Record<string, unknown>;

	public constructor(
		oidcUser: OidcUser,
		roles?: Role[],
		additionalProperties?: Record<string, unknown>
	) {
		super(oidcUser);
		this._roles = roles;
		this._additionalProperties = additionalProperties;
	}

	get roles(): Role[] | undefined {
		return this._roles;
	}

	get additionalProperties(): Record<string, unknown> | undefined {
		return this._additionalProperties;
	}
}

/**
 * @param obj
 */
export function isUaaOidcUser(obj: unknown): obj is OidcUser {
	return (
		!!obj &&
		typeof obj === "object" &&
		"access_token" in obj &&
		"id_token" in obj &&
		"token_type" in obj &&
		typeof obj.access_token === "string" &&
		typeof obj.id_token === "string" &&
		typeof obj.token_type === "string"
	);
}

/**
 * @param obj
 */
export function isUaaModifiedOidcUser(
	obj: unknown
): obj is UaaOidcModifiedUser {
	return isUaaOidcUser(obj) && "_roles" in obj && Array.isArray(obj._roles);
}

export interface UaaUserManager {
	/** @internal */
	signIn: (signinArgs?: SigninRedirectArgs | SigninPopupArgs) => Promise<void>;
	/** @internal */
	signOut: () => void;
	/** @internal */
	registerEvent: () => void;
	/** @internal */
	unRegisterEvent: () => void;
}
