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
import { SagaGenerator, select } from "typed-redux-saga";
import type { Action as ReduxAction } from "redux";

import { LoggerFactory } from "@com.mgmtp.a12.utils/utils-logging";
import {
	ConnectorLocator,
	RestServerConnector
} from "@com.mgmtp.a12.utils/utils-connector";

import { authenticationType } from "../selectors.js";
import { AUTH_KEYS } from "../locale/index.js";
import { uaaClient } from "../factories/index.js";
import {
	dispatchServerRequest,
	reduxStore,
	sessionStorage
} from "../utils/index.js";
import * as UaaActions from "../actions.js";
import * as UaaRequest from "../utils/request.js";
import {
	AuthenticationType,
	SessionStorageKeys,
	UaaLocalConfiguration,
	UaaSagaDescriptor,
	UaaSamlConfiguration
} from "../interfaces/index.js";

const logger = LoggerFactory.getLogger("UAA/Saga");

/**
 *
 */
function* userLoggingOutSaga(): SagaGenerator<void> {
	const type = yield* select(authenticationType);
	if (type === AuthenticationType.OAUTH2) {
		return;
	}

	const authType = sessionStorage.getItem(
		SessionStorageKeys.AUTHENTICATION_TYPE
	);
	const configuration = uaaClient.getClientConfiguration(
		authType as AuthenticationType
	) as UaaSamlConfiguration;

	if (AuthenticationType.SAML === authType && configuration.logoutIDP) {
		logoutSamlRedirect(configuration);
	} else {
		logout()
			.then(() => {
				reduxStore.dispatch(UaaActions.loggedOut());
			})
			.catch(error => {
				logger.error("Error", error);
				const { status, statusText } = error;
				const errorCode = AUTH_KEYS.auth.error.logoutfailed;
				reduxStore.dispatch(
					UaaActions.logoutFailed({ errorCode, status, statusText })
				);
			});
	}
}

/**
 *
 */
async function logout(): Promise<{
	method: string;
	res: string;
}> {
	const authType = sessionStorage.getItem(
		SessionStorageKeys.AUTHENTICATION_TYPE
	);
	const configuration = uaaClient.getClientConfiguration(
		authType as AuthenticationType
	) as UaaLocalConfiguration;
	const logoutRequest = UaaRequest.buildLogoutRequest(
		configuration.logoutRelativeUrl,
		configuration.logoutMethod
	);
	return {
		method: configuration.logoutMethod ?? "POST",
		res: await dispatchServerRequest(logoutRequest)
	};
}

/**
 *
 * @param configuration
 */
function logoutSamlRedirect(configuration: UaaSamlConfiguration) {
	const token = sessionStorage.getItem(SessionStorageKeys.ACCESS_TOKEN) || "";
	const formNode = document.createElement("form");
	formNode.method = "POST";
	formNode.style.display = "none";
	formNode.action = "/user/logout";
	document.body.appendChild(formNode);

	const tokenInputNode = document.createElement("input");
	tokenInputNode.id = "authToken";
	tokenInputNode.name = "token";
	tokenInputNode.value = token;
	formNode.appendChild(tokenInputNode);

	const logoutUrl = configuration.logoutRelativeUrl || formNode.action;
	const baseUrl = (
		ConnectorLocator.getInstance().getServerConnector() as RestServerConnector
	).getBaseUrl();
	if (baseUrl.slice(-1) !== "/") {
		formNode.action = encodeURI(`${baseUrl}/${logoutUrl}`);
	} else {
		formNode.action = encodeURI(baseUrl + logoutUrl);
	}
	formNode.requestSubmit();
	reduxStore.dispatch(UaaActions.loggedOut());
}

/**
 *
 */
const commonClientSaga: UaaSagaDescriptor[] = [
	{
		canHandle: (action: ReduxAction) => UaaActions.loggingOut.match(action),
		handle: () => userLoggingOutSaga()
	}
];

export default commonClientSaga;
