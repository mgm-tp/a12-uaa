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

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.mgmtp.a12.uaa.authentication.certificate.CertificateConverter;
import com.mgmtp.a12.uaa.authentication.principal.UAAPrincipal;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CertificateAuthenticationProviderTest {

	private DefaultResourceLoader resourceLoader = new DefaultResourceLoader();

	@Mock
	private CertificateConverter CertificateConverter;

	private CertificateAuthenticationProvider CertificateAuthenticationProvider;

	@BeforeEach
	void setUp() {
		UAAPrincipal<?> userDetail = new UAAPrincipal<>("admin", "*****", convertAuthorities("Admin;Manager"));
		Mockito.when(CertificateConverter.convert(Mockito.any())).thenReturn(userDetail);
	}

	@Test
	public void authenticationSuccess() throws Exception {
		UAAPrincipal<?> principal = callAuthenticationProviderAndGetUser("classpath:/certificate/client.crt");
		Assertions.assertNotNull(principal);
		Assertions.assertEquals("admin", principal.getUsername());
	}

	private Authentication callAuthenticationProvider(String certificateRef) throws Exception {
		CertificateAuthenticationProvider =
			new CertificateAuthenticationProvider(CertificateConverter, Optional.empty());
		X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(loadResourceContent(certificateRef));
		return CertificateAuthenticationProvider.authenticate(new CertificateAuthenticationToken(certificate));
	}

	private UAAPrincipal<?> callAuthenticationProviderAndGetUser(String certificateRef) throws Exception {
		Authentication authenticatedToken = callAuthenticationProvider(certificateRef);
		if (authenticatedToken != null)
			return (UAAPrincipal<?>) authenticatedToken.getPrincipal();
		return null;
	}

	private InputStream loadResourceContent(String resourceRef) throws IOException {
		return resourceLoader.getResource(resourceRef).getInputStream();
	}

	private Set<GrantedAuthority> convertAuthorities(String roles) {
		return Arrays.asList(roles.split(";")).stream()
			.map(roleName -> new SimpleGrantedAuthority(roleName))
			.collect(Collectors.toSet());
	}

}
