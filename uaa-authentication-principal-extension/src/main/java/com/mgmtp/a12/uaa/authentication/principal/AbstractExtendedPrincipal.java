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
package com.mgmtp.a12.uaa.authentication.principal;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.mgmtp.a12.uaa.authentication.principal.UAAJsonSerialization;
import com.mgmtp.a12.uaa.authentication.saml.SamlPrincipal;

/**
 * UAA User used in Authorizations and Authentication
 */
@JsonRootName("User")
@UAAJsonSerialization
public abstract class AbstractExtendedPrincipal<T> extends UAAPrincipal<T> implements Comparable<AbstractExtendedPrincipal<T>>, Serializable, SamlPrincipal {

	private String email;
	private String firstname;
	private String lastname;
	private Map<String, Object> additionalProperties = new HashMap<>();
	private String relayingPartyRegistration;

	public AbstractExtendedPrincipal(String username, String password, Collection<? extends GrantedAuthority> authorities) {
		super(username, password, authorities);
	}

	public AbstractExtendedPrincipal(String username, String password, Collection<? extends GrantedAuthority> authorities, T extendedPrincipalData) {
		super(username, password, authorities, extendedPrincipalData);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String eMail) {
		this.email = eMail;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public Optional<Role> getAuthority(String name) {
		return getAuthorities().stream()
			.filter(authority -> authority.getAuthority().equals(name))
			.map(Role.class::cast)
			.findFirst();
	}

	public void addAdditionalProperty(String name, Object value) {
		additionalProperties.put(name, value);
	}

	public Object getAdditionalProperty(String name) {
		return additionalProperties.get(name);
	}

	public Map<String, Object> getAdditionalProperties() {
		return additionalProperties;
	}

	public void setAdditionalProperties(Map<String, Object> additionalProperties) {
		this.additionalProperties = additionalProperties;
	}

	@Override
	public String getRelyingPartyRegistrationId() {
		return relayingPartyRegistration;
	}

	public void setRelayingPartyRegistration(String relayingPartyRegistration) {
		this.relayingPartyRegistration = relayingPartyRegistration;
	}

	@Override
	public String getName() {
		return getUsername();
	}

	@Override
	public int compareTo(AbstractExtendedPrincipal<T> other) {
		return Comparator.comparing(UserDetails::getUsername).compare(this, other);
	}

	@Override
	public String toString() {
		return "User [email=" + "*****" + ", firstname=" + "*****" + ", lastname=" + "*****" + ", additionalProperties="
			+ additionalProperties + ", getExtendedPrincipalData()=" + getExtendedPrincipalData() + ", toString()=" + super.toString() + ", getAuthorities()="
			+ getAuthorities() + ", getUsername()=" + getUsername() + ", isEnabled()=" + isEnabled() + ", isAccountNonExpired()=" + isAccountNonExpired()
			+ ", isAccountNonLocked()=" + isAccountNonLocked() + ", isCredentialsNonExpired()=" + isCredentialsNonExpired() + ", hashCode()=" + hashCode()
			+ ", getClass()=" + getClass() + "]";
	}

}
