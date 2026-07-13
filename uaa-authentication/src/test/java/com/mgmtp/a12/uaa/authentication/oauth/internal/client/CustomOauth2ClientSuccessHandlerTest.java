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
package com.mgmtp.a12.uaa.authentication.oauth.internal.client;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.mgmtp.a12.uaa.authentication.oauth.client.UaaOauth2ClientAuthenticationToken;
import com.mgmtp.a12.uaa.authentication.oauth.client.UaaOauth2ClientConverter;
import com.mgmtp.a12.uaa.authentication.oauth.client.internal.CustomOauth2ClientSuccessHandler;
import com.mgmtp.a12.uaa.authentication.principal.UAAPrincipal;
import com.mgmtp.a12.uaa.authentication.utils.TokenTester;
import com.mgmtp.a12.uaa.authentication.utils.UserDataCreator;

public class CustomOauth2ClientSuccessHandlerTest {

	private static String ACCESS_TOKEN = "eylyIn0..BDVU_ACnmNOyoHIc.DOBaIA.dq4hhRzgdiQ";

	@Test
	public void testClientSuccessHandlerSupport() throws IOException, ServletException {

		CustomOauth2ClientSuccessHandler customOauth2ClientSuccessHandler = new CustomOauth2ClientSuccessHandler(createUaaOauth2ClientConverter());
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		customOauth2ClientSuccessHandler.onAuthenticationSuccess(request, response, createAuthentication());

		Assertions.assertNotNull(SecurityContextHolder.getContext().getAuthentication());
		TokenTester.assertPrincipal((UAAPrincipal<UserDataCreator.TestExtendedData>) SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
			UserDataCreator.createUser("admin", "admin"), true, false);
		Assertions.assertEquals(SecurityContextHolder.getContext().getAuthentication().getClass(), UaaOauth2ClientAuthenticationToken.class);
		Assertions.assertEquals("oidc",
			((UaaOauth2ClientAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getAuthorizedClientRegistrationId());
	}

	private Authentication createAuthentication() {
		Map<String, Object> claims = new HashMap<>();
		claims.put("groups", "ROLE_USER");
		claims.put("sub", "12345");
		OidcIdToken idToken = new OidcIdToken(ACCESS_TOKEN, Instant.now(),
			Instant.now().plusSeconds(60), claims);

		Collection<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("USER"));
		OidcUser user = new DefaultOidcUser(authorities, idToken);
		Authentication authentication = new OAuth2AuthenticationToken(user, authorities, "oidc");
		return authentication;
	}

	private UaaOauth2ClientConverter createUaaOauth2ClientConverter() {
		return oAuth2AuthenticationToken -> {
			UserDetails userDetails = UserDataCreator.createUser("admin", "admin");
			UaaOauth2ClientAuthenticationToken uaaOauth2ClientAuthenticationToken = new UaaOauth2ClientAuthenticationToken(
				userDetails,
				userDetails.getAuthorities(),
				oAuth2AuthenticationToken.getAuthorizedClientRegistrationId(),
				oAuth2AuthenticationToken.getPrincipal());
			uaaOauth2ClientAuthenticationToken.setAuthenticated(true);
			uaaOauth2ClientAuthenticationToken.setDetails(oAuth2AuthenticationToken.getDetails());
			return uaaOauth2ClientAuthenticationToken;
		};
	}
}
