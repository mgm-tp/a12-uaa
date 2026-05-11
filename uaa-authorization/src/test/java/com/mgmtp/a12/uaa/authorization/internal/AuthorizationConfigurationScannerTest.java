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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.prepost.PreAuthorize;

@ExtendWith(MockitoExtension.class)
@AuthorizationConfigurationScannerTest.MockAnnotation("type_annotation")
public class AuthorizationConfigurationScannerTest {

	private AuthorizationConfigurationScanner scanner = new AuthorizationConfigurationScanner(this.getClass().getPackageName());

	@Test
	@MockAnnotation("test")
	void testScanAnnotatedMethods() {
		var methods = scanner.scanAnnotatedMethods(MockAnnotation.class);
		Assertions.assertNotNull(methods);
		Assertions.assertEquals(1, methods.size());
	}

	@Test
	void testScanAnnotatedMethodsWithNoResult() {
		var methods = scanner.scanAnnotatedMethods(PreAuthorize.class);
		Assertions.assertNotNull(methods);
		Assertions.assertEquals(0, methods.size());
	}

	@Test
	void testScanAnnotatedTypes() {
		var classes = scanner.scanAnnotatedTypes(MockAnnotation.class);
		Assertions.assertNotNull(classes);
		Assertions.assertEquals(1, classes.size());
	}

	@Test
	void testScanAnnotatedTypesWithNoResult() {
		var classes = scanner.scanAnnotatedTypes(PreAuthorize.class);
		Assertions.assertNotNull(classes);
		Assertions.assertEquals(0, classes.size());
	}

	@Test
	void testGetAnnotationExpressionOfMethods() {
		var methods = scanner.scanAnnotatedMethods(MockAnnotation.class);
		var expressions = scanner.getAnnotationExpressionsOfMethods(methods, MockAnnotation.class,
			(AuthorizationConfigurationScanner.AnnotationExpressionGetter<MockAnnotation>) MockAnnotation::value);
		Assertions.assertNotNull(expressions);
		Assertions.assertTrue(expressions.contains("test"));
	}

	@Test
	void testGetAnnotationExpressionsOfTypes() {
		var classes = scanner.scanAnnotatedTypes(MockAnnotation.class);
		var expressions = scanner.getAnnotationExpressionsOfTypes(classes, MockAnnotation.class,
			(AuthorizationConfigurationScanner.AnnotationExpressionGetter<MockAnnotation>) MockAnnotation::value);
		Assertions.assertNotNull(expressions);
		Assertions.assertTrue(expressions.contains("type_annotation"));
	}

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@interface MockAnnotation {
		String value();
	}
}
