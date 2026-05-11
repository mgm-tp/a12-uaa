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

import org.javers.core.metamodel.annotation.Id;

public class Family {
	@Id
	private String name;
	private String description;
	private Integer age;
	private List<String> addresses;
	private List<Address> addressObjects;
	private Map<String, Children> childrenAndAge;
	private Address primaryAddress;

	public List<Address> getAddressObjects() {
		return addressObjects;
	}

	public void setAddressObjects(List<Address> addressObjects) {
		this.addressObjects = addressObjects;
	}

	public Address getPrimaryAddress() {
		return primaryAddress;
	}

	public void setPrimaryAddress(Address primaryAddress) {
		this.primaryAddress = primaryAddress;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Children> getChildrenAndAge() {
		return childrenAndAge;
	}

	public void setChildrenAndAge(Map<String, Children> childrenAndAge) {
		this.childrenAndAge = childrenAndAge;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public List<String> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<String> addresses) {
		this.addresses = addresses;
	}

	@Override
	public String toString() {
		return "Family [name=" + name + ", description=" + description + ", age=" + age + ", addresses=" + addresses + ", addressObjects=" + addressObjects
			+ ", childrenAndAge=" + childrenAndAge + ", primaryAddress=" + primaryAddress + "]";
	}

}
