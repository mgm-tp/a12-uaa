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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.userdetails.UserDetails;

import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;
import com.mgmtp.a12.uaa.authentication.anonymous.AnonymousPrincipalAdapter;
import com.mgmtp.a12.uaa.authentication.autoconfigure.AuthenticationAutoConfiguration;
import com.mgmtp.a12.uaa.authentication.principal.AbstractExtendedPrincipal;
import com.mgmtp.a12.uaa.authentication.principal.ExternalPrincipalImpl;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalAdapter;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalConverter;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalFactory;
import com.mgmtp.a12.uaa.authentication.principal.RoleMappingLoader;
import com.mgmtp.a12.uaa.authentication.principal.a12internal.RoleMappingProcessor;
import com.mgmtp.a12.uaa.authentication.principal.anonymous.AnonymousPrincipalAdapterWithAccessRights;
import com.mgmtp.a12.uaa.authentication.principal.internal.ExternalPrincipalConverterImpl;
import com.mgmtp.a12.uaa.authentication.principal.internal.PrincipalDataProcessor;
import com.mgmtp.a12.uaa.authentication.principal.internal.PrincipalExtensionAdapter;
import com.mgmtp.a12.uaa.authentication.principal.internal.UAAPrincipalFactory;
import com.mgmtp.a12.uaa.authentication.principal.internal.YamlRoleMappingLoader;
import com.mgmtp.a12.uaa.authentication.principal.oauth.internal.JwtTokenRoleMappingLoader;

@ComponentScan("com.mgmtp.a12.uaa.authentication.principal")
@EnableConfigurationProperties(AuthenticationPrincipalExtensionProperties.class)
@AutoConfigureBefore(AuthenticationAutoConfiguration.class)
public class AuthenticationUserAutoConfiguration {

	@Inject
	private AuthenticationPrincipalExtensionProperties authenticationUserProperties;

	@Bean
	public RoleMappingProcessor accessRightProcessor() {
		return new RoleMappingProcessor();
	}

	@Bean
	@ConditionalOnProperty(name = "mgmtp.a12.uaa.authentication.principal.access-rights-resource", matchIfMissing = false)
	@Order(Ordered.LOWEST_PRECEDENCE)
	public RoleMappingLoader<?> accessRightsMappingLoaders() {
		return new YamlRoleMappingLoader(authenticationUserProperties.getAccessRightsResource());
	}

	@Bean
	@ConditionalOnProperty(name = "mgmtp.a12.uaa.authentication.principal.oauth2Config.role-mapping-from-token.field-name", matchIfMissing = false)
	@ConditionalOnAuthentication(AuthenticationType.OAUTH2)
	@Order(Ordered.LOWEST_PRECEDENCE + 1)
	public RoleMappingLoader<?> jwtTokenRoleMappingLoaders() {
		return new JwtTokenRoleMappingLoader(authenticationUserProperties.getOauth2Config().getRoleMappingFromToken().getFieldName());
	}

	@Bean
	@ConditionalOnMissingBean(PrincipalFactory.class)
	public PrincipalFactory userFactory() {
		return new UAAPrincipalFactory();
	}

	@Bean
	@ConditionalOnMissingBean(AnonymousPrincipalAdapter.class)
	@ConditionalOnAuthentication(AuthenticationType.ANONYMOUS)
	public AnonymousPrincipalAdapter anonymousPrincipalFactory() {
		return new AnonymousPrincipalAdapterWithAccessRights();
	}

	@Bean
	@Order(Ordered.LOWEST_PRECEDENCE - 1)
	public PrincipalConverter<AbstractExtendedPrincipal<?>, ExternalPrincipalImpl> defaultUserUserDetailConverter() {
		return new ExternalPrincipalConverterImpl();
	}

	@Bean
	@ConditionalOnMissingBean(PrincipalDataProcessor.class)
	public PrincipalDataProcessor userDataProcessor() {
		return new PrincipalDataProcessor(authenticationUserProperties.getAdditionalProperties());
	}

	@Bean
	@ConditionalOnMissingBean(PrincipalAdapter.class)
	@Order(Ordered.LOWEST_PRECEDENCE + 1)
	public PrincipalAdapter<? extends UserDetails> userExtensionPrincipalAdapter() {
		return new PrincipalExtensionAdapter();
	}
}
