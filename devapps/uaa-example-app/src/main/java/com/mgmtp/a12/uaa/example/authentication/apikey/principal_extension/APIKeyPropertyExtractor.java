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
package com.mgmtp.a12.uaa.example.authentication.apikey.principal_extension;

import java.security.cert.X509Certificate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;
import com.mgmtp.a12.uaa.authentication.principal.AbstractExtendedPrincipal;
import com.mgmtp.a12.uaa.authentication.principal.PropertyExtractor;
import com.mgmtp.a12.uaa.authentication.principal.certificate.CertificateUtils;

@Component
@Profile("principal")
@ConditionalOnAuthentication(AuthenticationType.API_KEY)
public class APIKeyPropertyExtractor implements PropertyExtractor<X509Certificate> {

	private static final String COUNTRY = "1.2.276.256";

	@Override
	public String extractProperty(AbstractExtendedPrincipal<?> user, X509Certificate payload, String propertyName) {
		if (StringUtils.equals(propertyName, "nationality")) {
			return CertificateUtils.getAttributeValueFromCertificateExtension(payload, COUNTRY);
		}
		return null;
	}
}
