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
package com.mgmtp.a12.uaa.authentication.principal.a12internal;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.security.core.GrantedAuthority;

import com.mgmtp.a12.uaa.authentication.internal.LambdaUtils;
import com.mgmtp.a12.uaa.authentication.principal.AbstractExtendedPrincipal;
import com.mgmtp.a12.uaa.authentication.principal.AccessRight;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalFactory;
import com.mgmtp.a12.uaa.authentication.principal.Role;
import com.mgmtp.a12.uaa.authentication.principal.RoleDefinition;
import com.mgmtp.a12.uaa.authentication.principal.RoleMappingDataHolder;
import com.mgmtp.a12.uaa.authentication.principal.RoleMappingLoader;

public class RoleMappingProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(RoleMappingProcessor.class);

	@Inject
	private Optional<List<RoleMappingLoader<?>>> roleMappingLoaders;
	@Inject
	private PrincipalFactory userFactory;

	public <T> AbstractExtendedPrincipal<?> populateRightsFromSource(AbstractExtendedPrincipal<?> user, T payload) {
		Optional<RoleMappingDataHolder> roleMappingDataHolder = processRoleMapping(payload);
		Collection<GrantedAuthority> originalAuthorities = user.getAuthorities();
		Collection<GrantedAuthority> mappedRoles = roleMappingDataHolder.map(holder -> getMappedRoles(originalAuthorities, holder)).orElse(originalAuthorities);
		return userFactory.createPrincipal(user.getUsername(), user.getPassword(), mappedRoles, user.getExtendedPrincipalData());
	}

	public void updateMappingData(String data) {
		roleMappingLoaders.orElse(Collections.emptyList()).stream()
			.forEach(LambdaUtils.uncheckedConsumerWithNoException(loader -> loader.updateData(data)));
	}

	private <T> Optional<RoleMappingDataHolder> processRoleMapping(T payload) {
		return roleMappingLoaders.orElse(Collections.emptyList()).stream()
			.filter(obj -> payload != null)
			.filter(roleMappingLoader -> GenericTypeResolver.resolveTypeArgument(roleMappingLoader.getClass(), RoleMappingLoader.class) != null)
			.filter(roleMappingLoader -> GenericTypeResolver.resolveTypeArgument(roleMappingLoader.getClass(), RoleMappingLoader.class)
				.isAssignableFrom(payload.getClass()))
			.map(roleMappingLoader -> callLoader(roleMappingLoader, payload))
			.filter(Objects::nonNull)
			.findFirst();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> RoleMappingDataHolder callLoader(RoleMappingLoader loader, T payLoad) {
		return loader.loadData(payLoad);
	}

	private Collection<GrantedAuthority> getMappedRoles(Collection<GrantedAuthority> authorities, RoleMappingDataHolder roleMappingDataHolder) {
		return authorities.stream()
			.map(authority -> {
				Role role = Role.builderFrom(authority).build();
				RoleDefinition roleMapping = roleMappingDataHolder.getRoleByName(role.getAuthority());
				if (roleMapping == null) {
					LOGGER.warn("The role [{}] has no mapping in role mapping. No access rights will be filled", role.getAuthority());
					return null;
				}
				roleMapping.getAccessRights().stream()
					.forEach(accessRight -> role.addAccessRight(new AccessRight.Builder(accessRight).build()));
				return role;
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}
}
