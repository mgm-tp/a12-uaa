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
package com.mgmtp.a12.uaa.authentication.ldap;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import com.mgmtp.a12.uaa.authentication.ldap.internal.UAALdapUserDetailMapper;
import com.mgmtp.a12.uaa.authentication.local.UAAExtendedPrincipalDataLoader;
import com.mgmtp.a12.uaa.authentication.principal.internal.UAALdapPrincipal;
import com.mgmtp.a12.uaa.authentication.utils.UserDataCreator;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UAALdapUserDetailMapperTest {
	@Mock
	private DirContextOperations ctx;

	@Mock
	private UAAExtendedPrincipalDataLoader extendedPrincipalDataLoader;

	@InjectMocks
	private UAALdapUserDetailMapper userDetailMapper;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(userDetailMapper, "extendedPrincipalDataLoader", Optional.of(extendedPrincipalDataLoader));
		LdapNameBuilder builder = LdapNameBuilder.newInstance("dc=jayway,dc=se")
			.add("ou", "People")
			.add("uid", "adam.skogman");

		String dn = builder.build().toString();

		ctx = new DirContextAdapter(dn);
		Mockito.when(extendedPrincipalDataLoader.loadExtendedPrincipalData(Mockito.anyString())).thenReturn(
			UserDataCreator.createTestSubData());
	}

	@Test
	public void mapUserFromContextText() {
		UserDetails userDetails = userDetailMapper.mapUserFromContext(ctx, "userName", Collections.emptyList());
		Assertions.assertEquals(UAALdapPrincipal.class, userDetails.getClass());
		Assertions.assertNotNull(((UAALdapPrincipal) userDetails).getExtendedPrincipalData());
	}

}
