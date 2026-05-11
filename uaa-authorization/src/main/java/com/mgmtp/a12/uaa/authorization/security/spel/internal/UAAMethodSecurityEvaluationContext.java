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

import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.parameters.DefaultSecurityParameterNameDiscoverer;

public class UAAMethodSecurityEvaluationContext extends StandardEvaluationContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(UAAMethodSecurityEvaluationContext.class);

	private ParameterNameDiscoverer parameterNameDiscoverer;
	private final MethodInvocation mi;
	private boolean argumentsAdded;

	public UAAMethodSecurityEvaluationContext(MethodInvocation mi) {
		this(mi, new DefaultSecurityParameterNameDiscoverer());
	}

	UAAMethodSecurityEvaluationContext(MethodInvocation mi, ParameterNameDiscoverer parameterNameDiscoverer) {
		this.mi = mi;
		this.parameterNameDiscoverer = parameterNameDiscoverer;
	}

	@Override
	public void setRootObject(Object rootObject) {
		// silently ignore
		LOGGER.debug("Ignoring set root");
	}

	@Override
	public void setRootObject(Object rootObject, TypeDescriptor typeDescriptor) {
		LOGGER.debug("Ignoring set root");
	}

	public void setUAARootObject(UAAMethodSecurityExpressionRoot rootObject) {
		super.setRootObject(rootObject);
	}

	/*
	 * ===== BEGIN THIRD-PARTY SOURCE: spring-security (https://github.com/spring-projects/spring-security),
	 * https://github.com/spring-projects/spring-security/blob/4.2.11.RELEASE/core/src/main/java/org/springframework/security/access/expression/method/MethodSecurityEvaluationContext.java
	 * Licensed under the Apache-2.0 License.
	 * Copyright 2002-2016 the original author or authors.
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 */
	@Override
	public Object lookupVariable(String name) {
		if (name == null) {
			return null;
		}
		Object variable = super.lookupVariable(name);
		if (variable != null) {
			return variable;
		}
		if (!argumentsAdded) {
			addArgumentsAsVariables();
			argumentsAdded = true;
		}
		variable = super.lookupVariable(name);
		return variable;
	}

	public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
		this.parameterNameDiscoverer = parameterNameDiscoverer;
	}

	private void addArgumentsAsVariables() {
		Object[] args = mi.getArguments();

		if (args.length == 0) {
			return;
		}

		Object targetObject = mi.getThis();
		// SEC-1454
		Class<?> targetClass = AopProxyUtils.ultimateTargetClass(targetObject);

		if (targetClass == null) {
			targetClass = targetObject.getClass();
		}

		Method method = AopUtils.getMostSpecificMethod(mi.getMethod(), targetClass);
		String[] paramNames = parameterNameDiscoverer.getParameterNames(method);

		if (paramNames == null) {
			LOGGER.warn("Unable to resolve method parameter names for method: "
				+ method
				+ ". Debug symbol information is required if you are using parameter names in expressions.");
			return;
		}

		for (int i = 0; i < args.length; i++) {
			if (paramNames[i] != null) {
				setVariable(paramNames[i], args[i]);
			}
		}
	}
	// ===== END THIRD-PARTY SOURCE =====

}
