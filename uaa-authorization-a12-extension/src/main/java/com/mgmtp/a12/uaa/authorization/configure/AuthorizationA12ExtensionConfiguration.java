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
package com.mgmtp.a12.uaa.authorization.configure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.property.internal.ResourceConverter;
import com.mgmtp.a12.uaa.authorization.security.DataMasking;
import com.mgmtp.a12.uaa.authorization.security.ext.property.internal.DocumentResourceConverter;
import com.mgmtp.a12.uaa.authorization.security.ext.property.internal.DocumentUAADataMasking;
import com.mgmtp.a12.uaa.authorization.security.ext.property.internal.documentv2.DocumentV2FilterTargetAdapter;
import com.mgmtp.a12.uaa.authorization.security.ext.property.internal.documentv2.DocumentV2ResourceConverter;
import com.mgmtp.a12.uaa.authorization.security.spel.internal.UAAMethodSecurityExpressionHandler;

@Configuration
public class AuthorizationA12ExtensionConfiguration {

	@Bean
	public DataMasking createDataMaskingWithDocumentSupport(AuthorizationDefinitionRepository authorizationRepository,
		IDocumentModelResolver documentModelResolver) {
		return new DocumentUAADataMasking(authorizationRepository, documentModelResolver);
	}
	
	@Bean
	public ResourceConverter<?, ?> createResourceConverter() {
		return new DocumentResourceConverter();
	}

	@Bean
	public ResourceConverter<?, ?> createDocumentV2ResourceConverter() {
		return new DocumentV2ResourceConverter();
	}

	@Bean
	public UAAMethodSecurityExpressionHandler.FilterTargetAdapter createFilterTargetAdapter() {
		return new DocumentV2FilterTargetAdapter();
	}

}
