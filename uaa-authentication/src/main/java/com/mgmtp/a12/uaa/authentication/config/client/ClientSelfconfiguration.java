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
package com.mgmtp.a12.uaa.authentication.config.client;

import java.util.Arrays;
import java.util.List;

/**
 * Note this class must be aligned with ClientSelfconfiguration form REST client.
 *
 */
public class ClientSelfconfiguration {
	private String applicationBaseUrl;
	private String uaaBaseUrl;
	private String authorizationDataStore;
	private String[] excludedDelegatedContexts;
	private BaseAuthenticationConfiguration local;
	private BaseAuthenticationConfiguration activeDirectoryLdap;
	private SamlConfiguration saml;
	private OidcConfiguration oidc;
	private List<TokenConfiguration> tokens;
	private CacheConfiguration cache;

	public String getUaaBaseUrl() {
		return uaaBaseUrl;
	}

	public void setUaaBaseUrl(String uaaBaseUrl) {
		this.uaaBaseUrl = uaaBaseUrl;
	}

	public String getApplicationBaseUrl() {
		return applicationBaseUrl;
	}

	public void setApplicationBaseUrl(String uaaBaseUrl) {
		this.applicationBaseUrl = uaaBaseUrl;
	}

	public String getAuthorizationDataStore() {
		return authorizationDataStore;
	}

	public void setAuthorizationDataStore(String authorizationDataStore) {
		this.authorizationDataStore = authorizationDataStore;
	}

	public String[] getExcludedDelegatedContexts() {
		return excludedDelegatedContexts;
	}

	public void setExcludedDelegatedContexts(String[] excludedDelegatedContexts) {
		this.excludedDelegatedContexts = excludedDelegatedContexts;
	}

	public BaseAuthenticationConfiguration getLocal() {
		return local;
	}

	public void setLocal(BaseAuthenticationConfiguration local) {
		this.local = local;
	}

	public BaseAuthenticationConfiguration getActiveDirectoryLdap() {
		return activeDirectoryLdap;
	}

	public void setActiveDirectoryLdap(BaseAuthenticationConfiguration activeDirectoryLdap) {
		this.activeDirectoryLdap = activeDirectoryLdap;
	}

	public SamlConfiguration getSaml() {
		return saml;
	}

	public void setSaml(SamlConfiguration saml) {
		this.saml = saml;
	}

	public OidcConfiguration getOidc() {
		return oidc;
	}

	public void setOidc(OidcConfiguration oidc) {
		this.oidc = oidc;
	}

	public List<TokenConfiguration> getTokens() {
		return tokens;
	}

	public void setTokens(List<TokenConfiguration> tokens) {
		this.tokens = tokens;
	}

	public CacheConfiguration getCache() {
		return cache;
	}

	public void setCache(CacheConfiguration cache) {
		this.cache = cache;
	}

	@Override
	public String toString() {
		return "ClientSelfconfiguration [applicationBaseUrl=" + applicationBaseUrl + ", uaaBaseUrl=" + uaaBaseUrl + ", authorizationDataStore="
			+ authorizationDataStore + ", excludedDelegatedContexts=" + Arrays.toString(excludedDelegatedContexts) + ", local=" + local
			+ ", activeDirectoryLdap=" + activeDirectoryLdap + ", saml=" + saml + ", oidc=" + oidc + ", tokens=" + tokens + ", cache=" + cache + "]";
	}

}
