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
package com.mgmtp.a12.uaa.authorization.security.ext.property.internal;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.a12.kernel.md.document.api.IDocumentIndexed;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentService;
import com.mgmtp.a12.kernel.md.facade.DocumentServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyTree;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyTreeRoot;

public class DocumentPropertyValueResolver {

	private final static Logger LOGGER = LoggerFactory.getLogger(DocumentPropertyValueResolver.class);

	private static String COLLECTION = "[]";
	private static String KERNEL_PATH_SEPARATOR = "/";
	private static String UAA_PATH_SEPARATOR = ".";

	private IDocumentModelResolver documentModelResolver;

	public DocumentPropertyValueResolver(IDocumentModelResolver documentModelResolver) {
		this.documentModelResolver = documentModelResolver;
	}

	public List<DocumentPropertyValue> findAllValues(IDocumentIndexed document, PropertyTreeRoot propertiesRoot) {
		DocumentServiceFactory factory = new DocumentServiceFactory(documentModelResolver);
		IDocumentService docService = factory.createDocumentService(document);
		IDocumentModelSearchService documentModelSearchService = docService.getDocumentModelSearchService();
		List<DocumentPropertyValue> values = new LinkedList<>();
		propertiesRoot.getChildrens()
			.forEach(property -> {
				findValue(values, document, property.getPropertyName(), new int[] { 1 }, property, documentModelSearchService);
			});

		return values;
	}

	private List<DocumentPropertyValue> findValue(List<DocumentPropertyValue> values, IDocumentIndexed document, String propertyPath, int[] repetitions,
		PropertyTree propertyParent, IDocumentModelSearchService documentModelSearchService) {
		String documentPropertyPath = getDocumentPropertyName(propertyPath);
		if (CollectionUtils.isEmpty(propertyParent.getChildrens())) {
			//leaf
			Optional<IFieldInstance> property = findProperty(document, documentPropertyPath, repetitions);
			values.add(new DocumentPropertyValue(propertyPath, repetitions, property));
			return values;
		}
		int repeatibility = getRepeatibility(documentPropertyPath, documentModelSearchService);
		int repetitionsLenght = repetitions.length;
		for (int i = 1; i <= repeatibility; i++) {
			int[] newRepetitions = Arrays.copyOf(repetitions, repetitionsLenght + 1);
			newRepetitions[newRepetitions.length - 2] = i; //current array prop
			newRepetitions[newRepetitions.length - 1] = 1; //last property
			propertyParent.getChildrens()
				.forEach(children -> {
					String childProperty = propertyPath + UAA_PATH_SEPARATOR + children.getPropertyName();
					findValue(values, document, childProperty, newRepetitions, children, documentModelSearchService);
				});
		}
		return values;
	}

	private int getRepeatibility(String path, IDocumentModelSearchService documentModelSearchService) {
		return documentModelSearchService.getByPath(path)
			.map(IGroup.class::cast)
			.map(IGroup::getRepeatability)
			.orElse(1);
	}

	private Optional<IFieldInstance> findProperty(IDocumentIndexed document, String propertyPath, int[] reperitions) {
		// repetitions can be null if you want to change all field instances in a repeatable group (javadoc should be helpful)

		Optional<IEntityInstance> entityInstance = document.getEntityInstance(propertyPath, reperitions);
		if (entityInstance.isPresent()) {
			IFieldInstance fieldInstance = (IFieldInstance) entityInstance.get();
			LOGGER.debug("Found property [{}], repetitions[{}]", propertyPath, reperitions);
			return Optional.of(fieldInstance);
		}
		LOGGER.debug("Property [{}] , repetitions[{}] not found", propertyPath, reperitions);
		return Optional.empty();
	}

	private String getDocumentPropertyName(String uaaPropertyName) {
		String documentProperty = StringUtils.replace(uaaPropertyName, UAA_PATH_SEPARATOR, KERNEL_PATH_SEPARATOR);
		documentProperty = StringUtils.replace(documentProperty, COLLECTION, "");
		return KERNEL_PATH_SEPARATOR + documentProperty;
	}
}
