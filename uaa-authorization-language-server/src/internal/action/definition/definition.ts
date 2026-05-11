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
	DefinitionParams,
	LocationLink,
	Range,
	TextDocuments
} from "vscode-languageserver";
import {
	ASTNode,
	LanguageService,
	TextDocument
} from "vscode-json-languageservice";

import { ContextValidator } from "../../utils/node/json/contextValidators.js";
import {
	findNamePropertyOffsets,
	findReferenceNodeOffsets,
	getKeyNamesOfNode
} from "../../utils/node/json/nodeSearch.js";
import {
	AUTHORIZATION_TYPE,
	PROPERTY_REFS
} from "../../constants/authorization.js";
import { parseComplexExpressions } from "../../utils/node/json/expressions.js";
import { loadMetadata, resolveMetadataClass } from "../../utils/utils.js";

import { wordsUseFunctionProposals } from "../suggestion/completion.js";

export function onDefinitionHandler(
	documents: TextDocuments<TextDocument>,
	definitionParams: DefinitionParams,
	languageService: LanguageService
) {
	const document = documents.get(definitionParams.textDocument.uri);

	if (!document) {
		return [];
	}

	const jsonDocument = languageService.parseJSONDocument(document);
	const offset = document.offsetAt(definitionParams.position);
	const node = jsonDocument.getNodeFromOffset(offset, true);

	if (node?.parent && jsonDocument.root) {
		const keys = getKeyNamesOfNode(node)?.keys || [];

		return (
			definitionInJsonSchemaProvider(
				keys,
				document,
				jsonDocument.root,
				offset,
				definitionParams
			) ?? definitionMetadataProvider(keys, document, definitionParams)
		);
	}
	return [];
}

function definitionMetadataProvider(
	keys: string[],
	document: TextDocument,
	definitionParams: DefinitionParams
) {
	if (!keys) return [];
	if (wordsUseFunctionProposals.includes(keys[0])) {
		return goToJavaClassProvider(document, definitionParams);
	}

	return null;
}

function definitionInJsonSchemaProvider(
	keys: string[],
	document: TextDocument,
	root: ASTNode,
	offset: number,
	definitionParams: DefinitionParams
) {
	if (!keys) return [];
	if (ContextValidator.isInPolicyRefsContext(keys[0])) {
		return goToPropertyDefinitionProvider(
			document,
			root,
			offset,
			definitionParams,
			AUTHORIZATION_TYPE.POLICES
		);
	}
	if (ContextValidator.isInRepositoryRefsContext(keys[0])) {
		return goToPropertyDefinitionProvider(
			document,
			root,
			offset,
			definitionParams,
			AUTHORIZATION_TYPE.REPOSITORY_POLICIES
		);
	}
	if (ContextValidator.isInRightsRefsContext(keys[0])) {
		return goToPropertyDefinitionProvider(
			document,
			root,
			offset,
			definitionParams,
			AUTHORIZATION_TYPE.PROPERTY_RIGHTS
		);
	}

	if (ContextValidator.isInNameOfPolicyContext(keys)) {
		return goToPropertyRefDefinitionProvider(
			document,
			root,
			definitionParams,
			PROPERTY_REFS.POLICY_REFS
		);
	}

	if (ContextValidator.isInNameOfRepositoryContext(keys)) {
		return goToPropertyRefDefinitionProvider(
			document,
			root,
			definitionParams,
			PROPERTY_REFS.REPOSITORY_REFS
		);
	}

	if (ContextValidator.isInNameOfRightContext(keys)) {
		return goToPropertyRefDefinitionProvider(
			document,
			root,
			definitionParams,
			PROPERTY_REFS.RIGHTS_REFS
		);
	}

	return null;
}

/*
 * Based on the name of main property (when be clicked), this function will return all name of property has property-refs include this name
 */
function goToPropertyRefDefinitionProvider(
	document: TextDocument,
	root: ASTNode,
	definitionParams: DefinitionParams,
	propertyRefKey: string
) {
	const fullTextInLine = document?.getText({
		start: { line: definitionParams.position.line, character: 0 },
		end: { line: definitionParams.position.line + 1, character: 0 }
	});

	const propertyRefValue = removeQuotesCommasAndBrackets(
		fullTextInLine
			.replace(/\s*\n\s*/g, " ")
			.trim()
			.replace(/("\D*":)(\s*)/g, "")
	);
	if (!propertyRefValue) {
		return [];
	}
	const offsetPropertyRefPositions = findReferenceNodeOffsets(
		root,
		propertyRefKey,
		propertyRefValue
	);
	if (offsetPropertyRefPositions.length === 0) {
		return [];
	}

	return offsetPropertyRefPositions.map(offsetPropertyRefPosition =>
		LocationLink.create(
			document.uri,
			Range.create(
				document.positionAt(offsetPropertyRefPosition.offset + 1),
				document.positionAt(
					offsetPropertyRefPosition.offset +
						offsetPropertyRefPosition.lengthText -
						1
				)
			),
			Range.create(
				document.positionAt(offsetPropertyRefPosition.offset + 1),
				document.positionAt(
					offsetPropertyRefPosition.offset +
						offsetPropertyRefPosition.lengthText -
						1
				)
			)
		)
	);
}

