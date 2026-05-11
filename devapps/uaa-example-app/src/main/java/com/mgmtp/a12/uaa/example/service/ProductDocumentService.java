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
package com.mgmtp.a12.uaa.example.service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.stereotype.Service;

import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;

@Service
public class ProductDocumentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductDocumentService.class);

	@Inject
	private ResourceLoader resourceLoader;
	@Inject
	private DocumentUpdateOperation documentUpdateOperation;
	@Inject
	private IDocumentV2Serializer documentV2Serializer;

	@PostFilter("hasUAAPermission('A12 document service', filterObject) && hasUAAPropertyPermission(filterObject)")
	public List<DocumentV2> loadAllProducts() {
		List<DocumentV2> documents = new LinkedList<>();
		documents.add(loadProductDocument("Product-Data.json"));
		documents.add(loadProductDocument("Product-Data1.json"));
		documents.add(loadProductDocument("Product-Data2.json"));
		documents.add(loadProductDocument("Product-Data3.json"));
		return documents;
	}

	@PreFilter("hasUAAPropertyPermission(filterObject)")
	public List<DocumentV2> preFilterProductDocuments(List<DocumentV2> documentV2s) {
		return documentV2s;
	}

	@PostAuthorize("hasUAAPropertyPermission(returnObject)")
	public DocumentV2 loadProduct() {
		return loadProductDocument("Product-Data.json");
	}

	@PreAuthorize("hasUAAPropertyPermission(#documentV2)")
	public DocumentV2 preAuthorizeProductDocument(DocumentV2 documentV2) {
		return documentV2;
	}

	public void updateProductDocument(String updatedDocumentContent) {
		DocumentV2 persistedDocument = loadProductDocument("Product-Data.json");
		DocumentV2 updatedDocument = loadDocument(new StringReader(updatedDocumentContent));
		documentUpdateOperation.updateDocuments(persistedDocument, updatedDocument);
	}

	public DocumentV2 loadProductDocument(String fileName) {
		try {
			Resource documentResource = resourceLoader.getResource("classpath:/%s".formatted(fileName));
			Reader documentReader = new InputStreamReader(documentResource.getInputStream());
			return loadDocument(documentReader);
		} catch (Exception e) {
			LOGGER.error("Load failed", e);
			throw new RuntimeException(e);
		}
	}

	private DocumentV2 loadDocument(Reader documentReader) {
		DocumentDeserializationConfig config =
			DocumentDeserializationConfig.builder().format(DocumentSerializationConfig.Format.JSON).build();
		return documentV2Serializer.deserializeV2(documentReader, "Product", config, rankedNotification -> {
		});
	}

}
