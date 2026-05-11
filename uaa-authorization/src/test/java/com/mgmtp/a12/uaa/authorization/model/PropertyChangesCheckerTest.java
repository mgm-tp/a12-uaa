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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.javers.core.diff.custom.CustomPropertyComparator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.userdetails.UserDetails;

import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.property.PropertyChangePathConverter;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyChangeConverter;
import com.mgmtp.a12.uaa.authorization.property.internal.ResourceConverter;
import com.mgmtp.a12.uaa.authorization.security.PropertyChangesChecker;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PropertyChangesCheckerTest {

	@Mock
	private AuthorizationDefinitionRepository authorizationDefinitionRepository;
	@Mock
	private UserDetails userDetails;
	@Spy
	private Optional<List<ResourceConverter>> resourceConverter = Optional.empty();
	@Spy
	private Optional<List<CustomPropertyComparator>> propertyComparators = Optional.empty();
	@Spy
	private Optional<List<PropertyChangePathConverter>> propertyChangeConverters = Optional.of(Arrays.asList(new PropertyChangeConverter()));
	@InjectMocks
	private PropertyChangesChecker propertyChangesChecker = new PropertyChangesChecker(Arrays.asList("com.mgmtp"));

	@BeforeEach
	void setUp() {
		propertyChangesChecker.initJavers();
	}

	@Test
	public void checkPropertyPermissionForChangesWithAllPermissions() {
		Rights rights = new Rights();
		rights.setWrite(Set.of("name", "description", "nested.nestedName", "nested.nestedDescription"));

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));
		boolean changesPermitted = checkPermission(false);
		Assertions.assertTrue(changesPermitted);
	}

	@Test
	public void checkPropertyPermissionForChangesWithSomePermissions() {
		Rights rights = new Rights();
		rights.setWrite(Set.of("name", "description", "nested.name"));

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));
		boolean changesPermitted = checkPermission(false);
		Assertions.assertFalse(changesPermitted);
	}

	@Test
	public void checkPropertyPermissionForChangesWithNoPermissions() {

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Collections.emptySet());
		boolean changesPermitted = checkPermission(false);
		Assertions.assertFalse(changesPermitted);
	}

	@Test
	public void checkPropertyPermissionForChangesCollectionWithAllPermissions() {
		Rights rights = new Rights();
		rights.setWrite(
			Set.of("name", "description", "nested.nestedName", "nested.nestedDescription", "nestedCollection", "nestedCollection[].nestedDescription"));

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));
		boolean changesPermitted = checkPermission(true);
		Assertions.assertTrue(changesPermitted);
	}

	@Test
	public void checkPropertyPermissionForChangesCollectionWithSomePermissions() {
		Rights rights = new Rights();
		rights.setWrite(
			Set.of("name", "description", "nested.nestedName", "nested.nestedDescription", "nestedCollection"));

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));
		boolean changesPermitted = checkPermission(true);
		Assertions.assertFalse(changesPermitted);
	}

	@Test
	public void checkPropertyPermissionForChangesNestedCollectionWithAllPermissions() {
		Rights rights = new Rights();
		rights.setWrite(
			Set.of("name", "description", "nested.nestedName", "nested.nestedDescription", "nestedCollection", "nestedCollection[].nestedDescription",
				"nestedCollection[].nestedCollection[].nestedDescription"));

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));
		boolean changesPermitted = checkPermissionWithDeepNestedObjects();
		Assertions.assertTrue(changesPermitted);
	}

	@Test
	public void checkPropertyPermissionForChangesNestedCollectionWithSomePermissions() {
		Rights rights = new Rights();
		rights.setWrite(
			Set.of("name", "description", "nested.nestedName", "nested.nestedDescription", "nestedCollection", "nestedCollection[].nestedDescription"));

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));
		boolean changesPermitted = checkPermissionWithDeepNestedObjects();
		Assertions.assertFalse(changesPermitted);
	}

	@Test
	public void checkPropertyPermissionForChangesCollectionRemoveWithAllPermissions() {
		Rights rights = new Rights();
		rights.setWrite(
			Set.of("name", "description", "nested.nestedName", "nested.nestedDescription", "nestedCollection", "nestedCollection[].nestedName",
				"nestedCollection[].nestedDescription"));

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));
		boolean changesPermitted = checkPermissionWithCollectionObjectsRemoved();
		Assertions.assertTrue(changesPermitted);
	}

	@Test
	public void checkPropertyPermissionForChangesCollectionRemoveWithSomePermissions() {
		Rights rights = new Rights();
		rights.setWrite(
			Set.of("name", "description", "nested.nestedName", "nested.nestedDescription", "nestedCollection", "nestedCollection[].nestedDescription"));

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));
		boolean changesPermitted = checkPermissionWithCollectionObjectsRemoved();
		Assertions.assertFalse(changesPermitted);
	}

	@Test
	public void checkPropertyPermissionForChangesNestedCollectionRemoveWithAllPermissions() {
		Rights rights = new Rights();
		rights.setWrite(
			Set.of("name", "description", "nested.nestedName", "nested.nestedDescription", "nestedCollection", "nestedCollection[].nestedName",
				"nestedCollection[].nestedDescription", "nestedCollection[].nestedCollection", "nestedCollection[].nestedCollection[].nestedName",
				"nestedCollection[].nestedCollection[].nestedDescription"));

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));
		boolean changesPermitted = checkPermissionWithNestedCollectionObjectsRemoved();
		Assertions.assertTrue(changesPermitted);
	}

	@Test
	public void checkPropertyPermissionForChangesNestedCollectionRemoveWithSomePermissions() {
		Rights rights = new Rights();
		rights.setWrite(
			Set.of("name", "description", "nested.nestedName", "nested.nestedDescription", "nestedCollection", "nestedCollection[].nestedName",
				"nestedCollection[].nestedDescription", "nestedCollection[].nestedCollection[].nestedName",
				"nestedCollection[].nestedCollection[].nestedDescription"));
		//missing nestedCollection[].nestedCollection
		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));
		boolean changesPermitted = checkPermissionWithNestedCollectionObjectsRemoved();
		Assertions.assertFalse(changesPermitted);
	}

	@Test
	public void checkCompanyCollectionRemoved() {
		Rights rights = new Rights();
		rights.setWrite(Set.of("id", "taxNumber", "countryCode", "employees", "employees[].firstName",
			"employees[].lastName", "employees[].id", "employees[].age"));
		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));

		Company resource = new Company("name");
		resource.setId(1L);
		resource.setTaxNumber("taxNumber");
		resource.setCountryCode("ctryCode");
		resource
			.setEmployees(new LinkedList<>(Set.of(new Employee(1l, "firstName1", "lastName1", 10),
				new Employee(2l, "firstName2", "lastName2", 20))));

		Company resourceUpdated = new Company("name");
		resourceUpdated.setId(1L);
		resourceUpdated.setTaxNumber("taxNumber_Updated");
		resourceUpdated.setCountryCode("ctryCode");
		resourceUpdated.setEmployees(new LinkedList<>());

		boolean changesPermitted = propertyChangesChecker.checkPropertyPermissionForChanges(resource, resourceUpdated, new PropertyPermission());
		Assertions.assertTrue(changesPermitted);
	}

	@Test
	public void checkCompanyCollectionUpdatedAndProperty() {
		Rights rights = new Rights();
		rights.setWrite(
			Set.of("id", "taxNumber", "countryCode", "employees", "employees[].firstName", "employees[].lastName", "employees[].id", "employees[].age"));
		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));

		Company resource = new Company("name");
		resource.setId(1L);
		resource.setTaxNumber("taxNumber");
		resource.setCountryCode("ctryCode");
		resource
			.setEmployees(new LinkedList<>(Set.of(new Employee(1l, "firstName1", "lastName1", 10),
				new Employee(2l, "firstName2", "lastName2", 20))));

		Company resourceUpdated = new Company("name");
		resourceUpdated.setId(1L);
		resourceUpdated.setTaxNumber("taxNumber_Updated");
		resourceUpdated.setCountryCode("ctryCode");
		resourceUpdated
			.setEmployees(new LinkedList<>(Set.of(new Employee(1l, "firstName1", "lastName1", 10),
				new Employee(2l, "firstName2", "lastName2_Updated", 20))));

		boolean changesPermitted = propertyChangesChecker.checkPropertyPermissionForChanges(resource, resourceUpdated, new PropertyPermission());
		Assertions.assertTrue(changesPermitted);
	}

	@Test
	public void checkCompanyCollectionUpdated() {
		Rights rights = new Rights();
		rights.setWrite(
			Set.of("id", "taxNumber", "countryCode", "employees", "employees[].firstName", "employees[].lastName", "employees[].id", "employees[].age"));
		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));

		Company resource = new Company("name");
		resource.setId(1L);
		resource.setTaxNumber("taxNumber");
		resource.setCountryCode("ctryCode");
		resource
			.setEmployees(new LinkedList<>(Set.of(new Employee(1l, "firstName1", "lastName1", 10),
				new Employee(2l, "firstName2", "lastName2", 20))));

		Company resourceUpdated = new Company("name");
		resourceUpdated.setId(1L);
		resourceUpdated.setTaxNumber("taxNumber");
		resourceUpdated.setCountryCode("ctryCode");
		resourceUpdated
			.setEmployees(new LinkedList<>(Set.of(new Employee(1l, "firstName1", "lastName1", 10),
				new Employee(2l, "firstName2", "lastName2_Updated", 20))));

		boolean changesPermitted = propertyChangesChecker.checkPropertyPermissionForChanges(resource, resourceUpdated, new PropertyPermission());
		Assertions.assertTrue(changesPermitted);
	}

	@Test
	public void checkCompanyBigCollectionUpdated() {

		Rights rights = new Rights();
		rights.setWrite(
			Set.of("id", "taxNumber", "countryCode", "employees", "employees[].firstName", "employees[].lastName", "employees[].id", "employees[].age"));
		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));

		Company resource = new Company("name");
		resource.setId(1L);
		resource.setTaxNumber("taxNumber");
		resource.setCountryCode("ctryCode");
		resource
			.setEmployees(new LinkedList<>(Set.of(
				new Employee(1l, "firstName1", "lastName1", 10),
				new Employee(2l, "firstName2", "lastName2", 20),
				new Employee(3l, "firstName3", "lastName3", 20),
				new Employee(4l, "firstName4", "lastName4", 20),
				new Employee(5l, "firstName5", "lastName5", 20),
				new Employee(6l, "firstName6", "lastName6", 20),
				new Employee(7l, "firstName7", "lastName7", 20),
				new Employee(8l, "firstName8", "lastName8", 20),
				new Employee(9l, "firstName9", "lastName9", 20),
				new Employee(10l, "firstName10", "lastName10", 20),
				new Employee(11l, "firstName11", "lastName11", 20)
			)));

		Company resourceUpdated = new Company("name");
		resourceUpdated.setId(1L);
		resourceUpdated.setTaxNumber("taxNumber_Updated");
		resourceUpdated.setCountryCode("ctryCode");
		resourceUpdated
			.setEmployees(new LinkedList<>(Set.of(
				new Employee(1l, "firstName1", "lastName1", 10),
				new Employee(2l, "firstName2", "lastName2_Updated", 20),
				new Employee(3l, "firstName3", "lastName3", 20),
				new Employee(4l, "firstName4", "lastName4", 20),
				new Employee(5l, "firstName5", "lastName5", 20),
				new Employee(6l, "firstName6", "lastName6", 20),
				new Employee(7l, "firstName7", "lastName7", 20),
				new Employee(8l, "firstName8", "lastName8", 20),
				new Employee(9l, "firstName9", "lastName9", 20),
				new Employee(10l, "firstName10", "lastName10_Updated", 20),
				new Employee(11l, "firstName11", "lastName11_Updated", 20)
			)));

		boolean changesPermitted = propertyChangesChecker.checkPropertyPermissionForChanges(resource, resourceUpdated, new PropertyPermission());
		Assertions.assertTrue(changesPermitted);
	}

	private boolean checkPermissionWithNestedCollectionObjectsRemoved() {
		TestResource resource = addNestedToNestedObjects(createResource(true), null, null);
		TestResource resourceUpdated = createResourceUpdated(true);

		return propertyChangesChecker.checkPropertyPermissionForChanges(resource, resourceUpdated, new PropertyPermission());
	}

	private boolean checkPermissionWithCollectionObjectsRemoved() {
		TestResource resource = createResource(true);
		TestResource resourceUpdated = createResourceUpdated(false);

		return propertyChangesChecker.checkPropertyPermissionForChanges(resource, resourceUpdated, new PropertyPermission());
	}

	private boolean checkPermissionWithDeepNestedObjects() {
		TestResource resource = addNestedToNestedObjects(createResource(true), null, null);
		TestResource resourceUpdated =
			addNestedToNestedObjects(createResourceUpdated(true), "nestedDescription1", "nestedDescription2Updated");

		return propertyChangesChecker.checkPropertyPermissionForChanges(resource, resourceUpdated, new PropertyPermission());
	}

	private boolean checkPermission(boolean withCollection) {
		TestResource resource = createResource(withCollection);
		TestResource resourceUpdated = createResourceUpdated(withCollection);

		return propertyChangesChecker.checkPropertyPermissionForChanges(resource, resourceUpdated, new PropertyPermission());
	}

	private TestResource addNestedToNestedObjects(TestResource testResource, String description1, String description2) {

		testResource.getNestedCollection().forEach(nested -> {
			nested.addToNestedCollection(new NestedResource("nestedName1", Optional.ofNullable(description1).orElse("nestedDescription1")))
				.addToNestedCollection(new NestedResource("nestedName2", Optional.ofNullable(description2).orElse("nestedDescription2")));
		});
		return testResource;
	}

	private TestResource createResource(boolean withCollection) {
		NestedResource nestedResource = new NestedResource("nestedName", "nestedDescription");
		NestedResource nestedResourceCollection1 = new NestedResource("nestedName1", "nestedDescription1");
		NestedResource nestedResourceCollection2 = new NestedResource("nestedName2", "nestedDescription2");
		TestResource resource = new TestResource("name", "description", nestedResource);
		if (withCollection) {
			resource
				.addToNestedCollection(nestedResourceCollection1)
				.addToNestedCollection(nestedResourceCollection2);
		}
		return resource;
	}

	private TestResource createResourceUpdated(boolean withCollection) {
		NestedResource nestedResourceUpdated = new NestedResource("nestedName2", "nestedDescriptionUpdated");
		NestedResource nestedResourceCollection1 = new NestedResource("nestedName1", "nestedDescription1Updated");
		NestedResource nestedResourceCollection2 = new NestedResource("nestedName2", "nestedDescription2Updated");
		TestResource resource = new TestResource("name", "descriptionUpdated", nestedResourceUpdated);
		if (withCollection) {
			resource
				.addToNestedCollection(nestedResourceCollection1)
				.addToNestedCollection(nestedResourceCollection2);
		}
		return resource;
	}

	static class TestResource {
		private String name;
		private String description;
		private NestedResource nested;
		private List<NestedResource> nestedCollection = new LinkedList<>();

		public TestResource(String name, String description, NestedResource nested) {
			this.name = name;
			this.description = description;
			this.nested = nested;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public NestedResource getNested() {
			return nested;
		}

		public List<NestedResource> getNestedCollection() {
			return nestedCollection;
		}

		public TestResource addToNestedCollection(NestedResource nestedResource) {
			nestedCollection.add(nestedResource);
			return this;
		}

	}

	static class NestedResource {
		private String nestedName;
		private String nestedDescription;
		private List<NestedResource> nestedCollection = new LinkedList<>();

		public NestedResource(String nestedName, String nestedDescription) {
			super();
			this.nestedName = nestedName;
			this.nestedDescription = nestedDescription;
		}

		public String getnestedName() {
			return nestedName;
		}

		public String getNestedDescription() {
			return nestedDescription;
		}

		public List<NestedResource> getNestedCollection() {
			return nestedCollection;
		}

		public NestedResource addToNestedCollection(NestedResource nestedRsource) {
			nestedCollection.add(nestedRsource);
			return this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((nestedName == null) ? 0 : nestedName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NestedResource other = (NestedResource) obj;
			if (nestedName == null) {
				if (other.nestedName != null)
					return false;
			} else if (!nestedName.equals(other.nestedName))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "NestedResource [nestedName=" + nestedName + ", nestedDescription=" + nestedDescription + "]";
		}
	}

}
