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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.google.gson.Gson;
import com.mgmtp.a12.uaa.authorization.model.RepositoryPolicy;
import com.mgmtp.a12.uaa.authorization.security.RepositoryPolicyProcessor;

public class SpelRepositoryPolicyProcessor extends SpelGenericPolicyProcessor implements RepositoryPolicyProcessor {

	public SpelRepositoryPolicyProcessor(StandardEvaluationContext evaluationContext) {
		super(evaluationContext);
	}

	@Override
	public Set<String> executeRepositoryTemplate(RepositoryPolicy repositoryPolicy, Object resource) {
		Set<String> processedRepositoryTemplates = new LinkedHashSet<>();
		Set<String> stringTemplates = new LinkedHashSet<>();
		Set<Map<?,?>> jsonObjectTemplates = new LinkedHashSet<>();

		repositoryPolicy.getTemplates()
			.forEach(template -> {
				try {
					Optional.ofNullable(new Gson().fromJson(template, Map.class))
						.ifPresentOrElse(jsonObjectTemplates::add,
							() -> stringTemplates.add(template));
				} catch (Exception e) {
					stringTemplates.add(template);
				}
			});
		preloadData(repositoryPolicy, resource);

		Set<String> parsedStringTemplates = stringTemplates.stream()
			.map(template -> spelRuleExecutor.executeExpression(template, resource, String.class))
			.flatMap(Optional::stream)
			.collect(Collectors.toCollection(LinkedHashSet::new));
		Set<String> parsedJsonObjectTemplates = jsonObjectTemplates.stream()
			.map(template -> spelRuleExecutor.executeExpressionForJsonObject(template, resource))
			.flatMap(Optional::stream)
			.map((template) -> new Gson().toJson(template))
			.collect(Collectors.toSet());

		processedRepositoryTemplates.addAll(parsedStringTemplates);
		processedRepositoryTemplates.addAll(parsedJsonObjectTemplates);

		return processedRepositoryTemplates;

	}

}
