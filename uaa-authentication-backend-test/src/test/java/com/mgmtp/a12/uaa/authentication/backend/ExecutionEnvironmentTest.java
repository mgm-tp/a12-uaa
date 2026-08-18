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
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import com.mgmtp.a12.uaa.authorization.AuthorizationContext;
import com.mgmtp.a12.uaa.authorization.AuthorizationContextHolder;

@SpringBootTest(classes = { TestConfiguration.class })
@ActiveProfiles("no_super_user")
public class ExecutionEnvironmentTest {

	@Inject
	private WrappingService wrappingService;

	@Test
	@WithMockUser(username = "test")
	public void checkExecutionEnvironmentIsolation() throws Exception {
		Assertions.assertNull(getExecutionEnvironment());
		boolean multiplePermissions = wrappingService.executeMultipleServicesAnnotation();
		Assertions.assertNull(getExecutionEnvironment());
		Assertions.assertTrue(multiplePermissions);
	}

	@Test
	@WithMockUser(username = "test")
	public void checkExecutionCleaning() throws Exception {
		Assertions.assertNull(getExecutionEnvironment());
		boolean multiplePermissions = wrappingService.executeMultipleServicesJava();
		Assertions.assertNull(getExecutionEnvironment());
		Assertions.assertTrue(multiplePermissions);
	}

	@Test
	@WithMockUser(username = "test")
	public void checkBackendSwapInsideAnnotation() throws Exception {
		Assertions.assertNull(getExecutionEnvironment());
		boolean elevatedPassed = wrappingService.executeBackendSwapInsideAnnotation();
		Assertions.assertNull(getExecutionEnvironment());
		Assertions.assertTrue(elevatedPassed, "Elevated Scope must pass after BackendAuthenticationService swap");
	}

	private StandardEvaluationContext getExecutionEnvironment() {
		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		StandardEvaluationContext executionEnvironment = authorizationContext.getExecutionEnvironment();
		return executionEnvironment;
	}

}
