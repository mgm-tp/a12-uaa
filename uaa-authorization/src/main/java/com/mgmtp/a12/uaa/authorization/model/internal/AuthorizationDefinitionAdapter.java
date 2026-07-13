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
package com.mgmtp.a12.uaa.authorization.model.internal;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mgmtp.a12.uaa.authorization.exception.InvalidAuthorizationDefinitionException;
import com.mgmtp.a12.uaa.authorization.model.AuthorizationDefinition;

import tools.jackson.databind.annotation.JsonDeserialize;

public class AuthorizationDefinitionAdapter extends AuthorizationDefinition {
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationDefinitionAdapter.class);
	private static String REPOSITORY_POLICIES_FIELD = "repositoryPolicies";
	@JsonMerge
	@JsonProperty("repositoryPolicies")
	@JsonDeserialize(as = LinkedHashSet.class)
	private Set<RepositoryPolicyAdapter> repositoryPoliciesAdapter = new LinkedHashSet<>();

	public Set<RepositoryPolicyAdapter> getRepositoryPoliciesAdapter() {
		return repositoryPoliciesAdapter;
	}

	void setRepositoryPoliciesAdapter(
		Set<RepositoryPolicyAdapter> repositoryPoliciesAdapter) {
		this.repositoryPoliciesAdapter = repositoryPoliciesAdapter;
	}

	public void toRepositoryPolicies() {
		try {
			Field templatesField = this.getClass().getSuperclass().getDeclaredField(REPOSITORY_POLICIES_FIELD);
			templatesField.setAccessible(true);
			templatesField.set(this, repositoryPoliciesAdapter.stream()
				.map(RepositoryPolicyAdapter::toRepositoryPolicy)
				.collect(Collectors.toCollection(LinkedHashSet::new)));
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new InvalidAuthorizationDefinitionException();
		}
	}
}
