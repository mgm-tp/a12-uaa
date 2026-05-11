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
package com.mgmtp.a12.uaa.authentication.config.client.properties;

import java.util.Arrays;

import com.mgmtp.a12.uaa.authentication.config.common.UrlProperty;

public abstract class BaseProperties {
	private UrlProperty applicationBase = new UrlProperty("http://localhost:8080");
	private UrlProperty uaaBase = new UrlProperty("http://localhost:8080");
	private String authorizationDataStore;
	private String[] excludedDelegatedContexts;

	public UrlProperty getApplicationBase() {
		return applicationBase;
	}

	public void setApplicationBase(UrlProperty applicationBase) {
		this.applicationBase = applicationBase;
	}

	public UrlProperty getUaaBase() {
		return uaaBase;
	}

	public void setUaaBase(UrlProperty uaaBase) {
		this.uaaBase = uaaBase;
	}

	public String getAuthorizationDataStore() {
		return authorizationDataStore;
	}

	public void setAuthorizationDataStore(String authorizationDataStore) {
		this.authorizationDataStore = authorizationDataStore;
	}

	public String[] getExcludedDelegatedContexts() {
		return excludedDelegatedContexts;
	}

	public void setExcludedDelegatedContexts(String[] excludedDelegatedContexts) {
		this.excludedDelegatedContexts = excludedDelegatedContexts;
	}

	@Override
	public String toString() {
		return "BaseProperties{" +
			"applicationBase='" + applicationBase + '\'' +
			", uaaBase='" + uaaBase + '\'' +
			", authorizationDataStore='" + authorizationDataStore + '\'' +
			", excludedDelegatedContexts=" + Arrays.toString(excludedDelegatedContexts) +
			'}';
	}
}
