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
import java.util.Optional;

import jakarta.inject.Inject;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionService;
import com.mgmtp.a12.uaa.authorization.AuthorizationService;
import com.mgmtp.a12.uaa.authorization.internal.RuntimeAuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.model.Permission;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyRightsValidator;
import com.mgmtp.a12.uaa.authorization.property.internal.UAADataMasking;
import com.mgmtp.a12.uaa.authorization.security.DataMasking;
import com.mgmtp.a12.uaa.authorization.security.PermissionCheckResult;
import com.mgmtp.a12.uaa.authorization.security.PermissionEvaluationResult;
import com.mgmtp.a12.uaa.authorization.security.PolicyProcessorFactory;
import com.mgmtp.a12.uaa.authorization.security.PropertyChangesChecker;
import com.mgmtp.a12.uaa.authorization.security.spel.internal.SpelPolicyProcessorFactory;

@ExtendWith(SpringExtension.class)
public class MultipleScopesAuthorizationServiceTest extends AbstractIntegrationTest {

	@Inject
	private AuthorizationService authorizationService;

	@Test
	public void checkPartiallyFailingPermissions() {
		PermissionCheckResult<Permission> permissionCheck = authorizationService.checkPermissions(null, "Partially Failing Scope");
		Assertions.assertFalse(permissionCheck.isPassed());
		Assertions.assertEquals(2, permissionCheck.getPassedPermissions().size());
		Assertions.assertEquals(1, permissionCheck.getFailedPermissions().size());

		Optional<PermissionEvaluationResult<Permission>> passingPermission = getPemissionEvaluationResult(permissionCheck, "Passing Permission");
		Assertions.assertTrue(passingPermission.isPresent());
		Assertions.assertTrue(passingPermission.get().isPassed());

		MatcherAssert.assertThat("Passing policy", passingPermission.get().getPassedPolicies(), Matchers.contains("Passing Policy"));

		MatcherAssert.assertThat("Filterred Policy", getPemissionEvaluationResult(permissionCheck, "Filterred Permission").get().getPassedPolicies(),
			Matchers.contains("Filterred Policy"));
		MatcherAssert.assertThat("Failing Policy", getPemissionEvaluationResult(permissionCheck, "Failing Permission").get().getFailedPolicies(),
			Matchers.contains("Failing Policy"));
	}

	@Test
	public void checkFailingPermissions() {
		PermissionCheckResult<Permission> permissionCheck = authorizationService.checkPermissions(null, "Failing Scope");
		Assertions.assertFalse(permissionCheck.isPassed());

		Assertions.assertEquals(2, permissionCheck.getFailedPermissions().size());
		Assertions.assertEquals(0, permissionCheck.getPassedPermissions().size());

		Optional<PermissionEvaluationResult<Permission>> failingPermission = getPemissionEvaluationResult(permissionCheck, "Failing Permission");
		Assertions.assertTrue(failingPermission.isPresent());
		Assertions.assertFalse(failingPermission.get().isPassed());

		MatcherAssert.assertThat("Failing Policy", failingPermission.get().getFailedPolicies(), Matchers.contains("Failing Policy"));
		MatcherAssert.assertThat("Failing Policy", getPemissionEvaluationResult(permissionCheck, "Failing Permission II").get().getFailedPolicies(),
			Matchers.contains("Failing Policy"));

	}

	@Test
	public void checkPassingPermissions() {
		PermissionCheckResult<Permission> permissionCheck = authorizationService.checkPermissions(null, "Passing Scope");
		Assertions.assertTrue(permissionCheck.isPassed());

		Assertions.assertEquals(0, permissionCheck.getFailedPermissions().size());
		Assertions.assertEquals(2, permissionCheck.getPassedPermissions().size());

		Optional<PermissionEvaluationResult<Permission>> passingPermission = getPemissionEvaluationResult(permissionCheck, "Passing Permission");
		Assertions.assertTrue(passingPermission.isPresent());
		Assertions.assertTrue(passingPermission.get().isPassed());

		MatcherAssert.assertThat("Passing Policy", passingPermission.get().getPassedPolicies(), Matchers.contains("Passing Policy"));
		MatcherAssert.assertThat("Filterred Policy", getPemissionEvaluationResult(permissionCheck, "Filterred Permission").get().getPassedPolicies(),
			Matchers.contains("Filterred Policy"));

	}

	private Optional<PermissionEvaluationResult<Permission>> getPemissionEvaluationResult(PermissionCheckResult<Permission> permissionCheck,
		String permissionName) {

		return permissionCheck.getPermissionEvaluationResult().stream()
			.filter(evaluationResult -> evaluationResult.getPermission().getName().equals(permissionName))
			.findFirst();

	}

	@Configuration
	static class TestConfig {
		@Bean
		public AuthorizationDefinitionRepository createAuthorizationDefinitionRepository() {
			return new RuntimeAuthorizationDefinitionRepository();
		}

		@Bean
		public AuthorizationDefinitionService crAuthorizationDefinitionService() {
			return new AuthorizationDefinitionService("classpath:testMultipleScopes.json", null);
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
