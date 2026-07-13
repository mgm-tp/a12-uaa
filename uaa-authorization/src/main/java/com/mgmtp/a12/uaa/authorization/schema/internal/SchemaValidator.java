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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.io.ResourceLoader;

import com.mgmtp.a12.uaa.authorization.schema.internal.collector.GlobalRefsCollector;
import com.mgmtp.a12.uaa.authorization.schema.internal.collector.RefsCollectors;
import com.mgmtp.a12.uaa.authorization.schema.internal.listener.RefsListener;
import com.mgmtp.a12.uaa.authorization.schema.internal.location.LocationJsonNodeFactory;
import com.mgmtp.a12.uaa.authorization.schema.internal.validator.ExpressionTypeKeyword;
import com.mgmtp.a12.uaa.authorization.schema.internal.validator.IdentityPropertyKeyword;
import com.mgmtp.a12.uaa.authorization.schema.internal.validator.RefInKeyword;
import com.networknt.schema.CollectorContext;
import com.networknt.schema.Error;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.OutputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.keyword.KeywordType;
import com.networknt.schema.path.PathType;
import com.networknt.schema.walk.PropertyWalkHandler;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

public class SchemaValidator {

	public static final String FILENAME_FIELD = "name";

	private static final String SCHEMA_FILE_PATH = "classpath:schema/authorization-definition-schema.json";

	private final ObjectMapper mapper;
	private final ResourceLoader resourceLoader;
	private final SchemaRegistry schemaFactory;
	private final SchemaRegistryConfig config;

	public SchemaValidator(ResourceLoader loader, ObjectMapper mapper) {
		this.resourceLoader = loader;
		this.mapper = mapper;

		Dialect extendingMeta = Dialect.builder(Dialects.getDraft7())
			.keyword(new ExpressionTypeKeyword())
			.keyword(new IdentityPropertyKeyword())
			.keyword(new RefInKeyword())
			.build();

		this.config = SchemaRegistryConfig.builder()
			.pathType(PathType.JSON_POINTER)
			.executionContextCustomizer((executionContext, schemaContext) -> {
				executionContext.walkConfig(walkConfig -> walkConfig.propertyWalkHandler(
					PropertyWalkHandler.builder()
						.propertyWalkListener(new RefsListener())
						.build()
				));

				CollectorContext collectorContext = executionContext.getCollectorContext();
				collectorContext.put(RefsCollectors.POLICIES.getId(), new GlobalRefsCollector());
				collectorContext.put(RefsCollectors.PROPERTY_RIGHT.getId(), new GlobalRefsCollector());
				collectorContext.put(RefsCollectors.REPOSITORY_POLICY.getId(), new GlobalRefsCollector());
			})
			.build();

		this.schemaFactory = SchemaRegistry.withDialect(extendingMeta, builder ->
			builder
				.schemaRegistryConfig(this.config)
				.nodeReader(nodeReader -> nodeReader.jsonMapper(mapper))
		);
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

	private List<ErrorMessageProducer> validate(List<String> paths) {
		List<JsonNode> nodes = parse(paths);

		ExecutionContext walkExecutionContext = walk(nodes);
		Schema jsonSchema = calculateSchema(walkExecutionContext);

		List<ErrorMessageProducer> producers = new ArrayList<>();

		nodes.forEach(node -> {
			ExecutionContext validationExecutionContext = jsonSchema.createExecutionContext();
			validationExecutionContext.setCollectorContext(walkExecutionContext.getCollectorContext());

			List<Error> errors = jsonSchema.validate(
				validationExecutionContext,
				node,
				OutputFormat.DEFAULT
			);

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
		try (InputStream inputStream = getInputStream(filePath);
			JsonParser parser = mapper.createParser(inputStream)) {

			ObjectNode node = mapper
				.reader(new LocationJsonNodeFactory(parser))
				.forType(ObjectNode.class)
				.readValue(parser);

			node.put(FILENAME_FIELD, filePath);
			return node;
		} catch (Exception e) {
			throw new RuntimeException("Unable parse file %s".formatted(filePath), e);
		}
	}

	private ExecutionContext walk(List<JsonNode> nodes) {
		Schema walkerSchema = schemaFactory.getSchema(SchemaLocation.of(SCHEMA_FILE_PATH));
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

	private Schema calculateSchema(ExecutionContext executionContext) {
		ObjectNode schemaNode = mapper.readValue(getInputStream(SCHEMA_FILE_PATH), ObjectNode.class);

		ArrayNode allOf = (ArrayNode) schemaNode.get(KeywordType.ALL_OF.getValue());
		Iterator<JsonNode> iterator = allOf.iterator();

		while (iterator.hasNext()) {
			JsonNode element = iterator.next();
			String requiredProperty = element.at("/then/required/0").asString();

			GlobalRefsCollector collector =
				executionContext.getCollectorContext().get("/" + requiredProperty);

			Set<JsonNode> collectedData =
				collector != null ? collector.collect() : Set.of();

			if (!collectedData.isEmpty()) {
				iterator.remove();
			}
		}

		return schemaFactory.getSchema(
			SchemaLocation.of(SCHEMA_FILE_PATH),
			schemaNode
		);
	}
}