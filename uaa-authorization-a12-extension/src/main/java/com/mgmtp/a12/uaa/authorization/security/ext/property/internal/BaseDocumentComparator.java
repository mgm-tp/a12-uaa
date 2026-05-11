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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;

import org.javers.core.diff.changetype.PropertyChangeMetadata;
import org.javers.core.diff.changetype.PropertyChangeType;
import org.javers.core.metamodel.object.GlobalId;
import org.javers.core.metamodel.object.UnboundedValueObjectId;
import org.javers.core.metamodel.property.Property;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentService;
import com.mgmtp.a12.kernel.md.document.api.services.IEntityInstanceChangeInfo;
import com.mgmtp.a12.kernel.md.facade.DocumentServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

public class BaseDocumentComparator {

	private static final String PATH_SEPARATOR_SLASH = "/";
	private static final String TEMPLATE_PROPERTY_NAME_WITH_SEPARATOR = "%s.";
	private static final String TEMPLATE_COLLECTION_PROPERTY = "%s/%s/";
	private static final String TEMPLATE_COLLECTION_BEFORE_LAST_PROPERTY = "%s/%s.";

	@Inject
	private IDocumentModelResolver documentModelResolver;

	public List<DocumentPropertyChange<?>> compareIDocument(IDocument left, IDocument right, PropertyChangeMetadata metadata, Property property) {
		DocumentServiceFactory factory = new DocumentServiceFactory(documentModelResolver);
		IDocumentService docService = factory.createDocumentService(left);
		IDocumentModelSearchService documentModelSearchService = docService.getDocumentModelSearchService();
		Collection<IEntityInstanceChangeInfo> changes = docService.compare(right);
		return processChanges(changes, documentModelSearchService, metadata);
	}

	private String computeUaaPath(IEntityInstanceChangeInfo change, IDocumentModelSearchService documentModelSearchService) {
		String[] pathParts = change.getPath().substring(1).split(PATH_SEPARATOR_SLASH);
		assert pathParts.length == change.getRepetitions().length;
		String currentPath = "";
		StringBuilder uaaPath = new StringBuilder();
		for (int i = 0; i < pathParts.length; i++) { // - 2 since the last part is the field
			currentPath = currentPath + PATH_SEPARATOR_SLASH + pathParts[i];
			boolean collection = isCollection(currentPath, documentModelSearchService);

			String pattern = (i == pathParts.length - 1) ? pathParts[i] : TEMPLATE_PROPERTY_NAME_WITH_SEPARATOR.formatted(pathParts[i]);
			if (collection) {
				pattern = (i == pathParts.length - 2) ? TEMPLATE_COLLECTION_BEFORE_LAST_PROPERTY.formatted(pathParts[i], change.getRepetitions()[i])
					: TEMPLATE_COLLECTION_PROPERTY.formatted(pathParts[i], change.getRepetitions()[i]);
			}
			uaaPath.append(pattern);
		}
		return uaaPath.toString();
	}

	private boolean isCollection(String path, IDocumentModelSearchService documentModelSearchService) {
		return documentModelSearchService.getByPath(path)
			.filter(IGroup.class::isInstance)
			.map(IGroup.class::cast)
			.map(IGroup::getRepeatability)
			.map(repeatability -> repeatability > 1)
			.orElse(false);
	}

	private List<DocumentPropertyChange<?>> processChanges(Collection<IEntityInstanceChangeInfo> documentChanges,
		IDocumentModelSearchService documentModelSearchService, PropertyChangeMetadata objectMetadata) {
		List<DocumentPropertyChange<?>> javersChanges = new LinkedList<>();
		documentChanges.forEach(change -> {
			String fullUaaPath = computeUaaPath(change, documentModelSearchService);
			String type = Optional.ofNullable(change.getChangedValue()).map(value -> value.getClass().getCanonicalName()).orElse("null");
			GlobalId propertyGlobalId = new UnboundedValueObjectId(type);
			PropertyChangeMetadata metadata =
				new PropertyChangeMetadata(propertyGlobalId, fullUaaPath, objectMetadata.getCommitMetadata(), PropertyChangeType.PROPERTY_VALUE_CHANGED);
			DocumentPropertyChange<Object> javersChange = new DocumentPropertyChange<>(metadata, change.getChangedValue(), change.getChangedValue());
			javersChanges.add(javersChange);
		});
		return javersChanges;
	}
}
