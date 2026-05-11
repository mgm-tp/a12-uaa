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
package com.mgmtp.a12.uaa.authorization.internal;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import com.mgmtp.a12.uaa.authorization.model.AuthorizationDefinition;

public class InMemoryAuthorizationDefinitionDataHolder {

	private static final AtomicReference<AuthorizationDefinition> PARENT = new AtomicReference<>(new AuthorizationDefinition());
	private static final AtomicReference<AuthorizationDefinition> CHILD = new AtomicReference<>(new AuthorizationDefinition());

	public static void initNewData(AuthorizationDefinition newMain, AuthorizationDefinition newAdditional) {
		Optional.ofNullable(newMain).ifPresent(PARENT::set);
		Optional.ofNullable(newAdditional).ifPresentOrElse(CHILD::set, () -> CHILD.set(new AuthorizationDefinition()));
	}

	static AuthorizationDefinition getParent() {
		return PARENT.get();
	}

	static AuthorizationDefinition getChild() {
		return CHILD.get();
	}

	private InMemoryAuthorizationDefinitionDataHolder() {
	}
}
