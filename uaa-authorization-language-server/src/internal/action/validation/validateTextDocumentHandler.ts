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
	ASTNode,
	LanguageService,
	TextDocument
} from "vscode-json-languageservice";
import {
	Connection,
	Diagnostic,
	DiagnosticSeverity,
	Range,
	TextDocuments
} from "vscode-languageserver";
import { DocumentLanguageSettings } from "vscode-json-languageservice/lib/umd/jsonLanguageTypes.js";

import { AUTHORIZATION_TYPE } from "../../constants/authorization.js";
import {
	propertyReferenceGrammarCheck,
	springExpressionGrammarCheck
} from "../../antlr4/grammarCheck.js";
import { parseComplexExpressions } from "../../utils/node/json/expressions.js";

import { PropertyCheck } from "./propertyCheck.js";

/**
 * @param documents
 * @param textDocument
 * @param languageService
 * @param connection
 */
export async function validateTextDocumentHandler(
	documents: TextDocuments<TextDocument>,
	textDocument: TextDocument,
	languageService: LanguageService,
	connection: Connection
): Promise<void> {
	const document = documents.get(textDocument.uri);

	const jsonDocument = languageService.parseJSONDocument(textDocument);
	const diagnostics: Diagnostic[] = [];

	if (!document) {
		return;
	}

	// Perform validation using the configured language service
	const documentSettings: DocumentLanguageSettings = {
		schemaValidation: "error",
		schemaRequest: "error"
	};
	const validationResult = await languageService.doValidation(
		textDocument,
		jsonDocument,
		documentSettings
	);

	const jsonDocumentRoot = languageService.parseJSONDocument(document);
	if (jsonDocumentRoot.root) {
		validationResult.push(
			...validateSameNameProvider(document, jsonDocumentRoot.root)
		);
		validationResult.push(
			...antlr4ValidationProvider(document, jsonDocumentRoot.root)
		);
		validationResult.push(
			...validateObjectNotUsageProvider(document, jsonDocumentRoot.root)
		);
	}

	validationResult.forEach(error => {
		diagnostics.push({
			severity: error.severity,
			range: {
				start: {
					line: error.range.start.line,
					character: error.range.start.character
				},
				end: {
					line: error.range.end.line,
					character: error.range.end.character
				}
			},
			message: typeof error.message === 'string' ? error.message : error.message.value,
			source: "json"
		});
	});

	// Send diagnostics to the client
	connection.sendDiagnostics({ uri: textDocument.uri, diagnostics });
}

function antlr4ValidationProvider(
	document: TextDocument,
	root: ASTNode
): Diagnostic[] {
	if (document.getText().length === 0) {
		return []; // ignore empty documents
	}
	const wordsUseSpeL = ["target", "dataPreload", "rules", "policies"];
	const wordsUsePropertyRef = ["policy-refs", "repository-refs", "rights-refs"];

	return [
		...validateSpringAndPropertyRefInValue(
			document,
			root,
			wordsUseSpeL,
			springExpressionGrammarCheck
		),
		...validateSpringAndPropertyRefInValue(
			document,
			root,
			wordsUsePropertyRef,
			propertyReferenceGrammarCheck
		)
	].map(diagnostic => {
		return {
			...diagnostic,
			severity: DiagnosticSeverity.Warning
		};
	});
}

function validateSameNameProvider(
	document: TextDocument,
	root: ASTNode
): Diagnostic[] {
	const diagnostics: Diagnostic[] = [];
	diagnostics.push(
		...(validateUniqueNamesInProperty(
			document,
			root,
			AUTHORIZATION_TYPE.POLICES
		) || [])
	);
	diagnostics.push(
		...(validateUniqueNamesInProperty(
			document,
			root,
			AUTHORIZATION_TYPE.PERMISSIONS
		) || [])
	);
	diagnostics.push(
		...(validateUniqueNamesInProperty(
			document,
			root,
			AUTHORIZATION_TYPE.REPOSITORY_POLICIES
		) || [])
	);
	diagnostics.push(
		...(validateUniqueNamesInProperty(
			document,
			root,
			AUTHORIZATION_TYPE.PROPERTY_PERMISSIONS
		) || [])
	);
	diagnostics.push(
		...(validateUniqueNamesInProperty(
			document,
			root,
			AUTHORIZATION_TYPE.PROPERTY_RIGHTS
		) || [])
	);
	return diagnostics;
}

function validateObjectNotUsageProvider(document: TextDocument, root: ASTNode) {
	const listPropertyCheck: {
		mainProperty: PropertyCheck[];
		refProperty: PropertyCheck[];
	} = {
		mainProperty: [],
		refProperty: []
	};
	const diagnostics: Diagnostic[] = [];
	validateObjectNotUsage(document, root, listPropertyCheck);
	listPropertyCheck.mainProperty.map(property => {
		if (!property.getIsUsed()) {
			diagnostics.push({
				severity: 2,
				range: property.getRange(),
				message: "This property is unused"
			});
		}
	});

	listPropertyCheck.refProperty.map(property => {
		if (!property.getIsUsed()) {
			diagnostics.push({
				severity: 2,
				range: property.getRange(),
				message: "Property of this reference name cannot be found"
			});
		}
	});

	return diagnostics;
}

