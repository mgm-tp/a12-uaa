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
package com.mgmtp.a12.uaa.authentication.user.local;

import jakarta.inject.Inject;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mgmtp.a12.uaa.authentication.principal.PrincipalFactory;
import com.mgmtp.a12.uaa.authentication.principal.RoleMappingLoader;
import com.mgmtp.a12.uaa.authentication.principal.a12internal.RoleMappingProcessor;
import com.mgmtp.a12.uaa.authentication.principal.internal.UAAPrincipalFactory;
import com.mgmtp.a12.uaa.authentication.principal.internal.YamlRoleMappingLoader;
import com.mgmtp.a12.uaa.authentication.user.LocalUser;
import com.mgmtp.a12.uaa.authentication.user.LocalUserLoader;
import com.mgmtp.a12.uaa.authentication.user.internal.UAALocalUserLoader;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserDeserializerTest {

	@Inject
	private ResourceLoader resourceLoader;

	@Inject
	private LocalUserLoader<LocalUser> userLoader;

	@Test
	public void checkDeserializer() throws Exception {
		Resource resource = resourceLoader.getResource("classpath:user.yaml");
		LocalUser user = userLoader.loadUser(resource);
		Assertions.assertEquals("a@a.com", user.getEmail());
		Assertions.assertEquals("First", user.getFirstname());
		Assertions.assertEquals("Last", user.getLastname());
		Assertions.assertEquals("testUser", user.getUsername());
		Assertions.assertEquals(3, user.getAuthorities().size());
		MatcherAssert.assertThat("unknown role exists", user.getAuthorities(), Matchers.hasItem(Matchers.equalTo("unknown")));
		MatcherAssert.assertThat("guest role exists", user.getAuthorities(), Matchers.hasItem(Matchers.equalTo("guest")));
		MatcherAssert.assertThat("admin role exists", user.getAuthorities(), Matchers.hasItem(Matchers.equalTo("admin")));
		//		

	}

	@Configuration
	static class TestConfig {

		@Bean
		public RoleMappingLoader createLoader() {
			return new YamlRoleMappingLoader(new ClassPathResource("access_rights.yaml"));
		}

		@Bean
		public PrincipalFactory userFactory() {
			return new UAAPrincipalFactory();
		}

		@Bean
		public LocalUserLoader<LocalUser> createUserLoader() {
			return new UAALocalUserLoader<LocalUser>(LocalUser.class);
		}

		@Bean
		public RoleMappingProcessor accessRightProcessor() {
			return new RoleMappingProcessor();
		}
	}

}
