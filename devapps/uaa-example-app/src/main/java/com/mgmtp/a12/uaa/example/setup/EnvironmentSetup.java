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
package com.mgmtp.a12.uaa.example.setup;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class EnvironmentSetup implements EnvironmentPostProcessor {

	private static final String CONFIG_AUTHENTICATION_TYPE = "mgmtp.a12.uaa.authentication.types";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		Map<String, Object> authTypes = new HashMap<>();
		authTypes.put(CONFIG_AUTHENTICATION_TYPE, getAuthenticationType(environment));
		environment.getPropertySources().addFirst(new MapPropertySource("authenticationTypes", authTypes));
	}

	private String getAuthenticationType(ConfigurableEnvironment environment) {
		Set<String> uniqueTypes = new LinkedHashSet<>();
		for (String profile : environment.getActiveProfiles()) {
			environment
				.getPropertySources()
				.stream()
				.filter(ps -> ps.getName().contains(profile))
				.map(ps -> (String) ps.getProperty(CONFIG_AUTHENTICATION_TYPE))
				.filter(Objects::nonNull)
				.findFirst()
				.ifPresent(value -> {
					for (String type : value.split(",")) {
						uniqueTypes.add(type.trim());
					}
				});
		}
		return String.join(",", uniqueTypes);
	}

}
