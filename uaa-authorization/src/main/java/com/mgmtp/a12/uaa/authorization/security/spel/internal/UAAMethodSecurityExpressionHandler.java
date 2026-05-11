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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

import com.mgmtp.a12.uaa.authorization.AuthorizationContext;
import com.mgmtp.a12.uaa.authorization.AuthorizationContextHolder;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.DecisionContext;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyRightsValidator;
import com.mgmtp.a12.uaa.authorization.security.DataMasking;
import com.mgmtp.a12.uaa.authorization.security.PolicyDecisionPoint;
import com.mgmtp.a12.uaa.authorization.security.PolicyProcessorFactory;
import com.mgmtp.a12.uaa.authorization.security.PropertyChangesChecker;
import com.mgmtp.a12.uaa.authorization.security.UAAPolicyEnforcementPoint;

public class UAAMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {
	private static Logger LOGGER = LoggerFactory.getLogger(UAAMethodSecurityExpressionHandler.class);

	private AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();
	private List<PropertyAccessor> propertyAccessors;
	private AuthorizationDefinitionRepository authorizationDefinitionRepository;
	private List<PolicyProcessorFactory> policyProcessorFactories;
	private PropertyRightsValidator propertyPermissionValidator;
	private DataMasking dataMasking;
	private PropertyChangesChecker propertyChangesChecker;
	private Optional<FilterTargetAdapter> filterTargetAdapter;

	public UAAMethodSecurityExpressionHandler(List<PropertyAccessor> propertyAccessors, AuthorizationDefinitionRepository authorizationDefinitionRepository,
		List<PolicyProcessorFactory> policyProcessorFactories, PropertyRightsValidator propertyPermissionValidator,
		DataMasking dataMasking, PropertyChangesChecker propertyChangesChecker, Optional<FilterTargetAdapter> filterTargetAdapter) {
		this.propertyAccessors = propertyAccessors;
		this.authorizationDefinitionRepository = authorizationDefinitionRepository;
		this.policyProcessorFactories = policyProcessorFactories;
		this.propertyPermissionValidator = propertyPermissionValidator;
		this.dataMasking = dataMasking;
		this.propertyChangesChecker = propertyChangesChecker;
		this.filterTargetAdapter = filterTargetAdapter;
	}

	//the initialization must be bit more complicated because we need reference to Execution context in  SpelPolicyDecisionPoint
	@Override
	protected MethodSecurityExpressionOperations createSecurityExpressionRoot(Authentication authentication, MethodInvocation invocation) {
		return null;
	}

	private UAAMethodSecurityExpressionRoot createSecurityExpressionRootInternal(Authentication authentication, MethodInvocation invocation,
		StandardEvaluationContext standardEvaluationContext) {
		PolicyDecisionPoint policyDecisionPoint = new UAAPolicyDecisionPoint(
			standardEvaluationContext, authorizationDefinitionRepository, policyProcessorFactories, propertyPermissionValidator, dataMasking,
			propertyChangesChecker);
		UAAPolicyEnforcementPoint policyEnforcementPoint = new UAAPolicyEnforcementPoint(policyDecisionPoint, authorizationDefinitionRepository);
		UAAMethodSecurityExpressionRoot root = new UAAMethodSecurityExpressionRoot(authentication, policyEnforcementPoint);
		root.setPermissionEvaluator(getPermissionEvaluator());
		root.setTrustResolver(trustResolver);
		root.setRoleHierarchy(getRoleHierarchy());
		root.setDefaultRolePrefix(getDefaultRolePrefix());
		root.setResourceObject(invocation.getThis());
		root.setMethod(invocation.getMethod());

		return root;
	}

	@Override
	public EvaluationContext createEvaluationContext(Supplier<Authentication> auth, MethodInvocation mi) {
		return createEvaluationContextInternal(auth.get(), mi);

	}

	@Override
	public StandardEvaluationContext createEvaluationContextInternal(Authentication auth, MethodInvocation mi) {

		Optional.ofNullable(auth).orElseThrow(() -> new IllegalArgumentException("Authentication object cannot be null"));

		UAAMethodSecurityEvaluationContext uaaEvaluationContext = new UAAMethodSecurityEvaluationContext(mi);
		uaaEvaluationContext.setUAARootObject(createSecurityExpressionRootInternal(auth, mi, uaaEvaluationContext));

		String defaultResource = constructDefaultResource(mi);
		LOGGER.debug("Creating default resource [{}] in context[{}]", defaultResource, uaaEvaluationContext.hashCode());
		uaaEvaluationContext.setVariable("resource", defaultResource);
		List<PropertyAccessor> propertyAccessorsList = new LinkedList<>();
		propertyAccessorsList.addAll(propertyAccessors);
		propertyAccessorsList.addAll(uaaEvaluationContext.getPropertyAccessors());
		uaaEvaluationContext.setPropertyAccessors(propertyAccessorsList);
		uaaEvaluationContext.setBeanResolver(getBeanResolver());

		return fillEvaluationContext(mi, uaaEvaluationContext);
	}

	private String constructDefaultResource(MethodInvocation invocation) {
		Method method = invocation.getMethod();
		Class<? extends Object> clazz = method.getDeclaringClass();
		return "%s.%s".formatted(clazz.getSimpleName(), method.getName());

	}

	private StandardEvaluationContext fillEvaluationContext(MethodInvocation method, StandardEvaluationContext evaluationContext) {
		Method wrappedMethod = method.getMethod();
		Parameter[] parameters = wrappedMethod.getParameters();
		Object[] arguments = method.getArguments();
		Annotation[][] parameterAnnotations = wrappedMethod.getParameterAnnotations();

		IntStream.range(0, parameterAnnotations.length)
			.mapToObj(i -> {
				DecisionContext annotation = AnnotatedElementUtils.findMergedAnnotation(parameters[i], DecisionContext.class);
				return ImmutablePair.of(annotation, arguments[i]);
			})
			.filter(parameterPair -> parameterPair.getLeft() != null)
			.forEach(parameterPair -> {
				String parameterName = parameterPair.getLeft().value();
				evaluationContext.setVariable(parameterName, parameterPair.getRight());
			});

		return evaluationContext;
	}

	@Override
	public Object filter(Object filterTarget, Expression filterExpression, EvaluationContext ctx) {
		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		authorizationContext.processCollection();
		try {
			return filterTargetAdapter.map(
				adapter -> {
					Object processedFilterTarget = adapter.preFilter(filterTarget);
					super.filter(processedFilterTarget, filterExpression, ctx);
					return adapter.postFilter(filterTarget, processedFilterTarget);
				}
			).orElseGet(() -> super.filter(filterTarget, filterExpression, ctx));
		} finally {
			authorizationContext.stopProcessCollection();
		}
	}

	/**
	 * This api support to resolve the data before and after the filter process is carried out.
	 * For example, it is able to wrap data into wrapper object which has extension functions for further purposes.
	 */
	public interface FilterTargetAdapter {
		/**
		 * This is called before the filter process
		 * @param filterTarget filterTarget
		 * @return the new filterTarget
		 */
		Object preFilter(Object filterTarget);

		/**
		 * This is called after the filter process
		 * @param originalFilterTarget the original filterTarget
		 * @param processedFilterTarget the filterTarget after being processed
		 * @return the final filterTarget
		 */
		Object postFilter(Object originalFilterTarget, Object processedFilterTarget);
	}
}
