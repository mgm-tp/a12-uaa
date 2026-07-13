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
package com.mgmtp.a12.uaa.authentication.principal.internal.jackson;

import java.util.Collections;

import jakarta.inject.Inject;

import org.springframework.stereotype.Component;

import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;
import com.mgmtp.a12.uaa.authentication.principal.AccessRight;
import com.mgmtp.a12.uaa.authentication.principal.ExtendedPrincipal;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalFactory;
import com.mgmtp.a12.uaa.authentication.principal.Role;
import com.mgmtp.a12.uaa.authentication.principal.UAAJacksonModule;

import tools.jackson.core.Version;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@Component
@ConditionalOnAuthentication({ AuthenticationType.LOCAL, AuthenticationType.ACTIVE_DIRECTORY_LDAP, AuthenticationType.SAML,
	AuthenticationType.UAA_ACCESS_TOKEN })
public class UserExtensionJacksonModule extends UAAJacksonModule {

	@Inject
	private PrincipalFactory userFactory;

	public UserExtensionJacksonModule() {
		super(UserExtensionJacksonModule.class.getName(), new Version(1, 0, 0, null, null, null));
	}

	@Override public void configurePolymorphicTypeValidator(BasicPolymorphicTypeValidator.Builder builder) {
		builder
			.allowIfSubType(AccessRight.class)
			.allowIfSubType(Role.class)
			.allowIfSubType(ExtendedPrincipal.class);
	}

	@Override
	public void setupModule(SetupContext context) {
		context.setMixIn(userFactory.createPrincipal(getClass().getName(), Collections.emptyList()).getClass(), UaaExtendedUserMixin.class);
	}

}
