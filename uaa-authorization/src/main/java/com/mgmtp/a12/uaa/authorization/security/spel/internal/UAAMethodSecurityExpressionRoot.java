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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import com.mgmtp.a12.uaa.authorization.AuthorizationContext;
import com.mgmtp.a12.uaa.authorization.AuthorizationContextData;
import com.mgmtp.a12.uaa.authorization.AuthorizationContextHolder;
import com.mgmtp.a12.uaa.authorization.RepositoryAuthorizationCallback;
import com.mgmtp.a12.uaa.authorization.model.internal.ResourceWrapper;
import com.mgmtp.a12.uaa.authorization.security.UAAPolicyEnforcementPoint;

public class UAAMethodSecurityExpressionRoot extends UAABaseSpelSecurityExpressionRoot implements MethodSecurityExpressionOperations {

	private Object filterObject;
	private Object returnObject;
	private Method method;
	private Object resourceObject;
	private UAAPolicyEnforcementPoint policyEnforcementPoint;

	public UAAMethodSecurityExpressionRoot(Authentication authentication, UAAPolicyEnforcementPoint policyEnforcementPoint) {
		super(authentication);
		this.policyEnforcementPoint = policyEnforcementPoint;
	}

	public boolean hasUAAPermission(String scope) {
		//implicit resource classname/method name
		return hasUAAPermission(scope, null);
	}

	public boolean hasUAAPermission(String scope, Object resource) {
		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		authorizationContext.pushContext(new AuthorizationContextData(scope, resolveResource(resource), Collections.emptyMap()));
		return policyEnforcementPoint.checkPermissions(resolveResource(resource), scope).isPassed();
	}

	/**
	 * Execute repository permission logic. Generated data are passed into {@link RepositoryAuthorizationCallback}
	 *
	 * @return <code>true</code>
	 */
	public boolean generateRepositoryPermissions() {
		return generateRepositoryPermissions((RepositoryAuthorizationCallback) null);
	}

	public boolean generateRepositoryPermissions(RepositoryAuthorizationCallback callback) {
		AuthorizationContextData authorizationContextData = AuthorizationContextHolder.getContext().getCurrentContext();
		if (authorizationContextData != null) {
			generateRepositoryPermissions(authorizationContextData.getCurrentScope(), authorizationContextData.getCurrentResource(), callback);
		}
		return true;

	}

	public boolean generateRepositoryPermissions(String scope) {
		return generateRepositoryPermissions(scope, null, null);
	}

	public boolean generateRepositoryPermissions(String scope, Object resource, RepositoryAuthorizationCallback callback) {
		Set<String> repositoryPermissions = policyEnforcementPoint.generateRepositoryPermissions(resolveResource(resource), scope);
		Optional.ofNullable(callback).ifPresent((c) -> c.filtersGenerated(repositoryPermissions));
		return true;
	}

	/**
	 * Check property permission on a resource object and mask the resource properties.
	 * <p>
	 * NOTE: Always returns true since it makes no sense to reject authorization. This is done by masking properties
	 *
	 */
	public boolean hasUAAPropertyPermission(Object resource) {
		policyEnforcementPoint.checkPropertyPermissionsAndMaskData(resource, resolvePrincipal());
		return true;
	}

	/**
	 * Check property permission on a resource object and detect changes between persisted and updated resource
	 *
	 * @return `false` when changes has been made to property with no access right 
	 *
	 */
	public boolean hasUAAPropertyPermission(Object persistedResource, Object updatedResource) {
		return policyEnforcementPoint.checkPropertyPermissionForChanges(resolveResource(persistedResource), resolveResource(updatedResource),
			resolvePrincipal());
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Object getResourceObject() {
		return resourceObject;
	}

	public void setResourceObject(Object resourceObject) {
		this.resourceObject = resourceObject;
	}

	@Override
	public Object getFilterObject() {
		return this.filterObject;
	}

	@Override
	public Object getReturnObject() {
		return this.returnObject;
	}

	@Override
	public Object getThis() {
		return this;
	}

	@Override
	public void setFilterObject(Object obj) {
		this.filterObject = obj;
	}

	@Override
	public void setReturnObject(Object obj) {
		this.returnObject = obj;
	}

	private Object resolveResource(Object resource) {
		if (resource instanceof ResourceWrapper wrapper) {
			return wrapper.getResource();
		}

		return resource;
	}
}
