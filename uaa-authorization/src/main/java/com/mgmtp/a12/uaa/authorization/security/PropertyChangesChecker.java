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
package com.mgmtp.a12.uaa.authorization.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.persistence.Entity;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Change;
import org.javers.core.diff.Diff;
import org.javers.core.diff.ListCompareAlgorithm;
import org.javers.core.diff.custom.CustomPropertyComparator;
import org.javers.core.graph.HashCodeObjectHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;

import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.model.PropertyPermission;
import com.mgmtp.a12.uaa.authorization.model.Rights;
import com.mgmtp.a12.uaa.authorization.property.PropertyChangePathConverter;
import com.mgmtp.a12.uaa.authorization.property.internal.ResourceConverter;
import com.mgmtp.a12.uaa.authorization.property.internal.UaaNotationConverter;

public class PropertyChangesChecker {

	private static final Logger LOGGER = LoggerFactory.getLogger(PropertyChangesChecker.class);
	private static Javers JAVERS;

	@Inject
	private AuthorizationDefinitionRepository authorizationDefinitionRepository;
	@Inject
	private Optional<List<PropertyChangePathConverter>> propertyChangeConverters;
	@Inject
	private Optional<List<CustomPropertyComparator>> propertyComparators;
	@Inject
	private Optional<List<ResourceConverter>> resourceConverter;

	private List<String> packagesToScan = Collections.emptyList();

	public PropertyChangesChecker(List<String> packagesToScan) {
		this.packagesToScan = packagesToScan;
	}

	@PostConstruct
	public void initJavers() {
		JAVERS = Optional.ofNullable(JAVERS).orElseGet(() -> {
			JaversBuilder javersBuilder = JaversBuilder.javers()
				.withListCompareAlgorithm(ListCompareAlgorithm.LEVENSHTEIN_DISTANCE)
				.registerObjectHasher(HashCodeObjectHasher.class)
				.withPrettyPrint(true);
			ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
			provider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
			packagesToScan.stream()
				.map(packagename -> provider.findCandidateComponents(packagename))
				.flatMap(Collection::stream)
				.forEach(entity -> {
					String beanClassName = entity.getBeanClassName();
					try {
						//all JPA entities needs to be registered as value objects otherwise path calculation is not working well
						javersBuilder.registerValueObject(Thread.currentThread().getContextClassLoader().loadClass(beanClassName));
					} catch (ClassNotFoundException e) {
						LOGGER.warn("Unable to load class [{}]", beanClassName);
					}
					LOGGER.debug("Found entity: [{}], registering to JAVERS as value object", beanClassName);
				});

			propertyComparators.orElse(Collections.emptyList())
				.forEach(comparator -> {
					Class<?>[] resolveTypeArguments = GenericTypeResolver.resolveTypeArguments(comparator.getClass(), CustomPropertyComparator.class);
					Class<Object> type = (Class<Object>) resolveTypeArguments[0];
					javersBuilder.registerCustomType(type, comparator);
				});
			return javersBuilder.build();

		});
	}

	public boolean checkPropertyPermissionForChanges(Object persistedResource, Object updatedResource, PropertyPermission passedPermission) {
		Set<Rights> propertyRights = authorizationDefinitionRepository.getPropertyRightsByNames(passedPermission.getRightsRefs());
		Assert.notNull(updatedResource, "Updated resource must be specified");
		AtomicReference<Boolean> result = new AtomicReference<Boolean>(true);
		if (resourceConverter.isPresent()) {
			ResourceConverter persistedResourceConverter = getSupportedResourceConverter(persistedResource);
			ResourceConverter updatedResourceConverter = getSupportedResourceConverter(updatedResource);
			if (persistedResourceConverter != null) {
				persistedResource = persistedResourceConverter.convert(persistedResource);
			}
			if (updatedResourceConverter != null) {
				updatedResource = updatedResourceConverter.convert(updatedResource);
			}
		}

		Diff diff = JAVERS.compare(persistedResource, updatedResource);
		List<Change> changes = diff.getChanges();
		Set<String> javersPathsChange = JAVERS.processChangeList(changes, new ResourceChangeLog(propertyChangeConverters));
		Set<String> pathChanges = UaaNotationConverter.convertToUaaNotation(javersPathsChange);

		Set<String> writeRights = propertyRights.stream()
			.flatMap(right -> right.getWrite().stream())
			.collect(Collectors.toSet());
		Set<String> invalidProperties = new HashSet<>();
		if (!pathChanges.isEmpty() && !CollectionUtils.containsAll(writeRights, pathChanges)) {
			LOGGER.debug(
				"[{}] object has been checked for modification and following properties has been changed [{}]," +
					"but you have WRITE rights only to [{}] properties. REJECTED",
				updatedResource.getClass().getSimpleName(), pathChanges, writeRights);
			result.set(false);
			invalidProperties = SetUtils.difference(pathChanges, writeRights);
		} else {
			LOGGER.debug("[{}] object has been checked for modification and following properties has been changed [{}]. You have WRITE rights all of them.",
				updatedResource.getClass().getSimpleName(), pathChanges);
		}
		passedPermission.rightExecuted(passedPermission.getName(), result.get(), pathChanges, invalidProperties);
		return result.get();
	}

	private ResourceConverter getSupportedResourceConverter(Object resource) {
		return resourceConverter.get().stream()
			.filter(cvt -> {
				Class<?>[] resolveTypeArguments = GenericTypeResolver.resolveTypeArguments(cvt.getClass(), ResourceConverter.class);
				Class<Object> resourceType = (Class<Object>) resolveTypeArguments[0];
				return resourceType.isInstance(resource);
			}).findFirst().orElse(null);
	}

}
