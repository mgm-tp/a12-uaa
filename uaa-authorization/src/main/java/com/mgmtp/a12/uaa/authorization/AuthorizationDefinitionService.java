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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import com.mgmtp.a12.uaa.authorization.exception.InvalidAuthorizationDefinitionException;
import com.mgmtp.a12.uaa.authorization.internal.InMemoryAuthorizationDefinitionDataHolder;
import com.mgmtp.a12.uaa.authorization.internal.RuntimeAuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.internal.introspection.AuthorizationIntrospector;
import com.mgmtp.a12.uaa.authorization.internal.introspection.AuthorizationIntrospectorFactory;
import com.mgmtp.a12.uaa.authorization.model.AuthorizationDefinition;
import com.mgmtp.a12.uaa.authorization.model.internal.AuthorizationDefinitionAdapter;
import com.mgmtp.a12.uaa.authorization.schema.internal.SchemaValidator;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.json.JsonMapper;

public class AuthorizationDefinitionService {
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationDefinitionService.class);
	private final String parentPath;
	private final List<String> childPaths;

	private static final ObjectMapper mapper = JsonMapper.builder()
		.defaultMergeable(Boolean.FALSE)
		.build();

	@Inject
	private ApplicationContext applicationContext;
	@Inject
	private ResourceLoader resourceLoader;
	@Inject
	private Environment environment;
	@Inject
	private RuntimeAuthorizationDefinitionRepository authorizationDefRepository;
	private SchemaValidator schemaValidator;

	public AuthorizationDefinitionService(String parentPath, List<String> childPaths) {
		this.parentPath = parentPath;
		this.childPaths = childPaths;
	}

	@PostConstruct
	void setUp() {
		schemaValidator = new SchemaValidator(resourceLoader, mapper);
		loadRules();
	}

	void loadRules() {
		try {
			List<String> errors = schemaValidator.validateAuthorizationFile(parentPath, childPaths);
			if (!errors.isEmpty()) {
				errors.forEach(LOGGER::error);
				throw new InvalidAuthorizationDefinitionException();
			}
			AuthorizationDefinition parent = readFiles(Collections.singletonList(parentPath));
			AuthorizationDefinition child = readFiles(childPaths);
			InMemoryAuthorizationDefinitionDataHolder.initNewData(parent, child);
			AuthorizationIntrospectorFactory.getInstance(authorizationDefRepository, environment)
				.allIntrospectors()
				.forEach(AuthorizationIntrospector::process);
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to read definition file", e);
		}
	}

	public void reloadRules() {
		loadRules();
		applicationContext.publishEvent(new AuthorizationReloadedEvent());
	}

	private AuthorizationDefinition readFiles(List<String> paths) throws IOException {
		if (CollectionUtils.isEmpty(paths)) {
			return new AuthorizationDefinition();
		}

		Iterator<String> pathsIterator = paths.listIterator();
		AuthorizationDefinitionAdapter authorizationDefinition = mapper.readValue(getInputStream(pathsIterator.next()), AuthorizationDefinitionAdapter.class);
		if (pathsIterator.hasNext()) {
			ObjectReader reader = mapper.readerForUpdating(authorizationDefinition);
			while (pathsIterator.hasNext()) {
				authorizationDefinition = reader.readValue(getInputStream(pathsIterator.next()));
			}
		}
		authorizationDefinition.toRepositoryPolicies();
		return authorizationDefinition;
	}

	private InputStream getInputStream(String path) throws IOException {
		return resourceLoader.getResource(path).getInputStream();
	}
}
