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
package com.mgmtp.a12.uaa.authentication.user.internal;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.mgmtp.a12.uaa.authentication.user.LocalUser;
import com.mgmtp.a12.uaa.authentication.user.LocalUserLoader;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

/**
 * Loads users from a giver resource.
 *
 * @param <T> user object type.
 */
public class UAALocalUserLoader<T extends LocalUser> implements LocalUserLoader<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(UAALocalUserLoader.class);

	private final ObjectMapper usersDeserializer = new ObjectMapper(new YAMLFactory())
		.rebuild()
		.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
		.build();

	private Class<T> userType;

	public UAALocalUserLoader(Class<T> userType) {
		this.userType = userType;
	}

	@Override
	public T loadUser(Resource resource) {
		try {
			return usersDeserializer.readValue(resource.getInputStream(), userType);
		} catch (Exception e) {
			LOGGER.debug("Unable to load individual user from resource [%s], it might contain a list of users so trying to load by #loadUsers()"
				.formatted(resource.getFilename()), e);
			return null;
		}
	}

	public Collection<T> loadUsers(Resource resource) {
		try {
			UserDataHolder<T> dataHolder = usersDeserializer.readValue(resource.getInputStream(),
				usersDeserializer.getTypeFactory().constructParametricType(UserDataHolder.class, userType));
			LOGGER.debug("Users has been loaded successfully by data [{}]", dataHolder);
			return dataHolder.getUsers();
		} catch (IOException e) {
			LOGGER.debug("Unable to load users from resource [%s]".formatted(resource.getFilename()), e);
			return Collections.emptyList();
		}
	}

	private static class UserDataHolder<T extends LocalUser> implements Serializable {
		private List<T> users;

		public List<T> getUsers() {
			return users;
		}

		public void setUsers(List<T> users) {
			this.users = users;
		}
	}

}
