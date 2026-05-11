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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import com.mgmtp.a12.uaa.authentication.oauth2.Oauth2GrantedAuthorityConverter;
import com.mgmtp.a12.uaa.authentication.principal.Role;


public class UAAGrantedAuthorityConverter implements Oauth2GrantedAuthorityConverter {
	private final String realmAccessName;
	private final String rolesName;

	public UAAGrantedAuthorityConverter(String realmAccessName, String rolesName) {
		super();
		this.realmAccessName = realmAccessName;
		this.rolesName = rolesName;
	}

	public Collection<GrantedAuthority> convert(Jwt source) {
		Stream<String> rolesStream = Optional.of(source)
			.map(s -> s.getClaimAsMap(realmAccessName))
			.map(r -> r.get(rolesName))
			.map(roles -> (List<String>) roles)
			.map(Collection::stream)
			.orElse(Stream.empty());
		return rolesStream
			.map(Role.Builder::new)
			.map(Role.Builder::build)
			.collect(Collectors.toList());
	}

}
