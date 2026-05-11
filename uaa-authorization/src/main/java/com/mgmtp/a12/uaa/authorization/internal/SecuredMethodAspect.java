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
package com.mgmtp.a12.uaa.authorization.internal;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;

//@Aspect
//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class SecuredMethodAspect {

	@Pointcut("execution(@org.springframework.security.access.prepost.PostAuthorize * *(..))")
	public void publicMethodWithPostSecurity() {
		// just pointcut definition
	}

	@Pointcut("execution(@org.springframework.security.access.prepost.PreAuthorize * *(..))")
	public void publicMethodWithPreSecurity() {
		// just pointcut definition
	}

	@Pointcut("execution(@org.springframework.security.access.prepost.PostFilter * *(..))")
	public void publicMethodWithPostFilter() {
		// just pointcut definition
	}

	@Pointcut("execution(@org.springframework.security.access.prepost.PreFilter * *(..))")
	public void publicMethodWithPreFilter() {
		// just pointcut definition
	}

	@Around(value = """
		publicMethodWithPostSecurity()\
		 && target(bean)\
		 && @annotation(postAuthorizeAnnotation)\
		""")
	public Object secureMethodPostAuthorize(ProceedingJoinPoint joinPoint, Object bean, PostAuthorize postAuthorizeAnnotation) throws Throwable {
		return executeMethod(joinPoint);
	}

	@Around(value = """
		publicMethodWithPreSecurity()\
		 && target(bean)\
		 && @annotation(preAuthorizeAnnotation)\
		""")
	public Object secureMethodPreAuthorize(ProceedingJoinPoint joinPoint, Object bean, PreAuthorize preAuthorizeAnnotation) throws Throwable {
		return executeMethod(joinPoint);
	}

	@Around(value = """
		publicMethodWithPostFilter()\
		 && target(bean)\
		 && @annotation(postFilterAnnotation)\
		""")
	public Object secureMethodPostFilter(ProceedingJoinPoint joinPoint, Object bean, PostFilter postFilterAnnotation) throws Throwable {
		return executeMethod(joinPoint);
	}

	@Around(value = """
		publicMethodWithPreFilter()\
		 && target(bean)\
		 && @annotation(preFilterAnnotation)\
		""")
	public Object secureMethodPreFilter(ProceedingJoinPoint joinPoint, Object bean, PreFilter preFilterAnnotation) throws Throwable {
		return executeMethod(joinPoint);
	}
	
	private void scanParameters(ProceedingJoinPoint joinPoint) {
		
	}

	private Object executeMethod(ProceedingJoinPoint joinPoint) throws Throwable {
		UAAUserDetails currentUser = UserUtils.resolveCurrentUser();
		//		currentUser.permissionInCheckStarted();
		Object methodResult = joinPoint.proceed();
		//		currentUser.permissionInCheckFinished();
		return methodResult;
	}

}
