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
package com.mgmtp.a12.uaa.authentication.principal.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import com.mgmtp.a12.uaa.authentication.internal.CacheStorageType;
import com.mgmtp.a12.uaa.authentication.principal.RoleMappingDataHolder;
import com.mgmtp.a12.uaa.authentication.principal.RoleMappingLoader;
import com.mgmtp.a12.uaa.authentication.principal.UnableToUpdateMappingException;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

/**
 * Yaml based role mapping loader supports every pay-load since roles are pre-loaded from file.
 * This bean is intended to be last one <code>@Order(Ordered.LOWEST_PRECEDENCE)</code> since loaders for concrete payload should have precedence. 
 *
 */
public class YamlRoleMappingLoader implements RoleMappingLoader<Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(YamlRoleMappingLoader.class);

	private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	private Resource resource;
	private AtomicReference<RoleMappingDataHolder> dataHolderReference = new AtomicReference<>();

	public YamlRoleMappingLoader(Resource resource) {
		this.resource = resource;
	}

	@PostConstruct
	private void loadAccessRightFromResourceFile() {
		Assert.notNull(resource, "Please specify a access right resource");
		try {
			loadFromStream(resource.getInputStream());
		} catch (IOException e) {
			throw new IllegalArgumentException("Unable to load resource [%s]".formatted(resource.getFilename()), e);
		}
	}

	@Override
	@Cacheable(value = CacheStorageType.YAML_ROLE_MAPPING, key = "'data'", unless = "#result == null")
	public RoleMappingDataHolder loadData(Object payload) {
		return dataHolderReference.get();
	}

	private RoleMappingDataHolder loadFromStream(InputStream stream) throws IOException {
		try (InputStream dataStream = stream) {
			RoleMappingDataHolder dataHolder = mapper.readValue(dataStream, RoleMappingDataHolder.class);
			dataHolderReference.set(dataHolder);
			return dataHolder;
		}
	}

	@CachePut(value = CacheStorageType.YAML_ROLE_MAPPING, key = "'data'", unless = "#result == null")
	@Override
	public RoleMappingDataHolder updateData(String data) throws UnableToUpdateMappingException {
		InputStream dataStream = IOUtils.toInputStream(data, StandardCharsets.UTF_8);
		RoleMappingDataHolder updatedData;
		try {
			updatedData = loadFromStream(dataStream);
		} catch (IOException e) {
			LOGGER.debug("The Role Mapping Data Holder could not be updated due to an unexpected error", e);
			throw new UnableToUpdateMappingException("Unable to update Role Mapping Data Holder");
		}
		LOGGER.debug("Role Mapping Data Holder has been updated successfully by data [{}]", updatedData);
		return updatedData;

	}
}
