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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IDocumentIndexed;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.internal.UAAUserDetails;
import com.mgmtp.a12.uaa.authorization.property.internal.AccessiblePropertiesFactory;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyTreeRoot;
import com.mgmtp.a12.uaa.authorization.security.ext.property.internal.AbstractDocumentPropertyTest;
import com.mgmtp.a12.uaa.authorization.security.ext.property.internal.DocumentPropertyValue;
import com.mgmtp.a12.uaa.authorization.security.ext.property.internal.DocumentPropertyValueResolver;
import com.mgmtp.a12.uaa.authorization.security.ext.property.internal.DocumentUAADataMasking;
import com.mgmtp.a12.uaa.authorization.security.ext.property.internal.IntrospectionVisitor;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DocumentUAADataMaskingTest extends AbstractDocumentPropertyTest {

	private DocumentUAADataMasking documentUAADataMasking;

	@Mock
	private SecurityContext securityContext;
	@Mock
	private Authentication authentication;
	@Mock
	private UAAUserDetails uaaUserDetails;

	@Mock
	private AuthorizationDefinitionRepository authorizationDefinitionRepository;

	private DocumentPropertyValueResolver documentPropertyValueResolver = new DocumentPropertyValueResolver(insecureDocumentModelResolver);

	@BeforeEach
	void setUp() {
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(authentication.getPrincipal()).thenReturn(uaaUserDetails);
		Mockito.when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);

		documentUAADataMasking = new DocumentUAADataMasking(authorizationDefinitionRepository, insecureDocumentModelResolver);
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void checkSimpleProperties() throws Exception {
		Set<String> accessibleProperties = Set.of("person.id", "person.firstName");
		Set<Rights> rights = new HashSet<>();
		Rights right = new Rights();
		right.setRead(accessibleProperties);
		right.setWrite(Set.of("person.firstName"));
		rights.add(right);

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(rights);

		IDocument document = createDocument();

		PropertyPermission propertyPermission = new PropertyPermission();
		propertyPermission.setName("simple");
		propertyPermission.setRightsRef(Set.of("test"));

		IDocument result = documentUAADataMasking.maskData(document, propertyPermission);
		showDocument(result);

		assertProperties(accessibleProperties, document);

	}

	@Test
	public void checkCollectionProperties() throws Exception {
		Set<String> accessibleProperties = Set.of("person.id", "person.firstName", "person.addresses[].firstName");
		Set<Rights> rights = new HashSet<>();
		Rights right = new Rights();
		right.setRead(accessibleProperties);
		right.setWrite(Set.of("person.firstName"));
		rights.add(right);

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(rights);

		IDocument document = createDocument();

		PropertyPermission propertyPermission = new PropertyPermission();
		propertyPermission.setName("checkCollection");
		propertyPermission.setRightsRef(Set.of("test"));

		showDocument(document);
		IDocument result = documentUAADataMasking.maskData(document, propertyPermission);
		showDocument(result);

		assertProperties(accessibleProperties, document);
	}

	@Test
	public void checkDeepCollectionProperties() throws Exception {
		Set<String> accessibleProperties = Set.of("person.id", "person.firstName", "person.addresses[].firstName", "person.job[].companies[].name",
			"person.job[].companies[].description", "person.job[].companies[].type", "person.job[].Name");
		Set<Rights> rights = new HashSet<>();
		Rights right = new Rights();
		right.setRead(accessibleProperties);
		right.setWrite(Set.of("person.firstName"));
		rights.add(right);

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(rights);

		IDocument document = createDocument();

		PropertyPermission propertyPermission = new PropertyPermission();
		propertyPermission.setName("checkDeepCollection");
		propertyPermission.setRightsRef(Set.of("test"));

		IDocument result = documentUAADataMasking.maskData(document, propertyPermission);
		showDocument(result);

		assertProperties(accessibleProperties, document);
	}

	private void assertProperties(Set<String> accessibleProperties, IDocument document) {
		IDocumentModel documentModel = insecureDocumentModelResolver.getDocumentModelById(document.getDocumentModelId());
		DocumentModelWalker walker = new DocumentModelWalker();
		IntrospectionVisitor introspectionVisitor = new IntrospectionVisitor();
		walker.acceptDocumentModel(documentModel, introspectionVisitor);
		List<String> allProperties = introspectionVisitor.getPaths();
		PropertyTreeRoot allPropertiesRoot = AccessiblePropertiesFactory.createPropertyPermissions(allProperties, Collections.emptyList());

		List<DocumentPropertyValue> allValues = documentPropertyValueResolver.findAllValues((IDocumentIndexed) document, allPropertiesRoot);
		allValues.stream()
			.filter(propertyValue -> propertyValue.getField().isPresent())
			.filter(propertyValue -> accessibleProperties.contains(propertyValue.getPropertyPath()))
			.forEach(propertyValue -> {
				Optional<Object> value = propertyValue.getField().get().getValue();
				Assertions.assertTrue(value.isPresent(), "Value must be presented for path: " +
					propertyValue.getPropertyPath() + ", repetitions: " + propertyValue.getRepetitions());
			});

		allValues.stream()
			.filter(propertyValue -> propertyValue.getField().isPresent())
			.filter(propertyValue -> !accessibleProperties.contains(propertyValue.getPropertyPath()))
			.forEach(propertyValue -> {
				Optional<Object> value = propertyValue.getField().get().getValue();
				Assertions.assertTrue(value.isEmpty(), "Value must be empty for path: " +
					propertyValue.getPropertyPath() + ", repetitions: " + propertyValue.getRepetitions());
			});

	}

}
