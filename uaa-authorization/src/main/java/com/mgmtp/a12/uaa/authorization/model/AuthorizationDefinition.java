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
package com.mgmtp.a12.uaa.authorization.model;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import tools.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"name",
	"description",
	"policies",
	"repositoryPolicies",
	"permissions",
	"propertyPermissions",
	"propertyRights"
})
public class AuthorizationDefinition extends NamedElement {

	@JsonProperty("description")
	private String description;

	@JsonMerge
	@JsonProperty("policies")
	@JsonDeserialize(as = LinkedHashSet.class)
	private Set<Policy> policies = new LinkedHashSet<>();

	@JsonMerge
	@JsonProperty("permissions")
	@JsonDeserialize(as = LinkedHashSet.class)
	private Set<Permission> permissions = new LinkedHashSet<>();

	@JsonMerge
	@JsonProperty("repositoryPolicies")
	@JsonDeserialize(as = LinkedHashSet.class)
	private Set<RepositoryPolicy> repositoryPolicies = new LinkedHashSet<>();

	@JsonMerge
	@JsonProperty("propertyPermissions")
	@JsonDeserialize(as = LinkedHashSet.class)
	private Set<PropertyPermission> propertyPermissions = new LinkedHashSet<>();

	@JsonMerge
	@JsonProperty("propertyRights")
	@JsonDeserialize(as = LinkedHashSet.class)
	private Set<PropertyRight> propertyRights = new LinkedHashSet<>();

	public String getDescription() {
		return description;
	}

	@Override
	void setName(String name) {
		if (getName() == null) {
			super.setName(name);
		}
	}

	void setDescription(String description) {
		if (this.description == null) {
			this.description = description;
		}
	}

	public Set<Policy> getPolicies() {
		return policies;
	}

	void setPolicies(Set<Policy> policies) {
		this.policies = policies;
	}

	public Set<Permission> getPermissions() {
		return permissions;
	}

	void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}

	public Set<PropertyPermission> getPropertyPermissions() {
		return propertyPermissions;
	}

	void setPropertyPermissions(Set<PropertyPermission> propertyPermissions) {
		this.propertyPermissions = propertyPermissions;
	}

	public Set<PropertyRight> getPropertyRights() {
		return propertyRights;
	}

	void setPropertyRights(Set<PropertyRight> propertyRights) {
		this.propertyRights = propertyRights;
	}

	public Set<RepositoryPolicy> getRepositoryPolicies() {
		return repositoryPolicies;
	}

	void setRepositoryPolicies(Set<RepositoryPolicy> repositoryPolicies) {
		this.repositoryPolicies = repositoryPolicies;
	}

	@Override
	public String toString() {
		return super.toString() + "AuthorizationDefinition [description=" + description + ", policies=" + policies + ", permissions=" + permissions
			+ ", repositoryPolicies=" + repositoryPolicies + ", propertyPermissions=" + propertyPermissions + ", propertyRights=" + propertyRights + "]";
	}
}
