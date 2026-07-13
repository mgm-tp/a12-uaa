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
package com.mgmtp.a12.uaa.authentication.anonymous.internal;

import java.util.List;
import java.util.stream.Collectors;

//import com.mgmtp.a12.uaa.authentication.local.internal.LocalAuthenticationProvider;
import jakarta.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;
import com.mgmtp.a12.uaa.authentication.anonymous.AnonymousPrincipalAdapter;
import com.mgmtp.a12.uaa.authentication.security.UAASecurityConfigurer;

@Configuration
@ConditionalOnAuthentication(AuthenticationType.ANONYMOUS)
public class AnonymousSecurityConfigurer extends UAASecurityConfigurer<AnonymousSecurityConfigurer> {

	@Inject
	private AuthenticationProperties authenticationProperties;

	@Inject
	private AnonymousPrincipalAdapter anonymousPrincipalFactory;

	@Override
	public void configureHttpSecurity(HttpSecurity http) throws Exception {
		List<String> anonymousUrls = authenticationProperties.getAnonymous().getAccess().getUrls();
		if (CollectionUtils.isNotEmpty(anonymousUrls)) {
			http.authorizeHttpRequests((authorize) -> {
				anonymousUrls.forEach(anonymousUrl -> authorize.requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(anonymousUrl)).permitAll());
			});
		}

		List<GrantedAuthority> authorities =
			authenticationProperties.getAnonymous().getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
		http
			.anonymous(customizer -> {
				customizer.principal(anonymousPrincipalFactory.createPrincipal(authorities));
				customizer.authorities(authorities);
			});

		LOGGER.debug("Anonymous access enabled with configuration [{}] ", authenticationProperties.getAnonymous());
	}
}
