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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Inject;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;

import com.mgmtp.a12.uaa.authorization.AuthorizationService;
import com.mgmtp.a12.uaa.authorization.DecisionContext;
import com.mgmtp.a12.uaa.authorization.RepositoryAuthorizationCallback;

public class Repository {

	@Inject
	private AuthorizationService authorizationService;

	@PreAuthorize("hasUAAPermission('Test Scope')")
	public Set<String> callRepositoryByService(@DecisionContext("parameter") String parameter) {
		return authorizationService.generateRepositoryPermissions();
	}
	
	@PreAuthorize("hasUAAPermission('Test Scope')")
	public List<String> justAuthorization(@DecisionContext("parameter") String parameter) {
		return Arrays.asList("1");
	}

	@PostFilter("hasUAAPermission('Test Scope', filterObject)")
	public List<String> loadList() {
		List<String> output = new LinkedList<>();
		output.addAll(Arrays.asList("First", "Second", "Third"));
		return output;
	}

	@PostFilter("hasUAAPermission('Test Scope', filterObject)")
	public Map<String, String> loadMap() {
		Map<String, String> output = new HashMap<>();
		output.put("First", "First");
		output.put("Second", "Second");
		output.put("Third", "Third");
		return output;
	}

	@PreFilter("hasUAAPermission('Test Scope', filterObject)")
	public String acceptList(List<String> parameters) {
		return "Hello";
	}

	@PreAuthorize("generateRepositoryPermissions('Check Repository Auto Execute By Scope')")
	public String getListWithRepositoryGenerateMethod() {
		return "value";
	}
	
	@PreAuthorize("generateRepositoryPermissions(#callback)")
	public String repositoryPermission(RepositoryAuthorizationCallback callback) {
		return "value";
	}
	
	@PreAuthorize("generateRepositoryPermissions(#callback)")
	public String repositoryPermissionAndParameter(@DecisionContext("parameter") String parameter, RepositoryAuthorizationCallback callback) {
		return "value";
	}
	
	@PostAuthorize("hasUAAPermission('Test Scope', returnObject)")
	public String simplePostAuthorize() {
		return "Nothing";
	}

}
