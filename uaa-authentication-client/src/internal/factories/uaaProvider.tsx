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
import React, { useState, useEffect, useContext } from "react";
import { Provider, ReactReduxContext } from "react-redux";
import { Store } from "redux";

import { SessionStorageKeys, UaaClient } from "../../index.js";

import { reduxStore, sessionStorage } from "../utils/index.js";
import { TokenManagement } from "../tokenManagement.js";
import {
	AuthenticationType,
	UaaClientConfiguration
} from "../interfaces/index.js";
import { uaaSaga } from "../sagas/uaaSaga.js";

import {
	LdapClient,
	LocalClient,
	OidcClient,
	SamlClient,
	uaaClient
} from "./index.js";

type ProviderProps = {
	store?: Store<unknown>;
	clientConfigure?: UaaClientConfiguration;
	children?: React.ReactNode;
};

export const UaaProvider: React.FC<ProviderProps> = props => {
	const [fetchingSelfConfigure, setFetchingSelfConfigure] = useState(true);
	const [restoring, setRestoring] = useState(false);

	const { store: providedStore, clientConfigure, children } = props;

	const contextStore = useContext(ReactReduxContext)?.store as
		| Store<unknown>
		| undefined;

	const store = providedStore ?? contextStore;

	if (!store) {
		throw new Error(
			"UaaProvider needs either a store prop or to be rendered inside a <Provider>."
		);
	}

	useEffect(() => {
		UaaClient.setStore(store);
		reduxStore.setStore(store);

		const windowOnLoad = () => {
			const lastAuthenticateType = sessionStorage.getItem(
				SessionStorageKeys.AUTHENTICATION_TYPE
			);
			if (
				SamlClient.getInstance() &&
				lastAuthenticateType === AuthenticationType.SAML
			) {
				samlWindowOnLoad(
					UaaClient.getSamlConfiguration().automaticallyLogin ?? false
				);
			}
			if (
				OidcClient.getInstance() &&
				lastAuthenticateType === AuthenticationType.OAUTH2
			) {
				OidcClient.getInstance().initConnector();
				OidcClient.userManager.processAutomaticallyLogin();
			}
			if (
				lastAuthenticateType === AuthenticationType.LOCAL &&
				UaaClient.getLocalConfiguration().automaticallyLogin
			) {
				setRestoring(true);
				LocalClient.getInstance()?.initConnector();
				LocalClient.getInstance()
					?.restoreAuthenticationState(store.dispatch)
					.finally(() => {
						setRestoring(false);
					});
			}
			if (
				lastAuthenticateType === AuthenticationType.ACTIVE_DIRECTORY_LDAP &&
				UaaClient.getLdapConfiguration().automaticallyLogin
			) {
				setRestoring(true);
				LdapClient.getInstance()?.initConnector();
				LdapClient.getInstance()
					?.restoreAuthenticationState(store.dispatch)
					.finally(() => {
						setRestoring(false);
					});
			}
		};

		const samlWindowOnLoad = (automaticallyLogin: boolean) => {
			const restoreAfterLogin = sessionStorage.getItem("restoreAfterLogin");
			if (restoreAfterLogin || automaticallyLogin) {
				setRestoring(true);
				SamlClient.getInstance()
					?.restoreAuthenticationState(store.dispatch)
					.finally(() => {
						setRestoring(false);
						sessionStorage.removeItem("restoreAfterLogin");
					});
			}
		};

		const initUaaClient = async () => {
			uaaSaga.init();

			if (clientConfigure) {
				setFetchingSelfConfigure(true);
				try {
					await uaaClient.init(clientConfigure);
				} finally {
					setFetchingSelfConfigure(false);
					windowOnLoad();
				}
			} else {
				setFetchingSelfConfigure(false);
				windowOnLoad();
			}
		};

		initUaaClient();

		return () => {
			if (OidcClient.getInstance()) {
				OidcClient.userManager.unRegisterEvent();
			}
			uaaSaga.task?.cancel();
			TokenManagement.getInstance().stopService();
		};
	}, [store, clientConfigure]);

	const needWait = fetchingSelfConfigure || restoring;
	if (needWait) {
		return null;
	}

	return providedStore ? (
		<Provider store={store}>{children}</Provider>
	) : (
		<>{children}</>
	);
};
