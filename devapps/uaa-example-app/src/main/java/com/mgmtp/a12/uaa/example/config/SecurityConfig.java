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
package com.mgmtp.a12.uaa.example.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;

import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalFactory;
import com.mgmtp.a12.uaa.authentication.principal.UAAJacksonModule;
import com.mgmtp.a12.uaa.authentication.user.LocalUserLoader;
import com.mgmtp.a12.uaa.example.authentication.local.principal_extension.ExtendedLocalUser;
import com.mgmtp.a12.uaa.example.authentication.local.principal_extension.ExtendedLocalUserLoader;
import com.mgmtp.a12.uaa.example.principal.extension.ExtendedPrincipalFactory;
import com.mgmtp.a12.uaa.example.service.CustomUserDetailsService;

@Configuration
@EnableJpaRepositories(basePackages = "com.mgmtp.a12.uaa.example.repository")
@EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true)
@ComponentScan(excludeFilters = @ComponentScan.Filter(pattern = "com.mgmtp.a12.uaa.authentication.security.internal.*", type = FilterType.REGEX))
public class SecurityConfig {

	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder(11);
	}

	@Bean
	public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
		return new SecurityEvaluationContextExtension();
	}

	@Bean
	@ConditionalOnAuthentication(AuthenticationType.LOCAL)
	@Profile("principal")
	public LocalUserLoader<ExtendedLocalUser> userLoader() {
		return new ExtendedLocalUserLoader<>(ExtendedLocalUser.class);
	}

	@Bean
	@Profile("principal")
	public PrincipalFactory userFactory() {
		return new ExtendedPrincipalFactory();
	}

	@Bean
	@ConditionalOnProperty(name = "mgmtp.a12.uaa.example.use-user-details.enabled", havingValue = "true")
	public UserDetailsService userDetailsService() {
		return new CustomUserDetailsService();
	}

	@Bean
	public UAAJacksonModule createJavaTimeModule() {
		return new JavaTimeUAAJacksonModule();
	}
}
