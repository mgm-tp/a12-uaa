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
package com.mgmtp.a12.uaa.authorization.model;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PropertyChangesCheckerTestMap extends AbstractPropertyChangesCheckerTest<TestResourceMap> {

	@Test
	public void checkPropertyPermissionForAddToMapWithSomePermissions() {
		Rights rights = new Rights();
		rights.setWrite(
			Set.of("name", "description", "nested.nestedName", "nested.nestedDescription", "nestedCollection", "nestedCollection[].nestedDescription"));

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));
		TestResourceMap resource = createResource(true);
		TestResourceMap resourceUpdated = createResource(true);
		TestNestedResourceList newNestedResource = new TestNestedResourceList("nestedName3", "nestedDescription3");
		resourceUpdated
			.addToNestedMap(newNestedResource.getnestedName(), newNestedResource);

		boolean changesPermitted = propertyChangesChecker.checkPropertyPermissionForChanges(resource, resourceUpdated, new PropertyPermission());
		Assertions.assertFalse(changesPermitted);
	}

	@Test
	public void checkPropertyPermissionForAddToMapWithAllPermissions() {
		Rights rights = new Rights();
		rights.setWrite(
			Set.of("name", "description", "nested.nestedName", "nested.nestedDescription", "nestedCollection",
				"nestedCollection[].nestedName", "nestedCollection[].nestedDescription"));

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));
		TestResourceMap resource = createResource(true);
		TestResourceMap resourceUpdated = createResource(true);
		TestNestedResourceList newNestedResource = new TestNestedResourceList("nestedName3", "nestedDescription3");
		resourceUpdated
			.addToNestedMap(newNestedResource.getnestedName(), newNestedResource);

		boolean changesPermitted = propertyChangesChecker.checkPropertyPermissionForChanges(resource, resourceUpdated, new PropertyPermission());
		Assertions.assertTrue(changesPermitted);
	}

	@Test
	public void checkPropertyPermissionForRemoveFromMapWithAllPermissions() {
		Rights rights = new Rights();
		rights.setWrite(
			Set.of("name", "description", "nested.nestedName", "nested.nestedDescription", "nestedCollection",
				"nestedCollection[].nestedName", "nestedCollection[].nestedDescription"));

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));
		TestResourceMap resource = createResource(true);
		TestResourceMap resourceUpdated = createResource(true);
		resourceUpdated.getNestedCollection().remove("nestedName2");

		boolean changesPermitted = propertyChangesChecker.checkPropertyPermissionForChanges(resource, resourceUpdated, new PropertyPermission());
		Assertions.assertTrue(changesPermitted);

	}

	@Test
	public void checkPropertyPermissionForRemoveFromMapWithSomePermissions() {
		Rights rights = new Rights();
		rights.setWrite(
			Set.of("name", "description", "nested.nestedName", "nested.nestedDescription", "nestedCollection", "nestedCollection[].nestedDescription"));

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));
		TestResourceMap resource = createResource(true);
		TestResourceMap resourceUpdated = createResource(true);
		resourceUpdated.getNestedCollection().remove("nestedName2");

		boolean changesPermitted = propertyChangesChecker.checkPropertyPermissionForChanges(resource, resourceUpdated, new PropertyPermission());
		Assertions.assertFalse(changesPermitted);
	}

	@Override
	TestResourceMap addNestedToNestedObjects(TestResourceMap testResource, String description1, String description2) {
		testResource.getNestedCollection().values().stream()
			.forEach(nested -> {
				nested
					.addToNestedCollection(new TestNestedResourceList("nestedName1",
						Optional.ofNullable(description1).orElse("nestedDescription1")))
					.addToNestedCollection(new TestNestedResourceList("nestedName2",
						Optional.ofNullable(description2).orElse("nestedDescription2")));
			});
		return testResource;
	}

	@Override
	TestResourceMap createResource(boolean withCollection) {
		TestNestedResourceList nestedResource = new TestNestedResourceList("nestedName", "nestedDescription");
		TestNestedResourceList nestedResourceCollection1 = new TestNestedResourceList("nestedName1", "nestedDescription1");
		TestNestedResourceList nestedResourceCollection2 = new TestNestedResourceList("nestedName2", "nestedDescription2");
		TestResourceMap resource = new TestResourceMap("name", "description", nestedResource);
		if (withCollection) {
			resource
				.addToNestedMap(nestedResourceCollection1.getnestedName(), nestedResourceCollection1)
				.addToNestedMap(nestedResourceCollection2.getnestedName(), nestedResourceCollection2);
		}
		return resource;
	}

	@Override
	TestResourceMap createResourceUpdated(boolean withCollection) {
		TestNestedResourceList nestedResourceUpdated = new TestNestedResourceList("nestedName", "nestedDescriptionUpdated");
		TestNestedResourceList nestedResourceCollection1 = new TestNestedResourceList("nestedName1", "nestedDescription1Updated");
		TestNestedResourceList nestedResourceCollection2 = new TestNestedResourceList("nestedName2", "nestedDescription2Updated");
		TestResourceMap resource = new TestResourceMap("name", "descriptionUpdated", nestedResourceUpdated);
		if (withCollection) {
			resource
				.addToNestedMap(nestedResourceCollection1.getnestedName(), nestedResourceCollection1)
				.addToNestedMap(nestedResourceCollection2.getnestedName(), nestedResourceCollection2);
		}
		return resource;
	}

}
