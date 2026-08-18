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

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.uaa.authorization.exception.ExpressionCheckerException;
import com.mgmtp.a12.uaa.authorization.schema.internal.ExpressionType;
import com.mgmtp.a12.uaa.authorization.security.uaaexpression.internal.UAAExpressionParser;
import com.networknt.schema.Error;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.SchemaException;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.keyword.BaseKeywordValidator;
import com.networknt.schema.keyword.Keyword;
import com.networknt.schema.path.NodePath;

public class ExpressionTypeValidator extends BaseKeywordValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionTypeValidator.class);

	private static final ExpressionParser springExpressionParser = new SpelExpressionParser();

	private final ExpressionType expressionType;

	public ExpressionTypeValidator(SchemaLocation schemaLocation, JsonNode schemaNode,
		Schema parentSchema, Keyword keyword, SchemaContext schemaContext) {
		super(keyword, schemaNode, schemaLocation, parentSchema, schemaContext);

		expressionType = getExpressionType(schemaNode, schemaLocation);
	}

	private ExpressionType getExpressionType(JsonNode schemaNode, SchemaLocation schemaLocation) {
		try {
			if (Objects.nonNull(schemaNode) && schemaNode.isTextual()) {
				return ExpressionType.fromValue(schemaNode.asText());
			}
		} catch (IllegalArgumentException ex) {

			throw new SchemaException(
				error()
					.messageKey("internal.cannotResolve")
					.message("{0}: {1} is not supported")
					.arguments(schemaLocation, schemaNode.toString())
					.build()
					.toString()
			);
		}
		return ExpressionType.NONE;
	}

	@Override
	public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
		LOGGER.debug("validate({}, {}, {})", node, rootNode, instanceLocation);

		String expression = node.asText();

		try {
			check(expression, expressionType);
		} catch (ExpressionCheckerException ex) {
			Error error = error()
				.message("{0}: [{1}] is a wrong expression syntax, Compile error: {2}")
				.arguments(instanceLocation, expression, ex.getMessage())
				.instanceLocation(instanceLocation)
				.instanceNode(node)
				.evaluationPath(executionContext.getEvaluationPath())
				.build();

			executionContext.addError(error);
			LOGGER.debug("Validation error: {}", error, ex);
		}
	}

	private void check(String expression, ExpressionType expressionType) {
		switch (expressionType) {
		case UAA_EXPRESSION -> uaaLogicExpressionCheck(expression.trim());
		case SPRING_EXPRESSION_LANGUAGE -> springExpressionCheck(expression.trim());
		default -> {
		}
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
