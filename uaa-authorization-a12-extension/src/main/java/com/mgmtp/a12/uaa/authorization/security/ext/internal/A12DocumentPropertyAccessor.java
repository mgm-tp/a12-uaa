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
package com.mgmtp.a12.uaa.authorization.security.ext.internal;

import java.util.Optional;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.api.IGroupInstance;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentSearchService;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.facade.DocumentServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;

@Component
public class A12DocumentPropertyAccessor implements PropertyAccessor {

	private static final String SLASH = "/";

	//we need it here because current kernel API doesn't provide API which can list all children of the group. 
	private static final String SEARCH_SERVICE = "___searchService";

	@Inject
	private IDocumentModelResolver documentModelResolver;

	private DocumentServiceFactory documentServiceFactory;

	@PostConstruct
	void init() {
		documentServiceFactory = new DocumentServiceFactory(documentModelResolver);
	}

	@Override
	public Class<?>[] getSpecificTargetClasses() {
		return null;
	}

	@Override
	public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
		if (target instanceof IDocument document) {
			IDocumentSearchService documentSearchService = documentServiceFactory.createDocumentSearchService(document);
			Set<IEntityInstance> set = documentSearchService.get(SLASH + name, null);
			if (set.size() != 1) {
				return false;
			}
			context.setVariable(SEARCH_SERVICE, documentSearchService);
			return true;
		}
		if (target instanceof IGroupInstance instance) {
			Optional<IEntityInstance> childrenByName = findChild(context, instance, name);
			return childrenByName.isPresent();

		}

		// documentV2
		if (target instanceof DocumentV2 documentV2) {
			return Optional.ofNullable(documentV2.group(name))
				.map(groupInstanceV2 -> true)
				.orElseGet(() -> Optional.ofNullable(documentV2.fieldValue(name)).isPresent());
		}

		if (target instanceof GroupInstanceV2 groupInstanceV2) {
			return Optional.ofNullable(groupInstanceV2.group(name))
				.map(gr -> true)
				.orElseGet(() -> Optional.ofNullable(groupInstanceV2.fieldValue(name)).isPresent());
		}
		return false;
	}

	@Override
	public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
		if (target instanceof IDocument document) {
			IDocumentSearchService documentSearchService = documentServiceFactory.createDocumentSearchService(document);
			Set<IEntityInstance> set = documentSearchService.get(SLASH + name, null);
			return new TypedValue(set.iterator().next());
		}
		if (target instanceof IGroupInstance instance) {
			Optional<IEntityInstance> child = findChild(context, instance, name);
			TypedValue typedValue = child.map(this::convert).orElse(null);
			return typedValue;
		}

		// documentV2
		if (target instanceof DocumentV2 documentV2) {
			return Optional.ofNullable(documentV2.group(name))
				.map(TypedValue::new)
				.orElseGet(() -> Optional.ofNullable(documentV2.fieldValue(name))
					.map(TypedValue::new)
					.orElse(null));
		}

		if (target instanceof GroupInstanceV2 groupInstanceV2) {
			return Optional.ofNullable(groupInstanceV2.group(name))
				.map(TypedValue::new)
				.orElseGet(() -> Optional.ofNullable(groupInstanceV2.fieldValue(name))
					.map(TypedValue::new)
					.orElse(null));
		}
		return null;
	}

	private TypedValue convert(IEntityInstance entity) {
		if (entity instanceof IFieldInstance instance) {
			return new TypedValue(instance.getValue().orElse(null));
		}
		return new TypedValue(entity);
	}

	private Optional<IEntityInstance> findChild(EvaluationContext context, IGroupInstance group, String name) {
		IDocumentSearchService documentSearchService = (IDocumentSearchService) context.lookupVariable(SEARCH_SERVICE);
		String childName = group.getPath() + SLASH + name;
		Set<IEntityInstance> children = documentSearchService.getChildren(group.getPath(), null);
		Optional<IEntityInstance> childrenByName = children.stream()
			.filter(child -> child.getPath().equals(childName))
			.findFirst();

		return childrenByName;
	}

	@Override
	public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
		return false;
	}

	@Override
	public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {

	}

}
