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
import { Store } from "redux";

import {
	ConnectorLocator,
	RestServerConnector
} from "@com.mgmtp.a12.utils/utils-connector";
import { LoggerFactory } from "@com.mgmtp.a12.utils/utils-logging";

import {
	AuthenticationType,
	isSelfConfigure,
	SelfConfigure,
	UaaClient as IUaaClient,
	UaaClientConfiguration,
	UaaLdapClient,
	UaaLdapConfiguration,
	UaaLocalClient,
	UaaLocalConfiguration,
	UaaOidcClient,
	UaaOidcConfiguration,
	UaaSamlClient,
	UaaSamlConfiguration
} from "../interfaces/index.js";
import {
	buildGetSelfconfigure,
	fetchServerRequest,
	reduxStore
} from "../utils/index.js";

import {
	configureClientsAndTokens,
	normalizeUrl,
	reformatSelfConfigure,
	resolveCommonConfig,
	validateOidcConfiguration
} from "./uaaClientUtils.js";

import {
	LdapClient,
	ldapClientSetup,
	LocalClient,
	localClientSetup,
	OidcClient,
	oidcClientSetup,
	SamlClient,
	samlClientSetup
} from "./index.js";

class UaaClient implements IUaaClient {
	public uaaClientConfiguration: UaaClientConfiguration | undefined;
	private _selfConfigure: SelfConfigure | undefined;

	public init(clientConfiguration: UaaClientConfiguration): Promise<void> {
		this.uaaClientConfiguration = clientConfiguration;
		if (clientConfiguration.store) {
			reduxStore.setStore(clientConfiguration.store);
		}
		if (
			clientConfiguration?.offlineSelfConfigure &&
			isSelfConfigure(clientConfiguration?.offlineSelfConfigure)
		) {
			this._selfConfigure = clientConfiguration?.offlineSelfConfigure;
			this.setupAllClient();
			configureClientsAndTokens(clientConfiguration, this._selfConfigure);
			return new Promise<void>(resolve => {
				resolve();
			});
		}

		localClientSetup({
			serverURL: clientConfiguration.serverURL,
			...clientConfiguration.overrideClientConfigures?.local
		});
		ldapClientSetup({
			serverURL: clientConfiguration.serverURL,
			...clientConfiguration.overrideClientConfigures?.ldap
		});
		samlClientSetup({
			serverURL: clientConfiguration.serverURL,
			...clientConfiguration.overrideClientConfigures?.saml
		});

		return this.fetchSelfConfigure(
			clientConfiguration.serverSelfConfigureUrl ??
				clientConfiguration.serverURL
		).then(res => {
			if (res) {
				this._selfConfigure = res;
				this.setupAllClient();
				configureClientsAndTokens(clientConfiguration, this._selfConfigure);
			} else {
				if (this.hasOidcConfiguration()) {
					oidcClientSetup(this.getOidcConfiguration());
				}
			}
		});
	}

	public setStore(store: Store): void {
		reduxStore.setStore(store);
	}

	private setupAllClient() {
		localClientSetup(this.getLocalConfiguration());
		ldapClientSetup(this.getLdapConfiguration());
		samlClientSetup(this.getSamlConfiguration());
		if (this.hasOidcConfiguration()) {
			oidcClientSetup(this.getOidcConfiguration());
		}
	}

	private hasOidcConfiguration(): boolean {
		return (
			!!this._selfConfigure?.oidc ||
			!!this.uaaClientConfiguration?.overrideClientConfigures?.oidc
		);
	}

	private initConnector(baseURL?: string | undefined): void {
		if (!this.uaaClientConfiguration) {
			return;
		}
		const {
			serverURL,
			serverConnector,
			additionalRequestFilter,
			additionalResponseFilter
		} = this.uaaClientConfiguration;
		const requestFilters = [...(additionalRequestFilter ?? [])];
		const responseFilters = [...(additionalResponseFilter ?? [])];
		const defaultServerConnector =
			serverConnector ??
			new RestServerConnector(
				baseURL ?? serverURL,
				requestFilters,
				responseFilters
			);
		ConnectorLocator.createInstance(defaultServerConnector);
	}

