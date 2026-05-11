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
package com.mgmtp.a12.uaa.authentication.principal.autoconfigure;

import jakarta.inject.Inject;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;
import com.mgmtp.a12.uaa.authentication.autoconfigure.AuthenticationAutoConfiguration;
import com.mgmtp.a12.uaa.authentication.local.LocalAuthenticationService;
import com.mgmtp.a12.uaa.authentication.principal.PropertyExtractor;
import com.mgmtp.a12.uaa.authentication.principal.local.internal.UAALocalAuthenticationServiceImpl;
import com.mgmtp.a12.uaa.authentication.user.LocalUser;
import com.mgmtp.a12.uaa.authentication.user.LocalUserLoader;
import com.mgmtp.a12.uaa.authentication.user.LocalUserManager;
import com.mgmtp.a12.uaa.authentication.user.internal.LocalUserPropertyExtractor;
import com.mgmtp.a12.uaa.authentication.user.internal.UAALocalUserLoader;
import com.mgmtp.a12.uaa.authentication.user.internal.UAALocalUserManager;
import com.mgmtp.a12.uaa.authentication.user.internal.UAAUserDetailsService;

@ConditionalOnAuthentication(AuthenticationType.LOCAL)
@AutoConfigureBefore(AuthenticationAutoConfiguration.class)
public class LocalUserAutoConfiguration {

	@Inject
	private AuthenticationPrincipalExtensionProperties authenticationUserProperties;

	@Bean
	@ConditionalOnMissingBean(LocalAuthenticationService.class)
	public LocalAuthenticationService<?> localAuthenticationFactory() {
		return new UAALocalAuthenticationServiceImpl();
	}

	@Bean
	@ConditionalOnMissingBean(UserDetailsService.class)
	public UserDetailsService localUserDetailsService() {
		return new UAAUserDetailsService();
	}

	@Bean
	@ConditionalOnMissingBean(LocalUserManager.class)
	public LocalUserManager localUserManager() {
		return new UAALocalUserManager(authenticationUserProperties.getLocalConfig().getUserResources());
	}

	@Bean
	@ConditionalOnMissingBean(LocalUserLoader.class)
	public LocalUserLoader<LocalUser> localUserLoader() {
		return new UAALocalUserLoader<LocalUser>(LocalUser.class);
	}

	@Bean
	public PropertyExtractor<LocalUser> localUserPropertyExtractor() {
		return new LocalUserPropertyExtractor<LocalUser>();
	}

}
