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
package com.mgmtp.a12.uaa.authorization.security.ext.internal;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.test.util.ReflectionTestUtils;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.api.IGroupInstance;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentSearchService;
import com.mgmtp.a12.kernel.md.facade.DocumentServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;

@ExtendWith(MockitoExtension.class)
public class A12DocumentPropertyAccessorTest {

	@InjectMocks
	private A12DocumentPropertyAccessor a12DocumentPropertyAccessor;
	@Mock
	private IDocumentModelResolver documentModelResolver;
	@Mock
	private EvaluationContext evaluationContext;
	@Mock
	private IDocument iDocument;
	@Mock
	private IGroupInstance iGroupInstance;
	@Mock
	private IDocumentSearchService documentSearchService;
	@Mock
	private IEntityInstance iEntityInstance;
	@Mock
	private IFieldInstance iFieldInstance;
	@Mock
	private DocumentServiceFactory documentServiceFactory;

	private static final String SEARCH_SERVICE = "___searchService";

	@BeforeEach
	public void setUp() {
	}

	@Test
	public void init() {
		ReflectionTestUtils.setField(a12DocumentPropertyAccessor, "documentModelResolver", documentModelResolver);
		ReflectionTestUtils.invokeMethod(a12DocumentPropertyAccessor, "init");
	}

	@Test
	public void getSpecificTargetClassesWillReturnNull() {
		Assertions.assertNull(a12DocumentPropertyAccessor.getSpecificTargetClasses());
	}

	@Test
	public void canReadReturnFalseWhenTargetIsNotIDocumentOrGroupInstanceImpl() throws Exception {
		Assertions.assertFalse(a12DocumentPropertyAccessor.canRead(evaluationContext, new Object(), "name"));
	}

	@Test
	public void canReadReturnFalseWhenTargetIsIDocumentAndIEntityInstanceSize0() throws Exception {
		Set<IEntityInstance> set = new HashSet<>();
		Mockito.when(documentSearchService.get(Mockito.anyString(), Mockito.any())).thenReturn(set);
		Mockito.when(documentServiceFactory.createDocumentSearchService(Mockito.any())).thenReturn(documentSearchService);

		boolean result = a12DocumentPropertyAccessor.canRead(evaluationContext, iDocument, "name");
		Assertions.assertFalse(result);
	}

	@Test
	public void canReadReturnTrueWhenTargetIsIDocumentAndIEntityInstanceSize1() throws Exception {
		Set<IEntityInstance> set = new HashSet<>();
		set.add(iEntityInstance);
		Mockito.when(documentSearchService.get(Mockito.anyString(), Mockito.any())).thenReturn(set);
		Mockito.when(documentServiceFactory.createDocumentSearchService(Mockito.any())).thenReturn(documentSearchService);
		EvaluationContext context = new StandardEvaluationContext();

		boolean result = a12DocumentPropertyAccessor.canRead(context, iDocument, "name");
		Assertions.assertEquals(documentSearchService, context.lookupVariable(SEARCH_SERVICE));
		Assertions.assertTrue(result);
	}

	@Test
	public void canReadReturnFalseWhenTargetIsIDocumentAndIEntityInstanceSize2() throws Exception {
		Set<IEntityInstance> set = new HashSet<>();
		set.add(iEntityInstance);
		set.add(iEntityInstance.getClass().getDeclaredConstructor().newInstance());
		Mockito.when(documentSearchService.get(Mockito.anyString(), Mockito.any())).thenReturn(set);
		Mockito.when(documentServiceFactory.createDocumentSearchService(Mockito.any())).thenReturn(documentSearchService);

		boolean result = a12DocumentPropertyAccessor.canRead(evaluationContext, iDocument, "name");
		Assertions.assertFalse(result);
	}

	@Test
	public void canReadReturnFalseWhenChildIsNotPresent() throws Exception {
		Set<IEntityInstance> children = new HashSet<>();
		Mockito.when(documentSearchService.getChildren(Mockito.anyString(), Mockito.any())).thenReturn(children);
		Mockito.when(evaluationContext.lookupVariable(Mockito.anyString())).thenReturn(documentSearchService);
		Mockito.when(iGroupInstance.getPath()).thenReturn("path");

		boolean result = a12DocumentPropertyAccessor.canRead(evaluationContext, iGroupInstance, "name");
		Assertions.assertFalse(result);
	}

