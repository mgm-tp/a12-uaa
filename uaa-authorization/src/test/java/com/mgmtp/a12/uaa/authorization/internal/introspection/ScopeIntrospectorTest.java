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
package com.mgmtp.a12.uaa.authorization.internal.introspection;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionService;
import com.mgmtp.a12.uaa.authorization.internal.RuntimeAuthorizationDefinitionRepository;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
public class ScopeIntrospectorTest {

	private static final List<String> AUTHORIZATION_DEFINITION_JSONS =
		Arrays.asList("classpath:testAuthorizationDefinition.json", "classpath:testAuthorizationDefinition_additional.json");

	private final ObjectMapper mapper = new ObjectMapper();

	private Environment environment;

	@Inject
	private ResourceLoader loader;

	@Inject
	private RuntimeAuthorizationDefinitionRepository authorizationDefRepository;

	@BeforeEach
	void beforeEach() {
		this.environment = Mockito.mock(Environment.class);
	}

	@Test
	void testIntrospect_EmptyRules() {
		RuntimeAuthorizationDefinitionRepository mockRepo = Mockito.mock(RuntimeAuthorizationDefinitionRepository.class);
		Mockito.when(mockRepo.getPermissions()).thenReturn(new HashSet<>());
		boolean result =
			AuthorizationIntrospectorFactory.getInstance(mockRepo, environment)
				.getScopeIntrospector()
				.process();

		Assertions.assertTrue(result);
	}

	@Test
	void testIntrospect_orphanScopes() {
		Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("url::inline policies success");
		boolean result =
			AuthorizationIntrospectorFactory.getInstance(authorizationDefRepository, environment)
				.getScopeIntrospector()
				.process();

		Assertions.assertFalse(result);
	}

	@Configuration
	static class TestConfig {
		@Bean
		public AuthorizationDefinitionService crAuthorizationDefinitionService() {
			return new AuthorizationDefinitionService(AUTHORIZATION_DEFINITION_JSONS.get(0),
				List.of(AUTHORIZATION_DEFINITION_JSONS.get(1)));
		}

		@Bean
		public RuntimeAuthorizationDefinitionRepository createAuthorizationDefRepository() {
			return new RuntimeAuthorizationDefinitionRepository();
		}
	}
}