	/**
	 * @param serverConfigureUrl
	 */
	private fetchSelfConfigure = (
		serverConfigureUrl: string | undefined = undefined
	): Promise<SelfConfigure | undefined> => {
		this.initConnector(serverConfigureUrl);
		LoggerFactory.getLogger("UAA/SelfConfigure").info("Get Self Configure");
		const getConfigureRequest = buildGetSelfconfigure();
		return fetchServerRequest(getConfigureRequest)
			.then(res => res.json())
			.then(res => {
				const configure = reformatSelfConfigure(res);
				if (isSelfConfigure(configure)) {
					return configure;
				}
				LoggerFactory.getLogger("UAA/SelfConfigure").error(
					"Invalid self-configure response"
				);
				return undefined;
			})
			.catch(() => {
				LoggerFactory.getLogger("UAA/SelfConfigure").info(
					"Not use self-configure"
				);
				return undefined;
			});
	};

	public getLocalClient(): UaaLocalClient {
		return LocalClient.getInstance();
	}

	public getLdapClient(): UaaLdapClient {
		return LdapClient.getInstance();
	}

	public getOidcClient(): UaaOidcClient {
		return OidcClient.getInstance();
	}

	public getSamlClient(): UaaSamlClient {
		return SamlClient.getInstance();
	}

	/**
	 * @param type
	 * @param selfConfig
	 * @internal
	 */
	// eslint-disable-next-line complexity
	public getClientConfiguration = (
		type: AuthenticationType,
		selfConfig: SelfConfigure | undefined = this._selfConfigure
	):
		| UaaLocalConfiguration
		| UaaSamlConfiguration
		| UaaOidcConfiguration
		| UaaLdapConfiguration
		| undefined => {
		switch (type) {
			case AuthenticationType.SAML:
				return {
					...resolveCommonConfig(this.uaaClientConfiguration),
					...this._selfConfigure?.saml,
					...this.uaaClientConfiguration?.overrideClientConfigures?.saml
				};
			case AuthenticationType.LOCAL:
				return {
					...resolveCommonConfig(this.uaaClientConfiguration),
					...this._selfConfigure?.local,
					...this.uaaClientConfiguration?.overrideClientConfigures?.local
				};
			case AuthenticationType.ACTIVE_DIRECTORY_LDAP:
				return {
					...resolveCommonConfig(this.uaaClientConfiguration),
					...this._selfConfigure?.activeDirectoryLdap,
					...this.uaaClientConfiguration?.overrideClientConfigures?.ldap
				};
			case AuthenticationType.OAUTH2:
				validateOidcConfiguration(
					selfConfig?.oidc,
					this.uaaClientConfiguration?.overrideClientConfigures?.oidc
				);
				return {
					...resolveCommonConfig(this.uaaClientConfiguration),
					tokenType: selfConfig?.oidc?.tokenType,
					authority: normalizeUrl(
						`${selfConfig?.oidc?.idpBaseUrl}/realms/${selfConfig?.oidc?.realmName}`
					),
					client_id: `${selfConfig?.oidc?.clientId}`,
					redirect_uri: normalizeUrl(
						`${window.location.origin}/${selfConfig?.oidc?.loginRedirectRelativeUrl}`
					),
					post_logout_redirect_uri: normalizeUrl(
						`${window.location.origin}/${selfConfig?.oidc?.logoutRedirectRelativeUrl}`
					),
					silent_redirect_uri: normalizeUrl(
						`${window.location.origin}/${selfConfig?.oidc?.silentRedirectRelativeUrl}`
					),
					logoutIdp: (this.uaaClientConfiguration?.overrideClientConfigures
						?.oidc?.logoutIdp ??
						selfConfig?.oidc?.logoutIdp ??
						true) as boolean,
					enableRefreshTokenGrant:
						selfConfig?.oidc?.enableRefreshTokenGrant ?? false,
					popupAuthentication: selfConfig?.oidc?.popupAuthentication ?? false,
					currentUserUrl: selfConfig?.oidc?.currentUserUrl,
					...this.uaaClientConfiguration?.overrideClientConfigures?.oidc
				};
			default:
				return undefined;
		}
	};

	public getLocalConfiguration() {
		return this.getClientConfiguration(
			AuthenticationType.LOCAL
		) as UaaLocalConfiguration;
	}

	public getLdapConfiguration() {
		return this.getClientConfiguration(
			AuthenticationType.ACTIVE_DIRECTORY_LDAP
		) as UaaLdapConfiguration;
	}

	public getOidcConfiguration() {
		return this.getClientConfiguration(
			AuthenticationType.OAUTH2
		) as UaaOidcConfiguration;
	}

	public getSamlConfiguration() {
		return this.getClientConfiguration(
			AuthenticationType.SAML
		) as UaaSamlConfiguration;
	}
}

export const uaaClient = new UaaClient();
