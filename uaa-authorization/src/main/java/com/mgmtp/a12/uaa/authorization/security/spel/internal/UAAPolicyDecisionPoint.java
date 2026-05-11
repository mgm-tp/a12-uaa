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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.mgmtp.a12.uaa.authorization.AuthorizationContext;
import com.mgmtp.a12.uaa.authorization.AuthorizationContextHolder;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.exception.MissingPolicyException;
import com.mgmtp.a12.uaa.authorization.model.Policy;
import com.mgmtp.a12.uaa.authorization.model.PolicyAware;
import com.mgmtp.a12.uaa.authorization.model.PolicyType;
import com.mgmtp.a12.uaa.authorization.model.PropertyPermission;
import com.mgmtp.a12.uaa.authorization.model.RepositoryPolicy;
import com.mgmtp.a12.uaa.authorization.model.internal.ResourceWrapper;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyRightsValidator;
import com.mgmtp.a12.uaa.authorization.security.DataMasking;
import com.mgmtp.a12.uaa.authorization.security.PermissionCheckResult;
import com.mgmtp.a12.uaa.authorization.security.PermissionEvaluationResult;
import com.mgmtp.a12.uaa.authorization.security.PolicyDecisionPoint;
import com.mgmtp.a12.uaa.authorization.security.PolicyEvaluationResult;
import com.mgmtp.a12.uaa.authorization.security.PolicyProcessor;
import com.mgmtp.a12.uaa.authorization.security.PolicyProcessorFactory;
import com.mgmtp.a12.uaa.authorization.security.PropertyChangesChecker;
import com.mgmtp.a12.uaa.authorization.security.RepositoryPolicyProcessor;
import com.mgmtp.a12.uaa.authorization.security.uaaexpression.internal.UAAExpressionParser;

public class UAAPolicyDecisionPoint implements PolicyDecisionPoint {

	private static Logger LOGGER = LoggerFactory.getLogger(UAAPolicyDecisionPoint.class);

	private static boolean bypassPermissions = false;

	private StandardEvaluationContext standardEvaluationContext;
	private AuthorizationDefinitionRepository authorizationDefinitionRepository;
	private RepositoryPolicyProcessor repositoryPolicyProcessor;
	private PolicyProcessorResolver policyProcessorResolver;
	private DataMasking dataMasking;
	private PropertyChangesChecker propertyChangesChecker;
	private PropertyRightsValidator propertyRightsValidator;

	public UAAPolicyDecisionPoint(StandardEvaluationContext standardEvaluationContext, AuthorizationDefinitionRepository authorizationDefinitionRepository,
		List<PolicyProcessorFactory> policyProcessorFactories, PropertyRightsValidator propertyRightsValidator, DataMasking dataMasking,
		PropertyChangesChecker propertyChangesChecker) {
		this.standardEvaluationContext = standardEvaluationContext;
		this.authorizationDefinitionRepository = authorizationDefinitionRepository;
		this.propertyRightsValidator = propertyRightsValidator;
		this.dataMasking = dataMasking;
		this.propertyChangesChecker = propertyChangesChecker;
		this.policyProcessorResolver = new PolicyProcessorResolver(policyProcessorFactories, standardEvaluationContext);
		this.repositoryPolicyProcessor =
			new SpelRepositoryPolicyProcessor(standardEvaluationContext, policyProcessorResolver, authorizationDefinitionRepository);
		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		authorizationContext.setExecutionEnvironment(standardEvaluationContext);
	}

	@Override
	public <T extends PolicyAware> PermissionCheckResult<T> hasPermission(Object resource, Set<T> permissionsDef) {
		if (bypassPermissions) {
			return new PermissionCheckResult.Builder<T>(true, Collections.emptyList()).build();
		}
		List<PermissionEvaluationResult<T>> permissionEvaluationResult = checkPermissions(resource, permissionsDef);

		Optional<PermissionEvaluationResult<T>> failedPermission = permissionEvaluationResult.stream()
			.filter(Predicate.not(PermissionEvaluationResult::isPassed))
			.findAny();
		boolean evaluationPassed = failedPermission.isEmpty() && !permissionEvaluationResult.isEmpty();
		if (evaluationPassed) {
			List<T> passedPermissions = permissionEvaluationResult.stream()
				.filter(PermissionEvaluationResult::isPassed)
				.map(PermissionEvaluationResult::getPermission)
				.collect(Collectors.toList());

			LOGGER.debug("Permission(s) [{}] passed in context[{}].", passedPermissions.stream().map(PolicyAware::getName).collect(Collectors.toList()),
				standardEvaluationContext.hashCode());
		}

		return new PermissionCheckResult.Builder<T>(evaluationPassed, permissionEvaluationResult).build();

	}

