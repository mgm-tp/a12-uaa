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
/* eslint-disable camelcase */
import {
	InMemoryWebStorage,
	Log,
	Logger,
	SigninPopupArgs,
	SigninRedirectArgs,
	User as OidcUser,
	UserManager,
	UserManagerSettings,
	WebStorageStateStore
} from "oidc-client-ts";

import { LoggerFactory } from "@com.mgmtp.a12.utils/utils-logging";

import { AUTH_KEYS } from "../../locale/index.js";
import { reduxStore, sessionStorage } from "../../utils/index.js";
import * as UaaActions from "../../actions.js";
import * as UaaSelectors from "../../selectors.js";
import {
	AuthenticationState,
	AuthenticationType,
	SessionStorageKeys,
	UaaOidcConfiguration,
	UaaOidcUser,
	UaaUserManager
} from "../../interfaces/index.js";

// event callback when the user has been loaded (on silent renew or redirect)
/**
 * @param user
 */
function onUserLoaded(user: OidcUser) {
	user.refresh_token = "";
	reduxStore.dispatch(UaaActions.oidc_userFound(user));
}

// event callback when silent renew error
/**
 * @param error
 */
function onSilentRenewError(error: Error) {
	reduxStore.dispatch(UaaActions.oidc_silentRenewError({ error }));
}

// event callback when the access token expired
/**
 *
 */
function onAccessTokenExpired() {
	reduxStore.dispatch(UaaActions.oidc_user_expired());
}

// event callback when the user is logged out
/**
 *
 */
function onUserUnloaded() {
	reduxStore.dispatch(UaaActions.oidc_session_terminated());
}

// event callback when the user is expiring
/**
 *
 */
function onAccessTokenExpiring() {
	reduxStore.dispatch(UaaActions.oidc_user_expiring());
}

// event callback when the user is signed out
/**
 *
 */
function onUserSignedOut() {
	reduxStore.dispatch(UaaActions.oidc_user_signed_out());
}

/**
 * @param userManager
 */
function registerEventHelper(userManager: UserManager) {
	userManager.events.addUserLoaded(onUserLoaded);
	userManager.events.addSilentRenewError(onSilentRenewError);
	userManager.events.addAccessTokenExpired(onAccessTokenExpired);
	userManager.events.addAccessTokenExpiring(onAccessTokenExpiring);
	userManager.events.addUserUnloaded(onUserUnloaded);
	userManager.events.addUserSignedOut(onUserSignedOut);
}

/**
 * @param userManager
 */
function unRegisterEventHelper(userManager: UserManager) {
	userManager.events.removeUserLoaded(onUserLoaded);
	userManager.events.removeSilentRenewError(onSilentRenewError);
	userManager.events.removeAccessTokenExpired(onAccessTokenExpired);
	userManager.events.removeAccessTokenExpiring(onAccessTokenExpiring);
	userManager.events.removeUserUnloaded(onUserUnloaded);
	userManager.events.removeUserSignedOut(onUserSignedOut);
}

/**
 * @param uaaOidcConfigurationInput
 */
