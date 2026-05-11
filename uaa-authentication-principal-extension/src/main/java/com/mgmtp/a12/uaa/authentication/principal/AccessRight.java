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

import jakarta.annotation.Generated;

/**
 * AccessRight creates abstraction allowing resource access restriction. It is used in Authorization
 */
@UAAJsonSerialization
public class AccessRight implements Serializable, Comparable<AccessRight> {

	private String name;
	private String description;

	public AccessRight() {
		super();
	}

	@Generated("SparkTools")
	private AccessRight(Builder builder) {
		this.name = builder.name;
		this.description = builder.description;
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

	@Override
	public int compareTo(AccessRight o) {
		return Comparator.comparing((AccessRight right) -> right.getName()).compare(this, o);
	}

	@Override
	public String toString() {
		return "AccessRight [name=" + name + ", description=" + description + "]";
	}

	/**
	 * Creates a builder to build {@link AccessRight} and initialize it with the given object.
	 * @param accessRight to initialize the builder with
	 * @return created builder
	 */
	@Generated("SparkTools")
	public static Builder builderFrom(AccessRight accessRight) {
		return new Builder(accessRight);
	}

	/**
	 * Builder to build {@link AccessRight}.
	 */
	@Generated("SparkTools")
	public static final class Builder {

		private String name;
		private String description;

		public Builder(String name) {
			this.name = name;
		}

		private Builder(AccessRight accessRight) {
			this.name = accessRight.name;
			this.description = accessRight.description;
		}

		public Builder withDescription(String description) {
			this.description = description;
			return this;
		}

		public AccessRight build() {
			return new AccessRight(this);
		}
	}
}
