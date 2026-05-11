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
package com.mgmtp.a12.uaa.authentication.apikey.internal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.bouncycastle.util.encoders.Base64;
import org.springframework.core.io.Resource;

class APIKeyUtils {

	private APIKeyUtils() {
	}

	/**
	 * convert certificate string to X509Certificate object
	 * @param certEntry as base64 encode string
	 * @return X509Certificate object
	 */
	public static X509Certificate getX509Certificate(String certEntry) {
		byte[] certEntryBytes = Base64.decode(certEntry);
		try (InputStream inputStream = new ByteArrayInputStream(certEntryBytes)) {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			return (X509Certificate) certFactory.generateCertificate(inputStream);
		} catch (Exception e) {
			throw new IllegalArgumentException("Can not get the certificate", e);
		}
	}

	/**
	 * convert certificate string to X509Certificate object
	 * @param certEntry as pem encoded certificate string
	 * @return X509Certificate object
	 */
	public static X509Certificate getX509Certificate(Resource certEntry) {
		try (InputStream inputStream = certEntry.getInputStream()) {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			return (X509Certificate) certFactory.generateCertificate(inputStream);
		} catch (Exception e) {
			throw new IllegalArgumentException("Can not get the certificate", e);
		}
	}

}
