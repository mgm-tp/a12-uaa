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
package com.mgmtp.a12.uaa.authorization.autoconfigure;

import java.util.LinkedList;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.mgmtp.a12.uaa.authorization.autoconfigure.security.internal.SecuredContext;

@ConfigurationProperties("mgmtp.a12.uaa.authorization.web")
public class AuthorizationWebProperties {

	private static final String RELATIVE_PATH = "RelativePath";

	private List<SecuredContext> uriSecuredContexts = new LinkedList<>();
	private String contextPath = "/";

	@Value("${management.endpoints.web.base-path:/actuator}/**")
	private String actuatorPath;

	public List<SecuredContext> getUriSecuredContexts() {
		return uriSecuredContexts;
	}

	public void setUriSecuredContexts(List<SecuredContext> uriSecuredContexts) {
		this.uriSecuredContexts = uriSecuredContexts;
	}

	public String getContextPath() {
		return StringUtils.removeEnd(contextPath, "/");
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	@PostConstruct
	void setDefault() {
		if (uriSecuredContexts.isEmpty()) {
			//set default value
			uriSecuredContexts.add(new SecuredContext(actuatorPath, RELATIVE_PATH));
		}
	}
}
