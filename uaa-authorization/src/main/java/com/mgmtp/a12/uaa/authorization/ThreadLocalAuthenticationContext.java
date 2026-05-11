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
package com.mgmtp.a12.uaa.authorization;

import java.util.Optional;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class ThreadLocalAuthenticationContext implements AuthorizationContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadLocalAuthenticationContext.class);

	private ThreadLocal<Stack<AuthorizationContextData>> contextData = ThreadLocal.withInitial(() -> new Stack<>());
	private ThreadLocal<Stack<StandardEvaluationContext>> executionEnvironment = ThreadLocal.withInitial(() -> new Stack<>());
	private ThreadLocal<Boolean> collectionProcessing = ThreadLocal.withInitial(() -> Boolean.FALSE);
	private ThreadLocal<Object> maskedReturnObject = new ThreadLocal<>();

	@Override
	public AuthorizationContextData getCurrentContext() {
		Stack<AuthorizationContextData> dataStack = contextData.get();
		if (dataStack.isEmpty()) {
			LOGGER.debug("Current context is empty");
			return null;
		}
		AuthorizationContextData authorizationContextData = dataStack.lastElement();
		LOGGER.debug("Current context: [{}]", authorizationContextData);
		return authorizationContextData;
	}

	@Override
	public void pushContext(AuthorizationContextData context) {
		context.setCollectionProcessing(collectionProcessing.get());
		LOGGER.debug("Pushing new context: [{}]", context);
		this.contextData.get().push(context);
	}

	@Override
	public AuthorizationContextData popContext() {
		maskedReturnObject.remove();
		Stack<AuthorizationContextData> authorizationContextDataStack = contextData.get();
		if (authorizationContextDataStack.empty()) {
			return null;
		}
		AuthorizationContextData authorizationContextData = popContextData();
		if (authorizationContextDataStack.empty()) {
			popExecutionEnvironment();
		}
		return authorizationContextData;
	}

	private AuthorizationContextData popContextData() {
		Stack<AuthorizationContextData> authorizationContextDataStack = contextData.get();
		AuthorizationContextData authorizationContextData = authorizationContextDataStack.pop();
		//we need to pop all collection processing
		LOGGER.debug("Popping context: [{}]", authorizationContextData);
		while (!authorizationContextDataStack.empty() && authorizationContextDataStack.peek().isCollectionProcessing()
			&& authorizationContextData.isCollectionProcessing()) {
			authorizationContextDataStack.pop();
			LOGGER.debug("... Popping collection context: [{}]", authorizationContextData);
		}
		return authorizationContextData;
	}

	@Override
	public void setExecutionEnvironment(StandardEvaluationContext evaluationContext) {
		StandardEvaluationContext currentExecutionEnvironment = getExecutionEnvironment();
		//only new execution env must be added
		if (currentExecutionEnvironment != evaluationContext) {
			LOGGER.debug("Setting execution environment [{}]", evaluationContext);
			executionEnvironment.get().push(evaluationContext);
			
		}
	}

	@Override
	public StandardEvaluationContext getExecutionEnvironment() {
		return Optional.of(executionEnvironment.get())
			.filter(stack -> !stack.empty())
			.map(Stack::peek)
			.orElse(null);
	}

	@Override
	public void processCollection() {
		LOGGER.debug("Indicate collection processing");
		collectionProcessing.set(Boolean.TRUE);
	}

	@Override
	public void stopProcessCollection() {
		LOGGER.debug("Indicate finished collection processing");
		collectionProcessing.set(Boolean.FALSE);
	}

	@Override
	public void setMaskedReturnObject(Object returnObject) {
		maskedReturnObject.set(returnObject);
	}

	@Override
	public Object getMaskedReturnObjectAndClear() {
		Object object = maskedReturnObject.get();
		maskedReturnObject.remove();
		return object;
	}

	@Override
	public void popExecutionEnvironment() {
		Stack<StandardEvaluationContext> executionEnvironmentsStack = executionEnvironment.get();
		if (executionEnvironmentsStack.isEmpty()) {
			return;
		}
		StandardEvaluationContext currentExecutionEnvironment = executionEnvironmentsStack.pop();
		LOGGER.debug("Popping execution environment: [{}]", currentExecutionEnvironment);
		
	}
}
