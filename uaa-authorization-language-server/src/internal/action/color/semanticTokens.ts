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
import { SemanticTokensBuilder } from "vscode-languageserver/node.js";
import {
	ASTNode,
	LanguageService,
	TextDocument
} from "vscode-json-languageservice";
import { SemanticTokensParams, TextDocuments } from "vscode-languageserver";

import {
	processPropertyReferences,
	processSpELExpression
} from "./tokenProcessor.js";

export const tokenTypes = [
	"comment",
	"string",
	"keyword",
	"number",
	"regexp",
	"operator",
	"namespace",
	"type",
	"struct",
	"class",
	"interface",
	"enum",
	"typeParameter",
	"function",
	"method",
	"decorator",
	"macro",
	"variable",
	"parameter",
	"property",
	"label"
];

export const tokenModifiers = [
	"declaration",
	"documentation",
	"readonly",
	"static",
	"abstract",
	"deprecated",
	"modification",
	"async"
];

export const semanticTokenProvider = (
	documents: TextDocuments<TextDocument>,
	languageService: LanguageService,
	params: SemanticTokensParams
) => {
	const document = documents.get(params.textDocument.uri);
	if (!document) return null;

	const builder = new SemanticTokensBuilder();
	const jsonDocument = languageService.parseJSONDocument(document);

	if (jsonDocument.root) {
		semanticTextDocument(document, jsonDocument.root, builder);
	}

	return builder.build();
};

function semanticTextDocument(
	document: TextDocument,
	root: ASTNode,
	builder: SemanticTokensBuilder
): void {
	const wordsUseSpeL = ["target", "dataPreload", "rules", "policies"];
	const wordsUsePropertyRef = [
		"policy-refs",
		"repository-refs",
		"rights-refs",
		"READ",
		"WRITE",
		"MASK"
	];

	processNode(document, root, builder, wordsUseSpeL, wordsUsePropertyRef);
}

function processNode(
	document: TextDocument,
	root: ASTNode,
	builder: SemanticTokensBuilder,
	spELWords: string[],
	propertyRefWords: string[]
): void {
	if (root.type === "array" && root.parent?.type === "property") {
		const parentKeyValue = root.parent.keyNode.value;

		if (spELWords.includes(parentKeyValue)) {
			for (const child of root.children || []) {
				processSpELExpression(document, child, builder, child.value as string);
			}
		} else if (propertyRefWords.includes(parentKeyValue)) {
			for (const child of root.children || []) {
				processPropertyReferences(
					document,
					child,
					builder,
					child.value as string
				);
			}
		}
	}

	if (root.type === "property") {
		const keyValue = root.keyNode.value;

		if (spELWords.includes(keyValue) && root.valueNode) {
			processSpELExpression(
				document,
				root.valueNode,
				builder,
				root.valueNode.value as string
			);
		} else if (propertyRefWords.includes(keyValue) && root.valueNode) {
			processPropertyReferences(
				document,
				root.valueNode,
				builder,
				root.valueNode.value as string
			);
		}
	}

	for (const child of root.children || []) {
		processNode(document, child, builder, spELWords, propertyRefWords);
	}
}
