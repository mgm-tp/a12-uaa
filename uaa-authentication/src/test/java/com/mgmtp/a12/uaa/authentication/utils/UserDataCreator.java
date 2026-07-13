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
package com.mgmtp.a12.uaa.authentication.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.mgmtp.a12.uaa.authentication.principal.UAAJsonSerialization;
import com.mgmtp.a12.uaa.authentication.principal.UAAPrincipal;

public class UserDataCreator {

	public static UAAPrincipal<TestExtendedData> createUser(String userName, String password) {

		Map<String, String> departmentActions = new HashMap<>();
		departmentActions.put("P01", "Technical Development");
		departmentActions.put("P02", "Security Development");
		CustomUser customUser =
			new CustomUser(userName, password, Arrays.asList(new SimpleGrantedAuthority("role1"), new SimpleGrantedAuthority("role2")), createTestSubData());
		customUser.setDepartmentActions(departmentActions);

		return customUser;
	}

	public static TestExtendedData createTestSubData() {
		TestSubData subData = new TestSubData();
		subData.setSubOne("oneData");
		subData.setSubTwo("twoData");

		TestExtendedData extData = new TestExtendedData();
		extData.setDataOne("firstData");
		extData.setDataTwo("secondData");
		extData.setSubData(subData);

		return extData;
	}

	@UAAJsonSerialization
	public static class TestExtendedData {
		private String dataOne;
		private String dataTwo;
		private TestSubData subData;

		public TestSubData getSubData() {
			return subData;
		}

		public void setSubData(TestSubData subData) {
			this.subData = subData;
		}

		public String getDataOne() {
			return dataOne;
		}

		public void setDataOne(String dataOne) {
			this.dataOne = dataOne;
		}

		public String getDataTwo() {
			return dataTwo;
		}

		public void setDataTwo(String dataTwo) {
			this.dataTwo = dataTwo;
		}

	}

	@UAAJsonSerialization
	public static class TestSubData {
		private String subOne;
		private String subTwo;

		public String getSubOne() {
			return subOne;
		}

		public void setSubOne(String subOne) {
			this.subOne = subOne;
		}

		public String getSubTwo() {
			return subTwo;
		}

		public void setSubTwo(String subTwo) {
			this.subTwo = subTwo;
		}
	}

	@UAAJsonSerialization
	public static class CustomUser extends UAAPrincipal {
		public CustomUser(String username, String password, Collection<? extends GrantedAuthority> authorities, Object extendedPrincipalData) {
			super(username, password, authorities, extendedPrincipalData);
		}

		private Map<String, String> departmentActions;

		public void setDepartmentActions(Map<String, String> departmentActions) {
			this.departmentActions = departmentActions;
		}
	}
}