export function createOidcUserManager(
	uaaOidcConfigurationInput: UaaOidcConfiguration
): OidcUserManager {
	const {
		authority,
		client_id,
		redirect_uri,
		post_logout_redirect_uri,
		silent_redirect_uri,
		loadUserInfo,
		automaticallyLogin,
		logLevel,
		scope,
		revokeTokensOnSignout,
		monitorSession,
		popupAuthentication,
		fetchRequestCredentialsToOIDCProvider,
		enableRefreshTokenGrant,
		tokenRenewThresholdInSeconds,
		currentUserUrl
	} = uaaOidcConfigurationInput;

	let isAutoLoggedIn: boolean = false;
	// oidc-client setting
	const internalUserManagerSetting: UserManagerSettings = {
		authority: authority ?? "",
		client_id: client_id ?? "",
		redirect_uri: redirect_uri ?? "",
		post_logout_redirect_uri,
		checkSessionIntervalInSeconds: 1,
		response_type: "code",
		automaticSilentRenew: true,
		validateSubOnSilentRenew: true,
		silent_redirect_uri,
		loadUserInfo: loadUserInfo ?? true,
		filterProtocolClaims: true,
		scope: scope ?? "openid",
		includeIdTokenInSilentRenew: true,
		response_mode: "query",
		revokeTokensOnSignout: revokeTokensOnSignout ?? false,
		revokeTokenTypes: ["access_token"],
		monitorSession: monitorSession ?? false,
		fetchRequestCredentials: fetchRequestCredentialsToOIDCProvider
			? fetchRequestCredentialsToOIDCProvider
			: "same-origin",
		accessTokenExpiringNotificationTimeInSeconds: tokenRenewThresholdInSeconds,
		...(enableRefreshTokenGrant && {
			userStore: new WebStorageStateStore({ store: new InMemoryWebStorage() })
		})
	};
	//redux-oidc
	const uaaInternalUserManager = new UserManager(internalUserManagerSetting);
	uaaInternalUserManager.events.addUserLoaded((user: OidcUser) => {
		const userWithoutRefreshToken = user;
		if (!enableRefreshTokenGrant) {
			userWithoutRefreshToken.refresh_token = "";
		}
		uaaInternalUserManager.storeUser(userWithoutRefreshToken).then(() => {
			LoggerFactory.getLogger("UAA/OIDC/storeUser").info("Store user");
			uaaInternalUserManager.clearStaleState();
		});
	});
	const parseError = (err: Error): { status?: number; statusText?: string } => {
		try {
			const patternError = /([\w\s]+)\((\d+)\)/;
			const result = patternError.exec(err.message);
			if (result && result.length === 3) {
				return {
					status: parseInt(result[2]),
					statusText: result[1]
				};
			}
			if (err.message.match("Invalid response Content-Type")) {
				return {
					status: 500,
					statusText: "Internal Server Error"
				};
			}

			if (err.message.match("Network Error")) {
				return {
					statusText: "Network Error"
				};
			}
		} catch (e) {
			Logger.error(`parseError has error :${e}`);
		}
		return {};
	};
	// TODO: make processAutomaticallyLogin run independence with React.
	const processAutomaticallyLogin = async (
		signinArgs?: SigninRedirectArgs
	): Promise<void> => {
		if (automaticallyLogin && !isAutoLoggedIn) {
			isAutoLoggedIn = true;
			LoggerFactory.getLogger("UAA/OIDC/restore").info("processing");
			reduxStore.dispatch(
				UaaActions.restoreProcessing({
					authenticationType: AuthenticationType.OAUTH2
				})
			);
			if (enableRefreshTokenGrant) {
				await uaaInternalUserManager
					.signinRedirect(signinArgs)
					.then(() => uaaInternalUserManager.getUser())
					.then(user => {
						if (user) {
							user.refresh_token = "";
						}
						successLoginProcess(user, "restore");
					})
					.catch(error => {
						failLoginProcess(error, "restore");
					});
			} else {
				await uaaInternalUserManager
					.signinSilent()
					.then(user => {
						successLoginProcess(user, "restore");
					})
					.catch(error => {
						failLoginProcess(error, "restore");
					});
			}
		}
	};

	const signIn = async (signinArgs?: SigninRedirectArgs | SigninPopupArgs) => {
		const authenticationType = sessionStorage.getItem(
			SessionStorageKeys.AUTHENTICATION_TYPE
		);
		const needRunAutoLoginFirst =
			!isAutoLoggedIn && authenticationType === AuthenticationType.OAUTH2;
		if (automaticallyLogin && needRunAutoLoginFirst) {
			await processAutomaticallyLogin(signinArgs);
			const state = UaaSelectors.state(
				reduxStore.getStore()?.getState() as UaaSelectors.UaaSlice
			);
			if (state !== AuthenticationState.AUTHENTICATED) {
				signInIdpProcess(signinArgs);
			}
		} else {
			signInIdpProcess(signinArgs);
		}
	};

	const signInIdpProcess = (
		signinArgs?: SigninRedirectArgs | SigninPopupArgs
	) => {
		if (popupAuthentication) {
			uaaInternalUserManager
				.signinPopup(signinArgs)
				.then(user => {
					successLoginProcess(user, "login");
				})
				.catch(err => {
					failLoginProcess(err, "login");
				});
		} else {
			uaaInternalUserManager.signinRedirect(signinArgs).catch(err => {
				LoggerFactory.getLogger("UAA/OIDC/signInIdpProcess").error(err);
				failLoginProcess(err, "login");
			});
		}
	};

	const successLoginProcess = (
		user: UaaOidcUser | null,
		successLoginType: "login" | "restore"
	) => {
		if (user) {
			const uaaOidcUser = user as UaaOidcUser;
			uaaOidcUser.currentUserUrl = currentUserUrl;
			if (successLoginType === "restore") {
				LoggerFactory.getLogger("UAA/OIDC/restore").info("success");
				reduxStore.dispatch(
					UaaActions.restoreSuccess({
						authenticationType: AuthenticationType.OAUTH2
					})
				);
			}
			reduxStore.dispatch(
				UaaActions.loggedIn({
					user: uaaOidcUser,
					type: AuthenticationType.OAUTH2
				})
			);
		} else {
			Promise.reject(new Error("User not found"));
		}
	};
	const failLoginProcess = (
		error: Error,
		failLoginType: "login" | "restore"
	) => {
		if (failLoginType === "restore") {
			LoggerFactory.getLogger("UAA/OIDC/restore").error(error);
			reduxStore.dispatch(
				UaaActions.restoreFailed({
					authenticationType: AuthenticationType.OAUTH2,
					error
				})
			);
			reduxStore.dispatch(UaaActions.loggedOut());
		}
		if (failLoginType === "login") {
			const errorCode = AUTH_KEYS.auth.error.authenticationfailed;
			reduxStore.dispatch(
				UaaActions.loginFailed({ errorCode, ...parseError(error) })
			);
		}
	};
	const signOutIdpProcess = () => {
		if (popupAuthentication) {
			uaaInternalUserManager
				.signoutPopup({
					popupWindowTarget: "_blank",
					popupWindowFeatures: {
						left: 0,
						top: 0,
						width: 100,
						height: 100
					}
				})
				.then(() => {
					reduxStore.dispatch(UaaActions.loggedOut());
				})
				.catch(err => {
					failSignOutProcess(err);
				});
		} else {
			uaaInternalUserManager.signoutRedirect().catch(err => {
				LoggerFactory.getLogger("UAA/OIDC/signOutIdpProcess").error(err);
				failSignOutProcess(err);
			});
		}
	};

	const failSignOutProcess = (err: Error) => {
		const errorCode = AUTH_KEYS.auth.error.logoutfailed;
		reduxStore.dispatch(
			UaaActions.logoutFailed({ errorCode, ...parseError(err) })
		);
	};

	const signOut = () => {
		if (
			uaaOidcConfigurationInput === undefined ||
			uaaOidcConfigurationInput.logoutIdp === undefined ||
			uaaOidcConfigurationInput.logoutIdp
		) {
			reduxStore.dispatch(UaaActions.logoutIdp());
			signOutIdpProcess();
		} else {
			reduxStore.dispatch(UaaActions.loggedOut());
		}
	};

	const logLevelMap = {
		debug: Log.DEBUG,
		error: Log.ERROR,
		info: Log.INFO,
		none: Log.NONE,
		warn: Log.WARN
	};

	Log.setLevel(logLevelMap[logLevel || "info"]);

	const registerEvent = () => {
		registerEventHelper(uaaInternalUserManager);
	};

	const unRegisterEvent = () => {
		unRegisterEventHelper(uaaInternalUserManager);
	};

	return {
		signIn,
		signOut,
		uaaInternalUserManager,
		registerEvent,
		unRegisterEvent,
		processAutomaticallyLogin
	};
}

export interface OidcUserManager extends UaaUserManager {
	uaaInternalUserManager: UserManager;
	processAutomaticallyLogin: VoidFunction;
}
