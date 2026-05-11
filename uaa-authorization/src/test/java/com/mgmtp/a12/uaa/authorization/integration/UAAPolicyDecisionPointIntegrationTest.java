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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.ReactorContextTestExecutionListener;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.uaa.authorization.AuthorizationContext;
import com.mgmtp.a12.uaa.authorization.AuthorizationContextHolder;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionService;
import com.mgmtp.a12.uaa.authorization.SecurityFreeCallback;
import com.mgmtp.a12.uaa.authorization.UAASecurityBypass;
import com.mgmtp.a12.uaa.authorization.example.TestResource;
import com.mgmtp.a12.uaa.authorization.internal.RuntimeAuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.model.Permission;
import com.mgmtp.a12.uaa.authorization.model.PolicyAware;
import com.mgmtp.a12.uaa.authorization.model.PropertyPermission;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyRightsValidator;
import com.mgmtp.a12.uaa.authorization.property.internal.UAADataMasking;
import com.mgmtp.a12.uaa.authorization.security.DataMasking;
import com.mgmtp.a12.uaa.authorization.security.PermissionCheckResult;
import com.mgmtp.a12.uaa.authorization.security.PolicyProcessorFactory;
import com.mgmtp.a12.uaa.authorization.security.PropertyChangesChecker;
import com.mgmtp.a12.uaa.authorization.security.spel.internal.SpelPolicyProcessorFactory;
import com.mgmtp.a12.uaa.authorization.security.spel.internal.UAAPolicyDecisionPoint;

