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
package com.mgmtp.a12.uaa.authorization;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.uaa.authorization.model.Permission;
import com.mgmtp.a12.uaa.authorization.model.PropertyPermission;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyRightsValidator;
import com.mgmtp.a12.uaa.authorization.security.DataMasking;
import com.mgmtp.a12.uaa.authorization.security.PermissionCheckResult;
import com.mgmtp.a12.uaa.authorization.security.PolicyDecisionPoint;
import com.mgmtp.a12.uaa.authorization.security.PolicyProcessorFactory;
import com.mgmtp.a12.uaa.authorization.security.PropertyChangesChecker;
import com.mgmtp.a12.uaa.authorization.security.UAAPolicyEnforcementPoint;
import com.mgmtp.a12.uaa.authorization.security.spel.internal.UAABaseSpelSecurityExpressionRoot;
import com.mgmtp.a12.uaa.authorization.security.spel.internal.UAAMethodSecurityExpressionHandler;
import com.mgmtp.a12.uaa.authorization.security.spel.internal.UAAPolicyDecisionPoint;

/**
 * Allow to call permission from JAVA code instead of Annotation.
 * <p>
 * NOTE: Keep in mind that no default resource ([Class name].[method name]) is
 * created.
 *
 */
@Component
public class AuthorizationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationService.class);

	private AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();
	@Inject
	private Optional<List<PropertyAccessor>> propertyAccessors;
	@Inject
	private ApplicationContext applicationContext;
	@Inject
	private AuthorizationDefinitionRepository authorizationDefinitionRepository;
	@Inject
	private List<PolicyProcessorFactory> policyProcessorFactories;
	@Inject
	private PropertyRightsValidator propertyPermissionValidator;
	@Inject
	private DataMasking dataMasking;
	@Inject
	private PropertyChangesChecker propertyChangesChecker;
	@Inject
	private Optional<UAAMethodSecurityExpressionHandler.FilterTargetAdapter> filterTargetAdapter;

	public PermissionCheckResult<Permission> checkPermissions(Object resource, String scopeName) {
		return checkPermissions(resource, scopeName, null);
	}

	public PermissionCheckResult<Permission> checkPermissions(Object resource, String scopeName, Map<String, Object> variables) {
		return executeInContext(scopeName, resource, variables, () -> createEnforcementPoint(variables).checkPermissions(resource, scopeName));
	}

	public PermissionCheckResult<PropertyPermission> checkPropertyPermissionsAndMaskData(Object resource) {
		return checkPropertyPermissionsAndMaskData(resource, null);
	}

	public PermissionCheckResult<PropertyPermission> checkPropertyPermissionsAndMaskData(Object resource, Map<String, Object> variables) {

		return executeInContext("PROPERTY_PERMISSION", resource, Collections.emptyMap(),
			() -> {
				PermissionCheckResult<PropertyPermission> result = createEnforcementPoint(variables)
					.checkPropertyPermissionsAndMaskData(resource, resolvePrincipal());
				AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
				result.setImmutableResourceAfterMasking(authorizationContext.getMaskedReturnObjectAndClear());
				return result;
			});
	}

	public Boolean checkPropertyPermissionsForChanges(Object persistedResource, Object updatedResource) {
		return checkPropertyPermissionsForChanges(persistedResource, updatedResource, null);
	}

	public Boolean checkPropertyPermissionsForChanges(Object persistedResource, Object updatedResource, Map<String, Object> variables) {
		return executeInContext("PROPERTY_PERMISSION", persistedResource, Collections.emptyMap(), () -> {
			UAAPolicyEnforcementPoint policyEnforcementPoint = createEnforcementPoint(variables);
			return policyEnforcementPoint.checkPropertyPermissionForChanges(persistedResource, updatedResource, resolvePrincipal());
		});
	}

	private <T> T executeInContext(String scope, Object resource, Map<String, Object> variables, Supplier<T> function) {
		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		authorizationContext.pushContext(new AuthorizationContextData(scope, resource, variables));
		try {
			return function.get();
		} finally {
			authorizationContext.popContext();
		}
	}

	private UserDetails resolvePrincipal() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Object principal = authentication.getPrincipal();

		if (principal instanceof UserDetails details) {
			return details;
		}
		return null;
	}

	public Set<String> generateRepositoryPermissions() {
		AuthorizationContextData authorizationContextData = AuthorizationContextHolder.getContext().getCurrentContext();
		if (authorizationContextData == null) {
			return Collections.emptySet();
		}
		return generateRepositoryPermissions(authorizationContextData.getCurrentResource(), authorizationContextData.getCurrentScope(), Collections.emptyMap());
	}

	public Set<String> generateRepositoryPermissions(Object resource, String scopeName, Map<String, Object> variables) {
		return executeInContext(scopeName, resource, variables, () -> createEnforcementPoint(variables).generateRepositoryPermissions(resource, scopeName));
	}

	private UAAPolicyEnforcementPoint createEnforcementPoint(Map<String, Object> variables) {

		StandardEvaluationContext evaluationContext = resolveExecutionEnvironment(variables);

		PolicyDecisionPoint policyDecisionPoint = new UAAPolicyDecisionPoint(evaluationContext, authorizationDefinitionRepository, policyProcessorFactories,
			propertyPermissionValidator, dataMasking, propertyChangesChecker);
		UAAPolicyEnforcementPoint enforcementPoint =
			new UAAPolicyEnforcementPoint(policyDecisionPoint, authorizationDefinitionRepository);

		return enforcementPoint;
	}

	private StandardEvaluationContext resolveExecutionEnvironment(Map<String, Object> variables) {
		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		StandardEvaluationContext executionEnvironment =
			Optional.ofNullable(authorizationContext.getExecutionEnvironment()).orElseGet(() -> createNewExecutionEnvironment());
		Optional.ofNullable(variables).map(v -> v.entrySet()).orElse(Collections.emptySet()).stream()
			.forEach(variable -> executionEnvironment.setVariable(variable.getKey(), variable.getValue()));

		return executionEnvironment;
	}

	private StandardEvaluationContext createNewExecutionEnvironment() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		LOGGER.debug("Creating new execution environment with user [{}]", authentication.getPrincipal());
		UAABaseSpelSecurityExpressionRoot root = new UAABaseSpelSecurityExpressionRoot(authentication);
		root.setDefaultRolePrefix("");
		root.setTrustResolver(trustResolver);

		StandardEvaluationContext evaluationContext = new StandardEvaluationContext(root);
		BeanResolver beanResolver = new BeanFactoryResolver(applicationContext);
		evaluationContext.setBeanResolver(beanResolver);
		List<PropertyAccessor> propertyAccessorsList = new LinkedList<>();
		propertyAccessorsList.addAll(propertyAccessors.orElse(Collections.emptyList()));
		propertyAccessorsList.addAll(evaluationContext.getPropertyAccessors());
		evaluationContext.setPropertyAccessors(propertyAccessorsList);
		return evaluationContext;
	}

}
