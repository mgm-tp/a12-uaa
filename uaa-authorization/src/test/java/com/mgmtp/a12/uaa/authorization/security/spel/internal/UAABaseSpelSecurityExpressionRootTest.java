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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
public class UAABaseSpelSecurityExpressionRootTest {

	@Mock
	private Authentication authentication;
	private UAABaseSpelSecurityExpressionRoot uaaBaseSpelSecurityExpressionRoot;

	@BeforeEach
	void setUp() {
		uaaBaseSpelSecurityExpressionRoot = new UAABaseSpelSecurityExpressionRoot(authentication);
	}

	@Test
	void checkIsResourceNameThrowExeptionWithResourceIsNull() {
		Exception exception = Assertions.assertThrows(Exception.class, () -> uaaBaseSpelSecurityExpressionRoot.isResourceName(null, null));
		Assertions.assertEquals(RuntimeException.class, exception.getClass());
		Assertions.assertEquals("No resource object", exception.getMessage());
	}

	@Test
	void checkIsResourceNameReturnTrue() {
		boolean result = uaaBaseSpelSecurityExpressionRoot.isResourceName("string", String.class.getName());
		Assertions.assertTrue(result);
	}

	@Test
	void checkIsResourceNameReturnFalse() {
		boolean result = uaaBaseSpelSecurityExpressionRoot.isResourceName(1, String.class.getName());
		Assertions.assertFalse(result);
	}

	@Test
	void checkPrintMessage() {
		boolean result = uaaBaseSpelSecurityExpressionRoot.print("This is message");
		Assertions.assertTrue(result);
	}
}
