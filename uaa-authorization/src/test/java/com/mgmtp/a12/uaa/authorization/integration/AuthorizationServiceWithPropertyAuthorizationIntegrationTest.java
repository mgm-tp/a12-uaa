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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithUserDetails;

import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionService;
import com.mgmtp.a12.uaa.authorization.AuthorizationService;
import com.mgmtp.a12.uaa.authorization.RepositoryAuthorizationCallback;
import com.mgmtp.a12.uaa.authorization.config.UAAMethodSecurityConfig;
import com.mgmtp.a12.uaa.authorization.internal.RuntimeAuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.model.PropertyPermission;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyRightsValidator;
import com.mgmtp.a12.uaa.authorization.property.internal.UAADataMasking;
import com.mgmtp.a12.uaa.authorization.security.DataMasking;
import com.mgmtp.a12.uaa.authorization.security.PermissionCheckResult;
import com.mgmtp.a12.uaa.authorization.security.PolicyProcessorFactory;
import com.mgmtp.a12.uaa.authorization.security.PropertyChangesChecker;
import com.mgmtp.a12.uaa.authorization.security.spel.internal.SpelPolicyProcessorFactory;

public class AuthorizationServiceWithPropertyAuthorizationIntegrationTest extends AbstractIntegrationTest {

	@Inject
	private AuthorizationService authorizationService;

