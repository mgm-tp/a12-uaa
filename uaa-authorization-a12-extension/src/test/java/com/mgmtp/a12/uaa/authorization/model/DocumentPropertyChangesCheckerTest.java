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
/*
 * (c) copyright 2012-2022 mgm technology partners GmbH
 *
 * This software, the underlying source code and other artifacts are protected by copyright.
 * All rights, in particular the right to use, reproduce, publish and edit are reserved.
 * A simple right of use (license) can be acquired for use, duplication, publication, editing etc..
 *
 * Requests for this can be made at A12-license@mgm-tp.com or other official channels of the copyright holder.
 */
package com.mgmtp.a12.uaa.authorization.model;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.javers.core.diff.custom.CustomPropertyComparator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentService;
import com.mgmtp.a12.kernel.md.document.api.services.IEntityInstanceChangeInfo;
import com.mgmtp.a12.kernel.md.facade.DocumentServiceFactory;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.property.PropertyChangePathConverter;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyChangeConverter;
import com.mgmtp.a12.uaa.authorization.property.internal.ResourceConverter;
import com.mgmtp.a12.uaa.authorization.security.PropertyChangesChecker;
import com.mgmtp.a12.uaa.authorization.security.ext.property.internal.AbstractDocumentPropertyTest;
import com.mgmtp.a12.uaa.authorization.security.ext.property.internal.DocumentComparator;
import com.mgmtp.a12.uaa.authorization.security.ext.property.internal.DocumentPropertyChangeConverter;
import com.mgmtp.a12.uaa.authorization.security.ext.property.internal.DocumentResourceConverter;
import com.mgmtp.a12.uaa.authorization.security.ext.property.internal.DocumentWrapper;
import com.mgmtp.a12.uaa.authorization.security.ext.property.internal.Property;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class DocumentPropertyChangesCheckerTest extends AbstractDocumentPropertyTest {

	@Mock
	private AuthorizationDefinitionRepository authorizationDefinitionRepository;

	@InjectMocks
	private DocumentComparator documentComparator = new DocumentComparator();
	private DocumentServiceFactory factory = new DocumentServiceFactory(insecureDocumentModelResolver);

	@Spy
	private Optional<List<ResourceConverter>> resourceConverter = Optional.of(List.of(new DocumentResourceConverter()));
	@Spy
	private Optional<List<CustomPropertyComparator>> propertyComparators = Optional.of(Arrays.asList(documentComparator));
	@Spy
	private Optional<List<PropertyChangePathConverter>> propertyChangeConverters =
		Optional.of(Arrays.asList(new DocumentPropertyChangeConverter(), new PropertyChangeConverter()));
	@InjectMocks
	private PropertyChangesChecker propertyChangesChecker = new PropertyChangesChecker(Arrays.asList("com.mgmtp"));

	@BeforeAll
	void cleanJavers() throws Exception {
		Field javersField = PropertyChangesChecker.class.getDeclaredField("JAVERS");
		javersField.setAccessible(true);
		javersField.set(null, null);
	}

	@BeforeEach
	void setUp() {
		propertyChangesChecker.initJavers();
	}

	@Test
	public void testPropertyCompareWithAllRights() throws Exception {
		IDocument documentOriginal = createDocument();
		IDocument document = createDocument();

		Property[] properties = new Property[] {
			new Property("/person/addresses/street", new int[] { 1, 2, 1 }),
			new Property("/person/job/Name", new int[] { 1, 1, 1 }),
			new Property("/person/job/companies/startDate", new int[] { 1, 2, 1, 1 }),
			new Property("/person/job/companies/name", new int[] { 1, 1, 1, 1 }),
			new Property("/person/job/companies/name", new int[] { 1, 2, 1, 1 }),
			new Property("/person/job/companies/type", new int[] { 1, 2, 1, 1 }),
			new Property("/person/job/companies/type", new int[] { 1, 3, 1, 1 })
		};
		document = removeProperty(document, properties);

		showDocument(document);
		IDocumentService docService = factory.createDocumentService(documentOriginal);
		Collection<IEntityInstanceChangeInfo> changes = docService.compare(document);
		showDocument(document);
		printChanges(changes);

		Rights rights = new Rights();
		rights.setWrite(Set.of("person.job[].Name", "person.job[].companies[].name", "person.job[].companies[].type", "person.addresses[].street",
			"person.job[].companies[].startDate"));

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));
		boolean checkPropertyPermissionForChanges = propertyChangesChecker.checkPropertyPermissionForChanges(new DocumentWrapper(documentOriginal),
			new DocumentWrapper(document), new PropertyPermission());
		Assertions.assertTrue(checkPropertyPermissionForChanges);

	}

	@Test
	public void testPropertyCompareWithSomeRights() throws Exception {
		IDocument documentOriginal = createDocument();
		IDocument document = createDocument();

		Property[] properties = new Property[] {
			new Property("/person/addresses/street", new int[] { 1, 2, 1 }),
			new Property("/person/job/Name", new int[] { 1, 1, 1 }),
			new Property("/person/job/companies/startDate", new int[] { 1, 2, 1, 1 }),
			new Property("/person/job/companies/name", new int[] { 1, 1, 1, 1 }),
			new Property("/person/job/companies/name", new int[] { 1, 2, 1, 1 }),
			new Property("/person/job/companies/type", new int[] { 1, 2, 1, 1 }),
			new Property("/person/job/companies/type", new int[] { 1, 3, 1, 1 })
		};
		document = removeProperty(document, properties);

		showDocument(document);
		IDocumentService docService = factory.createDocumentService(documentOriginal);
		Collection<IEntityInstanceChangeInfo> changes = docService.compare(document);
		showDocument(document);
		printChanges(changes);

		Rights rights = new Rights();
		rights.setWrite(Set.of("person.job[].Name", "person.job[].companies[].name", "person.addresses[].street",
			"person.job[].companies[].startDate"));

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));
		boolean checkPropertyPermissionForChanges =
			propertyChangesChecker.checkPropertyPermissionForChanges(documentOriginal, document, new PropertyPermission());
		Assertions.assertFalse(checkPropertyPermissionForChanges);
	}

	@Test
	public void testPropertyAddToCollectionWithAllRights() throws Exception {
		IDocument documentOriginal = createDocument();
		IDocument document = createDocument("tk/person.json", "doc_person_add.json");

		showDocument(document);
		IDocumentService docService = factory.createDocumentService(documentOriginal);
		Collection<IEntityInstanceChangeInfo> changes = docService.compare(document);
		showDocument(document);
		printChanges(changes);

		Rights rights = new Rights();
		rights.setWrite(Set.of("person.job[].companies[].name", "person.job[].companies[].startDate", "person.job[].companies[].type",
			"person.job[].companies[].description"));

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));
		boolean checkPropertyPermissionForChanges =
			propertyChangesChecker.checkPropertyPermissionForChanges(documentOriginal, document, new PropertyPermission());
		Assertions.assertTrue(checkPropertyPermissionForChanges);

	}

	@Test
	public void testPropertyAddToCollectionWithSomeRights() throws Exception {
		IDocument documentOriginal = createDocument();
		IDocument document = createDocument("tk/person.json", "doc_person_add.json");

		showDocument(document);
		IDocumentService docService = factory.createDocumentService(documentOriginal);
		Collection<IEntityInstanceChangeInfo> changes = docService.compare(document);
		showDocument(document);
		printChanges(changes);

		Rights rights = new Rights();
		rights.setWrite(Set.of("person.job[].companies[].startDate", "person.job[].companies[].type", "person.job[].companies[].description"));

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));
		boolean checkPropertyPermissionForChanges =
			propertyChangesChecker.checkPropertyPermissionForChanges(documentOriginal, document, new PropertyPermission());
		Assertions.assertFalse(checkPropertyPermissionForChanges);

	}

	@Test
	public void testPropertyRemoveFromCollectionWithAllRights() throws Exception {
		IDocument documentOriginal = createDocument();
		IDocument document = createDocument("tk/person.json", "doc_person_remove.json");

		showDocument(document);
		IDocumentService docService = factory.createDocumentService(documentOriginal);
		Collection<IEntityInstanceChangeInfo> changes = docService.compare(document);
		showDocument(document);
		printChanges(changes);

		Rights rights = new Rights();
		rights.setWrite(Set.of("person.job[].companies[].name", "person.job[].companies[].startDate", "person.job[].companies[].type",
			"person.job[].companies[].description"));

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));
		boolean checkPropertyPermissionForChanges =
			propertyChangesChecker.checkPropertyPermissionForChanges(documentOriginal, document, new PropertyPermission());
		Assertions.assertTrue(checkPropertyPermissionForChanges);

	}

	@Test
	public void testPropertyRemoveFromCollectionWithSomeRights() throws Exception {
		IDocument documentOriginal = createDocument();
		IDocument document = createDocument("tk/person.json", "doc_person_remove.json");

		showDocument(document);
		IDocumentService docService = factory.createDocumentService(documentOriginal);
		Collection<IEntityInstanceChangeInfo> changes = docService.compare(document);
		showDocument(document);
		printChanges(changes);

		Rights rights = new Rights();
		rights.setWrite(Set.of("person.job[].companies[].startDate", "person.job[].companies[].type", "person.job[].companies[].description"));

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));
		boolean checkPropertyPermissionForChanges =
			propertyChangesChecker.checkPropertyPermissionForChanges(documentOriginal, document, new PropertyPermission());
		Assertions.assertFalse(checkPropertyPermissionForChanges);

	}

}
