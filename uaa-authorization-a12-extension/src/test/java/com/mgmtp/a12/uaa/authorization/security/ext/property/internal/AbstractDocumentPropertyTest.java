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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.mockito.Spy;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IDocumentIndexed;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentSerializer;
import com.mgmtp.a12.kernel.md.document.api.services.IEntityInstanceChangeInfo;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DocumentSerializerImpl;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.uaa.authorization.security.FileDocumentModelResolver;

public class AbstractDocumentPropertyTest {

	@Spy
	protected FileDocumentModelResolver insecureDocumentModelResolver = new FileDocumentModelResolver();
	private IDocumentSerializer documentSerializer = new DocumentSerializerImpl(insecureDocumentModelResolver);

	protected  IDocument createDocument() throws IOException {
		return createDocument("tk/person.json", "doc_person.json");
	}

	protected IDocument createDocument(String modelFile, String documentFile) throws IOException {
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(documentFile);
		Reader reader = new InputStreamReader(inputStream);

		DocumentSerializationConfig config = DocumentSerializationConfig.builder().format(DocumentSerializationConfig.Format.JSON).build();
		DocumentDeserializationConfig deserializationConfig = DocumentDeserializationConfig.builder().format(DocumentSerializationConfig.Format.JSON).build();

		IDocument document = documentSerializer.deserialize(reader, modelFile, deserializationConfig, new Consumer<RankedNotification>() {
			@Override
			public void accept(RankedNotification rankedNotification) {

			}
		});

		return document;
	}

	protected void printChanges(Collection<IEntityInstanceChangeInfo> changes) {
		changes.forEach(change -> {
			System.out.println("Path; [%s], change: [%s], Repetitions: [%s]".formatted(change.getPath(), change.getChangedValue(),
				Arrays.toString(change.getRepetitions())));
		});
	}

	protected IDocument removeProperty(IDocument document, Property[] properties) {
		IDocumentIndexed indexedDoc = (IDocumentIndexed) document;
		// repetitions can be null if you want to change all field instances in a repeatable group (javadoc should be helpful)
		Stream.of(properties)
			.forEach(property -> {
				Optional<IEntityInstance> entityInstance = indexedDoc.getEntityInstance(property.path, property.repetitions);
				if (entityInstance.isPresent()) {
					IFieldInstance fieldInstance = (IFieldInstance) entityInstance.get();
					fieldInstance.setValue(null); // set new value
				}
			});
		return indexedDoc;
	}

	protected void showDocument(IDocument document) {
		DocumentSerializationConfig config = DocumentSerializationConfig.builder()
			.format(DocumentSerializationConfig.Format.JSON)
			.build();
		Writer writer = new StringWriter();
		documentSerializer.serialize(document, writer, config);

		System.out.println(writer.toString());

	}

}
