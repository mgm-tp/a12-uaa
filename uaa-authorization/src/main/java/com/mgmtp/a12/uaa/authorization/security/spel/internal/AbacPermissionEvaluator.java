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
package com.mgmtp.a12.uaa.authorization.security.spel.internal;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.model.Permission;

public class AbacPermissionEvaluator implements PermissionEvaluator {

	private SpelRuleExecutor uaaRuleExecutor;
	private AuthorizationDefinitionRepository authorizationDefinitionRepository;

	public AbacPermissionEvaluator(SpelRuleExecutor uaaRuleExecutor, AuthorizationDefinitionRepository authorizationDefinitionRepository) {
		this.uaaRuleExecutor = uaaRuleExecutor;
		this.authorizationDefinitionRepository = authorizationDefinitionRepository;
	}

	@Override
	public boolean hasPermission(Authentication auth, Object targetDomainObject, Object scope) {
		Set<Permission> permissions = authorizationDefinitionRepository.getPermissionsByScope(ObjectUtils.nullSafeToString(scope));
		Set<Permission> passedPermissions = permissions.stream()
			.filter(permission -> uaaRuleExecutor.executeRules(permission.getPolicies(), null))
			.collect(Collectors.toSet());
		return !CollectionUtils.isEmpty(passedPermissions);
	}

	@Override
	public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
		return false;
	}

}
