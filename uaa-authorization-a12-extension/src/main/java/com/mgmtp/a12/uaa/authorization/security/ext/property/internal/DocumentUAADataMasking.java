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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.RepetitionsV2;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.uaa.authorization.AuthorizationContext;
import com.mgmtp.a12.uaa.authorization.AuthorizationContextHolder;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.model.internal.ResourceWrapper;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyTree;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyTreeRoot;
import com.mgmtp.a12.uaa.authorization.property.internal.UAADataMasking;

public class DocumentUAADataMasking extends UAADataMasking {

	private final static Logger LOGGER = LoggerFactory.getLogger(DocumentUAADataMasking.class);

	private IDocumentModelResolver documentModelResolver;

	public DocumentUAADataMasking(AuthorizationDefinitionRepository authorizationDefinitionRepository, IDocumentModelResolver documentModelResolver) {
		super(authorizationDefinitionRepository);
		this.documentModelResolver = documentModelResolver;
	}

	@Override
	protected <T> void performDataMasking(T resource, PropertyTree accessiblePropertiesParent) {
		// DocumentV2 for PostFilter operation
		if (resource instanceof ResourceWrapper resourceWrapper && resourceWrapper.getResource() instanceof DocumentV2 documentV2) {
			resourceWrapper.setResource(documentV2Masking(documentV2, accessiblePropertiesParent));
			return;
		}

		// DocumentV2 for PostAuthorize operation
		if (resource instanceof DocumentV2 documentV2) {
			AuthorizationContext authorizationContext = AuthorizationContextHolder.getContext();
			authorizationContext.setMaskedReturnObject(documentV2Masking(documentV2, accessiblePropertiesParent));
			return;
		}
		super.performDataMasking(resource, accessiblePropertiesParent);
	}

	private DocumentV2 documentV2Masking(DocumentV2 documentV2, PropertyTree accessiblePropertiesParent) {
		Collection<String> allProperties = getAllPropertyPaths(documentV2);
		Collection<String> accessibleProperties = ((PropertyTreeRoot) accessiblePropertiesParent).getProperties();
		List<String> propertiesToMask = allProperties.stream()
			.filter(property -> !accessibleProperties.contains(property))
			.map(str -> str.replaceAll("\\.", "/")).toList();

		return maskDocumentPropertiesV2(documentV2, propertiesToMask);
	}

	private DocumentV2 maskDocumentPropertiesV2(DocumentV2 document, List<String> propertiesToMask) {
		Map<String, List<String>> firstLevelRepetitions = new HashMap<>();
		Map<String, List<String>> nonFirstLevelRepetitions = new HashMap<>();

		for (String property : propertiesToMask) {
			String[] split = property.split("\\[]/");
			// direct property
			if (split.length == 1) {
				document = document.withFieldValue(property, null);
				continue;
			}

			// split[0] is the path of repetition -> array
			List<String> subPathsOfFirstLevelRepetition = firstLevelRepetitions.computeIfAbsent(split[0], key -> new ArrayList<>());
			subPathsOfFirstLevelRepetition.add(split[1]);
			for (int i = 1; i < split.length - 1; i++) {
				List<String> subPathsOfNonDFirstLevelRepetition = nonFirstLevelRepetitions.computeIfAbsent(split[i], key -> new ArrayList<>());
				subPathsOfNonDFirstLevelRepetition.add(split[i + 1]);
			}
		}

		for (var entry : firstLevelRepetitions.entrySet()) {
			String repetitionPath = entry.getKey();
			List<String> repetitionSubPaths = entry.getValue();

			document = document.withGroupAllRepetitions(getRepetitionPointer(repetitionPath),
				traverseRepetition(document.groupAllRepetitions(getRepetitionPointer(repetitionPath)), repetitionSubPaths, nonFirstLevelRepetitions));
		}

		return document;
	}

	private RepetitionsV2 traverseRepetition(RepetitionsV2 repetitions, List<String> repetitionSubPaths, Map<String, List<String>> nonFirstLevelRepetitions) {
		for (int i = 1; i <= repetitions.size(); i++) {
			GroupInstanceV2 group = repetitions.get(i);
			for (String subPath : repetitionSubPaths) {
				// not a path of a repetition => direct field
				if (nonFirstLevelRepetitions.get(subPath) == null) {
					group = group.withFieldValue(subPath, null);
				} else {
					group = group.withGroupAllRepetitions(getRepetitionPointer(subPath),
						traverseRepetition(group.groupAllRepetitions(getRepetitionPointer(subPath)),
							nonFirstLevelRepetitions.get(subPath), nonFirstLevelRepetitions));
				}
			}
			repetitions = repetitions.withReplacedAt(i, group);
		}

		return repetitions;
	}

	private Collection<String> getAllPropertyPaths(DocumentV2 document) {
		return getAllPropertyPaths(documentModelResolver.getDocumentModelById(document.getDocumentModelId()));
	}

	private Collection<String> getAllPropertyPaths(IDocumentModel documentModel) {
		DocumentModelWalker walker = new DocumentModelWalker();
		IntrospectionVisitor introspectionVisitor = new IntrospectionVisitor();
		walker.acceptDocumentModel(documentModel, introspectionVisitor);
		return introspectionVisitor.getPaths();
	}

	private String getRepetitionPointer(String path) {
		return "%s[0]".formatted(path);
	}
}
