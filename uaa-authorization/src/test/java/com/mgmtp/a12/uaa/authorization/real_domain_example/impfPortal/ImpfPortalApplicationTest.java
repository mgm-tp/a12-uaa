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
package com.mgmtp.a12.uaa.authorization.real_domain_example.impfPortal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.springframework.security.test.context.support.ReactorContextTestExecutionListener;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionService;
import com.mgmtp.a12.uaa.authorization.AuthorizationService;
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
	listeners = { WithSecurityContextTestExecutionListener.class, ReactorContextTestExecutionListener.class },
	mergeMode = MergeMode.MERGE_WITH_DEFAULTS)
public class ImpfPortalApplicationTest {

	@Inject
	private AuthorizationService authorizationService;

	/**
	 * Test with user has a "GUEST" role:
	 * User is allowed to see his / her application
	 * <br>
	 * <li>Expect the validation rule to be passed.</li>
	 * <li>Expect the repository access template to be "Application.createdBy:guest".</li>
	 */
	@Test
	@WithUserDetails(value = "guestUser")
	public void checkWithGUEST_ViewGuestApplication() {
		Application application = new Application();
		application.setId("1");
		application.setName("Andrea");
		application.setRegion("HAMBURG");
		application.setCreatedBy("guestUser");

		//Execute rules must be passed.
		PermissionCheckResult<Permission> permissionCheckResult = authorizationService.checkPermissions(application, "Document Read");
		Assertions.assertTrue(permissionCheckResult.isPassed());
		Assertions.assertFalse(permissionCheckResult.getPassedPermissions().isEmpty());

		//Generate authorization repository template is "Application.createdBy:guestUser".
		List<String> authorizationRepository = new ArrayList<>(authorizationService.generateRepositoryPermissions(application, "Document Read", null));
		authorizationRepository.forEach(System.out::println);

		Assertions.assertEquals("Application.createdBy:guestUser", authorizationRepository.get(0));
	}

	/**
	 * Test with user has a "REGION_MANAGER" role:
	 * User is belonging to region and the person responsible for the application in this region have access to the application
	 * <br>
	 * <li>Expect the validation rule to be passed.</li>
	 * <li>Expect the repository access template to be "Application.region:BAVARIA OR Application.createdBy:regionManager".</li>
	 */
	@Test
	@WithUserDetails(value = "regionManagerUser")
	public void checkWithREGIONMANAGER_ViewBAVARIAApplication() {
		Application application = new Application();
		application.setId("1");
		application.setName("Andrea");
		application.setRegion("BAVARIA");
		application.setCreatedBy("guestUser");

		PermissionCheckResult<Permission> permissionCheckResult = authorizationService.checkPermissions(application, "Document Read");
		Assertions.assertTrue(permissionCheckResult.isPassed());
		Assertions.assertFalse(permissionCheckResult.getPassedPermissions().isEmpty());

		//Generate authorization template must be "Application.region:BAVARIA OR Application.createdBy:regionManagerUser".
		List<String> authorizationRepository = new ArrayList<>(authorizationService.generateRepositoryPermissions(application, "Document Read", null));
		authorizationRepository.forEach(System.out::println);

		Assertions.assertEquals("Application.region:BAVARIA OR Application.createdBy:regionManagerUser", authorizationRepository.get(0));
	}

	@Component
	static class TestUserDetailsService implements UserDetailsService {

		private static final List<String> AUTHORITIES_GUEST = Arrays.asList("GUEST");
		private static final List<String> AUTHORITIES_REGION_MANAGER = Arrays.asList("REGION_MANAGER");

		@Override
		public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
			List<String> authorities = new ArrayList<>();
			String region = "";
			switch (username) {
			case "guestUser":
				authorities = AUTHORITIES_GUEST;
				region = "HAMBURG";
				break;
			case "regionManagerUser":
				authorities = AUTHORITIES_REGION_MANAGER;
				region = "BAVARIA";
				break;
			}
			ImpfUser impfUser = new ImpfUser(username, authorities);
			impfUser.setRegion(region);
			return impfUser;
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
			return new AuthorizationDefinitionService("classpath:real-domain-example/impfPortalAuthorizationDefinition.json", null);
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
