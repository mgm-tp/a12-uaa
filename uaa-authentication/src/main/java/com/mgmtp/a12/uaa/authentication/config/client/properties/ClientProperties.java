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
package com.mgmtp.a12.uaa.authentication.config.client.properties;

import java.util.ArrayList;
import java.util.List;

import com.mgmtp.a12.uaa.authentication.config.client.TokenConfiguration;

public class ClientProperties extends BaseProperties {

	private SamlProperties saml = new SamlProperties();
	private OidcProperties oidc = new OidcProperties();
	private List<TokenConfiguration> tokenConfigurations = new ArrayList<>();
	private CacheProperties cache;

	public SamlProperties getSaml() {
		return saml;
	}

	public void setSaml(SamlProperties saml) {
		this.saml = saml;
	}

	public OidcProperties getOidc() {
		return oidc;
	}

	public void setOidc(OidcProperties oidc) {
		this.oidc = oidc;
	}

	public List<TokenConfiguration> getTokenConfigurations() {
		return tokenConfigurations;
	}

	public void setTokenConfigurations(List<TokenConfiguration> tokenConfigurations) {
		this.tokenConfigurations = tokenConfigurations;
	}

	public CacheProperties getCache() {
		return cache;
	}

	public void setCache(CacheProperties cache) {
		this.cache = cache;
	}

	@Override public String toString() {
		return "ClientProperties{" +
			"saml=" + saml +
			", oidc=" + oidc +
			", tokenConfigurations=" + tokenConfigurations +
			", cache=" + cache +
			'}';
	}
}
