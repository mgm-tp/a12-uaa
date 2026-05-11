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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;

public class IntrospectionVisitorTest extends AbstractDocumentPropertyTest {

	private static final List<String> ALL_PROPERTIES_PERSON = Arrays.asList("person.id",
		"person.firstName",
		"person.sureName",
		"person.age",
		"person.addresses[].firstName",
		"person.addresses[].sureName",
		"person.addresses[].street",
		"person.addresses[].streetNumber",
		"person.addresses[].zipCode",
		"person.primaryAddress.firstName",
		"person.primaryAddress.sureName",
		"person.primaryAddress.street",
		"person.primaryAddress.streetNumber",
		"person.primaryAddress.zipCode",
		"person.job[].companies[].name",
		"person.job[].companies[].startDate",
		"person.job[].companies[].description",
		"person.job[].companies[].type",
		"person.job[].Name");
	
	private static final List<String> ALL_PROPERTIES_PERSON_WITH_RULES = Arrays.asList("Person.PersonalData.FirstName",
		"Person.PersonalData.LastName",
		"Person.PersonalData.Gender",
		"Person.PersonalData.DateOfBirth",
		"Person.PersonalData.PlaceOfBirth",
		"Person.PersonalData.Nationality",
		"Person.PersonalData.Email",
		"Person.PersonalData.Photo.original_filename",
		"Person.PersonalData.Photo.internal_filename",
		"Person.PersonalData.Photo.content",
		"Person.PersonalData.Photo.attachment_id",
		"Person.PersonalData.Photo.size",
		"Person.PersonalData.Photo.mime_type",
		"Person.PersonalData.Photo.category",
		"Person.PersonalData.Photo.description",
		"Person.Addresses[].Street",
		"Person.Addresses[].City",
		"Person.Addresses[].Country",
		"Person.Addresses[].PostCode",
		"Person.Phones[].PhoneNumber",
		"Person.Phones[].Type"
);

	@Test
	public void testVisitorWithoutRules() throws Exception {
		IDocument document = createDocument();

		assertProperties(document, ALL_PROPERTIES_PERSON);
	}
	
	@Test
	public void testVisitorWithRules() throws Exception {
		IDocument document = createDocument("tk/person_with_rules.json", "doc_person_with_rules.json");
		assertProperties(document, ALL_PROPERTIES_PERSON_WITH_RULES);
	}
	
	private void assertProperties(IDocument document, List<String> properties) {
		IDocumentModel documentModel = insecureDocumentModelResolver.getDocumentModelById(document.getDocumentModelId());
		DocumentModelWalker walker = new DocumentModelWalker();
		IntrospectionVisitor introspectionVisitor = new IntrospectionVisitor();
		walker.acceptDocumentModel(documentModel, introspectionVisitor);
		List<String> allProperties = introspectionVisitor.getPaths();
		Collection<String> disjunction = CollectionUtils.disjunction(properties, allProperties);
		Assertions.assertTrue(disjunction.isEmpty());

	}
}
