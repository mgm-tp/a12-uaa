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
package com.mgmtp.a12.uaa.authorization.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionService;
import com.mgmtp.a12.uaa.authorization.model.AuthorizationDefinition;
import com.mgmtp.a12.uaa.authorization.model.internal.AuthorizationDefinitionAdapter;

@ExtendWith(SpringExtension.class)
public class AuthorizationDefinitionServiceTest {

	private static final List<String> AUTHORIZATION_DEFINITION_JSONS = Arrays.asList("classpath:testAuthorizationDefinition.json",
		"classpath:testAuthorizationDefinition_additional.json", "classpath:testAuthorizationDefinition_additional2.json");

	private final ObjectMapper mapper = new ObjectMapper();

	@Inject
	private ResourceLoader loader;

	@Test
	public void runSuccessful() throws IOException {
		// Expected
		List<AuthorizationDefinition> authorizationDefinitions = new ArrayList<>();
		for (String path : AUTHORIZATION_DEFINITION_JSONS) {
			authorizationDefinitions.add(mapper.readValue(loader.getResource(path).getInputStream(), AuthorizationDefinitionAdapter.class));
		}

		Assertions.assertEquals(authorizationDefinitions.get(0), InMemoryAuthorizationDefinitionDataHolder.getParent());
		Assertions.assertEquals(merge(authorizationDefinitions.get(2), authorizationDefinitions.get(1)), InMemoryAuthorizationDefinitionDataHolder.getChild());
	}

	private AuthorizationDefinition merge(AuthorizationDefinition source, AuthorizationDefinition dest) {
		dest.getPolicies().addAll(source.getPolicies());
		dest.getPropertyRights().addAll(source.getPropertyRights());
		dest.getRepositoryPolicies().addAll(source.getRepositoryPolicies());
		dest.getPermissions().addAll(source.getPermissions());
		dest.getPropertyPermissions().addAll(source.getPropertyPermissions());
		return dest;
	}

	@Configuration
	static class TestConfig {
		@Bean
		public AuthorizationDefinitionService crAuthorizationDefinitionService() {
			return new AuthorizationDefinitionService(AUTHORIZATION_DEFINITION_JSONS.get(0),
				Arrays.asList(AUTHORIZATION_DEFINITION_JSONS.get(1), AUTHORIZATION_DEFINITION_JSONS.get(2)));
		}

		@Bean
		public RuntimeAuthorizationDefinitionRepository createAuthorizationDefRepository() {
			return new RuntimeAuthorizationDefinitionRepository();
		}
	}
}
