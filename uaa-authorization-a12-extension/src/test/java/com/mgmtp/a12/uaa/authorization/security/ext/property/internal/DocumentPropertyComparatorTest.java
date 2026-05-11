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
package com.mgmtp.a12.uaa.authorization.security.ext.property.internal;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.javers.core.diff.changetype.PropertyChangeMetadata;
import org.javers.core.diff.changetype.PropertyChangeType;
import org.javers.core.metamodel.object.UnboundedValueObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentService;
import com.mgmtp.a12.kernel.md.document.api.services.IEntityInstanceChangeInfo;
import com.mgmtp.a12.kernel.md.facade.DocumentServiceFactory;

@ExtendWith(MockitoExtension.class)
public class DocumentPropertyComparatorTest extends AbstractDocumentPropertyTest {

	private DocumentServiceFactory factory = new DocumentServiceFactory(insecureDocumentModelResolver);
	@InjectMocks
	private DocumentComparator documentComparator = new DocumentComparator();

	private DocumentPropertyChangeConverter documentConverter = new DocumentPropertyChangeConverter();

	@Test
	public void testPropertyChange() throws Exception {
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

		PropertyChangeMetadata metadata =
			new PropertyChangeMetadata(new UnboundedValueObjectId("property"), "property", Optional.empty(), PropertyChangeType.PROPERTY_VALUE_CHANGED);
		DocumentChanges documentChanges = documentComparator.compare(documentOriginal, document, metadata, null).get();
		Assertions.assertEquals("person.job/1/companies/1.name", documentChanges.getPropertyChanges().get(0).getPropertyNameWithPath());
		Assertions.assertEquals("person.job/1.Name", documentChanges.getPropertyChanges().get(1).getPropertyNameWithPath());
		Assertions.assertEquals("person.job/2/companies/1.name", documentChanges.getPropertyChanges().get(2).getPropertyNameWithPath());
		Assertions.assertEquals("person.job/2/companies/1.type", documentChanges.getPropertyChanges().get(3).getPropertyNameWithPath());
		Assertions.assertEquals("person.job/2/companies/1.startDate", documentChanges.getPropertyChanges().get(4).getPropertyNameWithPath());
		Assertions.assertEquals("person.addresses/2.street", documentChanges.getPropertyChanges().get(5).getPropertyNameWithPath());

		List<String> convertedPaths = documentConverter.convertPropertyPath(documentChanges);
		Assertions.assertEquals("person.job/1/companies/1.name", convertedPaths.get(0));
		Assertions.assertEquals("person.job/1.Name", convertedPaths.get(1));
		Assertions.assertEquals("person.job/2/companies/1.name", convertedPaths.get(2));
		Assertions.assertEquals("person.job/2/companies/1.type", convertedPaths.get(3));
		Assertions.assertEquals("person.job/2/companies/1.startDate", convertedPaths.get(4));
		Assertions.assertEquals("person.addresses/2.street", convertedPaths.get(5));

	}

	@Test
	public void testPropertyAddToCollection() throws Exception {
		IDocument documentOriginal = createDocument();
		IDocument document = createDocument("tk/person.json", "doc_person_add.json");

		showDocument(document);
		IDocumentService docService = factory.createDocumentService(documentOriginal);
		Collection<IEntityInstanceChangeInfo> changes = docService.compare(document);
		showDocument(document);
		printChanges(changes);

		PropertyChangeMetadata metadata =
			new PropertyChangeMetadata(new UnboundedValueObjectId("property"), "property", Optional.empty(), PropertyChangeType.PROPERTY_VALUE_CHANGED);
		DocumentChanges documentChanges = documentComparator.compare(documentOriginal, document, metadata, null).get();

		Assertions.assertEquals("person.job/1/companies/3.name", documentChanges.getPropertyChanges().get(0).getPropertyNameWithPath());
		Assertions.assertEquals("person.job/1/companies/3.description", documentChanges.getPropertyChanges().get(1).getPropertyNameWithPath());
		Assertions.assertEquals("person.job/1/companies/3.type", documentChanges.getPropertyChanges().get(2).getPropertyNameWithPath());
		Assertions.assertEquals("person.job/1/companies/3.startDate", documentChanges.getPropertyChanges().get(3).getPropertyNameWithPath());

		List<String> convertedPaths = documentConverter.convertPropertyPath(documentChanges);
		Assertions.assertEquals("person.job/1/companies/3.name", convertedPaths.get(0));
		Assertions.assertEquals("person.job/1/companies/3.description", convertedPaths.get(1));
		Assertions.assertEquals("person.job/1/companies/3.type", convertedPaths.get(2));
		Assertions.assertEquals("person.job/1/companies/3.startDate", convertedPaths.get(3));

	}

}
