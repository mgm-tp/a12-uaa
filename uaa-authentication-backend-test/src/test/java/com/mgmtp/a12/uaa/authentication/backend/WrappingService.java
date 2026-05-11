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
package com.mgmtp.a12.uaa.authentication.backend;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.uaa.authorization.AuthorizationContext;
import com.mgmtp.a12.uaa.authorization.AuthorizationContextHolder;

@Component
public class WrappingService {

	@Inject
	private ElevatedPermissionService elevatedPermissionService;
	@Inject
	private RegularService regularService;

	@PreAuthorize("hasUAAPermission('Outer Scope')")
	public boolean executeMultipleServicesAnnotation() {
		Assertions.assertNotNull(getExecutionEnvironment());
		boolean firstCall = regularService.executePermissionCheckInJava();
		Assertions.assertNotNull(getExecutionEnvironment());
		elevatedPermissionService.checkPermissionInsideAnnotation();
		Assertions.assertNotNull(getExecutionEnvironment());
		boolean secondCall = regularService.executePermissionCheckInJava();
		Assertions.assertNotNull(getExecutionEnvironment());
		return (firstCall && secondCall);
	}

	public boolean executeMultipleServicesJava() {
		Assertions.assertNull(getExecutionEnvironment());
		boolean firstCall = regularService.executePermissionCheckInJava();
		Assertions.assertNull(getExecutionEnvironment());
		boolean elevatedPermission = elevatedPermissionService.checkPermissionJava();
		Assertions.assertNull(getExecutionEnvironment());
		boolean secondCall = regularService.executePermissionCheckInJava();
		Assertions.assertNull(getExecutionEnvironment());
		return (firstCall && secondCall && elevatedPermission);
	}

	private StandardEvaluationContext getExecutionEnvironment() {
		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		StandardEvaluationContext executionEnvironment = authorizationContext.getExecutionEnvironment();
		return executionEnvironment;
	}

}
