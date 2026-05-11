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

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionService;
import com.mgmtp.a12.uaa.authorization.UAASecurityBypass;
import com.mgmtp.a12.uaa.authorization.property.internal.UAADataMasking;
import com.mgmtp.a12.uaa.authorization.security.DataMasking;
import com.mgmtp.a12.uaa.authorization.security.PropertyChangesChecker;

@ComponentScan("com.mgmtp.a12.uaa.authorization")
@EnableConfigurationProperties(AuthorizationAutoconfigProperties.class)
public class AuthorizationAutoConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationAutoConfiguration.class);

	@Inject
	private AuthorizationAutoconfigProperties authorizationProperties;
	@Inject
	private AuthorizationDefinitionRepository authorizationRepository;

	@Bean
	public AuthorizationDefinitionService createAuthorizationDefinitionService() {
		LOGGER.info("Configuring Authorization [{}]", authorizationProperties);
		return new AuthorizationDefinitionService(authorizationProperties.getAuthorizationDefinition(),
			authorizationProperties.getChildAuthorizationDefinitions());
	}

	@Bean
	public UAASecurityBypass createUaaSecurityBypass() {
		return new UAASecurityBypass(authorizationProperties.getSecurityOnStartUp().isEnabled());
	}

	@Bean
	@ConditionalOnMissingBean(DataMasking.class)
	public DataMasking createDataMasking() {
		return new UAADataMasking(authorizationRepository);
	}

	@Bean
	public PropertyChangesChecker createPropertyChangesChecker() {
		return new PropertyChangesChecker(authorizationProperties.getScanEntityPackages());
	}

}
