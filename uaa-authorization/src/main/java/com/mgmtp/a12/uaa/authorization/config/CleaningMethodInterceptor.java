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

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

import com.mgmtp.a12.uaa.authorization.AuthorizationContext;
import com.mgmtp.a12.uaa.authorization.AuthorizationContextHolder;

class CleaningMethodInterceptor implements Ordered, MethodInterceptor, PointcutAdvisor, AopInfrastructureBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(CleaningMethodInterceptor.class);

	private Pointcut pointcut;
	private int order = Ordered.HIGHEST_PRECEDENCE;

	public CleaningMethodInterceptor(Pointcut pointcut) {
		Assert.notNull(pointcut, "pointcut cannot be null");
		this.pointcut = pointcut;
	}

	public void setPointcut(Pointcut pointcut) {
		this.pointcut = pointcut;
	}

	@Override
	public Advice getAdvice() {
		return this;
	}

	@Override
	public boolean isPerInstance() {
		return true;
	}

	@Override
	public Pointcut getPointcut() {
		return pointcut;
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		try {
			Object result = invocation.proceed();
			Object maskedReturnObject = authorizationContext.getMaskedReturnObjectAndClear();

			if (result != null && maskedReturnObject != null && result.getClass().equals(maskedReturnObject.getClass())) {
				return maskedReturnObject;
			}
			return result;
		} catch (Exception e) {
			LOGGER.debug("Decision is finished with an exception {}", e.getClass().getCanonicalName());
			throw e;
		} finally {
			LOGGER.debug("Annotation based decision is finished");
			authorizationContext.popContext();
			authorizationContext.popExecutionEnvironment();
		}
	}

	@Override
	public int getOrder() {
		return this.order;
	}

}
