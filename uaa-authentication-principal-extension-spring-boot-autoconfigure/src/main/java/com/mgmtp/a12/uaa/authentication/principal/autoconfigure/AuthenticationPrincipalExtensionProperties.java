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
package com.mgmtp.a12.uaa.authentication.principal.autoconfigure;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.internal.validation.NotNullForAuthenticationType;

@ConfigurationProperties("mgmtp.a12.uaa.authentication.principal")
@Validated
public class AuthenticationPrincipalExtensionProperties {

	private Oauth2Config oauth2Config = new Oauth2Config();
	private LdapConfig ldapConfig = new LdapConfig();
	@NotNullForAuthenticationType(authenticationTypes = AuthenticationType.LOCAL, message = "Provide a principal extension configuration for LOCAL type")
	private LocalConfig localConfig;
	private SamlConfig samlConfig = new SamlConfig();
	private List<String> additionalProperties = Collections.emptyList();
	private Resource accessRightsResource;
	private CertificateConfig certificateConfig = new CertificateConfig();
	private APIKeyConfig apiKeyConfig = new APIKeyConfig();

	public Oauth2Config getOauth2Config() {
		return oauth2Config;
	}

	public void setOauth2Config(Oauth2Config claimsConfig) {
		this.oauth2Config = claimsConfig;
	}

	public LdapConfig getLdapConfig() {
		return ldapConfig;
	}

	public void setLdapConfig(LdapConfig ldapConfig) {
		this.ldapConfig = ldapConfig;
	}

	public LocalConfig getLocalConfig() {
		return localConfig;
	}

	public void setLocalConfig(LocalConfig localConfig) {
		this.localConfig = localConfig;
	}

	public List<String> getAdditionalProperties() {
		return additionalProperties;
	}

	public void setAdditionalProperties(List<String> additionalProperties) {
		this.additionalProperties = additionalProperties;
	}

	public Resource getAccessRightsResource() {
		return accessRightsResource;
	}

	public void setAccessRightsResource(Resource accessRightsResource) {
		this.accessRightsResource = accessRightsResource;
	}

	public SamlConfig getSamlConfig() {
		return samlConfig;
	}

	public void setSamlConfig(SamlConfig samlConfig) {
		this.samlConfig = samlConfig;
	}

	public CertificateConfig getCertificateConfig() {
		return certificateConfig;
	}

	public void setCertificateConfig(CertificateConfig certificateConfig) {
		this.certificateConfig = certificateConfig;
	}

	public APIKeyConfig getApiKeyConfig() {
		return apiKeyConfig;
	}

	public void setApiKeyConfig(APIKeyConfig apiKeyConfig) {
		this.apiKeyConfig = apiKeyConfig;
	}

	public static class LdapConfig {

		private boolean loadLocalUser = true;
		private boolean ignoreMissingRoles = true;

		public boolean isLoadLocalUser() {
			return loadLocalUser;
		}

		public void setLoadLocalUser(boolean loadLocalUser) {
			this.loadLocalUser = loadLocalUser;
		}

		public boolean isIgnoreMissingRoles() {
			return ignoreMissingRoles;
		}

		public void setIgnoreMissingRoles(boolean ignoreMissingRoles) {
			this.ignoreMissingRoles = ignoreMissingRoles;
		}

	}

	public static class Oauth2Config {

		private String userName = "preferred_username";
		private String realmAccessMap = "realm_access";
		private String roles = "roles";
		private RoleMapping roleMappingFromToken;

		public String getRealmAccessMap() {
			return realmAccessMap;
		}

		public void setRealmAccessMap(String realmAccessMap) {
			this.realmAccessMap = realmAccessMap;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public String getRoles() {
			return roles;
		}

		public void setRoles(String roles) {
			this.roles = roles;
		}

		public RoleMapping getRoleMappingFromToken() {
			return roleMappingFromToken;
		}

		public void setRoleMappingFromToken(RoleMapping roleMappingFromToken) {
			this.roleMappingFromToken = roleMappingFromToken;
		}
	}

	public static class CertificateConfig extends KeyConfig{
	}

	public static class APIKeyConfig extends KeyConfig{
	}

	public static class KeyConfig {

		private String usernameField = "CN";
		private String userRoleField = "1.2.276.128";

		public String getUsernameField() {
			return usernameField;
		}

		public void setUsernameField(String username) {
			this.usernameField = username;
		}

		public String getUserRoleField() {
			return userRoleField;
		}

		public void setUserRoleField(String userRole) {
			this.userRoleField = userRole;
		}

	}

	public static class RoleMapping {

		private String fieldName;

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

	}

	public static class LocalConfig {

		@NotNullForAuthenticationType(authenticationTypes = AuthenticationType.LOCAL, message = "Missing configuration for user resources")
		private Resource[] userResources;

		public Resource[] getUserResources() {
			return userResources;
		}

		public void setUserResources(Resource[] userResources) {
			this.userResources = userResources;
		}

	}

	public static class SamlConfig {

		private String assertionUserProperty = "UserID";

		public String getAssertionUserProperty() {
			return assertionUserProperty;
		}

		public void setAssertionUserProperty(String assertionUserProperty) {
			this.assertionUserProperty = assertionUserProperty;
		}

	}

}
