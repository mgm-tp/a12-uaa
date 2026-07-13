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
package com.mgmtp.a12.uaa.authentication.jwt.integration.filter;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mgmtp.a12.uaa.authentication.jwt.integration.BaseTestConfig;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenGenerator;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenPrincipalCreator;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenVerifier;
import com.mgmtp.a12.uaa.authentication.principal.UAAPrincipal;
import com.mgmtp.a12.uaa.authentication.utils.TokenTester;
import com.mgmtp.a12.uaa.authentication.utils.UserDataCreator;
import com.mgmtp.a12.uaa.authentication.utils.UserDataCreator.TestExtendedData;

@ExtendWith(SpringExtension.class)
public class JwtTokenPrincipalCreatorHardCodeNoDataExtendTest {

	@Inject
	private JwtTokenPrincipalCreator jwtTokenPrincipalCreator;

	@Inject
	private JwtTokenGenerator jwtTokenGenerator;

	@Inject
	private JwtTokenVerifier jwtTokenVerifier;

	@Test
	public void testGetPrincipalHardCodeSupport() {
		UAAPrincipal<TestExtendedData> user = UserDataCreator.createUser("test", "N/A");
		user.getAuthorities().stream().limit(0);
		String tokenString = jwtTokenGenerator.generateToken(user).getToken();
		UAAPrincipal<TestExtendedData> rtnUserDetails =
			(UAAPrincipal<TestExtendedData>) jwtTokenPrincipalCreator.createPrincipal(jwtTokenVerifier.unpackToken(tokenString));
		TokenTester.assertPrincipal(user, rtnUserDetails);
	}

	@Configuration
	static class TestConfig extends BaseTestConfig {

	}
}
