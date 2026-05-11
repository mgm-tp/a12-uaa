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
package com.mgmtp.a12.uaa.example.controller;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.uaa.example.service.ProductDocumentService;

@RestController
public class ProductController {

	@Inject
	private ProductDocumentService productDocumentService;
	@Inject
	private IDocumentV2Serializer documentV2Serializer;

	@PreAuthorize("hasUAAPermission('A12 document')")
	@GetMapping(value = "/loadProductDocuments", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String loadProductDocuments() {
		DocumentSerializationConfig config = DocumentSerializationConfig.builder()
			.format(DocumentSerializationConfig.Format.JSON)
			.build();
		String output = productDocumentService.loadAllProducts().stream()
			.map(product -> {
				Writer writer = new StringWriter();
				documentV2Serializer.serializeV2(product, writer, config);
				return writer.toString();
			})
			.collect(Collectors.joining(",\n"));
		return "[" + output + "]";
	}

	@PreAuthorize("hasUAAPermission('A12 document')")
	@GetMapping(value = "/preFilterProductDocuments", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String preFilterProductDocuments() {
		List<DocumentV2> documentV2s = new ArrayList<>(List.of(productDocumentService.loadProductDocument("Product-Data.json"),
			productDocumentService.loadProductDocument("Product-Data1.json")));
		DocumentSerializationConfig config = DocumentSerializationConfig.builder()
			.format(DocumentSerializationConfig.Format.JSON)
			.build();
		String output = productDocumentService.preFilterProductDocuments(documentV2s).stream()
			.map(product -> {
				Writer writer = new StringWriter();
				documentV2Serializer.serializeV2(product, writer, config);
				return writer.toString();
			})
			.collect(Collectors.joining(",\n"));
		return "[" + output + "]";
	}

	@PreAuthorize("hasUAAPermission('A12 document')")
	@GetMapping(value = "/loadProductDocument", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String loadProductDocument() {
		DocumentSerializationConfig config = DocumentSerializationConfig.builder()
			.format(DocumentSerializationConfig.Format.JSON)
			.build();
		Writer writer = new StringWriter();
		documentV2Serializer.serializeV2(productDocumentService.loadProduct(), writer, config);
		return writer.toString();
	}

	@PreAuthorize("hasUAAPermission('A12 document')")
	@GetMapping(value = "/preAuthorizeProductDocument", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String preAuthorizeProductDocument() {
		DocumentSerializationConfig config = DocumentSerializationConfig.builder()
			.format(DocumentSerializationConfig.Format.JSON)
			.build();
		Writer writer = new StringWriter();
		documentV2Serializer.serializeV2(productDocumentService.preAuthorizeProductDocument(productDocumentService.loadProductDocument("Product-Data.json")),
			writer, config);
		return writer.toString();
	}

	@PreAuthorize("hasUAAPermission('A12 document')")
	@PostMapping(value = "/updateProductDocument", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String updateProductDocument(@RequestBody String updatedDocument) {
		productDocumentService.updateProductDocument(updatedDocument);
		return "OK";
	}
}
