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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;

import org.javers.core.diff.changetype.PropertyChangeMetadata;
import org.javers.core.diff.changetype.PropertyChangeType;
import org.javers.core.diff.custom.CustomPropertyComparator;
import org.javers.core.metamodel.object.GlobalId;
import org.javers.core.metamodel.object.UnboundedValueObjectId;
import org.javers.core.metamodel.property.Property;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.kernel.md.document.apiV2.documentchanges.Change;
import com.mgmtp.a12.kernel.md.document.apiV2.documentchanges.DocumentChanges;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.utils.DocumentV2Utils;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.uaa.authorization.security.ext.property.internal.BaseDocumentComparator;
import com.mgmtp.a12.uaa.authorization.security.ext.property.internal.DocumentPropertyChange;
import com.mgmtp.a12.uaa.authorization.security.ext.property.internal.IntrospectionVisitor;

@Component
public class DocumentV2Comparator extends BaseDocumentComparator implements CustomPropertyComparator<DocumentV2, DocumentV2Changes> {

	private static final String SLASH_SEPARATOR = "/";
	private static final String COLLECTION = "[]";

	@Inject
	private IDocumentModelResolver documentModelResolver;

	@Override
	public boolean equals(DocumentV2 a, DocumentV2 b) {
		return false;
	}

	@Override
	public String toString(DocumentV2 value) {
		return null;
	}

	@Override
	public Optional<DocumentV2Changes> compare(DocumentV2 left, DocumentV2 right, PropertyChangeMetadata metadata, Property property) {
		DocumentChanges documentChanges = DocumentV2Utils.compare(left, right, DocumentV2Utils.CompareConfig.builder().ignoreScaleOfNumbers(true).build());
		Collection<Change<FieldInstanceV2>> fieldChanges = documentChanges.fieldChanges();
		Collection<Change<GroupInstanceV2>> groupChanges = documentChanges.groupChanges();

		List<DocumentPropertyChange<?>> javersChanges = new LinkedList<>();
		Collection<String> allProperties = getAllPropertyPaths(documentModelResolver.getDocumentModelById(left.getDocumentModelId()));

		fieldChanges.forEach(change -> {
			String fullUaaPath = getNormalizePath(change.pointer().fullName(), allProperties);
			GlobalId propertyGlobalId = new UnboundedValueObjectId(PropertyChangeType.class.getCanonicalName());
			PropertyChangeMetadata newMetadata =
				new PropertyChangeMetadata(propertyGlobalId, fullUaaPath, metadata.getCommitMetadata(), PropertyChangeType.PROPERTY_VALUE_CHANGED);
			javersChanges.add(getDocumentPropertyChange(fullUaaPath, Optional.ofNullable(change.oldValue()).map(
				FieldInstanceV2::value).orElse(null), Optional.ofNullable(change.newValue()).map(FieldInstanceV2::value).orElse(null), newMetadata));
		});

		groupChanges.forEach(change -> Optional.ofNullable(change.newValue())
			.ifPresentOrElse(newChange ->
				getAllFieldsOfGroup(newChange, change.pointer().fullName(), javersChanges, metadata, allProperties),
				() -> getAllFieldsOfGroup(change.oldValue(), change.pointer().fullName(), javersChanges, metadata, allProperties)));

		return Optional.of(new DocumentV2Changes(metadata, javersChanges, left, right));
	}

	private Collection<String> getAllPropertyPaths(IDocumentModel documentModel) {
		DocumentModelWalker walker = new DocumentModelWalker();
		IntrospectionVisitor introspectionVisitor = new IntrospectionVisitor();
		walker.acceptDocumentModel(documentModel, introspectionVisitor);
		return introspectionVisitor.getPaths();
	}

	private String getNormalizePath(String path, Collection<String> allProperties) {
		return allProperties.stream()
			.filter(realPath -> (SLASH_SEPARATOR + realPath.replaceAll("\\" + COLLECTION, "")
				.replaceAll("\\.", SLASH_SEPARATOR))
				.equals(path.replaceAll("\\" + COLLECTION, "")))
			.findFirst()
			.orElse(path);
	}

	private void getAllFieldsOfGroup(GroupInstanceV2 group, String parentPath, List<DocumentPropertyChange<?>> javersChanges, PropertyChangeMetadata metadata,
		Collection<String> allProperties) {
		group.directSubgroups().forEach(repetition -> repetition.getValue().forEach(subGr ->
			getAllFieldsOfGroup(subGr, "%s/%s[]".formatted(parentPath, repetition.getKey()), javersChanges, metadata, allProperties)));
		group.directFields().forEach(field -> {
			String fullUaaPath = getNormalizePath("%s/%s".formatted(parentPath, field.getKey()), allProperties);
			javersChanges.add(getDocumentPropertyChange(fullUaaPath, field.getValue().value(), field.getValue().value(), metadata));
		});
	}

	private DocumentPropertyChange<?> getDocumentPropertyChange(String path, Object leftValue, Object rightValue, PropertyChangeMetadata metadata) {
		GlobalId propertyGlobalId = new UnboundedValueObjectId(PropertyChangeType.class.getCanonicalName());
		PropertyChangeMetadata newMetadata =
			new PropertyChangeMetadata(propertyGlobalId, path, metadata.getCommitMetadata(), PropertyChangeType.PROPERTY_VALUE_CHANGED);
		return new DocumentPropertyChange<>(newMetadata, leftValue, rightValue);
	}
}
