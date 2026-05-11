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
package com.mgmtp.a12.uaa.authorization.security;

import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.exception.MissingPermissionException;
import com.mgmtp.a12.uaa.authorization.model.Permission;
import com.mgmtp.a12.uaa.authorization.model.PropertyPermission;
import com.mgmtp.a12.uaa.authorization.security.spel.internal.ObjectResolver;

public class UAAPolicyEnforcementPoint {
	private static final Logger LOGGER = LoggerFactory.getLogger(UAAPolicyEnforcementPoint.class);
	private PolicyDecisionPoint policyDecisionPoint;
	private AuthorizationDefinitionRepository authorizationDefinitionRepository;

	public UAAPolicyEnforcementPoint(PolicyDecisionPoint policyDecisionPoint, AuthorizationDefinitionRepository authorizationDefinitionRepository) {
		this.policyDecisionPoint = policyDecisionPoint;
		this.authorizationDefinitionRepository = authorizationDefinitionRepository;
	}

	public PermissionCheckResult<Permission> checkPermissions(Object resource, String scopeName) {
		LOGGER.debug("UAA_Authorization_Execution: Start Resource Authorization with scope {}", scopeName);
		Set<Permission> permissions = getPermissions(scopeName);
		PermissionCheckResult<Permission> permissionCheckResult = policyDecisionPoint.hasPermission(resource, permissions);
		LOGGER.debug("UAA_Authorization_Execution: End Resource Authorization with scope {}", scopeName);
		return permissionCheckResult;
	}

	public Set<String> generateRepositoryPermissions(Object resource, String scopeName) {
		LOGGER.debug("UAA_Authorization_Execution: Start Repository Authorization with scope {}", scopeName);
		Set<Permission> permissions = getPermissions(scopeName);
		Set<String> repositoryTemplateGenerated = policyDecisionPoint.evaluateRepositoryPermissions(resource, permissions);
		LOGGER.debug("UAA_Authorization_Execution: End Repository Authorization with scope {}", scopeName);
		return repositoryTemplateGenerated;
	}

	public PermissionCheckResult<PropertyPermission> checkPropertyPermissionsAndMaskData(Object resource, UserDetails principal) {
		LOGGER.debug("UAA_Authorization_Execution: Start Property Authorization and Mask Data");
		Object plainResource = ObjectResolver.resolveDataObject(resource);
		Set<PropertyPermission> propertyPermissions = authorizationDefinitionRepository.getPropertyPermission();
		PermissionCheckResult<PropertyPermission> propertyPermissionsAndMaskData =
			policyDecisionPoint.checkPropertyPermissionsAndMaskData(plainResource, principal, propertyPermissions);
		LOGGER.debug("UAA_Authorization_Execution: End Property Authorization and Mask Data");
		return propertyPermissionsAndMaskData;
	}

	public Boolean checkPropertyPermissionForChanges(Object persistedResource, Object updatedResource, UserDetails principal) {
		Assert.notNull(updatedResource, "Updated resource must be specified");

		LOGGER.debug("UAA_Authorization_Execution: Start Property Authorization and checking changes");
		Object plainPersistedResource = Optional.ofNullable(persistedResource)
			.map(ObjectResolver::resolveDataObject)
			.orElse(null);
		Object plainUpdatedResource = ObjectResolver.resolveDataObject(updatedResource);
		Set<PropertyPermission> propertyPermissions = authorizationDefinitionRepository.getPropertyPermission();

		Boolean propertyPermissionsForChanges =
			policyDecisionPoint.checkPropertyPermissionsForChanges(plainPersistedResource, plainUpdatedResource, principal, propertyPermissions);
		LOGGER.debug("UAA_Authorization_Execution: End Property Authorization and checking changes");
		return propertyPermissionsForChanges;
	}

	private Set<Permission> getPermissions(String scopeName) {
		Set<Permission> permissions = authorizationDefinitionRepository.getPermissionsByScope(scopeName);
		if (permissions.isEmpty()) {
			throw new MissingPermissionException(scopeName);
		}
		return permissions;
	}

}
