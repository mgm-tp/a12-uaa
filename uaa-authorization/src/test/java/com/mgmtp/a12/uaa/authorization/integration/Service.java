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
import java.util.Collection;
import java.util.List;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;

import com.mgmtp.a12.uaa.authorization.AuthorizationContext;
import com.mgmtp.a12.uaa.authorization.AuthorizationContextData;
import com.mgmtp.a12.uaa.authorization.AuthorizationContextHolder;
import com.mgmtp.a12.uaa.authorization.DecisionContext;
import com.mgmtp.a12.uaa.authorization.RepositoryAuthorizationCallback;

public class Service {

	@Inject
	private Repository repository;

	private boolean isRepositoryPolicyExecute;

	public boolean isRepositoryPolicyExecute() {
		return isRepositoryPolicyExecute;
	}

	public void setRepositoryPolicyExecute(boolean repositoryPolicyExecute) {
		isRepositoryPolicyExecute = repositoryPolicyExecute;
	}

	@PreAuthorize("hasUAAPermission('Test Scope Service')")
	public void aMethod() {
		repository.loadList();
		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		AuthorizationContextData currentContext = authorizationContext.getCurrentContext();
		Assertions.assertNotNull(currentContext);
		Assertions.assertEquals("Test Scope Service", currentContext.getCurrentScope());

	}

	@PreAuthorize("hasUAAPermission('Test Data Preload')")
	public String aDataPreloadMethod(@DecisionContext("resource") String parameter) {
		return "SUCCESS";
	}

	@PreAuthorize("hasUAAPermission('Check Repository Auto Execute By Scope')")
	public boolean aMethodCallByRepository() {
		return isRepositoryPolicyExecute = true;
	}

	@PreAuthorize("hasUAAPermission('Test Scope Service')")
	public void repositoryPermissionWithAnnotation(RepositoryAuthorizationCallback callback) {
		repository.repositoryPermission(callback);
	}

	@PreAuthorize("hasUAAPermission('Test Scope')")
	public void repositoryPermissionWithAnnotationAndParameter(String parameter, RepositoryAuthorizationCallback callback) {
		repository.repositoryPermissionAndParameter(parameter, callback);
	}

	@PreAuthorize("hasUAAPermission('Test Scope Service')")
	public void throwException() {
		throw new RuntimeException("Failed");
	}

	@PostAuthorize("hasUAAPermission('Test Scope Service')")
	public void throwExceptionPost() {
		throw new RuntimeException("Failed");
	}

	@SuppressWarnings("unused")
	@PreFilter("hasUAAPermission('Test Scope Service', filterObject)")
	public void throwExceptionPreFilter(List<String> parameters) {
		throw new RuntimeException("Failed");
	}

	@PostFilter("hasUAAPermission('Test Scope Service', filterObject)")
	public Collection<String> throwExceptionPostFilter() {
		throw new RuntimeException("Failed");
	}

	@PreFilter("hasUAAPermission('Test Scope Service', filterObject)")
	public void methodWithPreFilter(List<String> parameters) {
		repository.acceptList(parameters);
	}

	@SuppressWarnings("unused")
	@PreFilter("hasUAAPermission('Test Scope Service', filterObject)")
	public void methodWithPreFilterAndInnerPostAuthorize(List<String> parameters) {
		repository.simplePostAuthorize();
	}

	@SuppressWarnings("unused")
	@PreFilter("hasUAAPermission('Test Scope Service', filterObject)")
	public void methodWithPrefilterCallRepositoryAndThrowException(List<String> parameters) {
		callInnerClass();
		throw new RuntimeException("Failed");
	}
	
	@PreAuthorize("hasUAAPermission('Test Scope Service', filterObject)")
	public void methodWithPreauthorizeCallRepositoryAndThrowException() {
		callInnerClass();
		throw new RuntimeException("Failed");
	}
	
	@PostAuthorize("hasUAAPermission('Test Scope Service')")
	public void methodWithPostAuthorizeCallRepositoryAndThrowException() {
		callInnerClassWithNoContext();
		throw new RuntimeException("Failed");
	}
	
	@PostFilter("hasUAAPermission('Test Scope Service', filterObject)")
	public void methodWithPostFilterCallRepositoryAndThrowException() {
		callInnerClassWithNoContext();
		throw new RuntimeException("Failed");
	}
	
	@SuppressWarnings("unused")
	@PreFilter("hasUAAPermission('Test Scope Service', filterObject)")
	public void methodWithPrefilterCallRepository(List<String> parameters) {
		callInnerClass();
	}
	
	@PreAuthorize("hasUAAPermission('Test Scope Service', filterObject)")
	public void methodWithPreauthorizeCallRepository() {
		callInnerClass();
	}
	
	@PostAuthorize("hasUAAPermission('Test Scope Service')")
	public void methodWithPostAuthorizeCallRepository() {
		callInnerClassWithNoContext();
	}
	
	@PostFilter("hasUAAPermission('Test Scope Service', filterObject)")
	public List<String> methodWithPostFilterCallRepository() {
		callInnerClassWithNoContext();
		return new ArrayList<>(Arrays.asList("1", "2"));
	}


	
	private void callInnerClassWithNoContext() {
		//with @PostAuthorize/Filter we have no context
		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		repository.justAuthorization("123");
		Assertions.assertNull(authorizationContext.getCurrentContext());
		
		repository.callRepositoryByService("123");
		Assertions.assertNull(authorizationContext.getCurrentContext());
	}
	
	private void callInnerClass() {
		AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
		repository.justAuthorization("123");
		AuthorizationContextData currentContext = authorizationContext.getCurrentContext();
		Assertions.assertNotNull(currentContext);
		Assertions.assertEquals("Test Scope Service", currentContext.getCurrentScope());
		
		repository.callRepositoryByService("123");
		currentContext = authorizationContext.getCurrentContext();
		Assertions.assertNotNull(currentContext);
		Assertions.assertEquals("Test Scope Service", currentContext.getCurrentScope());
	}

}
