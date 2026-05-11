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

import java.util.ArrayList;
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

import com.mgmtp.a12.uaa.authorization.AuthorizationContext;
import com.mgmtp.a12.uaa.authorization.AuthorizationContextData;
import com.mgmtp.a12.uaa.authorization.AuthorizationContextHolder;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionService;
import com.mgmtp.a12.uaa.authorization.AuthorizationService;
import com.mgmtp.a12.uaa.authorization.RepositoryAuthorizationCallback;
import com.mgmtp.a12.uaa.authorization.config.UAAMethodSecurityConfig;
import com.mgmtp.a12.uaa.authorization.internal.RuntimeAuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyRightsValidator;
import com.mgmtp.a12.uaa.authorization.property.internal.UAADataMasking;
import com.mgmtp.a12.uaa.authorization.security.DataMasking;
import com.mgmtp.a12.uaa.authorization.security.PolicyProcessorFactory;
import com.mgmtp.a12.uaa.authorization.security.PropertyChangesChecker;
import com.mgmtp.a12.uaa.authorization.security.spel.internal.SpelPolicyProcessorFactory;

public class AuthorizationServiceIntegrationTest extends AbstractIntegrationTest {

	@Inject
	private Repository repository;
	@Inject
	private Service service;

	@Test
	@WithUserDetails("test")
	public void executeRepositoryTemplateWithMethodParametersAndTestUser() {
		executeRepositoryTemplateWithMethodParameters("test");
	}

	@Test
	@WithUserDetails("another")
	public void executeRepositoryTemplateWithMethodParametersAndAnotherUser() {
		executeRepositoryTemplateWithMethodParameters("another");
	}

	@Test
	@WithUserDetails("test")
	public void executeMultiplePushForList() {
		repository.loadList();
		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		Assertions.assertNull(authorizationContext.getCurrentContext());
	}

	@Test
	@WithUserDetails("test")
	public void executeMultiplePushForMap() {
		repository.loadMap();
		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		Assertions.assertNull(authorizationContext.getCurrentContext());
	}

	@Test
	@WithUserDetails("test")
	public void executeMultiplePushInputParameter() {
		List<String> parameter = new LinkedList<>();
		parameter.addAll(Arrays.asList("First", "Second", "Third"));
		repository.acceptList(parameter);
		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		Assertions.assertNull(authorizationContext.getCurrentContext());
	}

	@Test
	@WithUserDetails("test")
	public void executeServiceWithInnerRepository() {
		service.aMethod();
		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		Assertions.assertNull(authorizationContext.getCurrentContext());
	}

	@Test
	@WithUserDetails("test")
	public void executeDataPreloadAndCheckResource() {
		String preloadMethod = service.aDataPreloadMethod("TestValue");
		Assertions.assertEquals("SUCCESS", preloadMethod);
	}

	@Test
	@WithUserDetails(value = "test")
	public void repositoryPolicyTemplateIsAutoExecuteTest() {
		repository.getListWithRepositoryGenerateMethod();
		Assertions.assertTrue(service.isRepositoryPolicyExecute());
		service.setRepositoryPolicyExecute(false);
	}

	@Test
	@WithUserDetails("test")
	public void checkRepositoryPermissionByAnnotation() {
		TestingCallback testingCallback = new TestingCallback();
		service.repositoryPermissionWithAnnotation(testingCallback);
		List<String> filters = new ArrayList<>(testingCallback.getFilters());
		Assertions.assertEquals("parameter test", filters.get(0));
	}

	@Test
	@WithUserDetails("test")
	public void checkRepositoryPermissionByAnnotationWithParameter() {
		TestingCallback testingCallback = new TestingCallback();
		service.repositoryPermissionWithAnnotationAndParameter("parameter", testingCallback);
		List<String> filters = new ArrayList<>(testingCallback.getFilters());
		Assertions.assertEquals("parameter test", filters.get(0));
	}

	
	@Test
	@WithUserDetails("test")
	public void checkCleaningAfterExceptionHandlingPreFilter() {
		checkException(() -> service.throwExceptionPreFilter(Arrays.asList("1", "2")));
		//now check that collection processing is reset
		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		authorizationContext.pushContext(new AuthorizationContextData("Test", "Empty", null));
		Assertions.assertFalse(authorizationContext.popContext().isCollectionProcessing(), "Collection processing must be switched off");
		Assertions.assertNull(authorizationContext.getCurrentContext());
	}
	
	@Test
	@WithUserDetails("test")
	public void checkCleaningAfterExceptionInsideAnnotationHandlingPreFilter() {
		checkException(() -> service.throwExceptionPreFilter(new LinkedList<>(Arrays.asList("1", "2"))));
		//now check that collection processing is reset
		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		authorizationContext.pushContext(new AuthorizationContextData("Test", "Empty", null));
		Assertions.assertFalse(authorizationContext.popContext().isCollectionProcessing(), "Collection processing must be switched off");
		Assertions.assertNull(authorizationContext.getCurrentContext());
	}
	
