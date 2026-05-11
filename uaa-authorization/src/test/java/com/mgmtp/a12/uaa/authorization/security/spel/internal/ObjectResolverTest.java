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

import java.util.Optional;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class ObjectResolverTest {

	@Inject
	private AsyncBean asyncBean;

	@Test
	void checkResolveDataObjectWithStringObject() {
		Object objectResult = ObjectResolver.resolveDataObject("string");
		Assertions.assertEquals(String.class, objectResult.getClass());
		Assertions.assertEquals("string", objectResult);
	}

	@Test
	void checkResolveDataObjectWithOptionalStringObject() {
		Object objectResult = ObjectResolver.resolveDataObject(Optional.of("string"));
		Assertions.assertEquals(String.class, objectResult.getClass());
		Assertions.assertEquals("string", objectResult);
	}

	@Test
	void checkResolveDataObjectWithOptionalObject() {
		Object objectResult = ObjectResolver.resolveDataObject(Optional.empty());
		Assertions.assertEquals(null, objectResult);
	}

	@Test
	void checkResolveDataObjectThrowErrorWithNullValue() {
		IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> ObjectResolver.resolveDataObject(null));
		Assertions.assertEquals("Candidate must not be null", exception.getMessage());
	}

	@Test
	void checkResolveDataObjectWithAopProxy() {
		asyncBean.work();
		Object objectResult = ObjectResolver.resolveDataObject(asyncBean);
		Assertions.assertEquals(AsyncBean.class, objectResult.getClass());
		AsyncBean asyncBeanResule = ((AsyncBean) objectResult);
		if (asyncBeanResule.getThreadOfExecution() != null) {
			Assertions.assertEquals("SimpleAsyncTaskExecutor-1", asyncBeanResule.getThreadOfExecution().getName());
		}
	}

	@Configuration
	@EnableAsync
	static class AsyncConfig {
		@Bean
		public AsyncBean asyncBean() {
			return new AsyncBean();
		}
	}

	static class AsyncBean {
		private Thread threadOfExecution;

		@Async
		public void work() {
			this.threadOfExecution = Thread.currentThread();
		}

		@Async
		public void fail() {
			throw new UnsupportedOperationException();
		}

		public Thread getThreadOfExecution() {
			return threadOfExecution;
		}
	}
}
