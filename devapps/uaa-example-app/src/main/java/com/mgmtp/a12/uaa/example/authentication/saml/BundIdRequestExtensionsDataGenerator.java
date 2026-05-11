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
package com.mgmtp.a12.uaa.example.authentication.saml;

import java.util.Arrays;
import java.util.List;

import com.mgmtp.a12.uaa.authentication.saml.RequestExtension;
import com.mgmtp.a12.uaa.authentication.saml.RequestExtensionsDataGenerator;

//@Component
//@ConditionalOnAuthentication(AuthenticationType.SAML)
//Enable it only when there are meaningful data
public class BundIdRequestExtensionsDataGenerator implements RequestExtensionsDataGenerator {

	private static final String PREFIX = "akdb";
	private static final String NAMESPACE = "https://www.akdb.de/request/2018/09";

	@Override
	public List<RequestExtension> generateExtensionData() {

		RequestExtension enabled = new RequestExtension.Builder("Enabled", PREFIX, "true").withNamespace(NAMESPACE).build();
		RequestExtension benutzername = new RequestExtension.Builder("Benutzername", PREFIX, null).addChild(enabled).withNamespace(NAMESPACE).build();

		RequestExtension attribute1 = createAttribute("urn:oid:1.2.40.0.10.2.1.1.261.94", "false");
		RequestExtension attribute2 = createAttribute("urn:oid:1.3.6.1.4.1.25484.494450.3", "false");

		RequestExtension attribute3 = createAttribute("urn:oid:2.5.4.4", "true");

		RequestExtension attributes = new RequestExtension.Builder("RequestedAttributes", PREFIX, null)
			.withNamespace(NAMESPACE)
			.addChild(attribute1)
			.addChild(attribute2)
			.addChild(attribute3)
			.build();

		RequestExtension authnMethods = new RequestExtension.Builder("AuthnMethods", PREFIX, null)
			.withNamespace(NAMESPACE)
			.addChild(benutzername)
			.build();
		RequestExtension authRequest =
			new RequestExtension.Builder("AuthenticationRequest", PREFIX, null)
				.withNamespace(NAMESPACE)
				.addAttribute(new RequestExtension.Builder("Version", null, "2").build())
				.addChild(authnMethods)
				.addChild(attributes)
				.build();

		return Arrays.asList(authRequest);

	}

	private RequestExtension createAttribute(String version, String required) {
		return new RequestExtension.Builder("RequestedAttribute", PREFIX, "")
			.withNamespace(NAMESPACE)
			.addAttribute(new RequestExtension.Builder("Name", "", version).build())
			.addAttribute(new RequestExtension.Builder("RequiredAttribute", "", required).build())
			.build();
	}

}