	@Test
	@WithUserDetails("admin")
	public void propertyPermissionForAdmin() {
		TestResult propertyPermissioinExecution = executePropertyPermissioinRead();
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getDescription());
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getName());
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getId());
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getNested());
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getNested().getId());
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getNested().getNestedName());
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getNested().getNestedDescription());
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getNestedCollection().get(0).getId());
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getNestedCollection().get(0).getNestedName());
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getNestedCollection().get(0).getNestedDescription());
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getNestedCollection().get(1).getId());
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getNestedCollection().get(1).getNestedName());
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getNestedCollection().get(1).getNestedDescription());
	}

	@Test
	@WithUserDetails("guest")
	public void propertyPermissionForGuest() {
		TestResult propertyPermissioinExecution = executePropertyPermissioinRead();
		Assertions.assertNull(propertyPermissioinExecution.resource.getDescription());
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getName());
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getId());
		Assertions.assertNull(propertyPermissioinExecution.resource.getNested());
		Assertions.assertNull(propertyPermissioinExecution.resource.getNestedCollection());
	}

	@Test
	@WithUserDetails("user")
	public void propertyPermissionForUser() {
		TestResult propertyPermissioinExecution = executePropertyPermissioinRead();
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getDescription());
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getName());
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getId());
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getNested());
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getNested().getId());
		Assertions.assertNull(propertyPermissioinExecution.resource.getNested().getNestedName());
		Assertions.assertNull(propertyPermissioinExecution.resource.getNested().getNestedDescription());
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getNestedCollection().get(0).getId());
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getNestedCollection().get(0).getNestedName());
		Assertions.assertNull(propertyPermissioinExecution.resource.getNestedCollection().get(0).getNestedDescription());
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getNestedCollection().get(1).getId());
		Assertions.assertNotNull(propertyPermissioinExecution.resource.getNestedCollection().get(1).getNestedName());
		Assertions.assertNull(propertyPermissioinExecution.resource.getNestedCollection().get(1).getNestedDescription());
		Assertions.assertEquals("Empty", propertyPermissioinExecution.resource.getNestedCollection().get(0).getNestedName());
		Assertions.assertEquals("Empty", propertyPermissioinExecution.resource.getNestedCollection().get(1).getNestedName());
	}

	@Test
	@WithUserDetails("admin")
	public void checkPropertyPermissionForChangesForAdmin() {
		Boolean hasPermission = executePropertyPermissioinWrite();
		Assertions.assertTrue(hasPermission);
	}

	@Test
	@WithUserDetails("guest")
	public void checkPropertyPermissionForChangesForGuest() {
		Boolean hasPermission = executePropertyPermissioinWrite();
		Assertions.assertFalse(hasPermission);
	}

	@Test
	@WithUserDetails("user")
	public void checkPropertyPermissionForChangesForUser() {
		Boolean hasPermission = executePropertyPermissioinWrite();
		Assertions.assertFalse(hasPermission);
	}

	private Boolean executePropertyPermissioinWrite() {

		NestedResourceObject nested = new NestedResourceObject(2L, "nestedName", "nested Descriprion");
		TestResourceObject resource = new TestResourceObject(1L, "name", "description", nested);

		NestedResourceObject nestedUpdated = new NestedResourceObject(2L, "nestedNameUpdated", "nested Description");
		TestResourceObject resourceUpdated = new TestResourceObject(1L, "nameUpdated", "descriptionUpdated", nestedUpdated);

		return authorizationService.checkPropertyPermissionsForChanges(resource, resourceUpdated);

	}

	private TestResult executePropertyPermissioinRead() {
		TestResult result = new TestResult();

		NestedResourceObject nested = new NestedResourceObject(2L, "nestedName", "nested Description");
		TestResourceObject resource = new TestResourceObject(1L, "name", "description", nested);
		resource
			.addNestedElement(new NestedResourceObject(1L, "nestedName1", "nested Description 1"))
			.addNestedElement(new NestedResourceObject(2L, "nestedName", "nested Description 2"));
		result.resource = resource;
		result.permissionCheckResult = authorizationService.checkPropertyPermissionsAndMaskData(resource);
		return result;

	}

	static class TestResult {
		PermissionCheckResult<PropertyPermission> permissionCheckResult;
		TestResourceObject resource;
	}

	public static class TestResourceObject {

		private Long id;
		private String name;
		private String description;
		private NestedResourceObject nested;
		private List<NestedResourceObject> nestedCollection = new LinkedList<>();

		public TestResourceObject(Long id, String name, String description, NestedResourceObject nested) {
			super();
			this.id = id;
			this.name = name;
			this.description = description;
			this.nested = nested;
		}

		public Long getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public NestedResourceObject getNested() {
			return nested;
		}

		public List<NestedResourceObject> getNestedCollection() {
			return nestedCollection;
		}

		public TestResourceObject addNestedElement(NestedResourceObject nested) {
			nestedCollection.add(nested);
			return this;
		}

	}

	public static class NestedResourceObject {
		private Long id;
		private String nestedName;
		private String nestedDescription;

		public NestedResourceObject(Long id, String nestedName, String nestedDescription) {
			super();
			this.id = id;
			this.nestedName = nestedName;
			this.nestedDescription = nestedDescription;
		}

		public Long getId() {
			return id;
		}

		public String getNestedName() {
			return nestedName;
		}

		public String getNestedDescription() {
			return nestedDescription;
		}

		@Override
		public String toString() {
			return "NestedResourceObject [id=" + id + ", nestedName=" + nestedName + ", nestedDescription=" + nestedDescription + "]";
		}

	}

	static class TestingCallback implements RepositoryAuthorizationCallback {

		private Set<String> filters;

		@Override
		public void filtersGenerated(Set<String> filters) {
			this.filters = filters;
		}

		public Set<String> getFilters() {
			return filters;
		}

	}

	@Configuration
	@Import({ UAAMethodSecurityConfig.class })
	static class TestConfig {

		@Bean
		public AuthorizationDefinitionRepository createAuthorizationDefinitionRepository() {
			return new RuntimeAuthorizationDefinitionRepository();
		}

		@Bean
		public AuthorizationDefinitionService createAuthorizationDefinitionService() {
			return new AuthorizationDefinitionService("classpath:testAuthorizationDefinition_property.json", null);
		}

		@Bean
		public UserDetailsService createUserDetailsService() {
			return new TestUserDetailsService();
		}

		@Bean
		public AuthorizationService authorizationService() {
			return new AuthorizationService();
		}

		@Bean
		public PolicyProcessorFactory spelFactory() {
			return new SpelPolicyProcessorFactory();
		}

		@Bean
		public Repository createRepository() {
			return new Repository();
		}

		@Bean
		public Service createService() {
			return new Service();

		}

		@Bean
		public PropertyRightsValidator createPropertyPermissionValidator() {
			return new PropertyRightsValidator();
		}

		@Bean
		public DataMasking createDataMasking() {
			return new UAADataMasking(createAuthorizationDefinitionRepository());
		}
		
		@Bean
		public PropertyChangesChecker createPropertyChangesChecker() {
			return new PropertyChangesChecker(Arrays.asList("com.mgmtp"));
		}
	}
}
