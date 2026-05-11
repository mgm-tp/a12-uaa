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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import jakarta.annotation.Generated;

import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Role provides mapping between users and access rights. Each user can have multiple roles assigned and each role
 * can have multiple access rights assigned
 */
@UAAJsonSerialization
public class Role implements GrantedAuthority, Serializable, Comparable<Role> {

	private String name;
	private String description;
	private Set<AccessRight> accessRights = new HashSet<>();

	public Role() {
		super();
	}

	@Generated("SparkTools")
	private Role(Builder builder) {
		this.name = builder.name;
		this.description = builder.description;
		this.accessRights = builder.accessRights;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<AccessRight> getAccessRights() {
		return accessRights;
	}

	public void setAccessRights(Set<AccessRight> accessRights) {
		this.accessRights = accessRights;
	}

	/**
	 * @param accessRight AccessRight that will be added to the rest of the accessRights
	 */
	public void addAccessRight(AccessRight accessRight) {
		accessRights.add(accessRight);
	}

	/**
	 * Removes AccessRight from AccessRights of a role
	 * @param accessRight to be removed from other access rights
	 */
	public void removeAccessRight(AccessRight accessRight) {
		accessRights.remove(accessRight);
	}

	@JsonIgnore
	@Override
	public String getAuthority() {
		return getName();
	}

	public int compareTo(Role other) {
		return Comparator.comparing((Role r) -> r.getName()).compare(this, other);
	}

	@Override
	public String toString() {
		return "Role [name=" + name + ", description=" + description + ", accessRights=" + accessRights + "]";
	}

	/**
	 * Creates a builder to build {@link Role} and initialize it with the given object.
	 * @param role to initialize the builder with
	 * @return created builder
	 */
	@Generated("SparkTools")
	public static Builder builderFrom(Role role) {
		return new Builder(role);
	}

	@Generated("SparkTools")
	public static Builder builderFrom(GrantedAuthority grantedAuthority) {
		return new Builder(grantedAuthority);
	}

	/**
	 * Builder to build {@link Role}.
	 */
	@Generated("SparkTools")
	public static final class Builder {
		private String name;
		private String description;
		private Set<AccessRight> accessRights = new HashSet<>();

		public Builder(String name) {
			this.name = name;
		}

		private Builder(Role role) {
			this.name = role.name;
			this.description = role.description;
			this.accessRights = role.accessRights;
		}

		private Builder(GrantedAuthority grantedAuthority) {
			this.name = grantedAuthority.getAuthority();
		}

		public Builder withDescription(String description) {
			this.description = description;
			return this;
		}

		public Builder withAccessRights(Set<AccessRight> accessRights) {
			this.accessRights = accessRights;
			return this;
		}

		public Role build() {
			return new Role(this);
		}
	};
}
