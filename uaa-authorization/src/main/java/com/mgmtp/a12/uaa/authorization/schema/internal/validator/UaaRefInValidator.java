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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.mgmtp.a12.uaa.authorization.schema.internal.ExpressionType;
import com.networknt.schema.BaseJsonValidator;
import com.networknt.schema.ErrorMessageType;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.Keyword;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ValidationContext;
import com.networknt.schema.ValidationMessage;

public class UaaRefInValidator extends BaseJsonValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(UaaRefInValidator.class);

	private static final ErrorMessageType ERROR_MESSAGE_TYPE = () -> "UAA_1003";
	private static final String UAA_LOGIC_PATTERN_REFERENCE = "[\\(\\)]+|(\\|\\|)+|(\\&\\&)+|(\\!)+";

	private static final String IDENTITY_PROPERTY = "name";
	private static final String EXPRESSION_TYPE = "expressionType";

	private final ExpressionType expressionType;
	private final String collectorId;

	public UaaRefInValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, Keyword keyword,
		ValidationContext validationContext, boolean suppressSubSchemaRetrieval) {
		super(schemaLocation, evaluationPath, schemaNode, parentSchema, ERROR_MESSAGE_TYPE, keyword, validationContext,
			suppressSubSchemaRetrieval);
		expressionType = getExpressionType(parentSchema.getSchemaNode().get(EXPRESSION_TYPE));
		collectorId = schemaNode.asText();
	}

	@Override
	public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {

		LOGGER.debug("validate( {}, {}, {})", node, rootNode, instanceLocation);
		Set<ValidationMessage> errors = new LinkedHashSet<>();
		Set<JsonNode> existingRefs =
			getExistingRefNodes(executionContext).stream().map(refNode -> refNode.get(IDENTITY_PROPERTY)).collect(Collectors.toSet());
		if (existingRefs.isEmpty()) {
			errors.add(
				message()
					.type(getKeyword())
					.message("{0}: The reference [{1}] does not exist in Authorization Definition files")
					.arguments(node.toString())
					.instanceLocation(instanceLocation)
					.instanceNode(node)
					.build());
			return errors;
		}

		getPureRefNames(node).forEach(pureRef -> {
			if (!existingRefs.contains(pureRef)) {
				errors.add(
					//buildValidationMessage(at, pureRef.toString())
					message()
						.type(getKeyword())
						.message("{0}: The reference [{1}] does not exist in Authorization Definition files")
						.arguments(pureRef.toString())
						.instanceLocation(instanceLocation)
						.instanceNode(node)
						.build());
			}
		});

		return errors;
	}

	private Set<JsonNode> getExistingRefNodes(ExecutionContext executionContext) {
		@SuppressWarnings("unchecked")
		Set<JsonNode> collectedData = ((Set<JsonNode>) executionContext.getCollectorContext().get(collectorId));
		return Optional.ofNullable(collectedData).orElse(Collections.emptySet());
	}

	private Set<JsonNode> getPureRefNames(JsonNode node) {
		if (ExpressionType.UAA_EXPRESSION == expressionType && node.isTextual()) {
			return Stream.of(node.textValue().split(UAA_LOGIC_PATTERN_REFERENCE))
				.filter(StringUtils::isNoneBlank)
				.map(token -> TextNode.valueOf(token.trim()))
				.collect(Collectors.toSet());
		}
		return Set.of(node.isTextual() ? TextNode.valueOf(node.asText().trim()) : node);
	}

	private static ExpressionType getExpressionType(JsonNode node) {
		if (Objects.nonNull(node) && node.isTextual()) {
			return ExpressionType.fromValue(node.textValue());
		}
		return ExpressionType.NONE;
	}
}