@ExtendWith(SpringExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
@TestExecutionListeners(inheritListeners = false, listeners = { WithSecurityContextTestExecutionListener.class,
	ReactorContextTestExecutionListener.class }, mergeMode = MergeMode.MERGE_WITH_DEFAULTS)
@WithMockUser
public class UAAPolicyDecisionPointIntegrationTest {

	@Inject
	private AuthorizationDefinitionRepository authorizationDefinitionRepository;
	@Inject
	private List<PolicyProcessorFactory> policyProcessorFactories;
	@Inject
	private PropertyRightsValidator propertyPermissionValidator;
	@Inject
	private DataMasking dataMasking;
	@Inject
	private PropertyChangesChecker propertyChangesChecker;

	private UAAPolicyDecisionPoint uaaPolicyDecisionPoint;
	private UAASecurityBypass securityBypass = new UAASecurityBypass(false);
	private StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

	@BeforeAll
	public void setUp() throws Exception {
		uaaPolicyDecisionPoint = new UAAPolicyDecisionPoint(evaluationContext, authorizationDefinitionRepository, policyProcessorFactories,
			propertyPermissionValidator, dataMasking, propertyChangesChecker);
	}

	@BeforeEach
	void addUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetails principal = (UserDetails) authentication.getPrincipal();
		evaluationContext.setVariable("principal", principal);
	}

	@AfterAll
	static void tearDown() {
		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		while (authorizationContext.popContext() != null) {
		}
		authorizationContext.setExecutionEnvironment(null);
	}

	@Test
	public void checkEmptyPermission() throws Exception {
		PermissionCheckResult<PolicyAware> permissionCheckResult = uaaPolicyDecisionPoint.hasPermission(createDefaultTestResource(), Collections.emptySet());
		Assertions.assertFalse(permissionCheckResult.isPassed());
		Assertions.assertTrue(permissionCheckResult.getPassedPermissions().isEmpty());
	}

	@Test
	public void checkFilteredPermission() throws Exception {
		Set<PropertyPermission> propertyPermission = authorizationDefinitionRepository.getPropertyPermission();
		PermissionCheckResult<PropertyPermission> permissionCheckResult = uaaPolicyDecisionPoint.hasPermission(createDefaultTestResource(), propertyPermission);
		Assertions.assertFalse(permissionCheckResult.isPassed());
		Assertions.assertTrue(permissionCheckResult.getPassedPermissions().isEmpty());
		Assertions.assertFalse(permissionCheckResult.getFailedPermissions().isEmpty());
	}

	@Test
	public void checkEmptyPermissionWithLoadFromStorage() throws Exception {
		checkPermission("unknown");
	}

	@Test
	public void checkInlinePermissionsWithSuccess() throws Exception {
		checkPermission("inline policies success", "Test Inline Success", true);
	}

	@Test
	public void checkInlinePermissionsWithFailure() throws Exception {
		checkPermission("inline policies failure");
	}

	@Test
	public void checkReferencedPolicyWithSuccess() throws Exception {
		checkPermission("ref policies success", "Test Ref Success", true);
	}

	@Test
	public void checkReferencedPolicyWithFailure() throws Exception {
		checkPermission("ref policies failure");
	}

	@Test
	public void checkReferencedPolicyAndLogicWithSuccess() throws Exception {
		checkPermission("ref policies sucess and", "Test And Success Ref", true);
	}

	@Test
	public void checkReferencedPolicyAndLogicWithFailure() throws Exception {
		checkPermission("ref policies failure and");
	}

	@Test
	public void checkReferencedPolicyAndInnerLogicWithSuccess() throws Exception {
		checkPermission("ref policies sucess inner and", "Test Inner And Success Ref", true);
	}

	@Test
	public void checkReferencedPolicyAndInnerLogicWithFailure() throws Exception {
		checkPermission("ref policies failure inner and");
	}

	@Test
	public void checkReferencedPolicyAndOrInnerLogicWithSuccess() throws Exception {
		checkPermission("ref policies sucess inner and or", "Test Inner And Or Success Ref", true);
	}

	@Test
	public void checkReferencedPolicyAndOrInnerLogicWithFailure() throws Exception {
		checkPermission("ref policies failure inner and or");
	}

	@Test
	public void checkNotOperatorInPolicyRef() throws Exception {
		checkPermission("Not operator", "Test NOT operator", true);
	}

	@Test
	public void checkReferencedPolicyAndOrLogicWithSuccess() throws Exception {
		checkPermission("ref policies sucess and or", "Test And Or Success Ref", true);
	}

	@Test
	public void checkReferencedPolicyAndOrLogicWithFailure() throws Exception {
		checkPermission("ref policies failure and or");
	}

	@Test
	public void checkBadPolicySyntax() throws Exception {
		checkPermission("bad policy syntax");
	}

	@Test
	public void checkTargetEvaluation() throws Exception {
		checkPermission("Target evaluation", null, false);
	}

	@Test
	public void checkTargetEvaluationFalse() throws Exception {
		checkPermission("Target evaluation fasle", null, false);
	}

	@Test
	public void securityBypassEnabled() throws Exception {
		securityBypass.runWithSecurityBypass(new SecurityFreeCallback() {

			@Override
			public void executeWithoutSecurityCheck() throws Exception {
				//checkEmptyPermission(); //all exept this test has to pass
				checkEmptyPermissionWithLoadFromStorage();
				checkInlinePermissionsWithSuccess();
				checkInlinePermissionsWithFailure();
				checkReferencedPolicyWithSuccess();
				checkReferencedPolicyWithFailure();
				checkReferencedPolicyAndLogicWithSuccess();
				checkReferencedPolicyAndLogicWithFailure();
				checkReferencedPolicyAndInnerLogicWithSuccess();
				checkReferencedPolicyAndInnerLogicWithFailure();
				checkReferencedPolicyAndOrInnerLogicWithSuccess();
				checkReferencedPolicyAndOrInnerLogicWithFailure();
				checkReferencedPolicyAndOrLogicWithSuccess();
				checkReferencedPolicyAndOrLogicWithFailure();
				propertyPermissionsWithSuccessAdmin();
				propertyPermissionsWithSuccessGuest();
				propertyPermissionsWithFailure();
				propertyPermissionsUpdateWithSuccess();
				propertyPermissionsUpdateWithNoPermission();
				propertyPermissionsUpdateWithNoPropertyRights();
				checkPermission("bad reference"); //checkReferencedPolicyWithBadRefFailure(); instead
				checkBadPolicySyntax();
			}
		});
	}

	@Test
	public void repositoryTemplate() throws JsonProcessingException {
		List<String> repositoryPermissions = new ArrayList<>(uaaPolicyDecisionPoint
			.evaluateRepositoryPermissions(createDefaultTestResource(), authorizationDefinitionRepository.getPermissionsByScope("Repository")));
		Assertions.assertEquals(4, repositoryPermissions.size());
		Assertions.assertEquals("one", repositoryPermissions.get(0));
		Assertions.assertEquals("2", repositoryPermissions.get(1));
		Assertions.assertEquals("country.specificName='100_Percents' && country.name='100_Percents'", repositoryPermissions.get(2));

		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = objectMapper.readValue(repositoryPermissions.get(3), Map.class);
		Assertions.assertEquals(4, map.size());
		Assertions.assertEquals("_100_Percents", map.get("value"));
		Assertions.assertTrue(map.get("array") instanceof Collection);
		Collection<?> list = (Collection<?>) map.get("array");
		Assertions.assertEquals(2, list.size());
	}

	@Test
	public void repositoryTemplateRef() {

		IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			uaaPolicyDecisionPoint.evaluateRepositoryPermissions(createDefaultTestResource(),
				authorizationDefinitionRepository.getPermissionsByScope("RespositoryRef"));
		});
		Assertions.assertEquals("SpEL policy processor doesn't support template generation", exception.getMessage());
	}

	@Test
	public void checkPermissionFiltering() {
		checkPermission("Filtering", null, false, true);
	}

	@Test
	@WithMockUser(username = "admin", authorities = { "manager", "guest" })
	public void propertyPermissionsWithSuccessAdmin() throws Exception {
		TestResource resource = checkPropertyPermissionMask("Admin Property Permission", false, true);
		Assertions.assertNotNull(resource.getFirst());
		Assertions.assertNotNull(resource.getSecond());
	}

	@Test
	@WithMockUser(username = "guest", authorities = { "guest" })
	public void propertyPermissionsWithSuccessGuest() throws Exception {
		TestResource resource = checkPropertyPermissionMask("Guest Property Permission", false, true);
		Assertions.assertNotNull(resource.getFirst());
		if (securityBypass.isSecurityBypassRunning()) {
			Assertions.assertNotNull(resource.getSecond());
		} else {
			Assertions.assertNull(resource.getSecond());
		}
	}

	@Test
	@WithMockUser(username = "unknown")
	public void propertyPermissionsWithFailureUnknown() throws Exception {
		TestResource resource = checkPropertyPermissionMask(null, true, false);
		if (securityBypass.isSecurityBypassRunning()) {
			Assertions.assertNotNull(resource.getFirst());
			Assertions.assertNotNull(resource.getSecond());
		} else {
			Assertions.assertNull(resource.getFirst());
			Assertions.assertNull(resource.getSecond());
		}
	}

	@Test
	@WithMockUser(username = "guest", authorities = { "guest" })
	public void propertyPermissionsWithFailure() throws Exception {
		checkPropertyPermissionMask(null, false, true);
	}

	@Test
	@WithMockUser(username = "admin", authorities = { "manager", "guest" })
	public void propertyPermissionsUpdateWithSuccess() throws Exception {
		checkPropertyPermissionForChanges(true);
	}

	@Test
	@WithMockUser(username = "unknown", authorities = { "guest" })
	public void propertyPermissionsUpdateWithNoPermission() throws Exception {
		checkPropertyPermissionForChanges(false);
	}

	@Test
	@WithMockUser(username = "guest", authorities = { "guest" })
	public void propertyPermissionsUpdateWithNoPropertyRights() throws Exception {
		checkPropertyPermissionForChanges(false);
	}

	private void checkPermission(String scope) {
		checkPermission(scope, null, false);
	}

	private void checkPermission(String scope, String policyName, boolean passed) {
		checkPermission(scope, policyName, policyName == null, passed);
	}

	private void checkPermission(String scope, String policyName, boolean emptyPassedPermissions, boolean passed) {
		PermissionCheckResult<Permission> permissionCheckResult =
			uaaPolicyDecisionPoint.hasPermission(createDefaultTestResource(), authorizationDefinitionRepository.getPermissionsByScope(scope));
		//when running in security bypass everything need to pass
		if (securityBypass.isSecurityBypassRunning()) {
			Assertions.assertTrue(permissionCheckResult.isPassed());
			Assertions.assertTrue(permissionCheckResult.getPassedPermissions().isEmpty());
			return;
		}
		if (policyName != null) {
			Assertions.assertEquals(policyName, permissionCheckResult.getPassedPermissions().get(0).getName());
		} else {
			Assertions.assertEquals(emptyPassedPermissions, permissionCheckResult.getPassedPermissions().isEmpty());
		}
		Assertions.assertEquals(passed, permissionCheckResult.isPassed());
	}

	private TestResource checkPropertyPermissionMask(String policyName, boolean emptyPassedPermissions, boolean passed) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetails principal = (UserDetails) authentication.getPrincipal();
		TestResource resource = createDefaultTestResource();
		PermissionCheckResult<PropertyPermission> permissionCheckResult =
			uaaPolicyDecisionPoint.checkPropertyPermissionsAndMaskData(resource, principal, authorizationDefinitionRepository.getPropertyPermission());
		//when running in security bypass everything need to pass
		if (securityBypass.isSecurityBypassRunning()) {
			Assertions.assertTrue(permissionCheckResult.isPassed());
			Assertions.assertTrue(permissionCheckResult.getPassedPermissions().isEmpty());
			return resource;
		}
		if (policyName != null) {
			Assertions.assertEquals(permissionCheckResult.getPassedPermissions().get(0).getName(), policyName);
		} else {
			Assertions.assertEquals(emptyPassedPermissions, permissionCheckResult.getPassedPermissions().isEmpty());
		}
		Assertions.assertEquals(passed, permissionCheckResult.isPassed());
		return resource;
	}

	private void checkPropertyPermissionForChanges(boolean passed) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetails principal = (UserDetails) authentication.getPrincipal();
		TestResource updatedResource = new TestResource("two", 3);
		Boolean permissionCheckResult = uaaPolicyDecisionPoint.checkPropertyPermissionsForChanges(createDefaultTestResource(), updatedResource, principal,
			authorizationDefinitionRepository.getPropertyPermission());
		//when running in security bypass everything need to pass
		if (securityBypass.isSecurityBypassRunning()) {
			Assertions.assertTrue(permissionCheckResult);
			return;
		}
		Assertions.assertEquals(passed, permissionCheckResult);
	}

	private TestResource createDefaultTestResource() {
		return new TestResource("one", 2);
	}

	@Configuration
	static class TestConfiguration {

		@Bean
		public AuthorizationDefinitionRepository createAuthorizationDefinitionRepository() {
			return new RuntimeAuthorizationDefinitionRepository();
		}

		@Bean
		public AuthorizationDefinitionService createAuthorizationDefinitionService() {
			return new AuthorizationDefinitionService("classpath:testAuthorizationDefinition.json", null);
		}

		@Bean
		public PolicyProcessorFactory spelFactory() {
			return new SpelPolicyProcessorFactory();
		}

		@Bean
		public DataMasking createDataMasking() {
			return new UAADataMasking(createAuthorizationDefinitionRepository());
		}

		@Bean
		public PropertyChangesChecker createChangesChecker() {
			return new PropertyChangesChecker(Arrays.asList("com.mgmtp"));
		}

		@Bean
		public PropertyRightsValidator createPropertyRightsValidator() {
			return new PropertyRightsValidator();
		}
	}

}
