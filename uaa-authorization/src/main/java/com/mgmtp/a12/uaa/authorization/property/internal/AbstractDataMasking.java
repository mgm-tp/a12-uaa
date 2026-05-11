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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.AuthorizationReloadedEvent;
import com.mgmtp.a12.uaa.authorization.internal.UAAUserDetails;
import com.mgmtp.a12.uaa.authorization.internal.UserUtils;
import com.mgmtp.a12.uaa.authorization.model.PropertyPermission;
import com.mgmtp.a12.uaa.authorization.model.Rights;
import com.mgmtp.a12.uaa.authorization.security.DataMasking;

public abstract class AbstractDataMasking implements DataMasking {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDataMasking.class);

	private static Map<PropertyPermission, PropertyTreeRoot> cacheAccessibleProperties = new ConcurrentHashMap<>();

	private AuthorizationDefinitionRepository authorizationDefinitionRepository;

	public AbstractDataMasking(AuthorizationDefinitionRepository authorizationDefinitionRepository) {
		this.authorizationDefinitionRepository = authorizationDefinitionRepository;
	}

	public <T> T maskData(T resource, PropertyPermission propertyPermission) {
		UAAUserDetails currentUser = UserUtils.resolveCurrentUser();
		if (resource == null || propertyPermission == null) {
			return resource;
		}
		if (currentUser.runPrivileged()) {
			LOGGER.info("Running in privilege mode. Masking data for object[{}] is ignored.", resource.getClass().getSimpleName());
			return resource;
		}
		LOGGER.info("Resource class[{}]: Masking resource data...", resource.getClass().getCanonicalName());
		LOGGER.debug("Resource class[{}]: Masking resource data[{}]", resource.getClass().getCanonicalName(), resource);

		PropertyTreeRoot accessibleProperties = getAccessiblePermissions(propertyPermission);
		LOGGER.debug("Resource class[{}]: Accessible properties {}", resource.getClass().getCanonicalName(), accessibleProperties);
		performDataMasking(resource, accessibleProperties);
		performCustomMasking(resource, accessibleProperties);
		return resource;
	}
	
	@EventListener
	void authorizationReloaded(@SuppressWarnings("unused") AuthorizationReloadedEvent event) {
		cacheAccessibleProperties.clear();
	}


	protected abstract <T> void performCustomMasking(T resource, PropertyTreeRoot accessibleProperties);

	protected abstract <T> void performDataMasking(T resource, PropertyTree accessiblePropertiesParent);

	PropertyTreeRoot getAccessiblePermissions(PropertyPermission propertyPermission) {
		return cacheAccessibleProperties.computeIfAbsent(propertyPermission, t -> {
			Set<Rights> propertyRights = authorizationDefinitionRepository.getPropertyRightsByNames(propertyPermission.getRightsRefs());
			Set<String> allAccessibleProperties = propertyRights.stream()
				.flatMap(right -> Stream.of(right.getRead(), right.getWrite()).flatMap(Collection::stream))
				.collect(Collectors.toCollection(LinkedHashSet::new));
			List<String> masking = propertyRights.stream()
				.flatMap(right -> Optional.ofNullable(right.getMask()).orElse(Collections.emptySet()).stream())
				.collect(Collectors.toList());

			return AccessiblePropertiesFactory.createPropertyPermissions(allAccessibleProperties, masking);
		});
	}

}