	@Override
	public <T extends PolicyAware> Set<String> evaluateRepositoryPermissions(Object resource, Set<T> permissionsDef) {
		LOGGER.debug("Evaluating repository permission [{}].", permissionsDef);
		if (bypassPermissions) {
			return Collections.emptySet();
		}

		HashSet<String> finalResult = new LinkedHashSet<>();
		Map<String, Set<String>> executedPolicy = new HashMap<>();
		permissionsDef.forEach(permission -> {
			var permissionResult = resolveRepositoryPolices(permission).stream()
				.map(policy -> {
					if (executedPolicy.get(policy.getName()) != null) {
						return executedPolicy.get(policy.getName());
					}

					Set<String> templates = Collections.emptySet();
					if (repositoryPolicyProcessor.targetFilter(policy, resource)) {
						templates = repositoryPolicyProcessor.executeRepositoryTemplate(policy, resource);
					}
					policy.policyExecuted(policy.getName(), templates);
					executedPolicy.put(policy.getName(), templates);
					return templates;
				})
				.flatMap(Set::stream)
				.collect(Collectors.toCollection(LinkedHashSet::new));

			permission.permissionExecuted(permission.getName(), permissionResult);
			finalResult.addAll(permissionResult);
		});

		return finalResult;
	}

	@Override
	public PermissionCheckResult<PropertyPermission> checkPropertyPermissionsAndMaskData(Object resource, UserDetails principal,
		Set<PropertyPermission> propertyPermissions) {
		if (bypassPermissions) {
			return new PermissionCheckResult.Builder<PropertyPermission>(true, Collections.emptyList()).build();
		}
		Assert.notNull(propertyPermissions, "Property permission can't be NULL");

		PermissionCheckResult<PropertyPermission> permissionCheckResult =
			hasPermission(resource instanceof ResourceWrapper wrapper ? wrapper.getResource() : resource, propertyPermissions);

		Boolean executedPermission = executeWithPassedPermissions(permissionCheckResult, (propertyRights) -> {
			dataMasking.maskData(resource, propertyRights);
			return true;
		});
		//custom property manipulation.
		boolean maskedInJava = propertyRightsValidator.maskData(resource, principal);
		if ((!executedPermission) && (!maskedInJava) && (!permissionCheckResult.getPermissionEvaluationResult().isEmpty())) {
			//mask complete object in case that no property permission passed
			dataMasking.maskData(resource, new PropertyPermission());
		}

		//we always pass this permission check unless all permissions fails
		boolean executionResult = executedPermission || permissionCheckResult.getPermissionEvaluationResult().isEmpty();
		return new PermissionCheckResult.Builder<PropertyPermission>(executionResult, permissionCheckResult.getPermissionEvaluationResult()).build();

	}

	@Override
	public Boolean checkPropertyPermissionsForChanges(Object persistedResource, Object updatedResource, UserDetails principal,
		Set<PropertyPermission> propertyPermissions) {
		if (bypassPermissions) {
			return true;
		}
		Assert.notNull(updatedResource, "Updated resource must be specified");

		//1-st we evaluate permissions for the object 
		PermissionCheckResult<PropertyPermission> permissionCheckResult = hasPermission(updatedResource, propertyPermissions);
		return propertyRightsValidator.validateChanges(persistedResource, updatedResource, principal)
			&& executeWithPassedPermissions(permissionCheckResult,
			(propertyRights) -> propertyChangesChecker.checkPropertyPermissionForChanges(persistedResource, updatedResource, propertyRights));

	}

	private Boolean executeWithPassedPermissions(PermissionCheckResult<PropertyPermission> permissionCheckResult,
		Function<PropertyPermission, Boolean> function) {
		return Optional.ofNullable(permissionCheckResult)
			.map(PermissionCheckResult::getPassedPermissions).orElse(Collections.emptyList())
			.stream()
			//.map(passedPermission -> function.apply(authorizationDefinitionRepository.getPropertyRightsByNames(passedPermission.getRightsRef())))
			.map(function)
			.filter(Boolean::booleanValue)
			.findAny().orElse(Boolean.FALSE);
	}

