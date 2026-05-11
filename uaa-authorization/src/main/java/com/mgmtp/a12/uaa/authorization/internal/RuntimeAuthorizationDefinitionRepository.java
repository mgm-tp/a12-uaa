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
package com.mgmtp.a12.uaa.authorization.internal;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.model.NamedElement;
import com.mgmtp.a12.uaa.authorization.model.Permission;
import com.mgmtp.a12.uaa.authorization.model.Policy;
import com.mgmtp.a12.uaa.authorization.model.PropertyPermission;
import com.mgmtp.a12.uaa.authorization.model.PropertyRight;
import com.mgmtp.a12.uaa.authorization.model.RepositoryPolicy;
import com.mgmtp.a12.uaa.authorization.model.Rights;

@Component
public class RuntimeAuthorizationDefinitionRepository implements AuthorizationDefinitionRepository {

	@Override
	public Set<Permission> getPermissionsByScope(String scopeName) {
		// Get Permission of the Child
		Set<Permission> permissions =
			new LinkedHashSet<>(InMemoryAuthorizationDefinitionDataHolder.getChild().getPermissions())
				.stream().filter(permission -> permission.getScopes().contains(scopeName)).collect(Collectors.toCollection(LinkedHashSet::new));

		// Calculating the Main Permissions
		if (permissions.isEmpty() || permissions.stream().anyMatch(Permission::getCallParentScope)) {
			permissions.addAll(InMemoryAuthorizationDefinitionDataHolder.getParent().getPermissions()
				.stream().filter(permission -> permission.getScopes().contains(scopeName)).collect(Collectors.toCollection(LinkedHashSet::new)));
		}

		return permissions;
	}

	@Override
	public Optional<Policy> getPolicyByName(String policyName) {
		return find(InMemoryAuthorizationDefinitionDataHolder.getParent().getPolicies(), InMemoryAuthorizationDefinitionDataHolder.getChild().getPolicies(),
			policyName);
	}

	@Override
	public Optional<RepositoryPolicy> getRepositoryPolicyByName(String repositoryPolicyName) {
		return find(InMemoryAuthorizationDefinitionDataHolder.getParent().getRepositoryPolicies(),
			InMemoryAuthorizationDefinitionDataHolder.getChild().getRepositoryPolicies(), repositoryPolicyName);
	}

	@Override
	public Set<PropertyPermission> getPropertyPermission() {
		Set<PropertyPermission> result = new LinkedHashSet<>(InMemoryAuthorizationDefinitionDataHolder.getParent().getPropertyPermissions());
		result.addAll(InMemoryAuthorizationDefinitionDataHolder.getChild().getPropertyPermissions());
		return result;
	}

	@Override
	public Set<Rights> getPropertyRightsByNames(Set<String> propertyRightNames) {
		List<PropertyRight> result = new ArrayList<>(InMemoryAuthorizationDefinitionDataHolder.getParent().getPropertyRights());
		result.addAll(InMemoryAuthorizationDefinitionDataHolder.getChild().getPropertyRights());
		return result.stream()
			.filter(propertyRight -> propertyRightNames.contains(propertyRight.getName()))
			.map(PropertyRight::getRights)
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	@Override
	public Set<Permission> getPermissions() {
		Set<Permission> result = new LinkedHashSet<>(InMemoryAuthorizationDefinitionDataHolder.getParent().getPermissions());
		result.addAll(InMemoryAuthorizationDefinitionDataHolder.getChild().getPermissions());
		return result;
	}

	private <T extends NamedElement> Optional<T> find(Set<T> parent, Set<T> child, String name) {
		return parent.stream().filter(element -> Objects.equals(element.getName(), name)).findFirst()
			.or(() -> child.stream().filter(element -> Objects.equals(element.getName(), name)).findFirst());
	}

}
