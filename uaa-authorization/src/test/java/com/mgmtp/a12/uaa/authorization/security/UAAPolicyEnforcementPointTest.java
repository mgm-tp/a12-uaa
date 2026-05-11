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
package com.mgmtp.a12.uaa.authorization.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.javers.core.diff.custom.CustomPropertyComparator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;

import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.exception.MissingPermissionException;
import com.mgmtp.a12.uaa.authorization.internal.UAAUserDetails;
import com.mgmtp.a12.uaa.authorization.model.AuthorizationModelFactory;
import com.mgmtp.a12.uaa.authorization.model.Permission;
import com.mgmtp.a12.uaa.authorization.model.PolicyAware;
import com.mgmtp.a12.uaa.authorization.model.PropertyPermission;
import com.mgmtp.a12.uaa.authorization.property.PropertyChangePathConverter;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyChangeConverter;
import com.mgmtp.a12.uaa.authorization.property.internal.ResourceConverter;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UAAPolicyEnforcementPointTest {

	private UAAPolicyEnforcementPoint uaaPolicyEnforcementPoint;
	@Mock
	private PolicyDecisionPoint policyDecisionPoint;
	@Mock
	private AuthorizationDefinitionRepository authorizationDefinitionRepository;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private UserDetails userDetails;
	@Mock
	private Authentication authentication;
	@Spy
	private Optional<ResourceConverter> resourceConverter = Optional.empty();
	@Spy
	private Optional<List<CustomPropertyComparator>> propertyComparators = Optional.empty();
	@Spy
	private Optional<List<PropertyChangePathConverter>> propertyChangeConverters = Optional.of(Arrays.asList(new PropertyChangeConverter()));
	@InjectMocks
	private PropertyChangesChecker propertyChangesChecker = new PropertyChangesChecker(Arrays.asList("com.mgmtp"));

	@BeforeEach
	void setUp() {
		uaaPolicyEnforcementPoint = new UAAPolicyEnforcementPoint(policyDecisionPoint, authorizationDefinitionRepository);
	}

	@Test
	void testCheckPermissionsThrowErrorIfPermissionsIsEmpty() {
		Set<Permission> permissionList = new LinkedHashSet<>();
		Mockito.when(authorizationDefinitionRepository.getPermissionsByScope("read")).thenReturn(permissionList);
		Exception exception = Assertions.assertThrows(Exception.class, () -> uaaPolicyEnforcementPoint.checkPermissions("data", "read"));
		Assertions.assertEquals(MissingPermissionException.class, exception.getClass());
		Assertions.assertEquals("Unable to find Permission named [read]", exception.getMessage());
	}

	@Test
	void testCheckPermissionsReturnPermissionCheckResult() {
		Set<Permission> permissions = new LinkedHashSet<>();
		permissions.add(new Permission());
		Mockito.when(authorizationDefinitionRepository.getPermissionsByScope("read")).thenReturn(permissions);
		PermissionCheckResult<Permission> permissionCheckResult = new PermissionCheckResult.Builder<>(true, convert(permissions)).build();
		Mockito.when(policyDecisionPoint.hasPermission("data", permissions)).thenReturn(permissionCheckResult);
		Object result = uaaPolicyEnforcementPoint.checkPermissions("data", "read");
		Assertions.assertNotNull(result);
		Assertions.assertEquals(permissionCheckResult, result);
	}
	
	private <T extends PolicyAware> List<PermissionEvaluationResult<T>> convert(Set<T> passedPermissions) {
		return passedPermissions.stream()
			.map(permission -> new PermissionEvaluationResult<>(permission, new PolicyEvaluationResult(true, Collections.emptySet(), Collections.emptySet())))
			.collect(Collectors.toList());
	}

	@Test
	void testCheckPropertyPermissions() {
		Set<PropertyPermission> propertyPermissions = new LinkedHashSet<>();
		UserDetails user = new UAAUserDetails() {

			@Override
			public boolean isEnabled() {
				return false;
			}

			@Override
			public boolean isCredentialsNonExpired() {
				return false;
			}

			@Override
			public boolean isAccountNonLocked() {
				return false;
			}

			@Override
			public boolean isAccountNonExpired() {
				return false;
			}

			@Override
			public String getUsername() {
				return null;
			}

			@Override
			public String getPassword() {
				return null;
			}

			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				return null;
			}
		};
		Mockito.when(authorizationDefinitionRepository.getPropertyPermission()).thenReturn(propertyPermissions);
		PermissionCheckResult<PropertyPermission> permissionCheckResult = new PermissionCheckResult.Builder<>(true, convert(propertyPermissions)).build();
		Mockito.when(policyDecisionPoint.hasPermission("data", propertyPermissions)).thenReturn(permissionCheckResult);
		Object result = uaaPolicyEnforcementPoint.checkPropertyPermissionsAndMaskData("data", user);
		Mockito.verify(policyDecisionPoint, Mockito.times(1)).checkPropertyPermissionsAndMaskData(Mockito.eq("data"), Mockito.eq(user), Mockito.any());
	}

	@Test
	void repositoryAuthorization() {
		Permission permission = AuthorizationModelFactory.createPermission(Set.of("Repository"));
		Set<Permission> permissions = new LinkedHashSet<>();
		permissions.add(permission);
		Mockito.when(authorizationDefinitionRepository.getPermissionsByScope("read")).thenReturn(permissions);
		Mockito.when(policyDecisionPoint.evaluateRepositoryPermissions("data", permissions)).thenReturn(Set.of("'hello'"));
		List<String> repositoryOutput = new ArrayList<>(uaaPolicyEnforcementPoint.generateRepositoryPermissions("data", "read"));
		Assertions.assertNotNull(repositoryOutput);
		Assertions.assertEquals("'hello'", repositoryOutput.get(0));
	}

}
