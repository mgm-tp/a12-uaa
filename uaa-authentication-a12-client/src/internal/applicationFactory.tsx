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
import type { FC, ReactNode } from "react";
import { useStore } from "react-redux";
import { Action } from "redux";

import {
	type A12ApplicationConfig,
	type ApplicationWithConfiguredFeature,
	type RequireFeatures,
	addAdditionalMiddlewares,
	addApplicationBusyTriggers,
	addWrapper,
	combineFeatures,
	setConfigured,
	withApplicationResetTriggers,
	withReducerMap
} from "@com.mgmtp.a12.client/client-core";
import {
	type UaaClientConfiguration,
	UaaActions,
	UaaMiddlewares,
	UaaProvider,
	UaaReducer
} from "@com.mgmtp.a12.uaa/uaa-authentication-client";

// ============================================================================
// Types
// ============================================================================

/**
 * UAA configuration options for A12 composable applications.
 *
 * @example
 * ```typescript
 * {
 *   uaa: {
 *     configuration: uaaClientConfig,
 *   }
 * }
 * ```
 */
export type UaaConfig = {
	/** UAA client configuration (endpoints, tokens, etc.). */
	readonly configuration: UaaClientConfiguration;
};

// Module augmentation
declare module "@com.mgmtp.a12.client/client-core" {
	interface A12ApplicationConfig {
		readonly uaa?: UaaConfig;
	}
}

/** Application config type that can have UAA configured. */
export type ApplicationWithUaaConfig = RequireFeatures<
	A12ApplicationConfig,
	{ uaa?: never }
>;

// ============================================================================
// Internal helpers
// ============================================================================

const addUaaBusyTriggers = <T extends ApplicationWithUaaConfig>(cfg: T) =>
	addApplicationBusyTriggers<T>({
		start: [
			UaaActions.loggingInLocal,
			UaaActions.loggingInLDAP,
			UaaActions.loggingInOIDC,
			UaaActions.loggingInSAML
		],
		end: [UaaActions.loggedIn, UaaActions.loginFailed]
	})(cfg);

const addUaaResetTriggers = <T extends ApplicationWithUaaConfig>(cfg: T) =>
	withApplicationResetTriggers<T>({
		resetRequested: [UaaActions.logoutRequested],
		resetConfirmed: UaaActions.loggingOut() as unknown as Action,
		reset: [UaaActions.loggedOut]
	})(cfg);

const addUaaMiddlewares = <T extends ApplicationWithUaaConfig>(cfg: T) =>
	addAdditionalMiddlewares<T>(...UaaMiddlewares())(cfg);

const addUaaReducers = <T extends ApplicationWithUaaConfig>(cfg: T) =>
	withReducerMap<T>({ uaa: UaaReducer })(cfg);

const addUaaProvider = <T extends ApplicationWithUaaConfig>(cfg: T) => {
	const clientConfigure = cfg.uaa?.configuration;

	const UaaProviderWrapper: FC<{ children?: ReactNode }> = ({ children }) => {
		const store = useStore();

		return (
			<UaaProvider store={store} clientConfigure={clientConfigure}>
				{children}
			</UaaProvider>
		);
	};
	UaaProviderWrapper.displayName = "UaaProviderWrapper";

	return addWrapper<T>(UaaProviderWrapper, "outer")(cfg);
};

// ============================================================================
// Main API
// ============================================================================

/**
 * Adds UAA authentication to an A12 composable application.
 *
 * This composable function sets up:
 * - **Authentication state management** - Redux reducer for user, tokens, and auth state
 * - **Middlewares** - Token refresh, session management, auth flow coordination
 * - **Busy indicators** - Shows loading during login (Local, LDAP, OIDC, SAML)
 * - **Reset triggers** - Clears app state on logout
 * - **UaaProvider** - Wraps the application with UaaProvider for UAA client initialization
 *
 * @example
 * ```TypeScript
 * createA12ApplicationSetup(
 *   combineFeatures(
 *     withModel(appModel),
 *     withUaa,
 *   )({ uaa: { configuration: uaaClientConfig } })
 * );
 * ```
 */
export const withUaa = <T extends ApplicationWithUaaConfig>(
	cfg: T
): ApplicationWithConfiguredFeature<T, "uaa"> =>
	setConfigured<T, "uaa">("uaa")(
		combineFeatures(
			addUaaBusyTriggers,
			addUaaResetTriggers,
			addUaaMiddlewares,
			addUaaReducers,
			addUaaProvider
		)(cfg)
	);
