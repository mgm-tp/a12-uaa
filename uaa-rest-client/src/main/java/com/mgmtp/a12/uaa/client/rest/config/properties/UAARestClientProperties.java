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
package com.mgmtp.a12.uaa.client.rest.config.properties;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import com.mgmtp.a12.uaa.client.rest.config.AuthenticationType;
import com.mgmtp.a12.uaa.client.rest.config.common.UrlProperty;
import com.mgmtp.a12.uaa.client.rest.config.validation.NotNullForAuthenticationType;

/**
 * Provide configuration for JAVA client. 
 */
@Validated
public class UAARestClientProperties {

	@NotNull
	private UrlProperty uaaBase = new UrlProperty("http://localhost:8080");
	private UrlProperty selfconfiguration;
	@NotNull
	private AuthenticationType authenticationType = AuthenticationType.LOCAL;
	@NotNull
	private String authorizationHeaderName = "Authorization";
	@NotNullForAuthenticationType(authenticationTypes = { AuthenticationType.SAML, AuthenticationType.LOCAL, AuthenticationType.ACTIVE_DIRECTORY_LDAP },
		message = "Missing configuration for generated Token Header Name")
	private String generatedTokenHeaderName = "access_token";
	@NotNullForAuthenticationType(authenticationTypes = { AuthenticationType.SAML, AuthenticationType.LOCAL, AuthenticationType.ACTIVE_DIRECTORY_LDAP },
		message = "Missing configuration for generated Token Renew In Seconds Header Name")
	private String generatedTokenRenewInSecondsHeaderName = "token_renew_in_seconds";
	private Resource authorizationDataStore;
	@Valid
	private UAARestClientAuthenticationProperties authenticationConfiguration = new UAARestClientAuthenticationProperties();
	private DelegatedModeProperties delegatedModeConfiguration;
	@NotNullForAuthenticationType(authenticationTypes = { AuthenticationType.API_KEY }, message = "Missing api_key resource")
	private String apiKeyResource;
	private CacheConfiguration cache = new CacheConfiguration();

	public UAARestClientProperties() {
		super();
	}

	public UAARestClientProperties(UrlProperty uaaBase, AuthenticationType authenticationType) {
		this.uaaBase = uaaBase;
		this.authenticationType = authenticationType;
	}

	public UrlProperty getSelfconfiguration() {
		return selfconfiguration;
	}

	public void setSelfconfiguration(UrlProperty selfconfiguration) {
		this.selfconfiguration = selfconfiguration;
	}

	public Resource getAuthorizationDataStore() {
		return authorizationDataStore;
	}

	public void setAuthorizationDataStore(Resource authorizationDataStore) {
		this.authorizationDataStore = authorizationDataStore;
	}

	public AuthenticationType getAuthenticationType() {
		return authenticationType;
	}

	public UrlProperty getUaaBase() {
		return uaaBase;
	}

	public void setUaaBase(UrlProperty uaaBase) {
		this.uaaBase = uaaBase;
	}

	public void setAuthenticationType(AuthenticationType authenticationType) {
		this.authenticationType = authenticationType;
	}

	public UAARestClientAuthenticationProperties getAuthenticationConfiguration() {
		return authenticationConfiguration;
	}

	public void setAuthenticationConfiguration(UAARestClientAuthenticationProperties authenticationConfiguration) {
		this.authenticationConfiguration = authenticationConfiguration;
	}

	public String getAuthorizationHeaderName() {
		return authorizationHeaderName;
	}

	public void setAuthorizationHeaderName(String authorizationHeaderName) {
		this.authorizationHeaderName = authorizationHeaderName;
	}

	public String getGeneratedTokenHeaderName() {
		return generatedTokenHeaderName;
	}

	public void setGeneratedTokenHeaderName(String generatedTokenHeaderName) {
		this.generatedTokenHeaderName = generatedTokenHeaderName;
	}


	public String getGeneratedTokenRenewInSecondsHeaderName() {
		return generatedTokenRenewInSecondsHeaderName;
	}

	public void setGeneratedTokenRenewInSecondsHeaderName(String generatedTokenRenewInSecondsHeaderName) {
		this.generatedTokenRenewInSecondsHeaderName = generatedTokenRenewInSecondsHeaderName;
	}

	public DelegatedModeProperties getDelegatedModeConfiguration() {
		return delegatedModeConfiguration;
	}

	public void setDelegatedModeConfiguration(DelegatedModeProperties delegatedModeConfiguration) {
		this.delegatedModeConfiguration = delegatedModeConfiguration;
	}

	public String getApiKeyResource() {
		return apiKeyResource;
	}

	public void setApiKeyResource(String apiKeyResource) {
		this.apiKeyResource = apiKeyResource;
	}

	public CacheConfiguration getCache() {
		return cache;
	}

	public void setCache(CacheConfiguration cache) {
		this.cache = cache;
	}

	@Override
	public String toString() {
		return "UaaRestClientProperties{" +
			"uaaBase=" + uaaBase +
			", selfconfiguration=" + selfconfiguration +
			", authenticationType=" + authenticationType +
			", authorizationHeaderName='" + authorizationHeaderName + '\'' +
			", generatedTokenHeaderName='" + generatedTokenHeaderName + '\'' +
			", authorizationDataStore=" + authorizationDataStore +
			", authenticationConfiguration=" + authenticationConfiguration +
			", delegatedModeConfiguration=" + delegatedModeConfiguration +
			", apiKeyResource=" + apiKeyResource +
			", cache='" + cache + '\'' +
			'}';
	}

	public static class DelegatedModeProperties {

		public DelegatedModeProperties() {
		}

		public DelegatedModeProperties(String[] excludedContexts) {
			this.excludedContexts = excludedContexts;
		}

		private String[] excludedContexts;

		public String[] getExcludedContexts() {
			return excludedContexts;
		}

		public void setExcludedContexts(String[] excludedContexts) {
			this.excludedContexts = excludedContexts;
		}

		@Override
		public String toString() {
			return "DelegatedModeConfiguration{" +
				"excludedContexts=" + Arrays.toString(excludedContexts) +
				'}';
		}
	}

	public static class CacheConfiguration {
		private boolean enabled = false;
		private List<CacheMappingConfiguration> mapping = new LinkedList<>();

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public List<CacheMappingConfiguration> getMapping() {
			return mapping;
		}

		public void setMapping(List<CacheMappingConfiguration> mapping) {
			this.mapping = mapping;
		}

		@Override
		public String toString() {
			return "CacheConfiguration [enabled=" + enabled + ", mapping=" + mapping + "]";
		}

	}

	public static class CacheMappingConfiguration {

		private String cachePathPattern;
		private String regionPattern;
		private String staticName;

		public String getCachePathPattern() {
			return cachePathPattern;
		}

		public void setCachePathPattern(String cachePathPattern) {
			this.cachePathPattern = cachePathPattern;
		}

		public String getRegionPattern() {
			return regionPattern;
		}

		public void setRegionPattern(String regionPattern) {
			this.regionPattern = regionPattern;
		}

		public String getStaticName() {
			return staticName;
		}

		public void setStaticName(String staticName) {
			this.staticName = staticName;
		}

		@Override
		public String toString() {
			return "CacheMappingConfiguration [cachePathPattern=" + cachePathPattern + ", regionPattern=" + regionPattern + ", staticName=" + staticName + "]";
		}

	}

}
