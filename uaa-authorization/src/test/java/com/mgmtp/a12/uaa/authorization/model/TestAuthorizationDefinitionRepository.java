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
import java.util.Optional;
import java.util.Set;

import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;

public class TestAuthorizationDefinitionRepository implements AuthorizationDefinitionRepository{

	@Override
	public Set<Permission> getPermissionsByScope(String scopeName) {
		return null;
	}

	@Override
	public Optional<Policy> getPolicyByName(String policyName) {
		return Optional.empty();
	}

	@Override
	public Optional<RepositoryPolicy> getRepositoryPolicyByName(String policyName) {
		return Optional.empty();
	}

	@Override
	public Set<PropertyPermission> getPropertyPermission() {
		return null;
	}

	@Override
	public Set<Rights> getPropertyRightsByNames(Set<String> names) {
		Set<Rights> rights = new LinkedHashSet<>();
		Rights right = new Rights();
		right.setRead(Set.of("R1", "name", "R3"));
		right.setWrite(Set.of("description", "W2", "W3", "primaryAddress"));
		rights.add(right);
		return rights;
	}

	@Override
	public Set<Permission> getPermissions() {
		return null;
	}
}
