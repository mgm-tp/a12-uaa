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
package com.mgmtp.a12.uaa.authorization.property.internal;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.util.FieldUtils;

import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;

public class UAADataMasking extends AbstractDataMasking {

	private final static Logger LOGGER = LoggerFactory.getLogger(UAADataMasking.class);

	private SpelExpressionParser parser = new SpelExpressionParser();

	private static final String CLASS_PROPERTY = "class";
	private static Map<Class<?>, List<PropertyDescriptor>> cachePropertyDescriptor = new ConcurrentHashMap<>();

	public UAADataMasking(AuthorizationDefinitionRepository authorizationDefinitionRepository) {
		super(authorizationDefinitionRepository);
	}

	@Override
	protected <T> void performDataMasking(T resource, PropertyTree accessiblePropertiesParent) {
		List<PropertyDescriptor> propertyDescriptors = getPropertyDescriptors(resource);
		propertyDescriptors.stream()
			.filter(descriptor -> !descriptor.getName().equals(CLASS_PROPERTY))
			.forEach(propertyDescriptor -> {
				final String propertyName = propertyDescriptor.getName();

				PropertyTree accessibleProperties = Optional.ofNullable(accessiblePropertiesParent)
						.map(parent -> parent.getChild(propertyName))
						.orElse(null);
					
				try {
					if (accessibleProperties == null) {
						//no access right
						LOGGER.debug("Resource class[{}]: Masking property [{}] ", resource.getClass().getCanonicalName(), propertyDescriptor.getName());
						maskProperty(resource, propertyDescriptor);
					} else if (Collection.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
						Collection<?> childCollectionProperty = (Collection<?>) propertyDescriptor.getReadMethod().invoke(resource);
						String collectionName = propertyName + "[]";
						PropertyTree accessibleCollectionProperties = accessiblePropertiesParent.getChild(collectionName);
						childCollectionProperty
							.forEach(element -> performDataMasking(element, accessibleCollectionProperties));
					} else if (Map.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
						Map<?, ?> childMapProperty = (Map<?, ?>) propertyDescriptor.getReadMethod().invoke(resource);
						String mapName = propertyName + "[]";
						PropertyTree accessibleMapProperties = accessiblePropertiesParent.getChild(mapName);
						childMapProperty
							.forEach((key, value) -> performDataMasking(value, accessibleMapProperties));
						
					} else if (CollectionUtils.isNotEmpty(accessibleProperties.getChildrens())) {
						Object childProperty = propertyDescriptor.getReadMethod().invoke(resource);
						LOGGER.debug("Resource class[{}]: Checking nested object referenced by property [{}] ", resource.getClass().getCanonicalName(),
							propertyDescriptor.getName());
						performDataMasking(childProperty, accessibleProperties);
					}
				} catch (Exception e) {
					LOGGER.warn("Resource class[{}]: Unable to get  property [{}]", resource.getClass().getCanonicalName(), propertyDescriptor.getName(), e);
				}
			});
	}

	@Override
	protected <T> void performCustomMasking(T resource, PropertyTreeRoot accessibleProperties) {
		accessibleProperties.getMasking()
			.forEach(maskingExpression -> {
				String expression = StringUtils.substringBefore(maskingExpression, "::");
				String value = StringUtils.substringAfter(maskingExpression, "::");
				maskPropertyWithExpressions(resource, expression, value);
			});

	}

	private <T> void maskPropertyWithExpressions(T resource, String expression, String valueExpression) {
		String propertyReference = StringUtils.substringBefore(expression, "[].");
		String childExpression = StringUtils.substringAfter(expression, "[].");

		StandardEvaluationContext evaluationContext = new StandardEvaluationContext(resource);
		PropertyAccessor reflectivePropertyAccessor = new UaaPropertyAccessor(true);
		evaluationContext.addPropertyAccessor(reflectivePropertyAccessor);
		try {
			Expression parsedExpression = parser.parseExpression(propertyReference);
			if (StringUtils.isBlank(childExpression)) {
				Expression parsedValueExpression = parser.parseExpression(valueExpression);
				Object newValue = parsedValueExpression.getValue(evaluationContext);
				parsedExpression.setValue(evaluationContext, newValue);
				return;
			}
			Collection<T> dataCollection = parsedExpression.getValue(evaluationContext, Collection.class);
			Optional.ofNullable(dataCollection).orElse(Collections.emptyList())
				.forEach(element -> maskPropertyWithExpressions(element, childExpression, valueExpression));
		} catch (Exception e) {
			LOGGER.warn("Unable to execute masking expression", e);
		}
	}

	private <T> void maskProperty(T resource, PropertyDescriptor propertyDescriptor) {
		Method writeMethod = propertyDescriptor.getWriteMethod();
		try {
			//what about primitive types ?
			if (writeMethod != null) {
				writeMethod.invoke(resource, new Object[] { null });
			} else {
				FieldUtils.setProtectedFieldValue(propertyDescriptor.getName(), resource, null);
			}
		} catch (Exception e) {
			LOGGER.warn("Resource class[{}]: Unable to set property [{}]", resource.getClass().getCanonicalName(), propertyDescriptor.getName());
		}
	}

	private List<PropertyDescriptor> getPropertyDescriptors(Object resource) {
		Class<?> resourceClass = resource.getClass();

		return Optional.ofNullable(cachePropertyDescriptor.get(resourceClass))
			.orElseGet(() -> {
				List<PropertyDescriptor> propertyDescriptors = Arrays.asList(BeanUtils.getPropertyDescriptors(resourceClass));
				cachePropertyDescriptor.putIfAbsent(resourceClass, propertyDescriptors);
				return propertyDescriptors;
			});
	}

}
