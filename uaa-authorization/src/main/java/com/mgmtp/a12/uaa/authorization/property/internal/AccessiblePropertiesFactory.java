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
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public class AccessiblePropertiesFactory {

	private static final String SEPARATOR = ".";

	public static PropertyTreeRoot createPropertyPermissions(Collection<String> permissions, Collection<String> masking) {
		PropertyTreeRoot root = new PropertyTreeRoot(masking, permissions);
		permissions.forEach(permission -> {
			createPropertyPermission(permission, root);
		});
		return root;
	}

	static private PropertyTree createPropertyPermission(String permission, PropertyTree parent) {
		String propertyName = StringUtils.substringBefore(permission, SEPARATOR);
		String remaining = StringUtils.substringAfter(permission, SEPARATOR);
		PropertyTree child = Optional.ofNullable(parent.getChild(propertyName)).orElseGet(() -> {
			PropertyTree newChild = new PropertyTree(propertyName);
			parent.addChild(newChild);
			return newChild;
		});
		if (StringUtils.isNotBlank(remaining)) {
			createPropertyPermission(remaining, child);
		}
		return child;
	}

}
