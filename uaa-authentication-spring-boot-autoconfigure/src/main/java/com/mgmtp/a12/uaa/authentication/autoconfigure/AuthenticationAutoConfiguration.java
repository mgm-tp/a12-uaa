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
package com.mgmtp.a12.uaa.authentication.autoconfigure;

import jakarta.inject.Inject;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;
import com.mgmtp.a12.uaa.authentication.anonymous.AnonymousPrincipalAdapter;
import com.mgmtp.a12.uaa.authentication.anonymous.internal.PlainAnonymousPrincipalAdapter;
import com.mgmtp.a12.uaa.authentication.internal.AuthenticationConfiguration;
import com.mgmtp.a12.uaa.authentication.jwt.encryption.DataEncoder;
import com.mgmtp.a12.uaa.authentication.jwt.encryption.internal.BypassingEncoder;
import com.mgmtp.a12.uaa.authentication.jwt.encryption.internal.HuffmanEncoder;
import com.mgmtp.a12.uaa.authentication.ldap.internal.UAALdapUserDetailMapper;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalAdapter;
import com.mgmtp.a12.uaa.authentication.principal.internal.AuthenticationPrincipalAdapter;

@EnableScheduling
@Import(AuthenticationConfiguration.class)
@EnableConfigurationProperties({ AuthenticationAutoconfigProperties.class })
public class AuthenticationAutoConfiguration {

	@Inject
	private AuthenticationAutoconfigProperties authenticationAutoconfigProperties;

	@Bean
	public DataEncoder bypassingEncoder() {
		if (authenticationAutoconfigProperties.getAuthentication().getJwt().getCompressUser().isEnabled()) {
			return new HuffmanEncoder();
		}
		return new BypassingEncoder();
	}

	@ConditionalOnMissingBean(AnonymousPrincipalAdapter.class)
	@Bean
	@ConditionalOnAuthentication(AuthenticationType.ANONYMOUS)
	public AnonymousPrincipalAdapter anonymousPrincipalFactory() {
		return new PlainAnonymousPrincipalAdapter();
	}

	@Bean
	public AuthenticationProperties authenticationProperties() {
		return authenticationAutoconfigProperties.getAuthentication();
	}

	@Bean
	@ConditionalOnMissingBean(PrincipalAdapter.class)
	@Order(Ordered.LOWEST_PRECEDENCE)
	public PrincipalAdapter<? extends UserDetails> authenticationPrincipalAdapter() {
		return new AuthenticationPrincipalAdapter();
	}

	@Bean
	@ConditionalOnMissingBean(UserDetailsContextMapper.class)
	public UserDetailsContextMapper ldapUserDetailsMapper() {
		return new UAALdapUserDetailMapper();
	}
}
