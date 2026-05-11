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
package com.mgmtp.a12.uaa.authentication.principal.saml.internal;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.util.CollectionUtils;

import com.mgmtp.a12.uaa.authentication.saml.SamlAssertionUtils;

public class AssertionUtils {

	public static Optional<String> findAttribute(Response response, String propertyName) {
		Assertion assertion = CollectionUtils.firstElement(response.getAssertions());
		Map<String, List<Object>> attributes = SamlAssertionUtils.getAssertionAttributes(assertion);

		Optional<String> propertyValue = attributes.entrySet().stream()
			.filter(entry -> entry.getKey().equals(propertyName))
			.map(entry -> CollectionUtils.firstElement(entry.getValue()))
			.map(value -> String.valueOf(value))
			.findFirst();

		return propertyValue;

	}

}
