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
package com.mgmtp.a12.uaa.client.rest.auth.token.internal;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mgmtp.a12.uaa.client.rest.auth.TokenRefresher;
import com.mgmtp.a12.uaa.client.rest.auth.internal.store.AtomicAuthorizationDataStore;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenSchedulerTest {

	private static final Integer MAXIMUM_RETRIES = 2;

	private Integer tokenRenewInSeconds = 50;
	private List<ScheduledFuture<?>> futures;
	@Mock
	private TokenRefresher tokenRefresher;

	@Test
	void scheduleTokenRenewalTest() {
		//Cleanup all schedule
		ScheduledFuture<?> scheduledFuture =
			RefreshTokenScheduler.scheduleTokenRenewal(tokenRefresher, new AtomicAuthorizationDataStore(), tokenRenewInSeconds, MAXIMUM_RETRIES);
		assert scheduledFuture != null;
		Assertions.assertFalse(scheduledFuture.isCancelled());
		futures = getScheduledFutureList();
		Assertions.assertEquals(1, futures.size());
		RefreshTokenScheduler.stopTokenRenewal();
		Assertions.assertTrue(scheduledFuture.isCancelled());
		futures = getScheduledFutureList();
		Assertions.assertEquals(0, futures.size());
	}

	private List<ScheduledFuture<?>> getScheduledFutureList() {
		return (List<ScheduledFuture<?>>) ReflectionTestUtils.getField(RefreshTokenScheduler.class, "futures");
	}

}