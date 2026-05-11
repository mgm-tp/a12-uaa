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
package com.mgmtp.a12.uaa.authorization.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.javers.core.diff.custom.CustomPropertyComparator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.mgmtp.a12.uaa.authorization.AuthorizationContext;
import com.mgmtp.a12.uaa.authorization.AuthorizationContextHolder;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.AuthorizationService;
import com.mgmtp.a12.uaa.authorization.exception.MissingPermissionException;
import com.mgmtp.a12.uaa.authorization.property.PropertyChangePathConverter;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyChangeConverter;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyRightsValidator;
import com.mgmtp.a12.uaa.authorization.property.internal.ResourceConverter;
import com.mgmtp.a12.uaa.authorization.security.DataMasking;
import com.mgmtp.a12.uaa.authorization.security.PermissionCheckResult;
import com.mgmtp.a12.uaa.authorization.security.PolicyProcessorFactory;
import com.mgmtp.a12.uaa.authorization.security.PropertyChangesChecker;
import com.mgmtp.a12.uaa.authorization.security.spel.internal.SpelPolicyProcessorFactory;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthorizationServiceTest {

	@Mock
	private SecurityContext securityContext;
	@Mock
	private ApplicationContext applicationContext;
	@Mock
	private AuthorizationDefinitionRepository authorizationDefinitionRepository;
	@Mock
	private UserDetails userDetails;
	@Mock
	private Authentication authentication;
	@Mock
	private PropertyAccessor propertyAccessor;
	@Mock
	private PropertyRightsValidator propertyPermissionValidator;
	@Spy
	private List<PolicyProcessorFactory> processorFactories = new ArrayList<>();
	@Spy
	private Optional<List<PropertyAccessor>> propertyAccessors = Optional.empty();
	@Mock
	private DataMasking dataMasking;
	@InjectMocks
	private AuthorizationService authorizationService = new AuthorizationService();
	@Spy
	private Optional<ResourceConverter> resourceConverter = Optional.empty();
	@Spy
	private Optional<List<CustomPropertyComparator>> propertyComparators = Optional.empty();
	@Spy
	private Optional<List<PropertyChangePathConverter>> propertyChangeConverters = Optional.of(Arrays.asList(new PropertyChangeConverter()));
	@InjectMocks
	private PropertyChangesChecker propertyChangesChecker = new PropertyChangesChecker(Arrays.asList("com.mgmtp"));

	@BeforeEach
	void setUp() throws IllegalAccessException {
		//Mock for private function createEnforcementPoint
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);
		Mockito.when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
		FieldUtils.writeField(authorizationService, "applicationContext", applicationContext, true);
		List<PropertyAccessor> propertyAccessors = new ArrayList<>();
		propertyAccessors.add(propertyAccessor);
		Optional<List<PropertyAccessor>> oPropertyAccessors = Optional.of(propertyAccessors);
		//FieldUtils.writeField(authorizationService, "propertyAccessors", oPropertyAccessors, true);
		processorFactories.add(new SpelPolicyProcessorFactory());
		propertyChangesChecker.initJavers();
		
	}
	
	@AfterAll
	static void tearDown() {
		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		while (authorizationContext.popContext() != null) {
		}
		authorizationContext.setExecutionEnvironment(null);
	}

	@Test
	void checkPermissionsIsPassed() {
		Set<Permission> permissions = new LinkedHashSet<>();
		Permission permission = new Permission();
		permission.setName("string");
		permissions.add(permission);
		Mockito.when(authorizationDefinitionRepository.getPermissionsByScope(ArgumentMatchers.anyString())).thenReturn(permissions);
		PermissionCheckResult<Permission> result = authorizationService.checkPermissions("data", "string");
		Assertions.assertTrue(result.isPassed());
		Assertions.assertEquals("string", result.getPassedPermissions().get(0).getName());
	}

	@Test
	void checkPermissionsIsFailed() {
		Exception exception = Assertions.assertThrows(Exception.class, () -> authorizationService.checkPermissions("data", "string"));
		Assertions.assertTrue(exception instanceof MissingPermissionException);
	}

	@Test
	void checkPropertyPermissionsIsPassed() {
		String resource = "data";
		Mockito.when(dataMasking.maskData(Mockito.any(), Mockito.any())).thenReturn(resource);
		
		Set<PropertyPermission> propertyPermissions = new LinkedHashSet<>();
		PropertyPermission propertyPermission = new PropertyPermission();
		propertyPermission.setName("string");
		propertyPermissions.add(propertyPermission);
		Mockito.when(authorizationDefinitionRepository.getPropertyPermission()).thenReturn(propertyPermissions);
		PermissionCheckResult<PropertyPermission> result = authorizationService.checkPropertyPermissionsAndMaskData(resource);
		Assertions.assertTrue(result.isPassed());
		Assertions.assertEquals("string", result.getPassedPermissions().get(0).getName());
	}

	@Test
	void checkPropertyPermissionsPassWhenEmptyPermission() {
		PermissionCheckResult<PropertyPermission> result = authorizationService.checkPropertyPermissionsAndMaskData("data");
		Assertions.assertTrue(result.isPassed());
	}
}
