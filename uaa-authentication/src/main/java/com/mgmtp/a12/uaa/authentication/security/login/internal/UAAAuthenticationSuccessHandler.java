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
import java.nio.charset.StandardCharsets;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import com.mgmtp.a12.uaa.authentication.internal.RedirectSupport;
import com.mgmtp.a12.uaa.authentication.internal.RedirectType;
import com.mgmtp.a12.uaa.authentication.internal.RedirectType.Type;
import com.mgmtp.a12.uaa.authentication.internal.StandardJsonHandler;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenData;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenGenerator;
import com.mgmtp.a12.uaa.authentication.principal.internal.PrincipalConverterService;

/**
 * Provide standard UAA auth success handler which generates JWT token after successful login.
 */
public class UAAAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(UAAAuthenticationSuccessHandler.class);

	public static final String TOKEN_KEY = "access_token";
	public static final String TOKEN_RENEW_IN_SECONDS = "token_renew_in_seconds";
	public static final String TOKEN_EXPIRATION_IN_SECONDS = "token_expiration_in_seconds";

	@Inject
	protected JwtTokenGenerator jwtTokenGeneratorSupport;
	@Inject
	private StandardJsonHandler jsonConverter;
	@Inject
	private PrincipalConverterService principalConverterService;
	@Inject
	@RedirectType(type = Type.LOGIN)
	private RedirectSupport loginRedirectSupport;
	private RedirectStrategy redirectStrategy = getRedirectStrategy();

	public UAAAuthenticationSuccessHandler() {
	}

	public UAAAuthenticationSuccessHandler(RedirectStrategy redirectStrategy) {
		this.redirectStrategy = redirectStrategy;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
		throws ServletException, IOException {
		///due to the fact that redirect commit response we need to handle before
		if (authentication != null) {
			UserDetails principal = (UserDetails) authentication.getPrincipal();
			LOGGER.debug("User [{}] has been authenticated", authentication.getName());
			generateJwtTokenAndStoreToHeader(response, principal);
			String userResponse = jsonConverter.convertToJson(principalConverterService.convertPrincipal(principal));
			ServletOutputStream outputStream = response.getOutputStream();
			IOUtils.write(userResponse, outputStream, StandardCharsets.UTF_8);
			response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
			loginRedirectSupport.performSuccessRedirect(redirectStrategy, request, response, null);
		}
	}

	protected void generateJwtTokenAndStoreToHeader(HttpServletResponse response, UserDetails principal) {
		JwtTokenData jwtTokenData = generateJwtToken(principal);
		String tokenRenewInSeconds = String.valueOf(jwtTokenData.getExpirationSeconds() - jwtTokenData.getTokenRenewThresholdInSeconds());
		String tokenExpirationInSeconds = String.valueOf(jwtTokenData.getExpirationSeconds());
		response.addHeader(TOKEN_KEY, jwtTokenData.getToken());
		response.addHeader(TOKEN_RENEW_IN_SECONDS, tokenRenewInSeconds);
		response.addHeader(TOKEN_EXPIRATION_IN_SECONDS, tokenExpirationInSeconds);
	}

	protected JwtTokenData generateJwtToken(UserDetails principal) {
		return jwtTokenGeneratorSupport.generateToken(principal);
	}

}
