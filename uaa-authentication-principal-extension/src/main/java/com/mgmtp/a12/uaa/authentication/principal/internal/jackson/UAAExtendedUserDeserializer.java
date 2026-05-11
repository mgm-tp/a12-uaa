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

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.mgmtp.a12.uaa.authentication.principal.AbstractExtendedPrincipal;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalFactory;
import com.mgmtp.a12.uaa.authentication.principal.Role;

public class UAAExtendedUserDeserializer extends JsonDeserializer<AbstractExtendedPrincipal<?>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(UAAExtendedUserDeserializer.class);

	@Override
	public AbstractExtendedPrincipal<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		PrincipalFactory userFactory = ApplicationContextProvider.getPrincipalFactory();
		ObjectMapper mapper = (ObjectMapper) p.getCodec();
		JsonNode jsonNode = mapper.readTree(p);
		Set<? extends Role> authorities =
			mapper.convertValue(
				jsonNode.get("authorities"),
				new TypeReference<Set<Role>>() {
				});
		Object extendedData = mapper.convertValue(jsonNode.get("extendedPrincipalData"), Object.class);
		JsonNode password = readJsonNode(jsonNode, "password");
		AbstractExtendedPrincipal<?> userObject =
			userFactory.createPrincipal(readJsonNode(jsonNode, "username").asText(), password.asText(), authorities, extendedData);
		return populateObject(userObject, jsonNode, mapper);
	}

	private AbstractExtendedPrincipal<?> populateObject(AbstractExtendedPrincipal<?> user, JsonNode jsonNode, ObjectMapper mapper) {
		PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(user);
		Arrays.asList(propertyDescriptors).stream()
			.forEach(descriptor -> {
				Object convertValue = mapper.convertValue(jsonNode.get(descriptor.getName()), descriptor.getPropertyType());
				try {
					if (PropertyUtils.isWriteable(user, descriptor.getName())) {
						PropertyUtils.setSimpleProperty(user, descriptor.getName(), convertValue);
					}
				} catch (Exception e) {
					LOGGER.warn("Unable to set user property [{}]", descriptor.getName());
				}
			});
		return user;
	}

	private JsonNode readJsonNode(JsonNode jsonNode, String field) {
		return jsonNode.has(field) ? jsonNode.get(field) : MissingNode.getInstance();
	}

}
