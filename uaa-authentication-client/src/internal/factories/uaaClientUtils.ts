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
	AuthenticationType,
	OidcSelfConfigure,
	SelfConfigure,
	SessionStorageKeys,
	UaaClientConfiguration,
	UaaOidcConfiguration
} from "../interfaces/index.js";
import { sessionStorage } from "../utils/index.js";
import { TokenManagement } from "../tokenManagement.js";

import { SamlClient } from "./saml.js";

/**
 *
 * @param configure
 */
export function reformatSelfConfigure(
	configure: Record<string, unknown>
): SelfConfigure {
	const {
		local,
		activeDirectoryLdap,
		oidc,
		saml,
		tokens,
		applicationBaseUrl,
		uaaBaseUrl
	} = configure;
	let overrideConfig = {};
	if (local) {
		overrideConfig = {
			...overrideConfig,
			local
		};
	}
	if (activeDirectoryLdap) {
		overrideConfig = {
			...overrideConfig,
			activeDirectoryLdap
		};
	}
	if (saml) {
		const { ssoConfiguration, ...res } = saml as Record<string, unknown>;
		overrideConfig = {
			...overrideConfig,
			saml: res
		};
	}
	if (oidc) {
		const { publicClient, tokenType, logoutIdp } = oidc as Record<
			string,
			unknown
		>;
		const {
			ssoConfiguration,
			tokenExchangeRelativeUrl,
			loginRelativeUrl,
			logoutRelativeUrl,
			...res
		} = publicClient as Record<string, unknown>;
		overrideConfig = {
			...overrideConfig,
			oidc: {
				tokenType,
				logoutIdp,
				...res
			}
		};
	}

	return {
		applicationBaseUrl,
		uaaBaseUrl,
		tokens,
		...overrideConfig
	} as SelfConfigure;
}

/**
 *
 * @param config
 * @param type
 */
export function resolveCommonConfig(
	config: UaaClientConfiguration | undefined,
	type?: AuthenticationType
) {
	return {
		serverURL: config?.serverURL,
		serverConnector: config?.serverConnector,
		additionalRequestFilter: config?.additionalRequestFilter,
		additionalResponseFilter: config?.additionalResponseFilter,
		automaticallyLogin:
			config?.automaticallyLogin ?? type === AuthenticationType.OAUTH2
	};
}

/**
 * @param parameter
 */
export function getUrlParameterValue(parameter: string) {
	const currentUrl = window.location.href;
	const url = new URL(currentUrl);
	return url.searchParams.get(parameter);
}

/**
 *
 * @param configuration
 * @param selfConfigure
 */
export function configureClientsAndTokens(
	configuration: UaaClientConfiguration,
	selfConfigure: SelfConfigure
) {
	sessionStorage.setItem(
		SessionStorageKeys.SELF_CONFIGURE,
		JSON.stringify(selfConfigure)
	);
	TokenManagement.getInstance().tokenConfigurations =
		configuration.overrideClientConfigures?.tokens ??
		configuration.offlineSelfConfigure?.tokens ??
		selfConfigure.tokens;
	if (getUrlParameterValue("exchangeAuthorizationCodeToToken")) {
		SamlClient.exchangeAuthorizationCodeToToken();
	}
}

/**
 *
 * @param url
 */
export function normalizeUrl(url: string): string {
	return url.replace(/([^:]\/)\/+/g, "$1");
}

/**
 *
 * @param oidcSelfConfig
 * @param oidcConfiguration
 */
export function validateOidcConfiguration(
	oidcSelfConfig: OidcSelfConfigure | undefined,
	oidcConfiguration: UaaOidcConfiguration | undefined
) {
	/**
	 *
	 */
	function validate(): boolean {
		if (oidcSelfConfig === undefined && oidcConfiguration === undefined) {
			return false;
		}

		if (oidcSelfConfig === undefined) {
			return (
				oidcConfiguration?.authority !== undefined &&
				oidcConfiguration?.redirect_uri !== undefined &&
				oidcConfiguration?.client_id !== undefined
			);
		}
		return true;
	}

	if (!validate()) {
		throw new Error(
			"Please declare all required configurations of OIDC: authority, redirect_uri, client_id"
		);
	}
}
