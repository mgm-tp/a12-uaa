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
package com.mgmtp.a12.uaa.authentication.certificate.internal;

import java.util.Optional;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.mgmtp.a12.uaa.authentication.certificate.CertificateConverter;
import com.mgmtp.a12.uaa.authentication.certificate.CertificateValidator;

public class CertificateAuthenticationProvider implements AuthenticationProvider {

	private CertificateConverter certificateConverter;
	private Optional<CertificateValidator> certificateValidator;

	public CertificateAuthenticationProvider(CertificateConverter certificateConverter, Optional<CertificateValidator> certificateValidator) {
		this.certificateConverter = certificateConverter;
		this.certificateValidator = certificateValidator;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		CertificateAuthenticationToken authToken = (CertificateAuthenticationToken) authentication;

		if (certificateValidator.isPresent() && !certificateValidator.get().validate(authToken.getCertificate())) {
			throw new BadCredentialsException("Validation of certificate failed");
		}

		return Optional.ofNullable(certificateConverter.convert(authToken.getCertificate()))
			.map(userDetails -> new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()))
			.orElse(null);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return CertificateAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