	private <T extends PolicyAware> List<PermissionEvaluationResult<T>> checkPermissions(Object resource, Set<T> permissionsDef) {
		LOGGER.debug("Executing permission check for custom resource [{}] in context[{}]", resource,
			standardEvaluationContext.hashCode());

		List<PermissionEvaluationResult<T>> executedPermissions = permissionsDef.stream()
			.map(permissionObject -> new PermissionEvaluationResult<T>(permissionObject, executeAllRules(permissionObject, resource)))
			.peek(this::logPermissionExecutionResult)
			.collect(Collectors.toList());

		return executedPermissions;
	}

	private <T extends PolicyAware> void logPermissionExecutionResult(PermissionEvaluationResult<T> executionResult) {
		if (executionResult.isPassed()) {
			LOGGER.debug("Passed permission [{}] in context[{}]", executionResult.getPermission().getName(), standardEvaluationContext.hashCode());
		} else {
			LOGGER.info("Policies {} in Permission {{}} have failed in context[{}]", executionResult.getFailedPolicies(),
				executionResult.getPermission().getName(), standardEvaluationContext.hashCode());
		}
	}

	private PolicyEvaluationResult executeAllRules(PolicyAware permission, Object resource) {
		Set<String> failedPoliciesRef = new HashSet<>();
		Set<String> passedPoliciesRef = new HashSet<>();
		LOGGER.debug("Checking permission [{}] in context[{}]", permission.getName(), standardEvaluationContext.hashCode());
		//for inline policies we have no way to define type. we have to hardcode it.
		PolicyProcessor inlinePoliciesProcessor = resolvePolicyProcessor(PolicyType.SpEL);
		boolean inlinePoliciesResult = inlinePoliciesProcessor.executeRules(permission.getPolicies(), resource);
		if (!inlinePoliciesResult) {
			failedPoliciesRef.add("%s[inline]".formatted(permission.getName()));
		}

		boolean policyRefsResult = true;
		if (!CollectionUtils.isEmpty(permission.getPolicyRefs())) {
			Set<String> executedPoliciesRefs = resolvePolicyReferences(permission.getPolicyRefs(), resource, passedPoliciesRef, failedPoliciesRef);
			PolicyProcessor permissionPoliciesProcessor = resolvePolicyProcessor(PolicyType.SpEL);
			policyRefsResult = permissionPoliciesProcessor.executeRules(executedPoliciesRefs, resource);
		}

		boolean allRulesPassed = policyRefsResult && inlinePoliciesResult;
		LOGGER.debug("Permission [{}] result is [{}] in context[{}]", permission.getName(), allRulesPassed, standardEvaluationContext.hashCode());
		permission.permissionExecuted(permission.getName(), allRulesPassed);
		return new PolicyEvaluationResult(allRulesPassed, failedPoliciesRef, passedPoliciesRef);

	}

	private Set<RepositoryPolicy> resolveRepositoryPolices(PolicyAware permission) {
		return permission.getRepositoryRefs().stream()
			.filter(ref -> !StringUtils.isBlank(ref))
			.map(StringUtils::trim)
			.map(ref -> authorizationDefinitionRepository.getRepositoryPolicyByName(ref).orElseThrow(() -> {
				LOGGER.error("Missing repository policy [{}]", ref);
				return new MissingPolicyException(ref);
			}))
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	private Set<String> resolvePolicyReferences(Set<String> policyReferences, Object resource, Set<String> passedPoliciesRef,
		Set<String> failurePoliciesRef) {
		return policyReferences.stream()
			.map(policyRef -> {
				String policyExecuteOutput = resolvePolicyReference(policyRef, resource);
				if ("(true)".equals(policyExecuteOutput)) {
					passedPoliciesRef.add(policyRef);
				} else {
					failurePoliciesRef.add(policyRef);
				}
				return policyExecuteOutput;
			}).collect(Collectors.toCollection(LinkedHashSet::new));

	}

	private String resolvePolicyReference(String reference, Object resource) {
		return UAAExpressionParser.booleanOperatorParse(reference, ref -> {
			Policy policy = authorizationDefinitionRepository.getPolicyByName(ref).orElseThrow(() -> {
				LOGGER.error("Missing policy [{}]", ref);
				return new MissingPolicyException(ref);
			});
			PolicyProcessor policyProcessor = resolvePolicyProcessor(policy.getType());
			//in case that the policy is filtered out
			boolean executionResult = true;
			if (policyProcessor.targetFilter(policy, resource)) {
				executionResult = policyProcessor.executeRules(policy, resource);
			}
			return executionResult;
		});
	}

	private PolicyProcessor resolvePolicyProcessor(PolicyType policyType) {
		return policyProcessorResolver.resolvePolicyProcessor(policyType);
	}

}
