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

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;

public class IntrospectionVisitor extends DocumentModelVisitor {

	private static final String PATH_SEPARATOR = ".";
	private static final String COLLECTION = "[]";
	
	private Stack<String> nameStack = new Stack<>();
	private List<String> paths = new LinkedList<>();

	@Override
	public VisitProcess visitField(IField field) {
		nameStack.add(field.getName());
		return super.visitField(field);
	}

	@Override
	public VisitProcess visitElement(IElement element) {
		return super.visitElement(element);
	}

	@Override
	public VisitProcess visitGroup(IGroup group) {
		String path = group.getName();
		if (group.getRepeatability() > 1) {
			path += COLLECTION;
		}
		nameStack.add(path);
		return super.visitGroup(group);
	}

	@Override
	public void leaveElement(IElement element) {
		if (element instanceof IField) {
			computePath();
		}
		if (element instanceof IField || element instanceof IGroup) {
			//we need to ignore types for which we don't have visit methods 
			nameStack.pop();
		}
		super.leaveElement(element);
	}

	private void computePath() {
		List<String> path = new LinkedList<>();
		nameStack.forEach(path::add);
		String completePath = StringUtils.join(path, PATH_SEPARATOR);
		paths.add(completePath);
	}

	public List<String> getPaths() {
		return paths;
	}
}
