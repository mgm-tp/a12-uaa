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
package com.mgmtp.a12.uaa.authentication.ldap.internal;

import java.util.Collection;
import java.util.Optional;

import jakarta.inject.Inject;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

import com.mgmtp.a12.uaa.authentication.local.UAAExtendedPrincipalDataLoader;
import com.mgmtp.a12.uaa.authentication.principal.internal.UAALdapPrincipal;

public class UAALdapUserDetailMapper extends LdapUserDetailsMapper {

	private static final String DEFAULT_PASSWORD = "***";

	@Inject
	private Optional<UAAExtendedPrincipalDataLoader> extendedPrincipalDataLoader;

	@Override
	public UserDetails mapUserFromContext(DirContextOperations ctx, String userName, Collection<? extends GrantedAuthority> authorities) {
		LdapUserDetailsImpl userDetails = (LdapUserDetailsImpl) super.mapUserFromContext(ctx, userName, authorities);
		Object extendedUserData = extendedPrincipalDataLoader.map(loader -> loader.loadExtendedPrincipalData(userName)).orElse(null);
		return new UAALdapPrincipal<>(userDetails.getUsername(), DEFAULT_PASSWORD, userDetails.isEnabled(), userDetails.isAccountNonExpired(),
			userDetails.isCredentialsNonExpired(), userDetails.isAccountNonLocked(), userDetails.getAuthorities(), extendedUserData, userDetails.getDn());
	}

}
