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

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.uaa.authorization.exception.ExpressionCheckerException;
import com.mgmtp.a12.uaa.authorization.schema.internal.ExpressionType;
import com.mgmtp.a12.uaa.authorization.security.uaaexpression.internal.UAAExpressionParser;
import com.networknt.schema.BaseJsonValidator;
import com.networknt.schema.ErrorMessageType;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaException;
import com.networknt.schema.Keyword;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ValidationContext;
import com.networknt.schema.ValidationMessage;

public class ExpressionTypeValidator extends BaseJsonValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionTypeValidator.class);

	private static final ErrorMessageType ERROR_MESSAGE_TYPE = () -> "UAA_1001";

	private static final ExpressionParser springExpressionParser = new SpelExpressionParser();

	private final ExpressionType expressionType;

	public ExpressionTypeValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
		JsonSchema parentSchema, Keyword keyword,
		ValidationContext validationContext, boolean suppressSubSchemaRetrieval) {
		super(schemaLocation, evaluationPath, schemaNode, parentSchema, ERROR_MESSAGE_TYPE, keyword, validationContext,
			suppressSubSchemaRetrieval);

		expressionType = getExpressionType(schemaNode, schemaLocation);
	}

	private ExpressionType getExpressionType(JsonNode schemaNode, SchemaLocation schemaLocation) {
		try {
			if (Objects.nonNull(schemaNode) && schemaNode.isTextual()) {
				return ExpressionType.fromValue(schemaNode.textValue());
			}
		} catch (IllegalArgumentException ex) {
			
			throw new JsonSchemaException(
				message()
				.type("internal.cannotResolve")
				.schemaLocation(schemaLocation)
				.message("{0}: {1} is not supported")
				.arguments(schemaLocation, schemaNode.toString())
				.schemaLocation(schemaLocation)
				.build()
				.toString()
				);
		}
		return ExpressionType.NONE;
	}

	@Override
	public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {

		LOGGER.debug("validate( {}, {}, {})", node, rootNode, instanceLocation);
		Set<ValidationMessage> errors = new LinkedHashSet<>();
		try {
			check(node.asText(), expressionType);
		} catch (ExpressionCheckerException ex) {
			ValidationMessage error = message()
				.type(getKeyword())
				.schemaLocation(schemaLocation)
				.message("{0}: [{1}] is a wrong expression syntax, Compile error: {2}")
				.arguments(node.toString(), ex.getMessage())
				.instanceLocation(instanceLocation).instanceNode(node).build();
			errors.add(error);
			LOGGER.debug(error.getMessage(), ex);

		}
		return errors;

	}

	private void check(String expression, ExpressionType expressionType) {
		switch (expressionType) {
			case UAA_EXPRESSION -> uaaLogicExpressionCheck(expression.trim());
			case SPRING_EXPRESSION_LANGUAGE -> springExpressionCheck(expression.trim());
			default -> {}
		}
	}

	private void springExpressionCheck(String expression) {
		try {
			springExpressionParser.parseExpression(expression);
		} catch (RuntimeException ex) {
			throw new ExpressionCheckerException(ex.getMessage(), ex);
		}
	}

	private void uaaLogicExpressionCheck(String expression) {
		springExpressionCheck(UAAExpressionParser.booleanOperatorParse(expression, token -> false));
	}

}
