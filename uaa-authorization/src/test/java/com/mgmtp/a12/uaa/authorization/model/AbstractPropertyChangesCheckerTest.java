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
public abstract class AbstractPropertyChangesCheckerTest<T extends TestResource> {

	@Mock
	AuthorizationDefinitionRepository authorizationDefinitionRepository;
	@Mock
	private UserDetails userDetails;
	@Spy
	private Optional<List<ResourceConverter>> resourceConverter = Optional.empty();
	@Spy
	private Optional<List<CustomPropertyComparator>> propertyComparators = Optional.empty();
	@Spy
	private Optional<List<PropertyChangePathConverter>> propertyChangeConverters = Optional.of(Arrays.asList(new PropertyChangeConverter()));
	@InjectMocks
	PropertyChangesChecker propertyChangesChecker = new PropertyChangesChecker(Arrays.asList("com.mgmtp"));
	
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
		rights.setWrite(
			Set.of("id", "taxNumber", "countryCode", "employees", "employees[].firstName", "employees[].lastName", "employees[].id", "employees[].age"));
		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));

		Company resource = new Company("name");
		resource.setId(1L);
		resource.setTaxNumber("taxNumber");
		resource.setCountryCode("ctryCode");
		resource
			.setEmployees(new LinkedList<>(Set.of(new Employee(1l, "firstName1", "lastName1", 10), new Employee(2l, "firstName2", "lastName2", 20))));

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
			.setEmployees(new LinkedList<>(Set.of(new Employee(1l, "firstName1", "lastName1", 10), new Employee(2l, "firstName2", "lastName2", 20))));

		Company resourceUpdated = new Company("name");
		resourceUpdated.setId(1L);
		resourceUpdated.setTaxNumber("taxNumber_Updated");
		resourceUpdated.setCountryCode("ctryCode");
		resourceUpdated
			.setEmployees(new LinkedList<>(Set.of(new Employee(1l, "firstName1", "lastName1", 10), new Employee(2l, "firstName2", "lastName2_Updated", 20))));

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
			.setEmployees(new LinkedList<>(Set.of(new Employee(1l, "firstName1", "lastName1", 10), new Employee(2l, "firstName2", "lastName2", 20))));

		Company resourceUpdated = new Company("name");
		resourceUpdated.setId(1L);
		resourceUpdated.setTaxNumber("taxNumber");
		resourceUpdated.setCountryCode("ctryCode");
		resourceUpdated
			.setEmployees(new LinkedList<>(Set.of(new Employee(1l, "firstName1", "lastName1", 10), new Employee(2l, "firstName2", "lastName2_Updated", 20))));

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
		TestResource resourceUpdated = addNestedToNestedObjects(createResourceUpdated(true), "nestedDescription1", "nestedDescription2Updated");

		return propertyChangesChecker.checkPropertyPermissionForChanges(resource, resourceUpdated, new PropertyPermission());
	}

	private boolean checkPermission(boolean withCollection) {
		TestResource resource = createResource(withCollection);
		TestResource resourceUpdated = createResourceUpdated(withCollection);

		return propertyChangesChecker.checkPropertyPermissionForChanges(resource, resourceUpdated, new PropertyPermission());
	}

	abstract T createResource(boolean withCollection);

	abstract T createResourceUpdated(boolean withCollection);

	abstract T addNestedToNestedObjects(T testResource, String description1, String description2);

}
