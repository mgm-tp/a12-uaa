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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import tools.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"name",
	"description",
	"policy-refs",
	"repository-refs",
	"policies",
	"scopes"
})
public class Permission extends PolicyAware {

	@JsonProperty("description")
	private String description;

	@JsonDeserialize(as = LinkedHashSet.class)
	@JsonProperty("scopes")
	private Set<String> scopes = new LinkedHashSet<>();

	@JsonProperty("call-parent-scope")
	private Boolean callParentScope = true;

	public String getDescription() {
		return description;
	}

	void setDescription(String description) {
		this.description = description;
	}

	public Set<String> getScopes() {
		return scopes;
	}

	void setScopes(Set<String> scopes) {
		this.scopes = scopes;
	}

	public Boolean getCallParentScope() {
		return callParentScope;
	}

	void setCallParentScope(Boolean callParentScope) {
		this.callParentScope = callParentScope;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(Permission.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
		sb.append("name");
		sb.append('=');
		sb.append(((this.getName() == null) ? "<null>" : this.getName()));
		sb.append(',');
		sb.append("description");
		sb.append('=');
		sb.append(((this.description == null) ? "<null>" : this.description));
		sb.append(',');
		sb.append("policyRefs");
		sb.append('=');
		sb.append(((this.getPolicyRefs() == null) ? "<null>" : this.getPolicyRefs()));
		sb.append(',');
		sb.append("policies");
		sb.append('=');
		sb.append(((this.getPolicies() == null) ? "<null>" : this.getPolicies()));
		sb.append(',');
		sb.append("scopes");
		sb.append('=');
		sb.append(((this.scopes == null) ? "<null>" : this.scopes));
		sb.append(',');
		sb.append("callParentScope");
		sb.append('=');
		sb.append(((this.callParentScope == null) ? "<null>" : this.callParentScope));
		if (sb.charAt((sb.length() - 1)) == ',') {
			sb.setCharAt((sb.length() - 1), ']');
		} else {
			sb.append(']');
		}
		return sb.toString();
	}
}
