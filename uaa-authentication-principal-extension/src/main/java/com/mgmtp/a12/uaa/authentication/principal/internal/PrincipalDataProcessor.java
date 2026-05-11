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
package com.mgmtp.a12.uaa.authentication.principal.internal;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.GenericTypeResolver;

import com.mgmtp.a12.uaa.authentication.principal.AbstractExtendedPrincipal;
import com.mgmtp.a12.uaa.authentication.principal.PropertyExtractor;

public class PrincipalDataProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(PrincipalDataProcessor.class);

	private List<String> additionalProperties;

	@Inject
	private Optional<List<PropertyExtractor<?>>> propertyExtractors;

	public PrincipalDataProcessor(List<String> additionalProperties) {
		this.additionalProperties = Optional.ofNullable(additionalProperties).orElse(Collections.emptyList());
	}

	public <T> AbstractExtendedPrincipal<?> processUser(AbstractExtendedPrincipal<?> user, T payload) {
		propertyExtractors.orElse(Collections.emptyList()).stream()
			.filter(obj -> payload != null)
			.filter(extractor -> GenericTypeResolver.resolveTypeArgument(extractor.getClass(), PropertyExtractor.class).isAssignableFrom(payload.getClass()))
			.map(extractor -> extractProperties(user, extractor, payload))
			.flatMap(List::stream)
			.forEach(property -> processProperty(user, property));
		return user;

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Pair<String, String>> extractProperties(AbstractExtendedPrincipal<?> user, PropertyExtractor extractor, Object payload) {
		return additionalProperties.stream()
			.map(property -> ImmutablePair.of(property, extractor.extractProperty(user, payload, property)))
			.collect(Collectors.toList());
	}

	private void processProperty(AbstractExtendedPrincipal<?> user, Pair<String, String> property) {
		String propertyName = property.getKey();
		String propertyValue = property.getValue();
		LOGGER.debug("Received additional property [{}={}]", propertyName, propertyValue);
		try {
			if (PropertyUtils.isWriteable(user, propertyName)) {
				LOGGER.debug("Setting direct property [{}] on user object", propertyName);
				PropertyUtils.setSimpleProperty(user, propertyName, propertyValue);
				return;
			}
		} catch (Exception e) {
			LOGGER.warn("Unable to write to the property [{}] of an object type [{}]", propertyName, user.getClass().getName());
		}
		if (StringUtils.isNotBlank(propertyValue)) {
			LOGGER.debug("Adding property [{}] to value map", propertyName);
			user.addAdditionalProperty(propertyName, propertyValue);
		}
	}

}
