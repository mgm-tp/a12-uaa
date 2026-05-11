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

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.ClassMetadata;

import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;

public class OnAuthenticationCondition implements Condition {

	private static final String CONFIG_AUTHENTICATION_TYPE = "mgmtp.a12.uaa.authentication.types";
	private static final Logger LOGGER = LoggerFactory.getLogger(OnAuthenticationCondition.class);

	@Override
	public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata metadata) {
		String types = StringUtils.upperCase(conditionContext.getEnvironment().getProperty(CONFIG_AUTHENTICATION_TYPE));
		LOGGER.debug("Evaluating configured authentication types [{}]", types);
		if (StringUtils.isBlank(types)) {
			LOGGER.debug("Using default authentication type LOCAL");
			//we assume LOCAL as default
			types = AuthenticationType.LOCAL.name();
		}
		List<AuthenticationType> activeTypes = AuthenticationType.fromTypesList(types);
		MergedAnnotation<ConditionalOnAuthentication> conditionAnnotation = metadata.getAnnotations().get(ConditionalOnAuthentication.class);
		ConditionalOnAuthentication conditionOnAuthentication = conditionAnnotation.synthesize();
		boolean passed = CollectionUtils.containsAny(activeTypes, conditionOnAuthentication.value());
		if ((metadata instanceof ClassMetadata classMetadata) && passed) {
			LOGGER.debug("Configuration [{}] is matched for authentication types [{}]", classMetadata.getClassName(), types);
		}
		return passed;
	}

}
