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
package com.mgmtp.a12.uaa.authorization.security.spel.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.mgmtp.a12.uaa.authorization.internal.UAAUserDetails;
import com.mgmtp.a12.uaa.authorization.internal.UserUtils;

public class SpelRuleExecutor {

	private static final String RESOURCE = "resource";
	private static Logger LOGGER = LoggerFactory.getLogger(SpelRuleExecutor.class);
	private SpelExpressionParser parser = new SpelExpressionParser();

	private StandardEvaluationContext evaluationContext;

	public SpelRuleExecutor(StandardEvaluationContext evaluationContext) {
		this.evaluationContext = evaluationContext;
	}

	public StandardEvaluationContext getEvaluationContext() {
		return evaluationContext;
	}

	public boolean executeRules(Set<String> rules, Object customResource) {

		Optional<Boolean> failedRule = rules.stream()
			.map(expression -> {
				try {
					Optional<Boolean> expressionResult = executeExpression(expression, customResource, Boolean.class);
					return expressionResult.orElse(true);
				} catch (Exception e) {
					LOGGER.debug("...Expression {} execution failed context[{}]. {}", expression, evaluationContext.hashCode(), e.getMessage());
					return false;
				}
			})
			.filter(result -> result == false)
			.findFirst();
		return failedRule.isEmpty();
	}

	public <T> Optional<T> executeExpression(String expression, Object customResource, Class<T> expectedType) {
		if (customResource != null) {
			evaluationContext.setVariable(RESOURCE, ObjectResolver.resolveDataObject(customResource));
		}

		UAAUserDetails currentUser = UserUtils.resolveCurrentUser();
		LOGGER.debug("...Level {} for expression {} context[{}]", currentUser.getPermissionCheckLevel(), expression, evaluationContext.hashCode());
		if (currentUser.runPrivileged()) {
			LOGGER.debug("...Running in privilege mode. Following expression has been ignored {}. Level {} in context[{}]", expression,
				currentUser.getPermissionCheckLevel(),
				evaluationContext.hashCode());
			return Optional.empty();
		}
		try {
			currentUser.permissionCheckStarted();
			//here we might execute new context which must be run with higher privileges
			Expression parsedExpression = parser.parseExpression(expression);
			T expressionResult = parsedExpression.getValue(evaluationContext, expectedType);
			LOGGER.debug("...Executed expression[{}] => [{}] in context[{}]", expression, expressionResult, evaluationContext.hashCode());
			return Optional.ofNullable(expressionResult);
		} catch (Exception e) {
			LOGGER.error("...Expression [{}] execution failed. [{}] in context[{}]", expression, e.getMessage(), evaluationContext.hashCode());
			throw e;
		} finally {
			currentUser.permissionCheckFinished();
		}
	}

	/**
	 * Execute expressions included insight a Json object.
	 *
	 * @param object the json object.
	 * @param customResource resource
	 * @return a new Map represents for Json object after replacing expression placeholders
	 */
	public Optional<Object> executeExpressionForJsonObject(Object object, Object customResource) {
		if (customResource != null) {
			evaluationContext.setVariable(RESOURCE, ObjectResolver.resolveDataObject(customResource));
		}
		UAAUserDetails currentUser = UserUtils.resolveCurrentUser();
		if (currentUser.runPrivileged()) {
			LOGGER.debug("...Running in privilege mode. Following expression has been ignored {}. Level {} in context[{}]", object,
				currentUser.getPermissionCheckLevel(),
				evaluationContext.hashCode());
			return Optional.empty();
		}
		Object result = traverseJsonObjectForExpression(object);
		LOGGER.debug("...Json object expression executed [{}] => [{}] in context[{}]", object, result, evaluationContext.hashCode());
		return Optional.ofNullable(result);
	}

	private Object traverseJsonObjectForExpression(Object originalObject) {
		if(originalObject instanceof Map<?,?> map) {
			Map<Object, Object> subMap = new LinkedHashMap<>();
			map.forEach((key, value) -> subMap.put(key, traverseJsonObjectForExpression(value)));
			return subMap;
		}

		if (originalObject instanceof Collection<?> collection) {
			List<Object> subArray = new ArrayList<>();
			collection.forEach(val -> subArray.add(traverseJsonObjectForExpression(val)));
			return subArray;
		}

		try {
			Expression parsedExpression = parser.parseExpression(originalObject.toString());
			return parsedExpression.getValue(evaluationContext, String.class);
		} catch (Exception e) {
			//nothing to do
		}

		return originalObject;
	}
}
