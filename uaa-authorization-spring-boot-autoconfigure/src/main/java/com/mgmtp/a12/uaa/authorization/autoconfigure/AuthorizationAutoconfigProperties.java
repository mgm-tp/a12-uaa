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
package com.mgmtp.a12.uaa.authorization.autoconfigure;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import com.mgmtp.a12.uaa.authorization.config.common.EnabledProperty;
import com.mgmtp.a12.uaa.authorization.internal.validation.ClassPathNotFound;
import com.mgmtp.a12.uaa.authorization.model.PolicyType;

@ConfigurationProperties("mgmtp.a12.uaa.authorization")
@Validated
public class AuthorizationAutoconfigProperties {

	@ClassPathNotFound(message = "The Authorization Definition Json file can not be found")
	private String authorizationDefinition;
	private List<String> childAuthorizationDefinitions;
	private EnabledProperty securityOnStartUp = new EnabledProperty(false);
	private PolicyProperties policy = new PolicyProperties();
	private List<String> scanEntityPackages = Arrays.asList("com.mgmtp");

	public String getAuthorizationDefinition() {
		return authorizationDefinition;
	}

	public void setAuthorizationDefinition(String definitionResource) {
		this.authorizationDefinition = definitionResource;
	}

	public List<String> getChildAuthorizationDefinitions() {
		return childAuthorizationDefinitions;
	}

	public void setChildAuthorizationDefinitions(List<String> additionalDefinitionResources) {
		this.childAuthorizationDefinitions = additionalDefinitionResources;
	}

	public EnabledProperty getSecurityOnStartUp() {
		return securityOnStartUp;
	}

	public void setSecurityOnStartUp(EnabledProperty securityOnStartUp) {
		this.securityOnStartUp = securityOnStartUp;
	}

	public PolicyProperties getPolicy() {
		return policy;
	}

	public void setPolicy(PolicyProperties policy) {
		this.policy = policy;
	}

	public List<String> getScanEntityPackages() {
		return scanEntityPackages;
	}

	public void setScanEntityPackages(List<String> entityPckageScan) {
		this.scanEntityPackages = entityPckageScan;
	}

	@Override
	public String toString() {
		return "AuthorizationAutoconfigProperties [authorizationDefinition=" + authorizationDefinition + ", childAuthorizationDefinitions="
			+ childAuthorizationDefinitions + ", securityOnStartUp=" + securityOnStartUp + ", policy=" + policy + ", entityPckageScan=" + scanEntityPackages
			+ "]";
	}

	public static class PolicyProperties {
		@NotNull
		private Collection<PolicyType> types = Arrays.asList(PolicyType.SpEL);

		public Collection<PolicyType> getTypes() {
			return types;
		}

		public void setTypes(Collection<PolicyType> types) {
			this.types = types;
		}

	}

}
