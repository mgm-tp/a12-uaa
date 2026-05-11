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
package com.mgmtp.a12.uaa.example.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jakarta.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;
import com.mgmtp.a12.uaa.authentication.internal.TokenType;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenService;
import com.mgmtp.a12.uaa.example.principal.PrincipalMetadata;
import com.mgmtp.a12.uaa.example.principal.extension.ExtendedPrincipal;

/**
 * This class just for demo how to use the JwtTokenService public api
 * This bean "JwtTokenService" only valid with authentication type LOCAL, SAML or ACTIVE_DIRECTORY_LDAP
 */
@RestController
@ConditionalOnAuthentication({ AuthenticationType.LOCAL, AuthenticationType.ACTIVE_DIRECTORY_LDAP, AuthenticationType.SAML,
	AuthenticationType.UAA_ACCESS_TOKEN })
public class FederationController {

	@Inject
	JwtTokenService jwtTokenService;

	@Value("${server.port}")
	private String serverPort;

	@GetMapping("/federation/loadAllCompanies")
	public String exampleEndpoint() {

		Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
		GrantedAuthority adminAuthority = new SimpleGrantedAuthority("Manager");
		grantedAuthorities.add(adminAuthority);
		PrincipalMetadata userMetadata = new PrincipalMetadata("VN");
		ExtendedPrincipal extendedPrincipal = new ExtendedPrincipal("admin", "*****", grantedAuthorities, userMetadata);
		extendedPrincipal.setNationality("VN");

		// JwtTokenService is used to generate the jwt token
		String jwtToken = jwtTokenService.generateToken(extendedPrincipal).getToken();

		// With above jwt token, it can communicate with other systems which understands uaa token
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "%s %s".formatted(TokenType.UAABEARER, jwtToken));
		HttpEntity<String> entity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();
		String requestUrl = "http://localhost:%s/loadAllCompanies".formatted(serverPort);
		restTemplate.exchange(requestUrl, HttpMethod.GET, entity, String.class).getBody();

		return "OK";

	}
}
