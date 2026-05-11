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
package com.mgmtp.a12.uaa.authorization.real_domain_example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionService;
import com.mgmtp.a12.uaa.authorization.AuthorizationService;
import com.mgmtp.a12.uaa.authorization.example.TestUser;
import com.mgmtp.a12.uaa.authorization.internal.RuntimeAuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.model.Permission;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyRightsValidator;
import com.mgmtp.a12.uaa.authorization.property.internal.UAADataMasking;
import com.mgmtp.a12.uaa.authorization.security.DataMasking;
import com.mgmtp.a12.uaa.authorization.security.PermissionCheckResult;
import com.mgmtp.a12.uaa.authorization.security.PolicyProcessorFactory;
import com.mgmtp.a12.uaa.authorization.security.PropertyChangesChecker;
import com.mgmtp.a12.uaa.authorization.security.spel.internal.SpelPolicyProcessorFactory;

@ExtendWith(SpringExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
@TestExecutionListeners(
	inheritListeners = false,
	listeners = { WithSecurityContextTestExecutionListener.class },
	mergeMode = MergeMode.MERGE_WITH_DEFAULTS)
public class MyCaseApplicationTest {

	@Inject
	private AuthorizationService authorizationService;

	@Test
	@WithUserDetails(value = "admin")
	public void checkAdmin() {
		String resource = "DomainCaseType";

		//Execute rules must be passed.
		PermissionCheckResult<Permission> permissionCheckResult = authorizationService.checkPermissions(resource, "Document Read");
		Assertions.assertTrue(permissionCheckResult.isPassed());
		Assertions.assertFalse(permissionCheckResult.getPassedPermissions().isEmpty());

		//Generate authorization repository template is empty to view all application.
		Set<String> authorizationRepository = authorizationService.generateRepositoryPermissions(resource, "Document Read", null);
		Assertions.assertEquals(Collections.emptySet(), authorizationRepository);
	}

	@Test
	@WithUserDetails(value = "formManager")
	public void checkFormManager() {
		String resource = "DomainCaseType";

		//Execute rules must be passed.
		PermissionCheckResult<Permission> permissionCheckResult = authorizationService.checkPermissions(resource, "Document Read");
		Assertions.assertTrue(permissionCheckResult.isPassed());
		Assertions.assertFalse(permissionCheckResult.getPassedPermissions().isEmpty());

		//Generate authorization template must be "CaseType.MetaInformation.createdBy:formManager".
		List<String> authorizationRepository = new ArrayList<>(authorizationService.generateRepositoryPermissions(resource, "Document Read", null));

		Assertions.assertEquals("CaseType.MetaInformation.createdBy:formManager", authorizationRepository.get(0));
	}

	@Test
	@WithUserDetails(value = "guest")
	public void checkGuest() {
		String resource = "DomainInvoice";

		//Execute rules must be failed.
		PermissionCheckResult<Permission> permissionCheckResult = authorizationService.checkPermissions(resource, "Document Read");
		Assertions.assertFalse(permissionCheckResult.isPassed());
		Assertions.assertTrue(permissionCheckResult.getPassedPermissions().isEmpty());
	}

	@Component
	static class TestUserDetailsService implements UserDetailsService {

		private static final List<String> AUTHORITIES_ADMIN = Arrays.asList("admin");
		private static final List<String> AUTHORITIES_GUEST = Arrays.asList("guest");
		private static final List<String> AUTHORITIES_FORM_MANAGER = Arrays.asList("formManager");

		@Override
		public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
			List<String> authorities = new ArrayList<>();
			switch (username) {
			case "admin":
				authorities = AUTHORITIES_ADMIN;
				break;
			case "guest":
				authorities = AUTHORITIES_GUEST;
				break;
			case "formManager":
				authorities = AUTHORITIES_FORM_MANAGER;
				break;
			}
			return new TestUser(username, authorities);
		}
	}

	@Configuration
	static class TestConfiguration {

		@Bean
		public AuthorizationDefinitionRepository createAuthorizationDefinitionRepository() {
			return new RuntimeAuthorizationDefinitionRepository();
		}

		@Bean
		public AuthorizationDefinitionService createAuthorizationDefinitionService() {
			return new AuthorizationDefinitionService("classpath:real-domain-example/myCaseAuthorizationDefinition.json", null);
		}

		@Bean
		public UserDetailsService createUserDetailsService() {
			return new TestUserDetailsService();
		}

		@Bean
		public PolicyProcessorFactory policyProcessorFactory() {
			return new SpelPolicyProcessorFactory();
		}

		@Bean
		public AuthorizationService createAuthorizationService() {
			return new AuthorizationService();
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