	@Test
	@WithUserDetails("test")
	public void checkScopeAfterPreFilterCallingInnerScopeAndThrowsException() {
		checkException(() -> service.methodWithPrefilterCallRepositoryAndThrowException(new LinkedList<>(Arrays.asList("1", "2"))));
	}
	
	@Test
	@WithUserDetails("test")
	public void checkScopeAfterPreAuthorizeCallingInnerScopeAndThrowsException() {
		checkException(() -> service.methodWithPreauthorizeCallRepositoryAndThrowException());
	}
	
	@Test
	@WithUserDetails("test")
	public void checkScopeAfterPostAuthorizeCallingInnerScopeAndThrowsException() {
		checkException(() -> service.methodWithPostAuthorizeCallRepositoryAndThrowException());
	}

	@Test
	@WithUserDetails("test")
	public void checkScopeAfterPostFilterCallingInnerScopeAndThrowsException() {
		checkException(() -> service.methodWithPostFilterCallRepositoryAndThrowException());
	}
	

	@Test
	@WithUserDetails("test")
	public void checkScopeAfterPreFilterCallingInnerScope() {
		service.methodWithPrefilterCallRepository(new LinkedList<>(Arrays.asList("1", "2")));
	}
	
	@Test
	@WithUserDetails("test")
	public void checkScopeAfterPreAuthorizeCallingInnerScope() {
		service.methodWithPreauthorizeCallRepository();
	}
	
	@Test
	@WithUserDetails("test")
	public void checkScopeAfterPostAuthorizeCallingInnerScope() {
		service.methodWithPostAuthorizeCallRepository();
	}

	@Test
	@WithUserDetails("test")
	public void checkScopeAfterPostFilterCallingInnerScope() {
		service.methodWithPostFilterCallRepository();
	}

	
	
	@Test
	@WithUserDetails("test")
	public void checkDoubleCollectionHandling() {
		service.methodWithPreFilter(new LinkedList<>(Arrays.asList("1", "2")));

		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		Assertions.assertNull(authorizationContext.getCurrentContext());
		
		authorizationContext.pushContext(new AuthorizationContextData("Test", "Empty", null));
		Assertions.assertFalse(authorizationContext.popContext().isCollectionProcessing(), "Collection processing must be switched off");
	}
	
	@Test
	@WithUserDetails("test")
	public void checkCollectionAndPostAuthorizeHandling() {
		service.methodWithPreFilterAndInnerPostAuthorize(new LinkedList<>(Arrays.asList("1", "2")));

		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		Assertions.assertNull(authorizationContext.getCurrentContext());
		
		authorizationContext.pushContext(new AuthorizationContextData("Test", "Empty", null));
		Assertions.assertFalse(authorizationContext.popContext().isCollectionProcessing(), "Collection processing must be switched off");
	}

	
	
	@Test
	@WithUserDetails("test")
	public void checkExceptionHandlingPreAuthorize() {
		checkException(() -> service.throwException());
	}
	
	@Test
	@WithUserDetails("test")
	public void checkExceptionHandlingPostAuthorize() {
		checkException(() -> service.throwExceptionPost());
	}
	
	@Test
	@WithUserDetails("test")
	public void checkExceptionHandlingPreFilter() {
		checkException(() -> service.throwExceptionPreFilter(Arrays.asList("1", "2")));
	}
	
	@Test
	@WithUserDetails("test")
	public void checkExceptionHandlingPostFilter() {
		checkException(() -> service.throwExceptionPostFilter());
	}
	
	private void checkException(Runnable runnable) {
		try {
			runnable.run();
		} catch (Exception e) {
			checkEmptyContext();
			return;
		}
		Assertions.fail("No exception thrown");
	}
	
	private void checkEmptyContext() {
		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		Assertions.assertNull(authorizationContext.getCurrentContext());
	}

	private void executeRepositoryTemplateWithMethodParameters(String username) {
		String parameter = "testValue";
		List<String> templates = new ArrayList<>(repository.callRepositoryByService(parameter));
		Assertions.assertEquals(1, templates.size());
		Assertions.assertEquals(parameter + " " + username, templates.get(0));
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
		public AuthorizationDefinitionService crAuthorizationDefinitionService() {
			return new AuthorizationDefinitionService("classpath:testAuthorizationDefinition_parameters.json",
				List.of("classpath:testAuthorizationDefinition_parameters_additional.json",
					"classpath:testAuthorizationDefinition_parameters_additional2.json"));
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
