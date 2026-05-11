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
import {
	CompletionItemKind,
	TextDocumentPositionParams,
	TextDocuments
} from "vscode-languageserver";
import {
	JSONDocument,
	LanguageService,
	Position,
	TextDocument
} from "vscode-json-languageservice";
import { CompletionList } from "vscode-languageserver-types";

import { getCurrentWord } from "../../utils/utils.js";
import { ContextValidator } from "../../utils/node/json/contextValidators.js";
import { getKeyNamesOfNode } from "../../utils/node/json/nodeSearch.js";
import { AUTHORIZATION_TYPE } from "../../constants/authorization.js";
import { NodeData } from "../../interfaces/nodeJson.js";
import { allCompletionFields } from "../../../uaaAuthorizationServerMain.js";

import { functionProposals, locatePosition } from "./uaaFunction.js";
import { resolveCompletionItemsBasedOnMetaData } from "./uaaAutoSuggestion.js";

export const wordsUseFunctionProposals = [
	"target",
	"dataPreload",
	"rules",
	"policies"
];
const wordsUsePropertyProposals = ["READ", "WRITE"];

export const onCompletionHandler =
	(documents: TextDocuments<TextDocument>, languageService: LanguageService) =>
	async (
		textDocumentPosition: TextDocumentPositionParams
	): Promise<CompletionList> => {
		const document = documents.get(textDocumentPosition.textDocument.uri);
		if (!document) return CompletionList.create();

		const jsonDocument = languageService.parseJSONDocument(document);
		const offset = document.offsetAt(textDocumentPosition.position);
		const node = jsonDocument.getNodeFromOffset(offset, true);

		// Get schema-based completions and clean up insertText and textEdit
		const schemaCompletions = await getSchemaBasedCompletions(
			document,
			textDocumentPosition.position,
			jsonDocument,
			languageService
		);
		if (schemaCompletions.items.length > 0) {
			return schemaCompletions;
		}

		// Return early if inside a key context
		if (
			await isPositionInKey(
				document,
				jsonDocument,
				offset,
				textDocumentPosition.position
			)
		) {
			return CompletionList.create();
		}

		const currentWord = getCurrentWord(document, offset);
		const additionalItems = resolveCompletionItemsBasedOnMetaData(
			currentWord,
			textDocumentPosition.position
		);
		if (additionalItems.length > 0) {
			return CompletionList.create(additionalItems);
		}

		if (node?.parent) {
			const nodeData = getKeyNamesOfNode(node);
			return getPropertyCompletions(
				nodeData,
				document,
				currentWord,
				textDocumentPosition.position
			);
		}

		return CompletionList.create();
	};

async function getSchemaBasedCompletions(
	document: TextDocument,
	position: Position,
	jsonDocument: JSONDocument,
	languageService: LanguageService
): Promise<CompletionList> {
	const completions = await languageService.doComplete(
		document,
		position,
		jsonDocument
	);
	return CompletionList.create(
		completions?.items?.map(item => ({
			...item,
			insertText: item.insertText?.replace(/\\}/g, "}"),
			textEdit: item.textEdit
				? {
					...item.textEdit,
					newText: item.textEdit.newText.replace(/\\}/g, "}")
				}
				: undefined
		})) || []
	);
}

async function isPositionInKey(
	document: TextDocument,
	jsonDocument: JSONDocument,
	offset: number,
	position: Position
): Promise<boolean> {
	const node = jsonDocument.getNodeFromOffset(offset, true);
	if (!node) return false;

	const textBeforeCursor = document
		.getText({ start: { line: position.line, character: 0 }, end: position })
		.trim();
	if (textBeforeCursor.length === 0) return true;

	const parent = node.parent;
	if (parent?.type === "array") return false;
	if (parent?.type === "property") {
		return parent.keyNode === node;
	}

	const textAfterCursor = document.getText().slice(offset).trim();
	return !textAfterCursor.startsWith(":");
}

function getPropertyCompletions(
	nodeData: NodeData | null,
	document: TextDocument,
	currentWord: string,
	position: Position
): CompletionList {
	if (!nodeData) return CompletionList.create();

	if (ContextValidator.isInPolicyRefsContext(nodeData.keys[0])) {
		return provideCompletionFromDocument(
			document,
			AUTHORIZATION_TYPE.POLICES,
			"Policy reference",
			CompletionItemKind.Reference,
			position
		);
	}
	if (ContextValidator.isInRepositoryRefsContext(nodeData.keys[0])) {
		return provideCompletionFromDocument(
			document,
			AUTHORIZATION_TYPE.REPOSITORY_POLICIES,
			"Repository reference",
			CompletionItemKind.Reference,
			position
		);
	}
	if (ContextValidator.isInRightsRefsContext(nodeData.keys[0])) {
		return provideCompletionFromDocument(
			document,
			AUTHORIZATION_TYPE.PROPERTY_RIGHTS,
			"Property Right reference",
			CompletionItemKind.Reference,
			position
		);
	}
	if (wordsUseFunctionProposals.includes(nodeData.keys[0])) {
		return CompletionList.create([
			...locatePosition(functionProposals, currentWord, position)
		]);
	}
	if (wordsUsePropertyProposals.includes(nodeData.keys[0])) {
		return CompletionList.create(
			allCompletionFields.map(resourceField => {
				return {
					label: resourceField.name + ".*",
					textEdit: {
						newText: Array.from(resourceField.completionLabels)
						              .map((completion, index) =>
										  index === resourceField.completionLabels.size - 1
											  ? `"${completion}`
											  : index === 0
												  ? `${completion}",`
												  : `"${completion}",`
									  )
						              .join("\n"),
						range: {
							start: position,
							end: position
						}
					}
				};
			})
		);
	}
	return CompletionList.create();
}

function provideCompletionFromDocument(
	document: TextDocument,
	jsonKey: string,
	detail: string,
	kind: CompletionItemKind,
	position: Position
): CompletionList {
	const names = extractNamesFromDocument(document, jsonKey);
	return CompletionList.create(
		names.map(name => ({
			label: name,
			kind: kind,
			detail: detail,
			textEdit: {
				newText: name,
				range: {
					start: position,
					end: position
				}
			}
		}))
	);
}

function extractNamesFromDocument(
	document: TextDocument,
	jsonKey: string
): string[] {
	try {
		const text = document.getText();
		const json = JSON.parse(text);
		if (json && Array.isArray(json[jsonKey])) {
			return json[jsonKey].map((item: any) => item.name);
		}
	} catch (error) {
		console.error("Error parsing document:", error);
	}
	return [];
}
