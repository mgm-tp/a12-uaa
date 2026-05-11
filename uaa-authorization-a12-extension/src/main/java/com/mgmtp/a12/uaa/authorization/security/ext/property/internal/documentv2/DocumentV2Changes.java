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

import java.util.List;

import org.javers.common.string.PrettyValuePrinter;
import org.javers.core.diff.changetype.PropertyChange;
import org.javers.core.diff.changetype.PropertyChangeMetadata;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.uaa.authorization.security.ext.property.internal.DocumentPropertyChange;

public class DocumentV2Changes extends PropertyChange<DocumentV2> {

	private List<DocumentPropertyChange<?>> propertyChanges;

	private final transient DocumentV2 left;
	private final transient DocumentV2 right;

	public DocumentV2Changes(PropertyChangeMetadata propertyChangeMetadata, List<DocumentPropertyChange<?>> propertyChanges, DocumentV2 left,
		DocumentV2 right) {
		super(propertyChangeMetadata);
		this.left = left;
		this.right = right;
		this.propertyChanges = propertyChanges;
	}

	@Override
	public DocumentV2 getLeft() {
		return left;
	}

	@Override
	public DocumentV2 getRight() {
		return right;
	}

	@Override
	public String prettyPrint(PrettyValuePrinter valuePrinter) {
		return null;
	}

	public List<DocumentPropertyChange<?>> getPropertyChanges() {
		return propertyChanges;
	}

}
