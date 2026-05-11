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
package com.mgmtp.a12.uaa.authorization.security;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;

public class FileDocumentModelResolver implements IDocumentModelResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileDocumentModelResolver.class);
	private static final DocumentModelServiceFactory documentModelServiceFactory = new DocumentModelServiceFactory();
	private static final Map<String, IDocumentModel> cache = new HashMap<>();

	@Override
	public IDocumentModel getDocumentModelById(String documentModelName) {
		LOGGER.info("Loading model[{}]", documentModelName);
		return Optional
			.ofNullable(cache.get(documentModelName))
			.orElseGet(() -> {
				try {
					IDocumentModel documentModel = ensureDocumentModel(documentModelName);
					return documentModel;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
	}

	public IDocumentModel ensureDocumentModel(String docModelPath) throws IOException {
		IDocumentModelSerializer serializer = documentModelServiceFactory.createDocumentModelSerializer();
		IDocumentModel documentModel;
		try (Reader reader = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(docModelPath), StandardCharsets.UTF_8)) {
			documentModel = serializer.deserialize(reader);
		}
		IDocumentModelService documentModelService = documentModelServiceFactory.createDocumentModelService();
		documentModelService.expand(documentModel, new FileDocumentModelReferenceResolver(StringUtils.substringBeforeLast(docModelPath, "/")));

		cache.put(documentModel.getHeader().getId(), documentModel);
		return documentModel;
	}

}
