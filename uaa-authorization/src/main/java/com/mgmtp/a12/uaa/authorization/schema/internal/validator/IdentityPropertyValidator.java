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
package com.mgmtp.a12.uaa.authorization.schema.internal.validator;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.uaa.authorization.schema.internal.SchemaValidator;
import com.networknt.schema.BaseJsonValidator;
import com.networknt.schema.ErrorMessageType;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.Keyword;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ValidationContext;
import com.networknt.schema.ValidationMessage;

public class IdentityPropertyValidator extends BaseJsonValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(IdentityPropertyValidator.class);

	private static ErrorMessageType ERROR_MESSAGE_TYPE = new ErrorMessageType() {
		@Override
		public String getErrorCode() {
			return "UAA_1002";
		}
	};

	private static final String GLOBAL_DUPLICATED_IDENTITY_ERROR =
		"{0}: The identity [{1}] with value [{2}] in the [{3}] file is a duplicate of the one in the [{4}] file";

	private final JsonNode identityField;
	private final Map<String, Set<JsonNode>> identityMap = new LinkedHashMap<>();

	public IdentityPropertyValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
		JsonSchema parentSchema, Keyword keyword,
		ValidationContext validationContext, boolean suppressSubSchemaRetrieval) {
		super(schemaLocation, evaluationPath, schemaNode, parentSchema, ERROR_MESSAGE_TYPE, keyword, validationContext,
			suppressSubSchemaRetrieval);
		identityField = schemaNode;
	}

	@Override
	public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
		LOGGER.debug("validate( {}, {}, {})", node, rootNode, instanceLocation);
		Set<ValidationMessage> errors = new LinkedHashSet<>();
		if (identityField.isTextual() && node.isObject()) {
			String currentFileName = getFileName(rootNode);
			Set<JsonNode> localIdentityValues = identityMap.computeIfAbsent(currentFileName, fname -> new LinkedHashSet<>());
			String field = identityField.textValue();
			JsonNode identity = node.get(field);
			if (Objects.isNull(identity) || identity.isNull()) {
				ValidationMessage error = message()
					.type(getKeyword())
					.schemaLocation(schemaLocation)
					.message("{0}: The identity property [{1}] cannot be null")
					.arguments(field)
					.instanceLocation(instanceLocation).instanceNode(node).build();
				errors.add(error);
			}
			if (!localIdentityValues.add(identity)) {
				ValidationMessage error = message()
					.type(getKeyword())
					.schemaLocation(schemaLocation)
					.message("{0}: The identity property [{1}] cannot be null")
					.arguments(field, identity.toString())
					.instanceLocation(instanceLocation).instanceNode(node).build();
				errors.add(error);
			}
			identityMap.entrySet().stream().filter(entry -> !entry.getKey().contains(currentFileName) && entry.getValue()
				.contains(identity)).map(Map.Entry::getKey).findFirst()
				.ifPresent(existingIdentityInFileName -> errors.add(
					message()
						.type("UAA.Global.DuplicatingIdentity")
						//.format(new MessageFormat(GLOBAL_DUPLICATED_IDENTITY_ERROR))
						.message(GLOBAL_DUPLICATED_IDENTITY_ERROR)
						.arguments(field, identity.toString(), currentFileName, existingIdentityInFileName)
						.instanceLocation(instanceLocation)
						.instanceNode(node)
						.build()));

		}
		return errors;
	}

	protected String getFileName(JsonNode rootNode) {
		return rootNode.get(SchemaValidator.FILENAME_FIELD).asText();
	}
}
