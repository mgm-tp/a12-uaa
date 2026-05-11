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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class UAASecurityBypassTest {

	@Mock
	private SecurityFreeCallback callback;
	@Mock
	private ContextRefreshedEvent event;
	@Mock
	private ApplicationContext applicationContext;

	@Test
	void checkInitializationUAASecurityBypass() {
		UAASecurityBypass uaaSecurityBypass = new UAASecurityBypass(false);
		Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void checkInitializationUAASecurityBypassPreparePrivilegedSecurityContext() {
		Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
		UAASecurityBypass uaaSecurityBypass = new UAASecurityBypass(true);
		Assertions.assertNotNull(SecurityContextHolder.getContext().getAuthentication());
		Assertions.assertNotNull(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		ReflectionTestUtils.setField(uaaSecurityBypass, "applicationContext", applicationContext);
		Mockito.when(event.getApplicationContext()).thenReturn(applicationContext);
		uaaSecurityBypass.disableBypass(event);
	}

	@Test
	void checkDisableBypassSecurityContextHolderClearContext() {
		UAASecurityBypass uaaSecurityBypass = new UAASecurityBypass(true);
		Assertions.assertNotNull(SecurityContextHolder.getContext().getAuthentication());
		Assertions.assertNotNull(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		ReflectionTestUtils.setField(uaaSecurityBypass, "applicationContext", applicationContext);
		Mockito.when(event.getApplicationContext()).thenReturn(applicationContext);
		uaaSecurityBypass.disableBypass(event);
		Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void checkIsSecurityBypassRunning() {
		UAASecurityBypass uaaSecurityBypass = new UAASecurityBypass(true);
		Assertions.assertTrue(uaaSecurityBypass.isSecurityBypassRunning());
		ReflectionTestUtils.setField(uaaSecurityBypass, "applicationContext", applicationContext);
		Mockito.when(event.getApplicationContext()).thenReturn(applicationContext);
		uaaSecurityBypass.disableBypass(event);
	}

	@Test
	void checkSecurityBypassNotRunning() {
		UAASecurityBypass uaaSecurityBypass = new UAASecurityBypass(false);
		Assertions.assertFalse(uaaSecurityBypass.isSecurityBypassRunning());
	}

	@Test
	void checkRunWithSecurityBypassButBypassDisabledThrowException() {
		UAASecurityBypass uaaSecurityBypass = new UAASecurityBypass(false);
		ReflectionTestUtils.setField(uaaSecurityBypass, "applicationContext", applicationContext);
		Mockito.when(event.getApplicationContext()).thenReturn(applicationContext);
		uaaSecurityBypass.disableBypass(event);
		Exception exception = Assertions.assertThrows(Exception.class, () -> uaaSecurityBypass.runWithSecurityBypass(null));
		Assertions.assertEquals("Security bypass usage is not allowed after application has been initialized.", exception.getMessage());
	}

	@Test
	void checkRunWithSecurityBypassThrowExceptionFailed() {
		UAASecurityBypass uaaSecurityBypass = new UAASecurityBypass(false);
		Exception exception = Assertions.assertThrows(Exception.class, () -> uaaSecurityBypass.runWithSecurityBypass(null));
		Assertions.assertEquals("Security free callback failed", exception.getMessage());
	}

	@Test
	void checkRunWithSecurityBypass() throws Exception {
		UAASecurityBypass uaaSecurityBypass = new UAASecurityBypass(true);
		uaaSecurityBypass.runWithSecurityBypass(callback);
		Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
		Mockito.verify(callback, Mockito.atLeastOnce()).executeWithoutSecurityCheck();
		ReflectionTestUtils.setField(uaaSecurityBypass, "applicationContext", applicationContext);
		Mockito.when(event.getApplicationContext()).thenReturn(applicationContext);
		uaaSecurityBypass.disableBypass(event);
	}
}