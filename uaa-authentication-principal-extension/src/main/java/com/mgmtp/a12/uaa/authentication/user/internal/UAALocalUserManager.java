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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.mgmtp.a12.uaa.authentication.principal.AbstractExtendedPrincipal;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalProcessor;
import com.mgmtp.a12.uaa.authentication.principal.Role;
import com.mgmtp.a12.uaa.authentication.user.LocalUser;
import com.mgmtp.a12.uaa.authentication.user.LocalUserLoader;
import com.mgmtp.a12.uaa.authentication.user.LocalUserManager;

public class UAALocalUserManager implements LocalUserManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(UAALocalUserManager.class);

	private final AtomicReference<Collection<? extends LocalUser>> userDataHolder = new AtomicReference<>();

	private List<Resource> userFiles;

	@Inject
	private LocalUserLoader<? extends LocalUser> userLoader;

	@Inject
	private PrincipalProcessor principalProcessor;

	@Inject
	private ResourcePatternResolver resourcePatternResolver;

	public UAALocalUserManager(Resource[] userFiles) {
		super();
		this.userFiles = Arrays.asList(userFiles);
	}

	@PostConstruct
	void init() {
		List<Resource> resources = userFiles.stream()
			.map(this::checkAndGetResourcesByWildCard)
			.flatMap(Stream::of).toList();
		List<LocalUser> users = new ArrayList<>();
		resources.forEach(resource -> {
			LocalUser individualUser = userLoader.loadUser(resource);
			if (individualUser != null) {
				users.add(individualUser);
				return;
			}

			Collection<? extends LocalUser> listOfUsers = userLoader.loadUsers(resource);
			if (CollectionUtils.isNotEmpty(listOfUsers)) {
				users.addAll(listOfUsers);
			}
		});
		LOGGER.info("Loaded [{}] local users.", users.size());
		userDataHolder.set(users);
	}

	@Override
	public LocalUser findLocalUser(String userName) {
		return userDataHolder.get().stream()
			.filter(u -> Objects.equals(userName, u.getUsername()))
			.findAny()
			.orElseThrow(() -> new UsernameNotFoundException("No user [%s] found.".formatted(userName)));
	}

	@Override
	public AbstractExtendedPrincipal<?> createPrincipal(String userName) {
		LocalUser localUser = findLocalUser(userName);
		List<Role> localUserRoles = localUser.getAuthorities().stream()
			.map(Role.Builder::new)
			.map(Role.Builder::build)
			.collect(Collectors.toList());
		return principalProcessor.createPrincipal(userName, localUserRoles, localUser);

	}

	@Override
	public Collection<? extends LocalUser> reloadUsers(String data) {
		Collection<? extends LocalUser> usersToReload = userLoader.loadUsers(new ByteArrayResource(data.getBytes()));
		userDataHolder.set(usersToReload);
		LOGGER.debug("LocalUser data has been updated successfully by data [{}]", usersToReload);
		return usersToReload;
	}

	private Resource[] checkAndGetResourcesByWildCard(Resource resource) {
		try {
			String path;
			if (!resource.isFile() && resource instanceof ClassPathResource pathResource) {
				path = "classpath:" + pathResource.getPath();
			} else {
				path = "file:" + resource.getURL().getPath();
			}
			if (path.contains("*")) {
				return resourcePatternResolver.getResources(path);
			}
		} catch (IOException e) {
			return Stream.of().toArray(Resource[]::new);
		}
		return Stream.of(resource).toArray(Resource[]::new);
	}

}
