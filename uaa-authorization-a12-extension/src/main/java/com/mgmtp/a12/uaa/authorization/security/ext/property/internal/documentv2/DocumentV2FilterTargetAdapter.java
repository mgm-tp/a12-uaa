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
package com.mgmtp.a12.uaa.authorization.security.ext.property.internal.documentv2;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.uaa.authorization.model.internal.ResourceWrapper;
import com.mgmtp.a12.uaa.authorization.security.spel.internal.UAAMethodSecurityExpressionHandler;

/**
 * This class is responsible to wrap the {@link DocumentV2} resources into {@link ResourceWrapper}.
 * The purpose is because {@link DocumentV2} is immutable so that we can not modify it directly.
 * We need the {@link ResourceWrapper wrapper} to set the new modified value
 */
public class DocumentV2FilterTargetAdapter implements UAAMethodSecurityExpressionHandler.FilterTargetAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentV2FilterTargetAdapter.class.getCanonicalName() + "_Print");

	@Override
	public Object preFilter(Object filterTarget) {
		if (filterTarget instanceof Collection collectionFilterTarget) {
			List<ResourceWrapper> resourceWrappers = new ArrayList<>(collectionFilterTarget.size());
			for (Object obj : collectionFilterTarget) {
				if (obj instanceof DocumentV2 documentV2) {
					resourceWrappers.add(new ResourceWrapper(documentV2));
				} else {
					break;
				}
			}
			return collectionFilterTarget.size() == resourceWrappers.size() ? resourceWrappers : filterTarget;
		}
		if (filterTarget.getClass().isArray()) {
			List<ResourceWrapper> resourceWrappers = new ArrayList<>();
			Object[] items = (Object[]) filterTarget;
			for (Object item : items) {
				if (item instanceof DocumentV2 documentV2) {
					resourceWrappers.add(new ResourceWrapper(documentV2));
				} else {
					break;
				}
			}
			return resourceWrappers.size() == items.length ? resourceWrappers.toArray() : filterTarget;
		}
		if (filterTarget instanceof Map mapFilterObject) {
			Map<Object, ResourceWrapper> documentV2Wrappers = new LinkedHashMap<>(mapFilterObject.size());
			mapFilterObject.forEach((key, value) -> {
				if (value instanceof DocumentV2 documentV2) {
					documentV2Wrappers.put(key, new ResourceWrapper(documentV2));
				}
			});
			return documentV2Wrappers.size() == mapFilterObject.size() ? documentV2Wrappers : filterTarget;
		}
		if (filterTarget instanceof Stream streamFilterObject) {
			return streamFilterObject
				.map(item -> {
					if (item instanceof DocumentV2 documentV2) {
						return new ResourceWrapper(documentV2);
					}
					return item;
				});
		}

		return filterTarget;
	}

	@Override
	public Object postFilter(Object originalFilterTarget, Object processedFilterTarget) {
		if (Objects.equals(originalFilterTarget, processedFilterTarget)) {
			return originalFilterTarget;
		}
		if (originalFilterTarget instanceof Collection collectionOriginalFilterTarget &&
			processedFilterTarget instanceof Collection collectionFilterTarget) {
			List<Object> documentV2s = new ArrayList<>(collectionFilterTarget.size());
			for (Object obj : collectionFilterTarget) {
				if (obj instanceof ResourceWrapper wrapper) {
					documentV2s.add(wrapper.getResource());
				} else {
					break;
				}
			}

			if (collectionFilterTarget.size() == documentV2s.size()) {
				try {
					collectionOriginalFilterTarget.clear();
					collectionOriginalFilterTarget.addAll(documentV2s);
					return collectionOriginalFilterTarget;
				} catch (UnsupportedOperationException readonly) {
					LOGGER.debug("Don no use immutable collection or else the masking process can not be done.");
					return collectionOriginalFilterTarget;
				}
			}
			return originalFilterTarget;
		}
		if (originalFilterTarget.getClass().isArray() && processedFilterTarget.getClass().isArray()) {
			List<Object> documentV2s = new ArrayList<>();
			Object[] originalItems = (Object[]) processedFilterTarget;
			Object[] processedItems = (Object[]) processedFilterTarget;
			for (Object item : processedItems) {
				if (item instanceof ResourceWrapper wrapper) {
					documentV2s.add(wrapper.getResource());
				} else {
					break;
				}
			}

			if (documentV2s.size() == processedItems.length) {
				Object[] filtered = (Object[]) Array.newInstance(originalItems.getClass().getComponentType(), documentV2s.size());
				for (int i = 0; i < documentV2s.size(); i++) {
					filtered[i] = documentV2s.get(i);
				}
				return filtered;
			}
			return originalFilterTarget;
		}
		if (originalFilterTarget instanceof Map originalMapFilterObject &&
			processedFilterTarget instanceof Map processedMapFilterObject) {
			Map<Object, Object> documentV2s = new LinkedHashMap<>();
			processedMapFilterObject.forEach((key, value) -> {
				if (value instanceof ResourceWrapper wrapper) {
					documentV2s.put(key, wrapper.getResource());
				}
			});

			if (documentV2s.size() == originalMapFilterObject.size()) {
				originalMapFilterObject.clear();
				originalMapFilterObject.putAll(documentV2s);
				return originalMapFilterObject;
			}
			return originalMapFilterObject;
		}
		if (processedFilterTarget instanceof Stream streamFilterObject) {
			return streamFilterObject
				.map(item -> {
					if (item instanceof ResourceWrapper wrapper) {
						return wrapper.getResource();
					}
					return item;
				});
		}

		return originalFilterTarget;
	}
}
