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

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import com.mgmtp.a12.uaa.authentication.principal.UAAPrincipal;

public class UAALdapPrincipal<T> extends UAAPrincipal<T> {
	private String dn;

	public UAALdapPrincipal(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired,
		boolean accountNonLocked,
		Collection<? extends GrantedAuthority> authorities, T extendedPrincipalData, String dn) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities, extendedPrincipalData);
		this.dn = dn;
	}

	public static UAALdapPrincipal<?> createLdapUser(UAAPrincipal<?> user) {
		return new UAALdapPrincipal(user.getUsername(), user.getPassword(), user.isEnabled(), user.isAccountNonExpired(),
			user.isCredentialsNonExpired(), user.isAccountNonLocked(), user.getAuthorities(), user.getExtendedPrincipalData(), null);
	}

	public void setDn(String dn) {
		this.dn = dn;
	}

	public String getDn() {
		return this.dn;
	}

}
