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
package com.mgmtp.a12.uaa.authorization.integration;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithUserDetails;

import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionService;
import com.mgmtp.a12.uaa.authorization.AuthorizationService;
import com.mgmtp.a12.uaa.authorization.internal.RuntimeAuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.model.Permission;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyRightsValidator;
import com.mgmtp.a12.uaa.authorization.property.internal.UAADataMasking;
import com.mgmtp.a12.uaa.authorization.security.DataMasking;
import com.mgmtp.a12.uaa.authorization.security.PermissionCheckResult;
import com.mgmtp.a12.uaa.authorization.security.PolicyProcessorFactory;
import com.mgmtp.a12.uaa.authorization.security.PropertyChangesChecker;
import com.mgmtp.a12.uaa.authorization.security.spel.internal.SpelPolicyProcessorFactory;

public class MethodSecurityConcurrencyTest extends AbstractIntegrationTest {

	@Inject
	private AuthorizationService authorizationService;

	@BeforeAll
	void setUp() {
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
	}

	@Test
	public void synchronousCheckExecution() {
		PermissionCheckResult<Permission> sucessCheck = authorizationService.checkPermissions(null, "Success");
		PermissionCheckResult<Permission> failureCheck = authorizationService.checkPermissions(null, "Failure");
		Assertions.assertTrue(sucessCheck.isPassed());
		Assertions.assertFalse(failureCheck.isPassed());
	}

	@Test
	public void asynchronousCheckExecution() throws InterruptedException, ExecutionException {
		PermissionCheckResult<Permission> failureCheck = runPermissionCheckConcurrently();
		Assertions.assertFalse(failureCheck.isPassed());
	}

	@Test
	@WithUserDetails("noConcurrent")
	public void asynchronousCheckExecutionFaiing() throws InterruptedException, ExecutionException {
		PermissionCheckResult<Permission> failureCheck = runPermissionCheckConcurrently();
		//result is false positive because if concurrent access to userDetail#permissioncheckLevel field 
		Assertions.assertTrue(failureCheck.isPassed());
	}

	private PermissionCheckResult<Permission> runPermissionCheckConcurrently() throws InterruptedException, ExecutionException {

		ExecutorService executorService = Executors.newFixedThreadPool(5);
		CompletableFuture.runAsync(() -> authorizationService.checkPermissions(null, "Success"), executorService);
		Thread.sleep(500);
		PermissionCheckResult<Permission> failureCheck = authorizationService.checkPermissions(null, "Failure");
		return failureCheck;
	}

	@Configuration
	static class TestConfig {
		@Bean
		public AuthorizationDefinitionRepository createAuthorizationDefinitionRepository() {
			return new RuntimeAuthorizationDefinitionRepository();
		}

		@Bean
		public AuthorizationDefinitionService crAuthorizationDefinitionService() {
			return new AuthorizationDefinitionService("classpath:concurrentAuthorizationDefinition.json", null);
		}

		@Bean
		public UserDetailsService createUserDetailsService() {
			return new TestUserDetailsService();
		}

		@Bean
		public AuthorizationService authorizationService() {
			return new AuthorizationService();
		}

		@Bean
		public PolicyProcessorFactory spelFactory() {
			return new SpelPolicyProcessorFactory();
		}

		@Bean
		public PropertyRightsValidator createPropertyPermissionValidator() {
			return new PropertyRightsValidator();
		}

		@Bean
		public DataMasking createDataMasking() {
			return new UAADataMasking(createAuthorizationDefinitionRepository());
		}
		@Bean
		public PropertyChangesChecker createPropertyChangesChecker() {
			return new PropertyChangesChecker(Arrays.asList("com.mgmtp"));
		}
	}

}
