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
package com.mgmtp.a12.uaa.authentication.principal.oauth;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mgmtp.a12.uaa.authentication.principal.RoleMappingDataHolder;
import com.mgmtp.a12.uaa.authentication.principal.RoleMappingLoader;
import com.mgmtp.a12.uaa.authentication.principal.internal.YamlRoleMappingLoader;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AccessRightLoaderTest {

	@Inject
	private RoleMappingLoader accessRightLoader;

	@Test
	public void checkLoader() {
		RoleMappingDataHolder dataHolder = accessRightLoader.loadData(null);
		Assertions.assertEquals(2, dataHolder.getRoles().size());
		Assertions.assertEquals(2, dataHolder.getRoleByName("guest").getAccessRights().size());
		Assertions.assertEquals(2, dataHolder.getRoleByName("admin").getAccessRights().size());

	}

	@Configuration
	static class TestConfig {

		@Bean
		public RoleMappingLoader createLoader() {
			return new YamlRoleMappingLoader(new ClassPathResource("access_rights.yaml"));
		}
	}

}
