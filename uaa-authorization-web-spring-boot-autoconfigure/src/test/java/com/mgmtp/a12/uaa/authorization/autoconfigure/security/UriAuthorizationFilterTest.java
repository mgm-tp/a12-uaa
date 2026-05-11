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
package com.mgmtp.a12.uaa.authorization.autoconfigure.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import com.mgmtp.a12.uaa.authorization.AuthorizationService;
import com.mgmtp.a12.uaa.authorization.model.Permission;
import com.mgmtp.a12.uaa.authorization.security.PermissionCheckResult;

@ExtendWith(MockitoExtension.class)
public class UriAuthorizationFilterTest {

	@Mock
	public AuthorizationService authorizationService;

	@Mock
	public FilterChain filterChain;

	@Mock
	public HttpServletRequest request;

	@Test
	public void response_with_OK_status() throws ServletException, IOException {
		// Given
		String scopeName = "loadCompany_scope";
		UriAuthorizationFilter filter = new UriAuthorizationFilter(authorizationService, scopeName);
		HttpServletResponse response = new MockHttpServletResponse();
		
		PermissionCheckResult<Permission> permissionCheckResult = new PermissionCheckResult.Builder<Permission>(true, null).build();

		// When
		Mockito.when(authorizationService.checkPermissions(request, scopeName)).thenReturn(permissionCheckResult);

		// Execute
		filter.doFilter(request, response, filterChain);

		// Expected
		Assertions.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
	}

	@Test
	public void response_with_FORBIDDEN_status() throws ServletException, IOException {
		// Given
		String scopeName = "loadCompany scope";
		UriAuthorizationFilter filter = new UriAuthorizationFilter(authorizationService, scopeName);
		HttpServletResponse response = new MockHttpServletResponse();

		PermissionCheckResult<Permission> permissionCheckResult = new PermissionCheckResult.Builder<Permission>(false, null).build();
		// When
		Mockito.when(authorizationService.checkPermissions(request, scopeName)).thenReturn(permissionCheckResult);

		// Execute
		filter.doFilter(request, response, filterChain);

		// Expected
		Assertions.assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
	}
}