	@Test
	public void canReadReturnTrueWhenChildIsPresent() throws Exception {
		Set<IEntityInstance> children = new HashSet<>();
		Mockito.when(iEntityInstance.getPath()).thenReturn("path/name");
		children.add(iEntityInstance);
		Mockito.when(documentSearchService.getChildren(Mockito.anyString(), Mockito.any())).thenReturn(children);
		Mockito.when(evaluationContext.lookupVariable(Mockito.anyString())).thenReturn(documentSearchService);
		Mockito.when(iGroupInstance.getPath()).thenReturn("path");

		boolean result = a12DocumentPropertyAccessor.canRead(evaluationContext, iGroupInstance, "name");
		Assertions.assertTrue(result);
	}

	@Test
	public void readReturnNullWhenTargetIsNotIDocumentOrGroupInstanceImpl() throws Exception {
		TypedValue result = a12DocumentPropertyAccessor.read(evaluationContext, new Object(), "name");
		Assertions.assertNull(result);
	}

	@Test
	public void readReturnTypedValueWhenTargetIsIDocument() throws Exception {
		Set<IEntityInstance> set = new HashSet<>();
		set.add(iEntityInstance);
		Mockito.when(documentSearchService.get(Mockito.anyString(), Mockito.any())).thenReturn(set);
		Mockito.when(documentServiceFactory.createDocumentSearchService(Mockito.any())).thenReturn(documentSearchService);

		TypedValue result = a12DocumentPropertyAccessor.read(evaluationContext, iDocument, "name");
		Assertions.assertEquals(iEntityInstance, result.getValue());
	}

	@Test
	public void readReturnIEntityInstanceWhenTargetIsGroupInstanceImpl() throws Exception {
		Set<IEntityInstance> children = new HashSet<>();
		Mockito.when(iEntityInstance.getPath()).thenReturn("path/name");
		children.add(iEntityInstance);
		Mockito.when(documentSearchService.getChildren(Mockito.anyString(), Mockito.any())).thenReturn(children);
		Mockito.when(evaluationContext.lookupVariable(Mockito.anyString())).thenReturn(documentSearchService);
		Mockito.when(iGroupInstance.getPath()).thenReturn("path");

		TypedValue result = a12DocumentPropertyAccessor.read(evaluationContext, iGroupInstance, "name");
		Assertions.assertEquals(iEntityInstance, result.getValue());
	}

	@Test
	public void readReturnIFieldInstanceWhenTargetIsGroupInstanceImpl() throws Exception {
		Set<IEntityInstance> children = new HashSet<>();
		Mockito.when(iFieldInstance.getValue()).thenReturn(Optional.of("value"));
		Mockito.when(iFieldInstance.getPath()).thenReturn("path/name");
		children.add(iFieldInstance);
		Mockito.when(documentSearchService.getChildren(Mockito.anyString(), Mockito.any())).thenReturn(children);
		Mockito.when(evaluationContext.lookupVariable(Mockito.anyString())).thenReturn(documentSearchService);
		Mockito.when(iGroupInstance.getPath()).thenReturn("path");

		TypedValue result = a12DocumentPropertyAccessor.read(evaluationContext, iGroupInstance, "name");
		Assertions.assertEquals("value", result.getValue());
	}

	@Test
	public void readReturnNullWhenTargetIsGroupInstanceImpl() throws Exception {
		Set<IEntityInstance> children = new HashSet<>();
		Mockito.when(iFieldInstance.getValue()).thenReturn(Optional.empty());
		Mockito.when(iFieldInstance.getPath()).thenReturn("path/name");
		children.add(iFieldInstance);
		Mockito.when(documentSearchService.getChildren(Mockito.anyString(), Mockito.any())).thenReturn(children);
		Mockito.when(evaluationContext.lookupVariable(Mockito.anyString())).thenReturn(documentSearchService);
		Mockito.when(iGroupInstance.getPath()).thenReturn("path");

		TypedValue result = a12DocumentPropertyAccessor.read(evaluationContext, iGroupInstance, "name");
		Assertions.assertNull(result.getValue());
	}

	@Test
	public void canWriteWillReturnFalse() throws Exception {
		Assertions.assertFalse(a12DocumentPropertyAccessor.canWrite(null, null, null));
	}

}