function validateObjectNotUsage(
	document: TextDocument,
	root: ASTNode,
	listPropertyCheck: {
		mainProperty: PropertyCheck[];
		refProperty: PropertyCheck[];
	}
): void {
	const propertyUsedByRef = [
		"policies",
		"repositoryPolicies",
		"propertyRights"
	];
	const propertyRefs = ["policy-refs", "repository-refs", "rights-refs"];

	// Check for main properties
	if (
		root.type === "property" &&
		propertyUsedByRef.includes(root.keyNode.value) &&
		root.valueNode
	) {
		root.valueNode.children?.forEach(child => {
			child.children?.forEach(n => {
				if (
					n.type === "property" &&
					n.keyNode.value === "name" &&
					n.valueNode
				) {
					const valueOfNode = n.valueNode.value as string;

					if (
						!listPropertyCheck.refProperty.some(
							propertyCheck =>
								propertyCheck.getNameProperty() === root.keyNode.value &&
								propertyCheck.getValueProperty() === valueOfNode
						)
					) {
						const position = document.positionAt(n.valueNode.offset);
						const range = Range.create(
							{ line: position.line, character: position.character + 1 },
							{
								line: position.line,
								character: position.character + valueOfNode.length + 1
							}
						);
						const property = new PropertyCheck(
							root.keyNode.value,
							valueOfNode,
							range
						);
						listPropertyCheck.mainProperty.push(property);
					} else {
						markPropertyAsUsed(
							listPropertyCheck,
							root.keyNode.value,
							valueOfNode
						);
					}
				}
			});
		});
	}

	// Check for reference properties
	if (
		root.type === "array" &&
		root.parent?.type === "property" &&
		propertyRefs.includes(root.parent.keyNode.value)
	) {
		const keyParent = root.parent.keyNode.value;
		const keyUsedToRef = mapKeyToRef(keyParent);

		root.children.forEach(child => {
			parseComplexExpressions(child.value as string, child.offset).forEach(
				exp => {
					const valueOfNode = exp.expression;

					if (
						!listPropertyCheck.mainProperty.some(
							property =>
								property.getValueProperty() === valueOfNode &&
								property.getNameProperty() === keyUsedToRef
						)
					) {
						const startPosition = document.positionAt(exp.startIndex + 1);
						const endPosition = document.positionAt(exp.endIndex + 1);
						const range = Range.create(startPosition, endPosition);
						const property = new PropertyCheck(
							keyUsedToRef,
							valueOfNode,
							range
						);
						listPropertyCheck.refProperty.push(property);
					} else {
						markPropertyAsUsed(listPropertyCheck, keyUsedToRef, valueOfNode);
					}
				}
			);
		});
	}

	// Recursively check child nodes
	root.children?.forEach(child =>
		validateObjectNotUsage(document, child, listPropertyCheck)
	);
}

function validateUniqueNamesInProperty(
	document: TextDocument,
	root: ASTNode,
	propertyKey: string
): Diagnostic[] | null {
	if (
		root.type === "property" &&
		root.keyNode.value === propertyKey &&
		root.valueNode
	) {
		const nameCount: { [key: string]: number } = {};
		const diagnostics: Diagnostic[] = [];
		for (const child of root.valueNode.children || []) {
			child.children?.forEach(n => {
				if (
					n.type === "property" &&
					n.keyNode.value === "name" &&
					n.valueNode
				) {
					nameCount[n.valueNode.value as string] =
						(nameCount[n.valueNode.value as string] || 0) + 1;
				}
			});
		}

		for (const child of root.valueNode.children || []) {
			child.children?.forEach(n => {
				if (
					n.type === "property" &&
					n.keyNode.value === "name" &&
					n.valueNode &&
					nameCount[n.valueNode.value as string] > 1
				) {
					diagnostics.push({
						severity: DiagnosticSeverity.Error,
						range: {
							start: document.positionAt(n.offset),
							end: document.positionAt(n.offset + n.length)
						},
						message: "Duplicate name. Names must be unique"
					});
				}
			});
		}

		return diagnostics;
	}
	for (const child of root.children || []) {
		const found = validateUniqueNamesInProperty(document, child, propertyKey);
		if (found) return found;
	}

	return null;
}

function validateSpringAndPropertyRefInValue(
	document: TextDocument,
	root: ASTNode,
	propertyKeys: string[],
	grammarCheck: (
		document: TextDocument,
		child: ASTNode,
		value: string
	) => Diagnostic[]
): Diagnostic[] {
	const diagnostics: Diagnostic[] = [];
	if (
		root.type === "array" &&
		root.parent?.type === "property" &&
		propertyKeys.includes(root.parent.keyNode.value)
	) {
		for (const child of root.children || []) {
			diagnostics.push(...grammarCheck(document, child, child.value as string));
		}
	}

	if (
		root.type === "property" &&
		propertyKeys.includes(root.keyNode.value) &&
		root.valueNode
	) {
		diagnostics.push(
			...grammarCheck(document, root.valueNode, root.valueNode.value as string)
		);
	}

	for (const child of root.children || []) {
		diagnostics.push(
			...validateSpringAndPropertyRefInValue(
				document,
				child,
				propertyKeys,
				grammarCheck
			)
		);
	}

	return diagnostics;
}

function markPropertyAsUsed(
	listPropertyCheck: {
		mainProperty: PropertyCheck[];
		refProperty: PropertyCheck[];
	},
	name: string,
	value: string
): void {
	listPropertyCheck.mainProperty.forEach(property => {
		if (
			property.getNameProperty() === name &&
			property.getValueProperty() === value
		) {
			property.setToUsed();
		}
	});

	listPropertyCheck.refProperty.forEach(property => {
		if (
			property.getNameProperty() === name &&
			property.getValueProperty() === value
		) {
			property.setToUsed();
		}
	});
}

function mapKeyToRef(key: string): string {
	switch (key) {
		case "policy-refs":
			return "policies";
		case "repository-refs":
			return "repositoryPolicies";
		case "rights-refs":
			return "propertyRights";
		default:
			return key;
	}
}
