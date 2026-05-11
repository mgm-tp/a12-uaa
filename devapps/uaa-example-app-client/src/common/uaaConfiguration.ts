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
  TokenManagement,
  UaaClientConfiguration,
} from "@com.mgmtp.a12.uaa/uaa-authentication-client";

declare global {
  interface Window {
    uaaConfiguration: {
      idpHost: string;
      idpClientId: string;
      idpRealm: string;
      serverURL: string;
    };
  }
}

export const isUnset = (v: unknown) =>
    v == null ||
    (typeof v === "string" && (v.trim() === "" || /^__.*__$/.test(v.trim())));

const DEFAULTS_CONFIGURATION = {
  idpHost: "http://localhost:9090",
  idpClientId: "UAARealm",
  idpRealm: "uaa-spa-client",
  serverURL: "http://localhost:8080",
};

const cfg = window.uaaConfiguration ?? {};

window.uaaConfiguration = {
  idpHost: isUnset(cfg.idpHost) ? DEFAULTS_CONFIGURATION.idpHost : cfg.idpHost!,
  idpClientId: isUnset(cfg.idpClientId) ? DEFAULTS_CONFIGURATION.idpClientId : cfg.idpClientId!,
  idpRealm: isUnset(cfg.idpRealm) ? DEFAULTS_CONFIGURATION.idpRealm : cfg.idpRealm!,
  serverURL: isUnset(cfg.serverURL) ? DEFAULTS_CONFIGURATION.serverURL : cfg.serverURL!,
};

export const DEVAPPS_CONFIGURATION = {
  serverUrl:
    process.env.npm_config_server_url || window.uaaConfiguration.serverURL,
  idpUrl: process.env.npm_config_idp_url || window.uaaConfiguration.idpHost,
  idpRealm:
    process.env.npm_config_idp_realm || window.uaaConfiguration.idpRealm,
  idpClientId:
    process.env.npm_config_idp_client_id || window.uaaConfiguration.idpClientId,
};

export const tokenConfigure = TokenManagement.getInstance().getTokenConfiguration(
  AuthenticationType.LOCAL,
);

export const uaaClientConfigure: UaaClientConfiguration = {
  serverURL: `${DEVAPPS_CONFIGURATION.serverUrl}`,
  automaticallyLogin: true,
  overrideClientConfigures: {
    saml: {
      logoutIDP: true,
    },
  },
};
