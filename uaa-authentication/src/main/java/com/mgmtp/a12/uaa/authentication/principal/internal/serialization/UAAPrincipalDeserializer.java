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

import java.util.Collections;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.mgmtp.a12.uaa.authentication.principal.UAAPrincipal;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.node.MissingNode;

public class UAAPrincipalDeserializer extends ValueDeserializer<UAAPrincipal<?>> {

	protected static final String DEFAULT_PASSWORD = "***";
	protected static final TypeReference<Set<SimpleGrantedAuthority>> AUTH_TYPE =
		new TypeReference<>() {
		};

	@Override
	public UAAPrincipal<?> deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
		ObjectReadContext rc = p.objectReadContext();
		if (rc == null) {
			rc = ctxt;
		}
		JsonNode root = rc.readTree(p);
		return deserializeInternal(rc, root);
	}

	protected UAAPrincipal<?> deserializeInternal(ObjectReadContext rc, JsonNode jsonNode) throws JacksonException {
		Set<? extends GrantedAuthority> authorities = readOrDefault(
			rc, readJsonNode(jsonNode, "authorities"), AUTH_TYPE, Collections.emptySet());

		Object extendedData = readOrDefault(
			rc, readJsonNode(jsonNode, "extendedPrincipalData"), Object.class, null);

		return new UAAPrincipal<>(
			readJsonNode(jsonNode, "username").asText(), DEFAULT_PASSWORD,
			readJsonNode(jsonNode, "enabled").asBoolean(),
			readJsonNode(jsonNode, "accountNonExpired").asBoolean(),
			readJsonNode(jsonNode, "credentialsNonExpired").asBoolean(),
			readJsonNode(jsonNode, "accountNonLocked").asBoolean(),
			authorities,
			extendedData
		);
	}

	protected JsonNode readJsonNode(JsonNode jsonNode, String field) {
		return (jsonNode != null && jsonNode.has(field)) ? jsonNode.get(field) : MissingNode.getInstance();
	}

	protected static <T> T readOrDefault(ObjectReadContext rc, JsonNode node, Class<T> type, T defaultValue)
		throws JacksonException {
		if (node == null || node.isMissingNode() || node.isNull())
			return defaultValue;
		try (JsonParser np = rc.treeAsTokens(node)) {
			np.nextToken();
			return rc.readValue(np, type);
		}
	}

	protected static <T> T readOrDefault(ObjectReadContext rc, JsonNode node, TypeReference<T> typeRef, T defaultValue)
		throws JacksonException {
		if (node == null || node.isMissingNode() || node.isNull())
			return defaultValue;
		try (JsonParser np = rc.treeAsTokens(node)) {
			np.nextToken();
			return rc.readValue(np, typeRef);
		}
	}
}