/*
 * Based on the name of property-refs (when be clicked), this function will return all position of property has this name
 */
function goToPropertyDefinitionProvider(
	document: TextDocument,
	root: ASTNode,
	offset: number,
	definitionParams: DefinitionParams,
	propertyKey: string
) {
	const fullTextInLine = document?.getText({
		start: { line: definitionParams.position.line, character: 0 },
		end: { line: definitionParams.position.line + 1, character: 0 }
	});
	const textAtCursor = document?.getText({
		start: { line: definitionParams.position.line, character: 0 },
		end: definitionParams.position
	});
	const expressionResults =
		parseComplexExpressions(fullTextInLine, offset, textAtCursor.length).find(
			exp => exp.isCursorInside
		)?.expression || "";
	const namePropertyRefs = removeQuotesCommasAndBrackets(expressionResults);
	if (!namePropertyRefs) {
		return [];
	}

	const offsetPropertyPositions = findNamePropertyOffsets(
		root,
		propertyKey,
		namePropertyRefs
	);

	if (!offsetPropertyPositions || offsetPropertyPositions.length === 0) {
		return [];
	}

	return offsetPropertyPositions.map(offsetPropertyPosition =>
		LocationLink.create(
			document.uri,
			Range.create(
				document.positionAt(offsetPropertyPosition),
				document.positionAt(offsetPropertyPosition)
			),
			Range.create(
				document.positionAt(offsetPropertyPosition),
				document.positionAt(offsetPropertyPosition)
			)
		)
	);
}

function removeQuotesCommasAndBrackets(input: string): string {
	return input.replace(/["|,[\]]/g, "");
}

function goToJavaClassProvider(
	document: TextDocument,
	definitionParams: DefinitionParams
): LocationLink[] | undefined {
	const fullTextInLine = document
		?.getText({
			start: { line: definitionParams.position.line, character: 0 },
			end: { line: definitionParams.position.line + 1, character: 0 }
		})
		.replace(/[",]/g, "")
		.trim();
	const textAtCursor = document
		?.getText({
			start: { line: definitionParams.position.line, character: 0 },
			end: definitionParams.position
		})
		.replace(/[",]/g, "")
		.trim();

	if (!fullTextInLine?.length || !textAtCursor?.length) {
		return undefined;
	}
	const wordInfo = getWordAtCursor(fullTextInLine, textAtCursor?.length);

	return wordInfo ? gotoJavaClassDefinition(wordInfo) : undefined;
}

function gotoJavaClassDefinition(word: string): LocationLink[] | undefined {
	const metadata = loadMetadata();
	if (!metadata) {
		return undefined;
	}
	if (word === "principal" && metadata.principal) {
		const principalClass = resolveMetadataClass(metadata.principal);
		if (!principalClass.className || !principalClass.classNamePosition) {
			return undefined;
		}
		return [
			LocationLink.create(
				"file://".concat(metadata?.principal),
				Range.create(
					principalClass.classNamePosition,
					principalClass.classNamePosition
				),
				Range.create(
					principalClass.classNamePosition,
					principalClass.classNamePosition
				)
			)
		];
	}

	if (word === "#resource" && metadata.resource) {
		return metadata.resource
			.map(({ path }) => {
				const resourceJava = resolveMetadataClass(path);
				const { className, classNamePosition } = resourceJava;

				if (!className || !classNamePosition) {
					return undefined;
				}

				return LocationLink.create(
					`file://${path}`,
					Range.create(classNamePosition, classNamePosition),
					Range.create(classNamePosition, classNamePosition)
				);
			})
			.filter((item): item is LocationLink => item !== undefined);
	}

	return undefined;
}

function getWordAtCursor(text: string, positionInLine: number): string | null {
	if (positionInLine < 0) {
		return null;
	}

	const validWordChars = /[a-zA-Z0-9_#]/;
	let start = positionInLine;
	while (start > 0 && validWordChars.test(text.charAt(start - 1))) {
		start--;
	}

	let end = positionInLine;
	while (end < text.length && validWordChars.test(text.charAt(end))) {
		end++;
	}

	return text.substring(start, end);
}
