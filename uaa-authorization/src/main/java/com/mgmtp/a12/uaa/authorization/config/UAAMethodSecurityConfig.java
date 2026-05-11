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
package com.mgmtp.a12.uaa.authorization.config;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.Pointcuts;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.expression.PropertyAccessor;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyRightsValidator;
import com.mgmtp.a12.uaa.authorization.security.DataMasking;
import com.mgmtp.a12.uaa.authorization.security.PolicyProcessorFactory;
import com.mgmtp.a12.uaa.authorization.security.PropertyChangesChecker;
import com.mgmtp.a12.uaa.authorization.security.spel.internal.UAAMethodSecurityExpressionHandler;

@Configuration
@EnableMethodSecurity
public class UAAMethodSecurityConfig {

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public static MethodSecurityExpressionHandler createExpressionHandler(
		Optional<List<PropertyAccessor>> propertyAccessors,
		ApplicationContext applicationContext,
		AuthorizationDefinitionRepository authorizationDefinitionRepository,
		List<PolicyProcessorFactory> policyProcessorFactories,
		PropertyRightsValidator propertyPermissionValidator,
		DataMasking dataMasking,
		PropertyChangesChecker propertyChangesChecker,
		Optional<UAAMethodSecurityExpressionHandler.FilterTargetAdapter> filterTargetAdapter) {
		UAAMethodSecurityExpressionHandler expressionHandler = new UAAMethodSecurityExpressionHandler(propertyAccessors.orElse(Collections.emptyList()),
			authorizationDefinitionRepository, policyProcessorFactories, propertyPermissionValidator, dataMasking, propertyChangesChecker, filterTargetAdapter);
		expressionHandler.setDefaultRolePrefix(null);
		expressionHandler.setApplicationContext(applicationContext);
		return expressionHandler;
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	Advisor cleaningAuthorizationMethodInterceptor() {
		CleaningMethodInterceptor interceptor = new CleaningMethodInterceptor(forAllAnnotations());
		return interceptor;
	}

	private Pointcut forAllAnnotations() {
		return forAnnotations(PreFilter.class, PreAuthorize.class, PostFilter.class, PostAuthorize.class);
	}

	@SafeVarargs
	private Pointcut forAnnotations(Class<? extends Annotation>... annotations) {
		ComposablePointcut pointcut = null;
		for (Class<? extends Annotation> annotation : annotations) {
			if (pointcut == null) {
				pointcut = new ComposablePointcut(classOrMethod(annotation));
			} else {
				pointcut.union(classOrMethod(annotation));
			}
		}
		return pointcut;
	}

	private Pointcut classOrMethod(Class<? extends Annotation> annotation) {
		return Pointcuts.union(new AnnotationMatchingPointcut(null, annotation, true),
			new AnnotationMatchingPointcut(annotation, true));
	}
}
