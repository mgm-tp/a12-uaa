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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.Generated;

public class ExternalPrincipalImpl implements ExternalPrincipal {

	private String username;
	private String email;
	private String firstName;
	private String lastName;
	private String displayName;
	private boolean accountNonExpired;
	private boolean accountNonLocked;
	private boolean credentialsNonExpired;
	private boolean enabled;
	private final Set<Role> roles = new HashSet<>();
	private Map<String, Object> additionalProperties = new HashMap<>();

	protected ExternalPrincipalImpl() {
	}

	@Generated("SparkTools")
	private ExternalPrincipalImpl(Builder builder) {
		this.username = builder.username;
		this.email = builder.email;
		this.firstName = builder.firstName;
		this.lastName = builder.lastName;
		this.displayName = builder.displayName;
		this.accountNonExpired = builder.accountNonExpired;
		this.accountNonLocked = builder.accountNonLocked;
		this.credentialsNonExpired = builder.credentialsNonExpired;
		this.enabled = builder.enabled;
		this.additionalProperties = builder.additionalProperties;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void addRole(Role role) {
		roles.add(role);
	}

	public void removeRole(Role role) {
		roles.remove(role);
	}

	public Map<String, Object> getAdditionalProperties() {
		return additionalProperties;
	}

	public void setAdditionalProperties(Map<String, Object> additionalProperties) {
		this.additionalProperties = additionalProperties;
	}

	@Override
	public String toString() {
		return "ExternalUser [username=" + username + ", eMail=" + "*****" + ", firstName=" + "*****" + ", lastName=" + "*****" + ", displayName="
			+ "*****" + ", accountNonExpired=" + accountNonExpired + ", accountNonLocked=" + accountNonLocked + ", credentialsNonExpired="
			+ credentialsNonExpired + ", enabled=" + enabled + ", roles=" + roles + "]";
	}

	/**
	 * Creates a builder to build {@link ExternalPrincipalImpl} and initialize it with the given object.
	 * @param externalUser to initialize the builder with
	 * @return created builder
	 */
	@Generated("SparkTools")
	public static Builder builderFrom(ExternalPrincipalImpl externalUser) {
		return new Builder(externalUser);
	}

	/**
	 * Builder to build {@link ExternalPrincipalImpl}.
	 */
	@Generated("SparkTools")
	public static final class Builder {
		private String username;
		private String email;
		private String firstName;
		private String lastName;
		private String displayName;
		private boolean accountNonExpired;
		private boolean accountNonLocked;
		private boolean credentialsNonExpired;
		private boolean enabled;
		private Map<String, Object> additionalProperties = new HashMap<>();

		public Builder(String username) {
			this.username = username;
		}

		private Builder(ExternalPrincipalImpl externalUser) {
			this.username = externalUser.username;
			this.email = externalUser.email;
			this.firstName = externalUser.firstName;
			this.lastName = externalUser.lastName;
			this.displayName = externalUser.displayName;
			this.accountNonExpired = externalUser.accountNonExpired;
			this.accountNonLocked = externalUser.accountNonLocked;
			this.credentialsNonExpired = externalUser.credentialsNonExpired;
			this.enabled = externalUser.enabled;
			this.additionalProperties = externalUser.additionalProperties;
		}


		public Builder withEmail(String email) {
			this.email = email;
			return this;
		}

		public Builder withFirstName(String firstName) {
			this.firstName = firstName;
			return this;
		}

		public Builder withLastName(String lastName) {
			this.lastName = lastName;
			return this;
		}

		public Builder withDisplayName(String displayName) {
			this.displayName = displayName;
			return this;
		}

		public Builder withAccountNonExpired(boolean accountNonExpired) {
			this.accountNonExpired = accountNonExpired;
			return this;
		}

		public Builder withAccountNonLocked(boolean accountNonLocked) {
			this.accountNonLocked = accountNonLocked;
			return this;
		}

		public Builder withCredentialsNonExpired(boolean credentialsNonExpired) {
			this.credentialsNonExpired = credentialsNonExpired;
			return this;
		}

		public Builder withEnabled(boolean enabled) {
			this.enabled = enabled;
			return this;
		}

		public Builder withAdditionalProperties(Map<String, Object> additionalProperties) {
			this.additionalProperties = additionalProperties;
			return this;
		}

		public ExternalPrincipalImpl build() {
			return new ExternalPrincipalImpl(this);
		}
	}

}
