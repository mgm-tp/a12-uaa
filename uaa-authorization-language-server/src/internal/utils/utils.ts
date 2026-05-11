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
import fs from "node:fs";
import path from "node:path";

import {
	JSONDocument,
	LanguageService,
	TextDocument
} from "vscode-json-languageservice";
import { integer } from "vscode-languageserver";

import { MetadataType, JavaClassMetadata } from "../interfaces/metadata.js";

import { getLanguageModelCache } from "./languageModelCache.js";
import { getMetadataClass } from "./node/java/resourceJavaCollector.js";

export const getCurrentWord = (document: TextDocument, offset: integer) => {
	let i = offset - 1;
	const text = document.getText();
	while (i >= 0 && ' \t\n\r\v":{[,]}'.indexOf(text.charAt(i)) === -1) {
		i--;
	}
	return text.substring(i + 1, offset);
};

export function getJSONDocument(
	document: TextDocument,
	languageService: LanguageService
): JSONDocument {
	const jsonDocuments = getLanguageModelCache<JSONDocument>(10, 60, document =>
		languageService.parseJSONDocument(document)
	);
	return jsonDocuments.get(document);
}

export const resolveMetadataClass = (
	normalizedPath: string
): JavaClassMetadata => {
	const resourceFileFound = fs.readFileSync(normalizedPath, {
		encoding: "utf8"
	});
	return getMetadataClass(resourceFileFound);
};

export function loadMetadata(): MetadataType | undefined {
	try {
		const metadataArg = process.argv
			.slice(2)
			.find(arg => arg.startsWith("--metadataPath"));
		if (metadataArg === undefined) {
			return undefined;
		}
		const metadataPath = path.resolve(metadataArg.substring(16));
		const metadataJson = fs.readFileSync(metadataPath, "utf8");
		return JSON.parse(metadataJson) as MetadataType;
	} catch (err) {
		console.error(`Failed to load metadata: ${err}`);
		return undefined;
	}
}
