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
package com.mgmtp.a12.uaa.authorization;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.mgmtp.a12.uaa.authorization.internal.UAADelegatedUserDetail;
import com.mgmtp.a12.uaa.authorization.internal.UAAUserDetails;
import com.mgmtp.a12.uaa.authorization.internal.UserUtils;

@ExtendWith(MockitoExtension.class)
public class UserUtilTest {

	@Mock
	private SecurityContext securityContext;
	@Mock
	private Authentication authentication;
	@Mock
	private UserDetails userDetails;
	@Mock
	private UAAUserDetails uaaUserDetails;

	@BeforeEach
	void setUp() {
		SecurityContextHolder.setContext(securityContext);
	}

	@Test
	void checkResolveCurrentUserWithUserDetail() {
		//Mock getPrincipal is UserDetails
		Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);
		Mockito.when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
		//Test
		Object result = UserUtils.resolveCurrentUser();
		Assertions.assertTrue(result instanceof UAADelegatedUserDetail);
	}

	@Test
	void checkResolveCurrentUserWithUAAUserDetails() {
		//Mock getPrincipal is UAAUserDetails
		Mockito.when(authentication.getPrincipal()).thenReturn(uaaUserDetails);
		Mockito.when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
		//Test
		Object result = UserUtils.resolveCurrentUser();
		Assertions.assertTrue(result instanceof UAAUserDetails);
	}

	@Test
	void checkResolveCurrentUserThrowException() {
		//Mock
		Mockito.when(authentication.getPrincipal()).thenReturn("UserString");
		Mockito.when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
		//Test
		Exception exception = Assertions.assertThrows(Exception.class, () -> UserUtils.resolveCurrentUser());
		Assertions.assertEquals("Wrong user type UserString", exception.getMessage());
	}
}