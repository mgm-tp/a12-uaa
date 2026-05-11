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
package com.mgmtp.a12.uaa.authentication.autoconfigure;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import com.mgmtp.a12.uaa.authentication.AuthenticationType;

public class CertificateConfiguration implements EnvironmentPostProcessor {
	private static final String CONFIG_AUTHENTICATION_TYPE = "mgmtp.a12.uaa.authentication.types";
	private static final String CONFIG_SSL_ENABLED = "server.ssl.enabled";
	private static final String SSL_PROPERTIES = "sslProperties";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		String authTypes = environment.getProperty(CONFIG_AUTHENTICATION_TYPE);
		if (authTypes == null || authTypes.isEmpty()) {
			return;
		}
		String[] listAuthTypes = authTypes.split(",");
		if (Arrays.asList(listAuthTypes).contains(AuthenticationType.CERTIFICATE.name())) {
			Map<String, Object> sslProps = new HashMap<>();
			sslProps.put(CONFIG_SSL_ENABLED, true);
			environment.getPropertySources().addFirst(new MapPropertySource(SSL_PROPERTIES, sslProps));
		}
	}
}
