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

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.internal.store.AtomicAuthorizationDataStore;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ReschedulingCallableTest {

	@Mock
	private ScheduledExecutorService scheduledExecutorService;
	@Mock
	private Callable<AuthorizationData> refresher;
	@Mock
	private ScheduledFuture<AuthorizationData> scheduledFuture;
	@Captor
	private ArgumentCaptor<Callable<AuthorizationData>> callableCaptor;
	@Captor
	private ArgumentCaptor<Long> delayCaptor;
	@Captor
	private ArgumentCaptor<TimeUnit> timeUnitCaptor;
	private Integer tokenRenewInSeconds;

	@BeforeEach
	void setUp() throws Exception {
		Mockito.when(scheduledExecutorService.schedule(Mockito.any(Callable.class), Mockito.anyLong(), Mockito.any())).thenReturn(scheduledFuture);
		tokenRenewInSeconds = 50;
		Mockito.when(refresher.call()).thenReturn(new AuthorizationData("token", TokenType.UAABEARER, null, tokenRenewInSeconds));
	}

	@Test
	void checkSchedule() throws Exception {
		ReschedulingCallable reschedulingCallable =
			new ReschedulingCallable(refresher, new AtomicAuthorizationDataStore(), 2, scheduledExecutorService, tokenRenewInSeconds);
		reschedulingCallable.schedule();
		Mockito.verify(scheduledExecutorService).schedule(callableCaptor.capture(), delayCaptor.capture(), timeUnitCaptor.capture());
		Assertions.assertEquals(reschedulingCallable, callableCaptor.getValue());
		Assertions.assertEquals(50, delayCaptor.getValue());
		Assertions.assertEquals(TimeUnit.SECONDS, timeUnitCaptor.getValue());

		reschedulingCallable.call();
		Mockito.verify(refresher, Mockito.times(1)).call();
	}

	@Test
	void checkFailedRefresh() throws Exception {
		ReschedulingCallable reschedulingCallable =
			new ReschedulingCallable(refresher, new AtomicAuthorizationDataStore(), 2, scheduledExecutorService, tokenRenewInSeconds);

		Mockito.when(refresher.call()).thenThrow(RuntimeException.class);
		ScheduledFuture<AuthorizationData> future = reschedulingCallable.schedule();
		Assertions.assertThrows(IllegalStateException.class, () -> reschedulingCallable.call());
		Mockito.verify(refresher, Mockito.times(3)).call();
	}

}