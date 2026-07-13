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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
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
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.mgmtp.a12.uaa.authentication.apikey.APIKeyConverter;
import com.mgmtp.a12.uaa.authentication.principal.UAAPrincipal;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jose.util.StandardCharset;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class APIKeyAuthenticationProviderTest {

	private DefaultResourceLoader resourceLoader = new DefaultResourceLoader();

	@Mock
	private APIKeyConverter apiKeyConverter;

	private APIKeyAuthenticationProvider apiKeyAuthenticationProvider;

	@BeforeEach
	void setUp() {
		UAAPrincipal<?> userDetail = new UAAPrincipal<>("test_server", "*****", convertAuthorities("Admin;Manager"));
		Mockito.when(apiKeyConverter.convert(Mockito.any())).thenReturn(userDetail);
	}

	@Test
	public void authenticationSuccessWithSingleProperServerCertificate() throws Exception {
		UAAPrincipal<?> principal =
			(UAAPrincipal<?>) callAuthenticationProviderAndGetUser(Arrays.asList("classpath:/apikey/server/ServerA_expired_after_36500_days.crt"),
				"classpath:/apikey/client/ClientA_expired_after_36500_days.crt");
		Assertions.assertNotNull(principal);
		Assertions.assertEquals("test_server", principal.getUsername());
	}

	@Test
	public void clientAAuthenticationSuccessWithOneOfProperServerCertificate() throws Exception {
		UAAPrincipal<?> principal =
			callAuthenticationProviderAndGetUser(Arrays.asList("classpath:/apikey/server/ServerB_expired_after_36500_days.crt",
					"classpath:/apikey/server/ServerA_expired_after_36500_days.crt"),
				"classpath:/apikey/client/ClientA_expired_after_36500_days.crt");
		Assertions.assertNotNull(principal);
		Assertions.assertEquals("test_server", principal.getUsername());

	}

	@Test
	public void clientBAuthenticationSuccessWithOneOfProperServerCertificate() throws Exception {
		UAAPrincipal<?> principal =
			callAuthenticationProviderAndGetUser(Arrays.asList("classpath:/apikey/server/ServerB_expired_after_36500_days.crt",
					"classpath:/apikey/server/ServerA_expired_after_36500_days.crt"),
				"classpath:/apikey/client/ClientB_expired_after_36500_days.crt");
		Assertions.assertNotNull(principal);
		Assertions.assertEquals("test_server", principal.getUsername());
	}

	@Test
	public void authenticationFailureWithSingleFileServerCertificateNotFound() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			callAuthenticationProviderAndGetUser(Arrays.asList("classpath:/apikey/server/ServerNotFound.crt"),
				"classpath:/apikey/client/ClientA_expired_after_36500_days.crt");
		});
	}

	@Test
	public void authenticationFailureWithoutProperServerCertificate() {
		Assertions.assertThrows(BadCredentialsException.class, () -> {
			callAuthenticationProvider(Arrays.asList("classpath:/apikey/server/ServerB_expired_after_36500_days.crt"),
				"classpath:/apikey/client/ClientA_expired_after_36500_days.crt");
		});
	}

	@Test
	public void authenticationFailureWithWrongClientCertificate() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			callAuthenticationProvider(Arrays.asList("classpath:/apikey/server/ServerA_expired_after_36500_days.crt"),
				"classpath:/apikey/client/Corrupted.crt");
		});
	}

	@Test
	public void authenticationFailureWithServerRootCAExpired() {
		Assertions.assertThrows(BadCredentialsException.class, () -> {
			callAuthenticationProvider(Arrays.asList("classpath:/apikey/server/ServerA.crt"),
				"classpath:/apikey/client/ClientA_expired_after_36500_days.crt");
		});
	}

	@Test
	public void authenticationFailureWithClientRootCAExpired() {
		Assertions.assertThrows(BadCredentialsException.class, () -> {
			callAuthenticationProvider(Arrays.asList("classpath:/apikey/server/ServerA_expired_after_36500_days.crt"),
				"classpath:/apikey/client/ClientA.crt");
		});
	}

	private Authentication callAuthenticationProvider(List<String> rootCertificatesRefs, String certificateRef) throws Exception {
		List<Resource> certResources = loadResources(rootCertificatesRefs);
		RootCAManager rootCAManager = new RootCAManager(certResources);
		rootCAManager.init();
		apiKeyAuthenticationProvider =
			new APIKeyAuthenticationProvider(apiKeyConverter, rootCAManager, new UAAAPIKeyValidator());
		return apiKeyAuthenticationProvider.authenticate(new APIKeyAuthenticationToken(loadResourceContentBase64Encoded(certificateRef)));
	}

	private UAAPrincipal<?> callAuthenticationProviderAndGetUser(List<String> rootCertificatesRefs, String certificateRef) throws Exception {
		Authentication authenticatedToken = callAuthenticationProvider(rootCertificatesRefs, certificateRef);
		if (authenticatedToken != null)
			return (UAAPrincipal<?>) authenticatedToken.getPrincipal();
		return null;
	}

	private String loadResourceContentBase64Encoded(String resourceRef) throws IOException {
		return Base64.encode(loadResourceContent(resourceRef)).toString();
	}

	private String loadResourceContent(String resourceRef) throws IOException {
		return IOUtils.toString(resourceLoader.getResource(resourceRef).getInputStream(), StandardCharset.UTF_8);
	}

	private List<Resource> loadResources(List<String> refs) {
		return refs.stream()
			.map(resourceLoader::getResource)
			.collect(Collectors.toList());
	}

	private Set<GrantedAuthority> convertAuthorities(String roles) {
		return Arrays.asList(roles.split(";")).stream()
			.map(roleName -> new SimpleGrantedAuthority(roleName))
			.collect(Collectors.toSet());
	}

}
