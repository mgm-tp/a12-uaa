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

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;

import com.mgmtp.a12.uaa.authentication.principal.AbstractExtendedPrincipal;
import com.mgmtp.a12.uaa.authentication.principal.AccessRight;
import com.mgmtp.a12.uaa.authentication.principal.ExternalPrincipalImpl;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalConverter;
import com.mgmtp.a12.uaa.authentication.principal.Role;

public class ExternalPrincipalConverterImpl implements PrincipalConverter<AbstractExtendedPrincipal<?>, ExternalPrincipalImpl> {

	@Override
	public ExternalPrincipalImpl convertPrincipal(AbstractExtendedPrincipal<?> userDetails) {

		ExternalPrincipalImpl convertedUser = new ExternalPrincipalImpl.Builder(userDetails.getUsername())
			.withDisplayName(userDetails.getUsername())
			.withAccountNonExpired(userDetails.isAccountNonExpired())
			.withAccountNonLocked(userDetails.isAccountNonLocked())
			.withCredentialsNonExpired(userDetails.isCredentialsNonExpired())
			.withEmail(userDetails.getEmail())
			.withEnabled(userDetails.isEnabled())
			.withFirstName(userDetails.getFirstname())
			.withLastName(userDetails.getLastname())
			.withAdditionalProperties(userDetails.getAdditionalProperties())
			.build();

		userDetails.getAuthorities().forEach(r -> convertedUser.addRole(convertRole(r)));
		return convertedUser;
	}

	private Role convertRole(GrantedAuthority originalAuthority) {
		Role.Builder roleBuilder = new Role.Builder(originalAuthority.getAuthority());
		if (originalAuthority instanceof Role originalRole) {
			roleBuilder.withDescription(originalRole.getDescription());
			Set<AccessRight> convertedAccessRights = originalRole.getAccessRights().stream()
				.map(this::convert)
				.collect(Collectors.toSet());
			roleBuilder.withAccessRights(convertedAccessRights);
		}
		return roleBuilder.build();
	}

	private AccessRight convert(AccessRight originalAccessRight) {
		return new AccessRight.Builder(originalAccessRight.getName())
			.withDescription(originalAccessRight.getDescription())
			.build();
	}
}