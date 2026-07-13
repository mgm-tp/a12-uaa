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
package com.mgmtp.a12.uaa.authentication.internal;

import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;
import com.mgmtp.a12.uaa.authentication.principal.UAAJacksonModule;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@Component
@ConditionalOnAuthentication({
	AuthenticationType.LOCAL,
	AuthenticationType.ACTIVE_DIRECTORY_LDAP,
	AuthenticationType.SAML,
	AuthenticationType.UAA_ACCESS_TOKEN
})
public class UAASpringJsonHandler implements JsonHandler {

	private ObjectMapper objectMapper;

	@Inject
	private List<UAAJacksonModule> uaaJacksonModules;

	@PostConstruct
	public void initialize() {
		ClassLoader loader = getClass().getClassLoader();

		BasicPolymorphicTypeValidator.Builder builder =
			BasicPolymorphicTypeValidator.builder();

		uaaJacksonModules.forEach(module ->
			module.configurePolymorphicTypeValidator(builder)
		);

		objectMapper = JsonMapper.builder()
			.addModules(SecurityJacksonModules.getModules(loader, builder))
			.addModules(uaaJacksonModules)
			.build();
	}

	@Override
	public String convertToJson(Object value) throws JacksonException {
		return objectMapper.writeValueAsString(value);
	}

	@Override
	public <T> T convertFromJson(String content, Class<T> valueType) throws JacksonException {
		return objectMapper.readValue(content, valueType);
	}

	@Override
	public JsonNode readTree(String jsonDocument) throws JacksonException {
		return objectMapper.readTree(jsonDocument);
	}
}