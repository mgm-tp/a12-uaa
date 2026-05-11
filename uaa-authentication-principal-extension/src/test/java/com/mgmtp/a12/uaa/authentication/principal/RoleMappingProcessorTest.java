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
package com.mgmtp.a12.uaa.authentication.principal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import com.mgmtp.a12.uaa.authentication.principal.a12internal.RoleMappingProcessor;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RoleMappingProcessorTest {

	@Mock
	private TestRoleMappingLoader roleMappingLoader;
	@Spy
	private List<RoleMappingLoader<?>> roleMappingLoaders = new ArrayList<>();
	@Mock
	private PrincipalFactory userFactory;
	@InjectMocks
	private RoleMappingProcessor roleMappingProcessor;
	@Captor
	private ArgumentCaptor<String> usernameCaptor;
	@Captor
	private ArgumentCaptor<String> passwordCaptor;
	@Captor
	private ArgumentCaptor<Collection<? extends GrantedAuthority>> rolesCaptor;
	@Captor
	private ArgumentCaptor<Object> extendedDataCaptor;

	@BeforeEach
	void setUp() {
		RoleDefinition roleDef = new RoleDefinition("testRole");
		roleDef.addAccessRight("testAccessRight");
		RoleMappingDataHolder holder = new RoleMappingDataHolder();
		holder.setRoles(Arrays.asList(roleDef));
		Mockito.when(roleMappingLoader.loadData(Mockito.any())).thenReturn(holder);
		roleMappingLoaders.add(roleMappingLoader);
		ReflectionTestUtils.setField(roleMappingProcessor, "roleMappingLoaders", Optional.of(roleMappingLoaders));
	}

	@Test
	public void checkRoleMappingWithRole() {
		ExtendedPrincipal user = new ExtendedPrincipal("test", "", Arrays.asList(new Role.Builder("testRole").build()));
		checkMappedRoles(user);
	}

	@Test
	public void checkRoleMappingWithGrantedAuthority() {
		ExtendedPrincipal user = new ExtendedPrincipal("test", "", Arrays.asList(new SimpleGrantedAuthority("testRole")));
		checkMappedRoles(user);
	}

	@Test
	public void checkOriginalAuthorityIsReturn() {
		ReflectionTestUtils.setField(roleMappingProcessor, "roleMappingLoaders", Optional.empty());
		ExtendedPrincipal user = new ExtendedPrincipal("test", "", Arrays.asList(new SimpleGrantedAuthority("testRole")));
		checkCanNotMappedRoles(user);
	}

	private void checkMappedRoles(ExtendedPrincipal input) {
		roleMappingProcessor.populateRightsFromSource(input, "any Payload");
		Mockito.verify(userFactory).createPrincipal(usernameCaptor.capture(), passwordCaptor.capture(),
			rolesCaptor.capture(), extendedDataCaptor.capture());
		Collection<? extends GrantedAuthority> roles = rolesCaptor.getValue();
		Assertions.assertTrue(CollectionUtils.isNotEmpty(roles));
		GrantedAuthority grantedAuthority = roles.iterator().next();
		Assertions.assertTrue((grantedAuthority instanceof Role));
		Role role = (Role) grantedAuthority;
		Assertions.assertEquals(1, role.getAccessRights().size());
		Assertions.assertEquals("testAccessRight", role.getAccessRights().iterator().next().getName());
	}

	private void checkCanNotMappedRoles(ExtendedPrincipal input) {
		roleMappingProcessor.populateRightsFromSource(input, "any Payload");
		Mockito.verify(userFactory).createPrincipal(usernameCaptor.capture(), passwordCaptor.capture(), rolesCaptor.capture(), extendedDataCaptor.capture());
		Collection<? extends GrantedAuthority> roles = rolesCaptor.getValue();
		Assertions.assertTrue(CollectionUtils.isNotEmpty(roles));
		GrantedAuthority grantedAuthority = roles.iterator().next();
		Assertions.assertFalse((grantedAuthority instanceof Role));
	}

	static class TestRoleMappingLoader implements RoleMappingLoader<String> {

		@Override
		public RoleMappingDataHolder loadData(String payload) {
			return null;
		}

		@Override
		public RoleMappingDataHolder updateData(String data) throws UnableToUpdateMappingException {
			return null;
		}
	}

}
