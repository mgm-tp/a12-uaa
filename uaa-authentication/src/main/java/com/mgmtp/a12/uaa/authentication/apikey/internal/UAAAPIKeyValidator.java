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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;

import com.mgmtp.a12.uaa.authentication.apikey.APIKeyValidator;

public class UAAAPIKeyValidator implements APIKeyValidator {
	private static final Logger LOGGER = LoggerFactory.getLogger(UAAAPIKeyValidator.class);

	@Override
	public boolean validate(X509Certificate certificateToVerify, List<X509Certificate> rootCAs) {
		int index = 0;
		for (X509Certificate serverRootCA : rootCAs) {
			boolean rootCAValid = isCertificateValid(serverRootCA);
			LOGGER.debug("Does the server rootCA from index [{}] is valid?: [{}]", index++, rootCAValid);
			if (rootCAValid) {
				boolean certificateValid = isCertificateValid(certificateToVerify);
				LOGGER.debug("Does the input certificate is valid?: [{}]", certificateValid);
				if (certificateValid && isCorrectSigned(certificateToVerify, serverRootCA)) {
					return true;
				}
			}
		}
		throw new BadCredentialsException("Certificate validation fails");
	}

	private boolean isCertificateValid(X509Certificate certificate) {
		try {
			certificate.checkValidity();
		} catch (CertificateExpiredException | CertificateNotYetValidException e) {
			LOGGER.debug("Certificate expired", e);
			return false;
		}
		return true;
	}

	private boolean isCorrectSigned(X509Certificate clientCertificate, X509Certificate rootCA) {
		try {
			clientCertificate.verify(rootCA.getPublicKey());
		} catch (CertificateException | NoSuchAlgorithmException |
				 InvalidKeyException | NoSuchProviderException |
				 SignatureException e) {
			LOGGER.debug("The input certificate has invalid signature", e);
			return false;
		}
		return true;
	}

}
