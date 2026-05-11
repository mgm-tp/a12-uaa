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

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotationPredicates;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.mgmtp.a12.uaa.authentication.ConditionalOnConfiguration;

public class OnConfigurationCondition implements Condition {

	private static final String PROPERTY_KEY = "configurationKey";
	private static final String PROPERTY_MATCH_IF_MISSING = "matchIfMissing";
	private static final String PROPERTY_VALUE = "configurationValue";
	private static final Logger LOGGER = LoggerFactory.getLogger(OnConfigurationCondition.class);

	@Override
	public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata metadata) {
		AnnotationAttributes annotationAttributes = metadata.getAnnotations()
			.stream(ConditionalOnConfiguration.class.getName())
			.filter(MergedAnnotationPredicates.unique(MergedAnnotation::getMetaTypes))
			.map(MergedAnnotation::asAnnotationAttributes)
			.findFirst().get();

		String key = annotationAttributes.getString(PROPERTY_KEY);
		Environment environment = conditionContext.getEnvironment();
		if (environment.containsProperty(key)) {
			String configurationValue = environment.getProperty(key);
			String expectedValue = annotationAttributes.getString(PROPERTY_VALUE);
			LOGGER.debug("Checking configuration key [{}] for expected value: [{}], actual value: [{}]", key, expectedValue, configurationValue);
			return Objects.equals(configurationValue, expectedValue);
		}
		boolean matchIfMissing = annotationAttributes.getBoolean(PROPERTY_MATCH_IF_MISSING);
		LOGGER.debug("Configuration key [{}] not found using default matching [{}]", key, matchIfMissing);
		return matchIfMissing;
	}

}
