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
package com.mgmtp.a12.uaa.authentication.principal.oauth.internal;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;

import com.mgmtp.a12.uaa.authentication.principal.RoleDefinition;
import com.mgmtp.a12.uaa.authentication.principal.RoleMappingDataHolder;
import com.mgmtp.a12.uaa.authentication.principal.RoleMappingLoader;
import com.mgmtp.a12.uaa.authentication.principal.UnableToUpdateMappingException;

public class JwtTokenRoleMappingLoader implements RoleMappingLoader<Jwt> {
	private static final String ROLE_SEPARATOR = ":";
	private static final String RIGHT_SEPARATOR = ",";
	private static final String ITEM_SEPARATOR = ";";

	private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenRoleMappingLoader.class);

	private final String accessRightsName;

	public JwtTokenRoleMappingLoader(String accessRightsName) {
		this.accessRightsName = accessRightsName;
	}

	@Override
	public RoleMappingDataHolder loadData(Jwt payload) {
		String accessRightsMapping = payload.getClaimAsString(accessRightsName);
		if (accessRightsMapping == null) {
			LOGGER.warn("No access rights in the JWT token under key [{}]", accessRightsName);
			return null;
		}
		List<RoleDefinition> roles = Arrays.stream(accessRightsMapping.split(ITEM_SEPARATOR))
			.map(item -> {
				String[] itemSplit = item.split(ROLE_SEPARATOR);
				if (itemSplit.length != 2) {
					throw new RuntimeException("Unable to convert item [%s] into 'ROLE:RIGHT(s)'".formatted(item));
				}
				RoleDefinition roleDef = new RoleDefinition(itemSplit[0]);
				Arrays.asList(itemSplit[1].split(RIGHT_SEPARATOR)).forEach(roleDef::addAccessRight);
				return roleDef;
			}).collect(Collectors.toList());

		return new RoleMappingDataHolder().setRoles(roles);
	}

	@Override
	public RoleMappingDataHolder updateData(String data) throws UnableToUpdateMappingException {
		LOGGER.debug("The Role Mapping Data Holder cannot be updated as it is loaded from the JWT token");
		throw new UnableToUpdateMappingException("Data are loaded from JWT token");
	}
}
