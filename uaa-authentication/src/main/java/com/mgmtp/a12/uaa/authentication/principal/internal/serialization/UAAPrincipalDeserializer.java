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
package com.mgmtp.a12.uaa.authentication.principal.internal.serialization;

import java.io.IOException;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.mgmtp.a12.uaa.authentication.principal.UAAPrincipal;

public class UAAPrincipalDeserializer extends JsonDeserializer<UAAPrincipal<?>> {

	private static final String DEFAULT_PASSWORD = "***";

	UAAPrincipal<?> deserializeInternal(ObjectMapper mapper, JsonNode jsonNode) throws IOException, JsonProcessingException {

		Set<? extends GrantedAuthority> authorities =
			mapper.convertValue(
				jsonNode.get("authorities"),
				new TypeReference<Set<SimpleGrantedAuthority>>() {
				});
		Object extendedData = mapper.convertValue(jsonNode.get("extendedPrincipalData"), Object.class);
		UAAPrincipal<Object> result = new UAAPrincipal<>(
			readJsonNode(jsonNode, "username").asText(), DEFAULT_PASSWORD,
			readJsonNode(jsonNode, "enabled").asBoolean(), readJsonNode(jsonNode, "accountNonExpired").asBoolean(),
			readJsonNode(jsonNode, "credentialsNonExpired").asBoolean(),
			readJsonNode(jsonNode, "accountNonLocked").asBoolean(),
			authorities, extendedData
		);

		return result;
	}

	@Override
	public UAAPrincipal<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		ObjectMapper mapper = (ObjectMapper) p.getCodec();
		JsonNode jsonNode = mapper.readTree(p);
		return deserializeInternal(mapper, jsonNode);
	}

	JsonNode readJsonNode(JsonNode jsonNode, String field) {
		return jsonNode.has(field) ? jsonNode.get(field) : MissingNode.getInstance();
	}

}
