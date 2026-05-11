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
package com.mgmtp.a12.uaa.authorization.example;

import java.util.List;
import java.util.Map;

public final class FamilyBuilder {
	private String name;
	private String description;
	private Integer age;
	private List<String> addresses;
	private List<Address> addressObjects;
	private Map<String, Children> childrenAndAge;
	private Address primaryAddress;

	private FamilyBuilder() {
	}

	public static FamilyBuilder aFamily() {
		return new FamilyBuilder();
	}

	public FamilyBuilder withName(String name) {
		this.name = name;
		return this;
	}

	public FamilyBuilder withDescription(String description) {
		this.description = description;
		return this;
	}

	public FamilyBuilder withAge(Integer age) {
		this.age = age;
		return this;
	}

	public FamilyBuilder withAddresses(List<String> addresses) {
		this.addresses = addresses;
		return this;
	}

	public FamilyBuilder withAddressObjects(List<Address> addressObjects) {
		this.addressObjects = addressObjects;
		return this;
	}

	public FamilyBuilder withChildrenAndAge(Map<String, Children> childrenAndAge) {
		this.childrenAndAge = childrenAndAge;
		return this;
	}

	public FamilyBuilder withPrimaryAddress(Address primaryAddress) {
		this.primaryAddress = primaryAddress;
		return this;
	}

	public Family build() {
		Family family = new Family();
		family.setName(name);
		family.setDescription(description);
		family.setAge(age);
		family.setAddresses(addresses);
		family.setAddressObjects(addressObjects);
		family.setChildrenAndAge(childrenAndAge);
		family.setPrimaryAddress(primaryAddress);
		return family;
	}
}
