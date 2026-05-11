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
package com.mgmtp.a12.uaa.authentication.internal;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnOpaqueTokenCondition implements Condition {

	private static final String CONFIG_OPAQUE_INTROSPECTION_URL = "spring.security.oauth2.resourceserver.opaquetoken.introspection-uri";
	private static final String CONFIG_OPAQUE_INTROSPECTION_CLIENT_ID = "spring.security.oauth2.resourceserver.opaquetoken.client-id";
	private static final String CONFIG_OPAQUE_INTROSPECTION_CLIENT_SECRET = "spring.security.oauth2.resourceserver.opaquetoken.client-secret";
	private static final Logger LOGGER = LoggerFactory.getLogger(OnOpaqueTokenCondition.class);

	@Override
	public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata metadata) {
		String introspectionUrl = StringUtils.upperCase(conditionContext.getEnvironment().getProperty(CONFIG_OPAQUE_INTROSPECTION_URL));
		String introspectionClientId = StringUtils.upperCase(conditionContext.getEnvironment().getProperty(CONFIG_OPAQUE_INTROSPECTION_CLIENT_ID));
		String introspectionClientSecret = StringUtils.upperCase(conditionContext.getEnvironment().getProperty(CONFIG_OPAQUE_INTROSPECTION_CLIENT_SECRET));

		LOGGER.debug("Evaluating configured introspection url [{}]", introspectionUrl);
		LOGGER.debug("Evaluating configured introspection client-id [{}]", introspectionClientId);
		LOGGER.debug("Evaluating configured introspection client-secret [{}]", introspectionClientSecret);
		if (StringUtils.isBlank(introspectionUrl) || StringUtils.isBlank(introspectionClientId) || StringUtils.isBlank(introspectionClientSecret)) {
			return false;
		}
		return true;
	}

}
