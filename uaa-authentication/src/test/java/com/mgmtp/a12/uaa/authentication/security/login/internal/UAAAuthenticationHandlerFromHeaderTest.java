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
package com.mgmtp.a12.uaa.authentication.security.login.internal;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider.ResponseToken;
import org.springframework.test.util.ReflectionTestUtils;

import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.internal.RedirectSupport;
import com.mgmtp.a12.uaa.authentication.internal.StandardJsonHandler;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenData;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenGenerator;
import com.mgmtp.a12.uaa.authentication.principal.UAAPrincipal;
import com.mgmtp.a12.uaa.authentication.principal.internal.PrincipalConverterService;
import com.mgmtp.a12.uaa.authentication.principal.internal.UAAExternalUserDetailsImpl;
import com.mgmtp.a12.uaa.authentication.utils.UserDataCreator;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UAAAuthenticationHandlerFromHeaderTest {
	private static final String SERIALIZED_VALUE = "serializedValue";
	protected static final String TOKEN_KEY_JWT_HEADER = "access_token";
	private static final String TOKEN_RENEW_IN_SECONDS = "token_renew_in_seconds";

	@Mock
	private JwtTokenGenerator jwtTokenGeneratorSupport;

	@Mock
	private StandardJsonHandler standardJsonHandler;
	@Mock
	private PrincipalConverterService userConvertorService;
	@Mock
	private RedirectSupport loginRedirectSupport;
	@InjectMocks
	private UAAAuthenticationSuccessHandler uAAAuthenticationHandler;
	private UAAPrincipal<?> user;
	private TypedUsernamePasswordAuthenticationToken<ResponseToken> authReq;
	private Integer tokenRenewInSeconds;

	@BeforeEach
	public void setIUp() throws Exception {
		ReflectionTestUtils.setField(uAAAuthenticationHandler, "jwtTokenGeneratorSupport", jwtTokenGeneratorSupport);
		ReflectionTestUtils.setField(uAAAuthenticationHandler, "jsonConverter", standardJsonHandler);
		ReflectionTestUtils.setField(uAAAuthenticationHandler, "principalConverterService", userConvertorService);
		ReflectionTestUtils.setField(uAAAuthenticationHandler, "loginRedirectSupport", loginRedirectSupport);
		user = UserDataCreator.createUser("test", "password");
		authReq = new TypedUsernamePasswordAuthenticationToken<ResponseToken>(user, "password", AuthenticationType.SAML, Collections.emptyList());
		tokenRenewInSeconds = 45;
		JwtTokenData jwtTokenData = new JwtTokenData.Builder("user")
			.withToken("JWT-TOKEN")
			.withExpirationSeconds(60)
			.withIssuedTime(Instant.now())
			.withTokenRenewThresholdInSeconds(15)
			.build();
		Mockito.when(userConvertorService.convertPrincipal(Mockito.any(UserDetails.class))).thenReturn(new UAAExternalUserDetailsImpl("test", "test"));
		Mockito.when(standardJsonHandler.convertToJson(Mockito.any())).thenReturn(SERIALIZED_VALUE);
		Mockito.when(jwtTokenGeneratorSupport.generateToken(Mockito.any())).thenReturn(jwtTokenData);
		Mockito.when(loginRedirectSupport.performSuccessRedirect(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Boolean.TRUE);
	}

	@Test
	public void checkSuccessFlow() throws ServletException, IOException {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		MockHttpServletResponse httpResponse = new MockHttpServletResponse();

		Assertions.assertEquals(httpResponse.getCookies().length, 0);
		Assertions.assertNull(httpResponse.getHeader(TOKEN_KEY_JWT_HEADER));
		uAAAuthenticationHandler.onAuthenticationSuccess(httpRequest, httpResponse, authReq);
		Assertions.assertEquals("JWT-TOKEN", httpResponse.getHeader(TOKEN_KEY_JWT_HEADER));
		Integer tokenRenewInSecondsHeader = Integer.parseInt(httpResponse.getHeader(TOKEN_RENEW_IN_SECONDS));
		Assertions.assertEquals(tokenRenewInSeconds, tokenRenewInSecondsHeader);
		String body = httpResponse.getContentAsString();
		Assertions.assertEquals(SERIALIZED_VALUE, body);
	}

}
