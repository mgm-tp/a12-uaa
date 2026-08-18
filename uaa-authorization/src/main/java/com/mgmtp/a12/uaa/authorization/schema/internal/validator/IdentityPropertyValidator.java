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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.uaa.authorization.schema.internal.SchemaValidator;
import com.networknt.schema.CollectorContext;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.keyword.BaseKeywordValidator;
import com.networknt.schema.keyword.Keyword;
import com.networknt.schema.path.NodePath;

public class IdentityPropertyValidator extends BaseKeywordValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(IdentityPropertyValidator.class);

	private static final String GLOBAL_DUPLICATED_IDENTITY_ERROR =
		"{0}: The identity [{1}] with value [{2}] in the [{3}] file is a duplicate of the one in the [{4}] file";

	private static final String IDENTITY_MAP_COLLECTOR_KEY = "identityPropertyValidator.identityMap";

	private final JsonNode identityField;
	private final String schemaLocationKey;

	public IdentityPropertyValidator(SchemaLocation schemaLocation, JsonNode schemaNode,
		Schema parentSchema, Keyword keyword, SchemaContext schemaContext) {
		super(keyword, schemaNode, schemaLocation, parentSchema, schemaContext);
		identityField = schemaNode;
		// Use schema location to differentiate identity spaces (e.g., policies vs propertyPermissions)
		schemaLocationKey = schemaLocation.toString();
	}

	@Override
	public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
		LOGGER.debug("validate({}, {}, {})", node, rootNode, instanceLocation);

		if (identityField.isTextual() && node.isObject()) {
			Map<String, Map<String, Set<JsonNode>>> identityMapBySchema = getOrCreateIdentityMap(executionContext);
			Map<String, Set<JsonNode>> identityMap = identityMapBySchema.computeIfAbsent(schemaLocationKey, k -> new LinkedHashMap<>());
			String currentFileName = getFileName(rootNode);
			Set<JsonNode> localIdentityValues =
				identityMap.computeIfAbsent(currentFileName, fname -> new LinkedHashSet<>());

			String field = identityField.asText();
			JsonNode identity = node.get(field);

			if (identity == null || identity.isNull()) {
				executionContext.addError(
					error()
						.message("{0}: The identity property [{1}] cannot be null")
						.arguments(instanceLocation, field)
						.instanceLocation(instanceLocation)
						.instanceNode(node)
						.evaluationPath(executionContext.getEvaluationPath())
						.build()
				);
				return;
			}

			if (!localIdentityValues.add(identity)) {
				executionContext.addError(
					error()
						.message("{0}: The identity property [{1}] cannot be null")
						.arguments(instanceLocation, field)
						.instanceLocation(instanceLocation)
						.instanceNode(node)
						.evaluationPath(executionContext.getEvaluationPath())
						.build()
				);
			}

			identityMap.entrySet().stream()
				.filter(entry -> !entry.getKey().contains(currentFileName) && entry.getValue().contains(identity))
				.map(Map.Entry::getKey)
				.findFirst()
				.ifPresent(existingIdentityInFileName ->
					executionContext.addError(
						error()
							.messageKey("UAA.Global.DuplicatingIdentity")
							.message(GLOBAL_DUPLICATED_IDENTITY_ERROR)
							.arguments(instanceLocation, field, identity.toString(), currentFileName, existingIdentityInFileName)
							.instanceLocation(instanceLocation)
							.instanceNode(node)
							.evaluationPath(executionContext.getEvaluationPath())
							.build()
					)
				);
		}
	}

	protected String getFileName(JsonNode rootNode) {
		return rootNode.get(SchemaValidator.FILENAME_FIELD).asText();
	}

	@SuppressWarnings("unchecked")
	private Map<String, Map<String, Set<JsonNode>>> getOrCreateIdentityMap(ExecutionContext executionContext) {
		CollectorContext collectorContext = executionContext.getCollectorContext();
		Map<String, Map<String, Set<JsonNode>>> identityMap = collectorContext.get(IDENTITY_MAP_COLLECTOR_KEY);
		if (identityMap == null) {
			identityMap = new LinkedHashMap<>();
			collectorContext.put(IDENTITY_MAP_COLLECTOR_KEY, identityMap);
		}
		return identityMap;
	}
}
