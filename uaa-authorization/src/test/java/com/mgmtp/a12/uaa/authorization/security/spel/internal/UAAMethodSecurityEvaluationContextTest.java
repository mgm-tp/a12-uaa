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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UAAMethodSecurityEvaluationContextTest {

	private UAAMethodSecurityEvaluationContext uaaEvaluationContext;
	@Mock
	private Authentication authentication;
	@Mock
	private MethodInvocation methodInvocation;
	@Mock
	private ParameterNameDiscoverer parameterNameDiscoverer;

	@BeforeEach
	void setUp() throws NoSuchMethodException {
		BDDMockito.given(methodInvocation.getMethod()).willReturn(String.class.getMethod("toString"));
		BDDMockito.given(methodInvocation.getThis()).willReturn(this);
		BDDMockito.given(methodInvocation.getArguments()).willReturn(new Object[] {});
		uaaEvaluationContext = new UAAMethodSecurityEvaluationContext(methodInvocation, parameterNameDiscoverer);
	}

	@Test
	void checkLookupVariableReturnNull() {
		Object result = uaaEvaluationContext.lookupVariable("resource");
		Assertions.assertNull(result);
	}

	@Test
	void checkLookupVariableReturnData() {
		uaaEvaluationContext.setVariable("resource", "data");
		Object result = uaaEvaluationContext.lookupVariable("resource");
		Assertions.assertEquals("data", result);
	}

	@Test
	void checkLookupVariableReturnDataWithParams() {
		BDDMockito.given(methodInvocation.getArguments()).willReturn(new Object[] { "data" });
		BDDMockito.given(parameterNameDiscoverer.getParameterNames(ArgumentMatchers.any(Method.class))).willReturn(new String[] { "resource" });
		Object result = uaaEvaluationContext.lookupVariable("resource");
		Assertions.assertEquals("data", result);
	}
}