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

import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = { TestConfiguration.class })
@ActiveProfiles("default")
public class ChildThreadAuthenticatedAspectTest {

	@Inject
	private AsynchronousTestJobRunner asynchronousTestJobRunner;
	@Inject
	private AnnotatedAsynchronousTestJobRunner annotatedAsynchronousTestJobRunner;

	static {
		// This configuration most likely overrides the strategy in all tests.
		// Currently, there is no way to override it only for this test, because AbstractSecurityInterceptor directly references the strategy
		// instead of retrieving it from SecurityContextHolder. Therefore, this configuration has no effect.
		System.setProperty("spring.security.strategy", "MODE_INHERITABLETHREADLOCAL");
	}

	@Test
	public void checkUser() throws InterruptedException, ExecutionException {
		try {
			asynchronousTestJobRunner.executeJob();
		} catch (Exception e) {
			Assertions.fail("no user", e);
		}
	}

	@Test
	public void checkUserWithAPI() {
		try {
			asynchronousTestJobRunner.executeJobWithAPI();
		} catch (Exception e) {
			Assertions.fail("no user", e);
		}

	}

	@Test
	public void checkUserAnnotatedAsync() throws InterruptedException, ExecutionException {
		try {
			annotatedAsynchronousTestJobRunner.executeJob();
		} catch (Exception e) {
			Assertions.fail("no user", e);
		}
	}

	@Test
	public void checkUserWithAPIAndAnnotatedAsync() {
		try {
			annotatedAsynchronousTestJobRunner.executeJobWithAPI();
		} catch (Exception e) {
			Assertions.fail("no user", e);
		}

	}

}
