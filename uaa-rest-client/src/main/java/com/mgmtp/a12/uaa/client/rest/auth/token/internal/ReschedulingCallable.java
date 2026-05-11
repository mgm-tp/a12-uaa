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
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationDataStore;

public class ReschedulingCallable implements Callable<AuthorizationData>, ScheduledFuture<AuthorizationData> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReschedulingCallable.class);

	private AuthorizationDataStore authorizationDataStore;
	private ScheduledFuture<AuthorizationData> currentFuture;
	private Callable<AuthorizationData> delegate;
	private int maximumRetries;
	private ScheduledExecutorService scheduledExecutorService;
	private Integer tokenRenewInSeconds;
	private final Object triggerContextMonitor = new Object();

	public ReschedulingCallable(Callable<AuthorizationData> delegate, AuthorizationDataStore authorizationDataStore, int maximumRetries,
		ScheduledExecutorService scheduledExecutorService,
		Integer tokenRenewInSeconds) {
		this.delegate = delegate;
		this.authorizationDataStore = authorizationDataStore;
		this.maximumRetries = maximumRetries;
		this.scheduledExecutorService = scheduledExecutorService;
		this.tokenRenewInSeconds = tokenRenewInSeconds;
	}

	public ScheduledFuture<AuthorizationData> schedule() {
		synchronized (this.triggerContextMonitor) {
			if (tokenRenewInSeconds < 0) {
				authorizationDataStore.setAuthorizationData(null);
				return null;
			}
			LOGGER.debug("Scheduling token renewal in [{}] seconds.", tokenRenewInSeconds);
			currentFuture = scheduledExecutorService.schedule(this, tokenRenewInSeconds, TimeUnit.SECONDS);
			return this;
		}
	}

	/*
	 * ===== BEGIN THIRD-PARTY SOURCE: spring-framework (https://github.com/spring-projects/spring-framework),
	 * https://github.com/spring-projects/spring-framework/blob/main/spring-context/src/main/java/org/springframework/scheduling/concurrent/ReschedulingRunnable.java
	 * Licensed under the Apache-2.0 License.
	 * Copyright 2002-present the original author or authors.
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 *
	 * Modified by mgm technology partners on [2021-09-01]
	 */
	@Override
	public AuthorizationData call() {
		AuthorizationData newAuthorizationData = callWithRetry(0);
		synchronized (this.triggerContextMonitor) {
			tokenRenewInSeconds = newAuthorizationData.getTokenRenewInSeconds();
			if (!obtainCurrentFuture().isCancelled()) {
				schedule();
			}
		}
		return newAuthorizationData;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		synchronized (this.triggerContextMonitor) {
			return obtainCurrentFuture().cancel(mayInterruptIfRunning);
		}
	}

	@Override
	public boolean isCancelled() {
		synchronized (this.triggerContextMonitor) {
			return obtainCurrentFuture().isCancelled();
		}
	}

	@Override
	public boolean isDone() {
		synchronized (this.triggerContextMonitor) {
			return obtainCurrentFuture().isDone();
		}
	}

	@Override
	public AuthorizationData get() throws InterruptedException, ExecutionException {
		ScheduledFuture<AuthorizationData> curr;
		synchronized (this.triggerContextMonitor) {
			curr = obtainCurrentFuture();
		}
		return curr.get();
	}

	@Override
	public AuthorizationData get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		ScheduledFuture<AuthorizationData> curr;
		synchronized (this.triggerContextMonitor) {
			curr = obtainCurrentFuture();
		}
		return curr.get(timeout, unit);
	}

	@Override
	public long getDelay(TimeUnit unit) {
		ScheduledFuture<?> curr;
		synchronized (this.triggerContextMonitor) {
			curr = obtainCurrentFuture();
		}
		return curr.getDelay(unit);
	}

	@Override
	public int compareTo(Delayed other) {
		if (this == other) {
			return 0;
		}
		long diff = getDelay(TimeUnit.MILLISECONDS) - other.getDelay(TimeUnit.MILLISECONDS);
		return (diff == 0 ? 0 : ((diff < 0) ? -1 : 1));
	}
	// ===== END THIRD-PARTY SOURCE =====

	private AuthorizationData callWithRetry(int retryAttempt) {
		try {
			return delegate.call();
		} catch (Exception e) {
			retryAttempt++;
			if (retryAttempt <= maximumRetries) {
				LOGGER.error("Unable to refresh token, counterRetry=%s.".formatted(retryAttempt), e);
				return callWithRetry(retryAttempt);
			} else {
				if (authorizationDataStore != null) {
					authorizationDataStore.setAuthorizationData(null);
				}
			}
		}
		throw new IllegalStateException("Unable to refresh the token");
	}

	private ScheduledFuture<AuthorizationData> obtainCurrentFuture() {
		Assert.state(this.currentFuture != null, "No scheduled future");
		return this.currentFuture;
	}
}