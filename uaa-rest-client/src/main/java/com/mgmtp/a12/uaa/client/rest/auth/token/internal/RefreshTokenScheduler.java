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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.TokenRefresher;

public class RefreshTokenScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(RefreshTokenScheduler.class);

	private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
	private static final List<ScheduledFuture<?>> futures = new LinkedList<>();

	private RefreshTokenScheduler() {
	}

	/**
	 * Creates and executes schedule for renew token.
	 * @param refresher the token refresher to execute.
	 * @param tokenRenewInSeconds How many seconds before token expiration to start silent renewal.
	 * @return a ScheduledFuture representing pending completion of the task.
	 */
	public static ScheduledFuture<?> scheduleTokenRenewal(TokenRefresher refresher, AuthorizationDataStore authorizationDataStore, Integer tokenRenewInSeconds,
		int retries) {
		ReschedulingCallable callable =
			new ReschedulingCallable(refresher::refreshAuthorizationData, authorizationDataStore, retries, scheduledExecutorService, tokenRenewInSeconds);
		LOGGER.info("Registered token renewal job.");
		ScheduledFuture<?> schedule = callable.schedule();
		if (schedule != null) {
			futures.add(schedule);
		}
		return schedule;
	}

	/**
	 * Stops and removes all of the scheduled futures.
	 */
	public static void stopTokenRenewal() {
		futures.forEach(future -> future.cancel(true));
		futures.clear();
		LOGGER.info("The token renewal job has been canceled.");
	}

	/**
	 * Shutdown the scheduler.
	 */
	public static void stopScheduler() {
		stopTokenRenewal();
		scheduledExecutorService.shutdown();
	}

}
