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
package com.mgmtp.a12.uaa.authorization.schema.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mgmtp.a12.uaa.authorization.schema.internal.collector.GlobalRefsCollector;
import com.mgmtp.a12.uaa.authorization.schema.internal.collector.RefsCollectors;
import com.mgmtp.a12.uaa.authorization.schema.internal.listener.RefsListener;
import com.mgmtp.a12.uaa.authorization.schema.internal.location.LocationJsonNodeFactory;
import com.mgmtp.a12.uaa.authorization.schema.internal.validator.ExpressionTypeKeyword;
import com.mgmtp.a12.uaa.authorization.schema.internal.validator.IdentityPropertyKeyword;
import com.mgmtp.a12.uaa.authorization.schema.internal.validator.RefInKeyword;
import com.networknt.schema.CollectorContext;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.PathType;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;

public class SchemaValidator {
	
	public static final String FILENAME_FIELD = "name";

	private static final String SCHEMA_FILE_PATH = "classpath:schema/authorization-definition-schema.json";
	private ObjectMapper mapper;
	private ResourceLoader resourceLoader;

	private JsonSchemaFactory schemaFactory;
	private SchemaValidatorsConfig config;

	public SchemaValidator(ResourceLoader loader, ObjectMapper mapper) {
		this.resourceLoader = loader;
		this.mapper = mapper;
		// Create Json meta schema
		JsonMetaSchema extendingMeta = JsonMetaSchema.builder(JsonMetaSchema.getV7())
			.keyword(new ExpressionTypeKeyword())
			.keyword(new IdentityPropertyKeyword())
			.keyword(new RefInKeyword())
			.build();

		// Create Json Schema Factory
		schemaFactory = JsonSchemaFactory
			.getInstance(VersionFlag.V7, builder -> {
				builder.metaSchema(extendingMeta);
				builder.jsonMapper(mapper);
			});

		// Create config
		config = new SchemaValidatorsConfig();
		config.addPropertyWalkListener(new RefsListener());
		config.setPathType(PathType.JSON_POINTER);
		config.setExecutionContextCustomizer((executionContext, validationContext) -> {
			CollectorContext collectorContext = executionContext.getCollectorContext();
			collectorContext.add(RefsCollectors.POLICIES.getId(), new GlobalRefsCollector());
			collectorContext.add(RefsCollectors.PROPERTY_RIGHT.getId(), new GlobalRefsCollector());
			collectorContext.add(RefsCollectors.REPOSITORY_POLICY.getId(), new GlobalRefsCollector());
		});

	}

	public List<String> validateAuthorizationFile(String parentPath, List<String> childPaths) {
		List<String> paths = new ArrayList<>(List.of(parentPath));
		Optional.ofNullable(childPaths).ifPresent(paths::addAll);
		List<ErrorMessageProducer> producers = validate(paths);
		return produceErrorMessages(producers);
	}

	private List<String> produceErrorMessages(List<ErrorMessageProducer> producers) {
		return producers.stream()
			.map(ErrorMessageProducer::generateMessages)
			.flatMap(List::stream)
			.collect(Collectors.toList());
	}

	/**
	 *
	 * @param paths the first path will be granted to a main authorization definition
	 */
	private List<ErrorMessageProducer> validate(List<String> paths) {
		List<JsonNode> nodes = parse(paths);

		// Walk all node to collect the refs information to the CollectorContext
		ExecutionContext walkExecutionContext = walk(nodes);

		// Calculating the schema with the CollectorContext
		JsonSchema jsonSchema = calculateSchema(walkExecutionContext);

		// Validate
		ExecutionContext validationExecutionContext = jsonSchema.createExecutionContext();
		validationExecutionContext.setCollectorContext(walkExecutionContext.getCollectorContext());
		List<ErrorMessageProducer> producers = new ArrayList<>();
		nodes.forEach(node -> {
			Set<ValidationMessage> errors = jsonSchema.validate(validationExecutionContext, node);
			if (!errors.isEmpty()) {
				producers.add(new ErrorMessageProducer(errors, node));
			}
		});
		return producers;
	}

	private List<JsonNode> parse(List<String> paths) {
		return paths.stream()
			.map(this::parse)
			.collect(Collectors.toList());
	}

	private JsonNode parse(String filePath) {
		try {
			JsonParser parser = mapper.getFactory().createParser(getInputStream(filePath));
			ObjectReader reader = mapper.reader(new LocationJsonNodeFactory(parser));
			ObjectNode node = reader.readValue(parser, ObjectNode.class);
			node.put(SchemaValidator.FILENAME_FIELD, filePath);
			return node;
		} catch (Exception e) {
			throw new RuntimeException("Unable parse file %s".formatted(filePath), e);
		}

	}

	private ExecutionContext walk(List<JsonNode> nodes) {
		JsonSchema walkerSchema = schemaFactory.getSchema(getInputStream(SCHEMA_FILE_PATH), this.config);
		ExecutionContext executionContext = walkerSchema.createExecutionContext();

		nodes.forEach(node -> walkerSchema.walk(executionContext, node, false));
		return executionContext;
	}

	private InputStream getInputStream(String filePath) {
		try {
			return resourceLoader.getResource(filePath).getInputStream();
		} catch (IOException e) {
			throw new RuntimeException("Unable to read file %s".formatted(filePath), e);
		}
	}

	private JsonSchema calculateSchema(ExecutionContext executionContext) {
		try {
			ObjectNode schemaNode = mapper.readValue(getInputStream(SCHEMA_FILE_PATH), ObjectNode.class);
			// Get allOf node in the authorization-definition-schema.json, allOf node is always array
			ArrayNode allOf = ((ArrayNode) schemaNode.get(ValidatorTypeCode.ALL_OF.getValue()));
			Iterator<JsonNode> iterator = allOf.iterator();
			while (iterator.hasNext()) {
				JsonNode element = iterator.next();
				// Required Property in all, {... "then": { "required": ["repositoryPolicies"] } ...}
				String requiredProperty = element.at("/then/required/0").asText();
				@SuppressWarnings("unchecked")
				Set<JsonNode> collectedData = ((Set<JsonNode>) executionContext.getCollectorContext().get("/" + requiredProperty));
				// If there are any existing global refs, remove required property validator in 'allOf' condition of root schema
				if (Objects.nonNull(collectedData) && !collectedData.isEmpty()) {
					iterator.remove();
				}
			}
			Resource resource = resourceLoader.getResource(SCHEMA_FILE_PATH);
			return schemaFactory.getSchema(resource.getURI(), schemaNode, this.config);
		} catch (IOException e) {
			throw new RuntimeException("Unable to read file %s".formatted(SCHEMA_FILE_PATH), e);
		}
	}

}
