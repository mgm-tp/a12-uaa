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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.integration.TestUserDetails;
import com.mgmtp.a12.uaa.authorization.model.AuthorizationModelFactory;
import com.mgmtp.a12.uaa.authorization.model.Permission;
import com.mgmtp.a12.uaa.authorization.model.Policy;
import com.mgmtp.a12.uaa.authorization.model.PolicyAware;
import com.mgmtp.a12.uaa.authorization.model.PropertyPermission;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyRightsValidator;
import com.mgmtp.a12.uaa.authorization.security.DataMasking;
import com.mgmtp.a12.uaa.authorization.security.PermissionCheckResult;
import com.mgmtp.a12.uaa.authorization.security.PolicyProcessorFactory;
import com.mgmtp.a12.uaa.authorization.security.PropertyChangesChecker;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UAAPolicyDecisionPointTest {

	private StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
	private SpelPolicyProcessor spelPolicyProcessor = new SpelPolicyProcessor(evaluationContext);
	@Mock
	private PolicyProcessorFactory policyProcessorFactory;
	@Mock
	private AuthorizationDefinitionRepository authorizationDefinitionRepository;
	@Mock
	private PropertyRightsValidator propertyRightsValidator;
	@Mock
	private DataMasking dataMasking;
	@Mock
	private PropertyChangesChecker propertyChangesChecker;

	private UAAPolicyDecisionPoint policyDecisionPoint;

	@BeforeEach
	void setUp() {
		UserDetails user = new TestUserDetails("test");

		Authentication auth = new UsernamePasswordAuthenticationToken(user, null);
		SecurityContextHolder.getContext().setAuthentication(auth);
		Mockito.when(policyProcessorFactory.createPolicyProcessor(Mockito.any())).thenReturn(spelPolicyProcessor);
		Policy passingPolicy = AuthorizationModelFactory.createPolicy("Passing Policy", Set.of("true"));
		Policy failingPolicy = AuthorizationModelFactory.createPolicy("Failing Policy", Set.of("false"));
		Mockito.when(authorizationDefinitionRepository.getPolicyByName(Mockito.eq("passing"))).thenReturn(Optional.of(passingPolicy));
		Mockito.when(authorizationDefinitionRepository.getPolicyByName(Mockito.eq("failing"))).thenReturn(Optional.of(failingPolicy));
		Mockito.when(propertyRightsValidator.validateChanges(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
		Mockito.when(propertyChangesChecker.checkPropertyPermissionForChanges(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);

		policyDecisionPoint = new UAAPolicyDecisionPoint(evaluationContext, authorizationDefinitionRepository, Arrays.asList(policyProcessorFactory),
			propertyRightsValidator, dataMasking, propertyChangesChecker);
	}

	@Test
	public void hasPermissionWithOneSuccessPolicy() {
		Permission permission = AuthorizationModelFactory.createPermissionWithPolicyRefs("Success", Set.of("passing"));
		PermissionCheckResult<PolicyAware> permissionCheckResult = policyDecisionPoint.hasPermission(new Object(), Set.of(permission));
		Assertions.assertTrue(permissionCheckResult.isPassed());
	}

	@Test
	public void hasPermissionWithOneFailurePolicy() {
		Permission permission = AuthorizationModelFactory.createPermissionWithPolicyRefs("Failing", Set.of("failing"));
		PermissionCheckResult<PolicyAware> permissionCheckResult = policyDecisionPoint.hasPermission(new Object(), Set.of(permission));
		Assertions.assertTrue(permissionCheckResult.isNotPassed());
	}

	@Test
	public void hasPermissionWithOnePassingAndOneFailurePolicy() {
		Permission permission = AuthorizationModelFactory.createPermissionWithPolicyRefs("Failing", Set.of("failing", "passing"));
		PermissionCheckResult<PolicyAware> permissionCheckResult = policyDecisionPoint.hasPermission(new Object(), Set.of(permission));
		Assertions.assertTrue(permissionCheckResult.isNotPassed());
		List<PolicyAware> passedPermissions = permissionCheckResult.getPassedPermissions();
		Assertions.assertEquals(0, passedPermissions.size());
		List<PolicyAware> failedPermissions = permissionCheckResult.getFailedPermissions();
		Assertions.assertEquals(1, failedPermissions.size());
		Assertions.assertEquals("Failing", failedPermissions.get(0).getName());
	}

	@Test
	public void hasPermissionWithOnePassingAndOneFailurePermission() {
		Permission passingPermission = AuthorizationModelFactory.createPermissionWithPolicyRefs("Passing Permission", Set.of("passing"));
		Permission failingPermission = AuthorizationModelFactory.createPermissionWithPolicyRefs("Failing Permission", Set.of("failing"));
		PermissionCheckResult<PolicyAware> permissionCheckResult =
			policyDecisionPoint.hasPermission(new Object(), Set.of(passingPermission, failingPermission));
		Assertions.assertTrue(permissionCheckResult.isNotPassed());
		List<PolicyAware> passedPermissions = permissionCheckResult.getPassedPermissions();
		Assertions.assertEquals(1, passedPermissions.size());
		Assertions.assertEquals("Passing Permission", passedPermissions.get(0).getName());
		List<PolicyAware> failedPermissions = permissionCheckResult.getFailedPermissions();
		Assertions.assertEquals(1, failedPermissions.size());
		Assertions.assertEquals("Failing Permission", failedPermissions.get(0).getName());
	}

	@Test
	public void hasPermissionWithEmptyPermission() {
		PermissionCheckResult<PolicyAware> permissionCheckResult = policyDecisionPoint.hasPermission(new Object(), Collections.emptySet());
		Assertions.assertTrue(permissionCheckResult.isNotPassed());
	}

	@Test
	public void hasPermissionWithEmptyPolicies() {
		Permission permission = AuthorizationModelFactory.createPermissionWithPolicyRefs("Empty", Collections.emptySet());
		PermissionCheckResult<PolicyAware> permissionCheckResult = policyDecisionPoint.hasPermission(new Object(), Set.of(permission));
		Assertions.assertTrue(permissionCheckResult.isPassed());
	}

	@Test
	public void checkPropertyPermissionAndMaskDataWithOneSuccessPolicy() {
		UserDetails user = new TestUserDetails("test");
		PropertyPermission propertyPermission = AuthorizationModelFactory.createPropertyPermissionWithPolicyRefs("Success", Set.of("passing"));
		PermissionCheckResult<PropertyPermission> permissionCheckResult =
			policyDecisionPoint.checkPropertyPermissionsAndMaskData(new Object(), user, Set.of(propertyPermission));
		Assertions.assertTrue(permissionCheckResult.isPassed());
	}

	@Test
	public void checkPropertyPermissionAndMaskDataWithOneFailurePolicy() {
		UserDetails user = new TestUserDetails("test");
		PropertyPermission propertyPermission = AuthorizationModelFactory.createPropertyPermissionWithPolicyRefs("Failing", Set.of("failing"));
		PermissionCheckResult<PropertyPermission> permissionCheckResult =
			policyDecisionPoint.checkPropertyPermissionsAndMaskData(new Object(), user, Set.of(propertyPermission));
		Assertions.assertTrue(permissionCheckResult.isNotPassed());
	}

	@Test
	public void checkPropertyPermissionAndMaskDataWithOnePassingAndOneFailurePolicy() {
		UserDetails user = new TestUserDetails("test");
		PropertyPermission propertyPermission = AuthorizationModelFactory.createPropertyPermissionWithPolicyRefs("Failing", Set.of("failing", "passing"));
		PermissionCheckResult<PropertyPermission> permissionCheckResult =
			policyDecisionPoint.checkPropertyPermissionsAndMaskData(new Object(), user, Set.of(propertyPermission));
		Assertions.assertTrue(permissionCheckResult.isNotPassed());
		List<PropertyPermission> passedPermissions = permissionCheckResult.getPassedPermissions();
		Assertions.assertEquals(0, passedPermissions.size());
		List<PropertyPermission> failedPermissions = permissionCheckResult.getFailedPermissions();
		Assertions.assertEquals(1, failedPermissions.size());
		Assertions.assertEquals("Failing", failedPermissions.get(0).getName());
	}

	@Test
	public void checkPropertyPermissionAndMaskDataWithOnePassingAndOneFailurePermission() {
		UserDetails user = new TestUserDetails("test");
		PropertyPermission passingPermission = AuthorizationModelFactory.createPropertyPermissionWithPolicyRefs("Passing Permission", Set.of("passing"));
		PropertyPermission failingPermission = AuthorizationModelFactory.createPropertyPermissionWithPolicyRefs("Failing Permission", Set.of("failing"));
		PermissionCheckResult<PropertyPermission> permissionCheckResult =
			policyDecisionPoint.checkPropertyPermissionsAndMaskData(new Object(), user, Set.of(passingPermission, failingPermission));
		//here we use 
		Assertions.assertTrue(permissionCheckResult.isPassed());
		List<PropertyPermission> passedPermissions = permissionCheckResult.getPassedPermissions();
		Assertions.assertEquals(1, passedPermissions.size());
		Assertions.assertEquals("Passing Permission", passedPermissions.get(0).getName());
		List<PropertyPermission> failedPermissions = permissionCheckResult.getFailedPermissions();
		Assertions.assertEquals(1, failedPermissions.size());
		Assertions.assertEquals("Failing Permission", failedPermissions.get(0).getName());
	}

	@Test
	public void checkPropertyPermissionAndMaskDataWithEmptyPermission() {
		UserDetails user = new TestUserDetails("test");
		PermissionCheckResult<PropertyPermission> permissionCheckResult =
			policyDecisionPoint.checkPropertyPermissionsAndMaskData(new Object(), user, Collections.emptySet());
		Assertions.assertTrue(permissionCheckResult.isPassed());
	}

	@Test
	public void checkPropertyPermissionAndMaskDataWithEmptyPolicies() {
		UserDetails user = new TestUserDetails("test");
		PropertyPermission propertyPermission = AuthorizationModelFactory.createPropertyPermissionWithPolicyRefs("Empty", Collections.emptySet());
		PermissionCheckResult<PropertyPermission> permissionCheckResult =
			policyDecisionPoint.checkPropertyPermissionsAndMaskData(new Object(), user, Set.of(propertyPermission));
		Assertions.assertTrue(permissionCheckResult.isPassed());
	}

	@Test
	public void checkPropertyPermissionAndMaskDataWithFailingPolicy() {
		UserDetails user = new TestUserDetails("test");
		PropertyPermission propertyPermission = AuthorizationModelFactory.createPropertyPermissionWithPolicyRefs("Failing", Set.of("failing"));
		PermissionCheckResult<PropertyPermission> permissionCheckResult =
			policyDecisionPoint.checkPropertyPermissionsAndMaskData(new Object(), user, Set.of(propertyPermission));
		Assertions.assertTrue(permissionCheckResult.isNotPassed());
	}

	@Test
	public void checkPropertyPermissionsForChangesWithOneSuccessPolicy() {
		UserDetails user = new TestUserDetails("test");
		PropertyPermission propertyPermission = AuthorizationModelFactory.createPropertyPermissionWithPolicyRefs("Success", Set.of("passing"));
		Boolean permissionCheckResult =
			policyDecisionPoint.checkPropertyPermissionsForChanges(new Object(), new Object(), user, Set.of(propertyPermission));
		Assertions.assertTrue(permissionCheckResult);
	}

	@Test
	public void checkPropertyPermissionsForChangesWithOneFailurePolicy() {
		UserDetails user = new TestUserDetails("test");
		PropertyPermission propertyPermission = AuthorizationModelFactory.createPropertyPermissionWithPolicyRefs("Failing", Set.of("failing"));
		Boolean permissionCheckResult =
			policyDecisionPoint.checkPropertyPermissionsForChanges(new Object(), new Object(), user, Set.of(propertyPermission));
		Assertions.assertFalse(permissionCheckResult);
	}

	@Test
	public void checkPropertyPermissionsForChangesWithOnePassingAndOneFailurePolicy() {
		UserDetails user = new TestUserDetails("test");
		PropertyPermission propertyPermission = AuthorizationModelFactory.createPropertyPermissionWithPolicyRefs("Failing", Set.of("failing", "passing"));
		Boolean permissionCheckResult =
			policyDecisionPoint.checkPropertyPermissionsForChanges(new Object(), new Object(), user, Set.of(propertyPermission));
		Assertions.assertFalse(permissionCheckResult);
	}

	@Test
	public void checkPropertyPermissionsForChangesWithOnePassingAndOneFailurePermission() {
		UserDetails user = new TestUserDetails("test");
		PropertyPermission passingPermission = AuthorizationModelFactory.createPropertyPermissionWithPolicyRefs("Passing Permission", Set.of("passing"));
		PropertyPermission failingPermission = AuthorizationModelFactory.createPropertyPermissionWithPolicyRefs("Failing Permission", Set.of("failing"));
		Boolean permissionCheckResult =
			policyDecisionPoint.checkPropertyPermissionsForChanges(new Object(), new Object(), user, Set.of(passingPermission, failingPermission));
		//here we use 
		Assertions.assertTrue(permissionCheckResult);
	}

	@Test
	public void checkPropertyPermissionsForChangesWithEmptyPermission() {
		UserDetails user = new TestUserDetails("test");
		Boolean permissionCheckResult =
			policyDecisionPoint.checkPropertyPermissionsForChanges(new Object(), new Object(), user, Collections.emptySet());
		Assertions.assertFalse(permissionCheckResult);
	}

	@Test
	public void checkPropertyPermissionsForChangesWithEmptyPolicies() {
		UserDetails user = new TestUserDetails("test");
		PropertyPermission propertyPermission = AuthorizationModelFactory.createPropertyPermissionWithPolicyRefs("Empty", Collections.emptySet());
		Boolean permissionCheckResult =
			policyDecisionPoint.checkPropertyPermissionsForChanges(new Object(), new Object(), user, Set.of(propertyPermission));
		Assertions.assertTrue(permissionCheckResult);
	}

	@Test
	public void checkPropertyPermissionsForChangesWithFailingPolicy() {
		UserDetails user = new TestUserDetails("test");
		PropertyPermission propertyPermission = AuthorizationModelFactory.createPropertyPermissionWithPolicyRefs("Failing", Set.of("failing"));
		Boolean permissionCheckResult =
			policyDecisionPoint.checkPropertyPermissionsForChanges(new Object(), new Object(), user, Set.of(propertyPermission));
		Assertions.assertFalse(permissionCheckResult);
	}

}
