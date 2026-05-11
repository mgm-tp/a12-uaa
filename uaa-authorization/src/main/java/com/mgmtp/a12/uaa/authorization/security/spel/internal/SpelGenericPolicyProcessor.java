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

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.CollectionUtils;

import com.mgmtp.a12.uaa.authorization.model.AbstractPolicy;
import com.mgmtp.a12.uaa.authorization.security.GenericPolicyProcessor;

public abstract class SpelGenericPolicyProcessor implements GenericPolicyProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpelGenericPolicyProcessor.class);

	protected SpelRuleExecutor spelRuleExecutor;
	protected StandardEvaluationContext standardEvaluationContext;

	protected SpelGenericPolicyProcessor(StandardEvaluationContext evaluationContext) {
		this.spelRuleExecutor = new SpelRuleExecutor(evaluationContext);
		standardEvaluationContext = evaluationContext;
	}

	@Override
	public boolean targetFilter(AbstractPolicy policy, Object resource) {
		String targetFilterExpression = policy.getTarget();
		boolean policyPassed = true;
		if (StringUtils.isNotBlank(targetFilterExpression)) {
			LOGGER.debug("Applying filter [{}] in context[{}]", targetFilterExpression, standardEvaluationContext.hashCode());
			policyPassed = spelRuleExecutor.executeRules(Set.of(targetFilterExpression), resource);
		}
		policy.targetExecuted(policy.getName(), policyPassed);
		return policyPassed;
	}

	protected void preloadData(AbstractPolicy policy, Object resource) {
		List<String> dataPreload = policy.getDataPreload();
		if (!CollectionUtils.isEmpty(dataPreload)) {
			dataPreload.stream()
				.forEach(preloadItem -> {
					//Expression expression = parser.parseExpression(preloadItem);
					LOGGER.debug("Preloading data [{}] in context[{}]", preloadItem, standardEvaluationContext.hashCode());
					try {
						spelRuleExecutor.executeExpression(preloadItem, resource, Object.class);
					} catch (Exception e) {
						//all pre-loads must pass
						LOGGER.warn("Expression failed to execute [{}], {}", preloadItem, e.getMessage());
					}
				});
		}
	}

}
