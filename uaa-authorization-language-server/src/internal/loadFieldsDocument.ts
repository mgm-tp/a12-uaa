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
import * as fs from "node:fs";
import * as path from "node:path";

import { URI } from "vscode-uri";

import {
	DocumentModel,
	DocumentServiceFactory
} from "@com.mgmtp.a12.kernel/kernel-md-facade";

import { allCompletionFields } from "../uaaAuthorizationServerMain.js";

import {
	DocumentElementReference,
	DocumentModelUtils
} from "./utils/document/document-model-utils.js";
import { loadMetadata } from "./utils/utils.js";

export const fieldsOfAllDocument: Set<string> = new Set();
export const allDocumentModels: DocumentModel[] = [];

export function preloadFieldOfAllDocument(): void {
	try {
		const metadataObject = loadMetadata();
		if (!metadataObject?.A12Models) return;
		const listUrlFiles = listAllJsonFiles(metadataObject.A12Models);

		listUrlFiles.forEach(loadAllDocumentModels);

		listUrlFiles.forEach(url => {
			getFieldsOfDocument(url).forEach(field => fieldsOfAllDocument.add(field));
		});
	} catch (err) {
		console.error(`Error during preload: ${err}`);
	}
}

function getFieldsOfDocument(filePath: string): Set<string> {
	const uri = URI.parse(filePath);
	const allFieldDocument: Set<string> = new Set();

	try {
		const content = fs.readFileSync(uri.fsPath, "utf8");
		const documentModel = deserializeDocumentModel(content);

		if (documentModel.header.modelType !== "document") {
			return new Set();
		}

		const rootGroup: DocumentElementReference<DocumentModel.Group> = {
			element: documentModel.content.modelRoot,
			path: []
		};

		const fieldElements = DocumentModelUtils.filterChildElements(
			rootGroup,
			(element): element is DocumentModel.Field => element.type === "Field",
			(element): element is DocumentModel.Group => element.name === "__meta"
		);

		fieldElements.forEach(elementReference =>
			allFieldDocument.add(
				elementReference.path.map(el => el.elementName).join(".")
			)
		);

		allCompletionFields.push({
			name: documentModel.header.id,
			completionLabels: allFieldDocument
		});
	} catch (err) {
		console.error(`Error processing file at ${filePath}: ${err}`);
	}

	return allFieldDocument;
}

function loadAllDocumentModels(filePath: string): void {
	const uri = URI.parse(filePath);

	try {
		const content = fs.readFileSync(uri.fsPath, "utf8");
		const documentModel = deserializeDocumentModel(content);

		if (documentModel.header.modelType === "document") {
			allDocumentModels.push(documentModel);
		}
	} catch (err) {
		console.error(`Error loading document model at ${filePath}: ${err}`);
	}
}

function deserializeDocumentModel(content: string): DocumentModel {
	try {
		return new DocumentServiceFactory()
			.getDocumentModelSerializer()
			.deserialize(content);
	} catch (err) {
		throw new Error(`Failed to deserialize document model: ${err}`);
	}
}

function listAllJsonFiles(absolutePath: string): string[] {
	const jsonFiles: string[] = [];

	function findJsonFiles(currentPath: string) {
		const items = fs.readdirSync(currentPath);

		for (const item of items) {
			const itemPath = path.join(currentPath, item);
			const stats = fs.statSync(itemPath);

			if (stats.isDirectory()) {
				findJsonFiles(itemPath);
			} else if (stats.isFile() && item.endsWith(".json")) {
				jsonFiles.push(itemPath);
			}
		}
	}

	findJsonFiles(absolutePath);

	return jsonFiles;
}
