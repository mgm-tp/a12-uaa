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

import jakarta.inject.Inject;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;
import com.mgmtp.a12.uaa.authentication.certificate.CertificateConverter;
import com.mgmtp.a12.uaa.authentication.certificate.CertificateValidator;
import com.mgmtp.a12.uaa.authentication.security.UAASecurityConfigurer;
import com.mgmtp.a12.uaa.authentication.security.login.internal.UAALoginEntryPoint;

@Configuration
@ConditionalOnAuthentication(AuthenticationType.CERTIFICATE)
public class CertificateSecurityConfigurer extends UAASecurityConfigurer<CertificateSecurityConfigurer> {

	@Inject
	private AuthenticationProperties authenticationProperties;
	@Inject
	private CertificateConverter certificatePrincipalCreator;
	@Inject
	private Optional<CertificateValidator> certificateValidator;

	@Override
	public void configure(HttpSecurity http) {
		UAALoginEntryPoint loginEntryPoint = new UAALoginEntryPoint(authenticationProperties.getUnauthorizedCode());
		CertificateAuthenticationFilter certificateAuthenticationFilter = new CertificateAuthenticationFilter(
			getAuthenticationManager(http), authenticationProperties.getContextPath(),
			authenticationProperties.getCertificateWhiteListAccessUrlPatterns(), loginEntryPoint);
		http.addFilterBefore(certificateAuthenticationFilter, AuthorizationFilter.class);
	}

	@Override
	protected Optional<AuthenticationProvider> createAuthenticationProvider() {
		return Optional.of(new CertificateAuthenticationProvider(certificatePrincipalCreator, certificateValidator));
	}
}
