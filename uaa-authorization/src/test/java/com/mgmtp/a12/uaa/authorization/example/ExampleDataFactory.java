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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExampleDataFactory {

	public static Family getOldFamilyObject() {
		List<String> addresses = new ArrayList<>();
		addresses.add("Add1");
		addresses.add("Add2");
		addresses.add("Add3");
		addresses.add("Add4");
		addresses.add("Add5");
		List<Address> addressObjects = new ArrayList<>();
		addressObjects.add(AddressBuilder.anAddress().withNumber(12346).withStreetName("Nguyen Chi Thanh").build());
		addressObjects.add(AddressBuilder.anAddress().withNumber(127646).withStreetName("Nguyen Van Thoai").build());
		addressObjects.add(AddressBuilder.anAddress().withNumber(123216).withStreetName("Le Duan").build());
		Map<String, Children> family = new HashMap<>();
		family.put("Hung", ChildrenBuilder.aChildren().withAge(10).withDescription("brother1").withName("Hung").build());
		family.put("Ha", ChildrenBuilder.aChildren().withAge(12).withDescription("brother2").withName("Ha").build());
		family.put("Vi", ChildrenBuilder.aChildren().withAge(14).withDescription("sister1").withName("Vi").build());
		family.put("Loan", ChildrenBuilder.aChildren().withAge(16).withDescription("sister2").withName("Loan").build());
		return FamilyBuilder
			.aFamily()
			.withAge(10)
			.withName("Nguyen Minh")
			.withAddresses(addresses)
			.withChildrenAndAge(family)
			.withPrimaryAddress(
				AddressBuilder.anAddress().withNumber(1234).withStreetName("Ngo Quyen").build()
			)
			.withAddressObjects(addressObjects)
			.build();
	}

	public static Family getNewFamilyObject() {
		List<String> newAddresses = new ArrayList<>();
		newAddresses.add("NewAdd1");
		newAddresses.add("NewAdd2");
		newAddresses.add("Add3");
		newAddresses.add("Add4");
		newAddresses.add("NewAdd5");
		List<Address> newAddressObjects = new ArrayList<>();
		newAddressObjects.add(AddressBuilder.anAddress().withNumber(12346).withStreetName("Nguyen Chi Thanh").build());
		newAddressObjects.add(AddressBuilder.anAddress().withNumber(1288646).withStreetName("Nguyen Van Thoai").build());
		newAddressObjects.add(AddressBuilder.anAddress().withNumber(123216).withStreetName("Nguyen Tat Thanh").build());
		newAddressObjects.add(AddressBuilder.anAddress().withNumber(123216).withStreetName("Add new Address Object").build());
		Map<String, Children> newFamily = new HashMap<>();
		newFamily.put("Hung", ChildrenBuilder.aChildren().withAge(10).withDescription("brother1").withName("Hung").build());
		newFamily.put("Ha 2", ChildrenBuilder.aChildren().withAge(12).withDescription("brother3").withName("Ha").build());
		newFamily.put("Vi", ChildrenBuilder.aChildren().withAge(18).withDescription("sister6").withName("Vi").build());
		newFamily.put("Loan 2", ChildrenBuilder.aChildren().withAge(20).withDescription("sister2").withName("Loan").build());
		return FamilyBuilder
			.aFamily()
			.withAge(17)
			.withDescription("group of people 2")
			.withAddresses(newAddresses)
			.withChildrenAndAge(newFamily)
			.withPrimaryAddress(
				AddressBuilder.anAddress().withNumber(1234).withStreetName("Phan Chau Trinh").build()
			)
			.withAddressObjects(newAddressObjects)
			.build();
	}
}
