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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.aopalliance.intercept.MethodInvocation;
import org.javers.core.diff.custom.CustomPropertyComparator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;

import com.mgmtp.a12.uaa.authorization.AuthorizationContext;
import com.mgmtp.a12.uaa.authorization.AuthorizationContextHolder;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.property.PropertyChangePathConverter;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyChangeConverter;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyRightsValidator;
import com.mgmtp.a12.uaa.authorization.property.internal.ResourceConverter;
import com.mgmtp.a12.uaa.authorization.security.DataMasking;
import com.mgmtp.a12.uaa.authorization.security.PropertyChangesChecker;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UAAMethodSecurityExpressionHandlerTest {

	private UAAMethodSecurityExpressionHandler expressionHandler;
	@Mock
	private PropertyAccessor propertyAccessor;
	@Mock
	private AuthorizationDefinitionRepository authorizationDefinitionRepository;
	@Mock
	private Authentication authentication;
	@Mock
	private MethodInvocation methodInvocation;
	@Mock
	private PropertyRightsValidator propertyPermissionValidator;
	@Mock
	private DataMasking dataMasking;
	@Spy
	private Optional<ResourceConverter> resourceConverter = Optional.empty();
	@Spy
	private Optional<List<CustomPropertyComparator>> propertyComparators = Optional.empty();
	@Spy
	private Optional<List<PropertyChangePathConverter>> propertyChangeConverters = Optional.of(Arrays.asList(new PropertyChangeConverter()));
	@InjectMocks
	private PropertyChangesChecker propertyChangesChecker = new PropertyChangesChecker(Arrays.asList("com.mgmtp"));

	@BeforeEach
	void setUp() throws Exception {
		List<PropertyAccessor> propertyAccessors = new ArrayList<>();
		propertyAccessors.add(propertyAccessor);
		expressionHandler = new UAAMethodSecurityExpressionHandler(propertyAccessors, authorizationDefinitionRepository,
			Collections.emptyList(), propertyPermissionValidator, dataMasking, propertyChangesChecker, Optional.empty());
		BDDMockito.given(methodInvocation.getMethod()).willReturn(String.class.getMethod("toString"));
		BDDMockito.given(methodInvocation.getArguments()).willReturn(new Object[] { "object" });
		BDDMockito.given(methodInvocation.getThis()).willReturn(this);
	}

	@AfterAll
	static void tearDown() {
		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		while (authorizationContext.popContext() != null) {
		}
		authorizationContext.setExecutionEnvironment(null);
	}

	@Test
	void checkCreateSecurityExpressionRootReturnNull() {
		Assertions.assertNull(expressionHandler.createSecurityExpressionRoot(authentication, methodInvocation));
	}

	@Test
	void checkCreateEvaluationContextInternalThrowErrorWhenMiIsNull() {
		Exception exception = Assertions.assertThrows(Exception.class, () -> expressionHandler.createEvaluationContextInternal(authentication, null));
		Assertions.assertEquals(NullPointerException.class, exception.getClass());
	}

	@Test
	void checkCreateEvaluationContextInternal() {
		StandardEvaluationContext standardEvaluationContext = expressionHandler.createEvaluationContextInternal(authentication, methodInvocation);
		Assertions.assertNotNull(standardEvaluationContext.getRootObject());
		Assertions.assertTrue(standardEvaluationContext.getRootObject().getValue() instanceof UAAMethodSecurityExpressionRoot);
		Assertions.assertTrue(standardEvaluationContext.getPropertyAccessors().size() > 0);
		Mockito.verify(methodInvocation, Mockito.times(3)).getMethod();
		Mockito.verify(methodInvocation, Mockito.times(1)).getArguments();
	}
}