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
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.FilterChain;

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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mgmtp.a12.uaa.authentication.principal.UAAPrincipal;
import com.mgmtp.a12.uaa.authentication.security.login.internal.UAALoginEntryPoint;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CertificateAuthenticationFilterTest {

	@Mock
	private AuthenticationManager authenticationManager;
	@Mock
	private FilterChain filterChain;
	private CertificateAuthenticationFilter certificateAuthenticationFilter;
	private UsernamePasswordAuthenticationToken token;
	private MockHttpServletRequest request = new MockHttpServletRequest();

	@BeforeEach
	public void init() throws IOException {
		X509Certificate cert;
		try (InputStream certInputStream =
			this.getClass().getResourceAsStream("/certificate/client.crt")) {

			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			cert = (X509Certificate) cf.generateCertificate(certInputStream);
		} catch (CertificateException e) {
			throw new RuntimeException(e);
		}
		request.setAttribute("jakarta.servlet.request.X509Certificate", new X509Certificate[] { cert });
		request.setServletPath("/api/aUrl");
		UAAPrincipal userDetail = new UAAPrincipal<>("admin", "*****", convertAuthorities("Admin;Manager"));
		token = new UsernamePasswordAuthenticationToken(userDetail, null);
		Mockito.when(authenticationManager.authenticate(Mockito.any())).thenReturn(token);
		certificateAuthenticationFilter = new CertificateAuthenticationFilter(authenticationManager, "/api", List.of("/**"), new UAALoginEntryPoint(403));
		SecurityContextHolder.clearContext();
	}

	@Test
	public void successFlow() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		certificateAuthenticationFilter.doFilterInternal(request, response, filterChain);
		Mockito.verify(filterChain, Mockito.times(1)).doFilter(request, response);
		Mockito.verify(authenticationManager, Mockito.times(1)).authenticate(Mockito.any());
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Assertions.assertNotNull(authentication);
	}

	@Test
	public void invalidUrlFlow() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServletPath("/aUrl");
		MockHttpServletResponse response = new MockHttpServletResponse();
		certificateAuthenticationFilter.doFilterInternal(request, response, filterChain);
		Mockito.verify(filterChain, Mockito.times(1)).doFilter(request, response);
		Mockito.verify(authenticationManager, Mockito.times(0)).authenticate(Mockito.any());
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Assertions.assertNull(authentication);
	}

	@Test
	public void exceptionFlow() throws Exception {
		Mockito.when(authenticationManager.authenticate(Mockito.any())).thenThrow(new BadCredentialsException("bas credentials"));
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("principal", "credentials"));

		MockHttpServletResponse response = new MockHttpServletResponse();
		certificateAuthenticationFilter.doFilterInternal(request, response, filterChain);
		Mockito.verify(filterChain, Mockito.times(0)).doFilter(request, response);
		Mockito.verify(authenticationManager, Mockito.times(1)).authenticate(Mockito.any());
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Assertions.assertNull(authentication);
	}

	@Test
	public void noCertificateFlow() throws Exception {
		request.setAttribute("jakarta.servlet.request.X509Certificate", null);
		MockHttpServletResponse response = new MockHttpServletResponse();
		certificateAuthenticationFilter.doFilterInternal(request, response, filterChain);
		Mockito.verify(filterChain, Mockito.times(1)).doFilter(request, response);
		Mockito.verify(authenticationManager, Mockito.times(0)).authenticate(Mockito.any());
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Assertions.assertNull(authentication);
	}

	@Test
	public void noCertificateWithInvalidUrlFlow() throws Exception {
		request.setAttribute("jakarta.servlet.request.X509Certificate", null);
		request.setServletPath("/aUrl");
		MockHttpServletResponse response = new MockHttpServletResponse();
		certificateAuthenticationFilter.doFilterInternal(request, response, filterChain);
		Mockito.verify(filterChain, Mockito.times(1)).doFilter(request, response);
		Mockito.verify(authenticationManager, Mockito.times(0)).authenticate(Mockito.any());
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Assertions.assertNull(authentication);
	}

	private Set<GrantedAuthority> convertAuthorities(String roles) {
		return Arrays.asList(roles.split(";")).stream()
			.map(roleName -> new SimpleGrantedAuthority(roleName))
			.collect(Collectors.toSet());
	}

}
