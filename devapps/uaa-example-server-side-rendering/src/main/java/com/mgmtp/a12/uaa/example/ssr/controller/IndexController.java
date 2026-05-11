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
package com.mgmtp.a12.uaa.example.ssr.controller;

import java.io.IOException;
import java.util.Optional;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;
import com.mgmtp.a12.uaa.authentication.oauth.client.UaaOauth2ClientAuthenticationToken;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@RestController
@ConditionalOnAuthentication(AuthenticationType.OAUTH2_CLIENT)
public class IndexController {

	@Autowired(required = false)
	private OAuth2AuthorizedClientService authorizedClientService;

	@Autowired(required = false)
	private WebClient webClient;

	@Value("${mgmtp.a12.uaa.example.dev.app.url:}")
	private Optional<String> depAppEndpoint;

	@GetMapping("/")
	public String index() {
		return "Welcome to Server Side Rendering (Oauth2 client mode)";
	}

	@RequestMapping("/callback")
	public void callback(HttpServletResponse response) throws IOException {
		response.sendRedirect("/");
	}

	@RequestMapping("/callDevAppExampleEndPoint")
	public String fetchDataFromDevAppServer() throws Exception {
		if (this.webClient == null) {
			throw new Exception("Webclient only available via mgmtp.a12.uaa.authentication.types=OAUTH2_CLIENT or please create your own WebClient");
		}
		UaaOauth2ClientAuthenticationToken uaaOauth2ClientAuthenticationToken =
			(UaaOauth2ClientAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		OAuth2AuthorizedClient oAuth2AuthorizedClient = authorizedClientService
			.loadAuthorizedClient(uaaOauth2ClientAuthenticationToken.getAuthorizedClientRegistrationId(), uaaOauth2ClientAuthenticationToken
				.getoAuth2UserPrincipal().getName());

		String data = this.webClient
			.get()
			.uri(depAppEndpoint.get())
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + oAuth2AuthorizedClient.getAccessToken().getTokenValue())
			.header(HttpHeaders.USER_AGENT, "Auth2 Client")
			.attributes(clientRegistrationId("uaa-auth-client"))
			.retrieve()
			.bodyToMono(String.class)
			.block();
		return data;
	}
}
