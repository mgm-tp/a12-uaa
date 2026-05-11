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
import { CompletionItem } from "vscode-languageserver/node.js";
import { CompletionItemKind, Position } from "vscode-languageserver-types";

export const functionProposals: CompletionItem[] = [
	{
		label: "principal",
		kind: CompletionItemKind.Class,
		documentation: "principal",
		insertText: "principal"
	},
	{
		label: "isResourceName",
		kind: CompletionItemKind.Function,
		documentation: "Check if the resource name matches.",
		insertText: "isResourceName(Object resource, String name)"
	},
	{
		label: "print",
		kind: CompletionItemKind.Function,
		documentation: "Print a message to the console.",
		insertText: "print($0)",
		insertTextFormat: 2 // This allows for tab stops ($0 will be the tab stop)
	},
	{
		label: "hasObjectWithPropertyValue",
		kind: CompletionItemKind.Function,
		documentation:
			"Check if collection contains an object with the given property value.",
		insertText:
			"hasObjectWithPropertyValue(Collection<Object> collection, String propertyName, String properyValue)"
	},
	{
		label: "hasNestedObjectWithPropertyValue",
		kind: CompletionItemKind.Function,
		documentation:
			"Check if collection contains a nested object with the given property value.",
		insertText:
			"hasNestedObjectWithPropertyValue(Collection<? extends Object> collection, String collectionName, String propertyName, String properyValue)"
	},
	{
		label: "hasAccessRight",
		kind: CompletionItemKind.Function,
		documentation: "Check if the role has access rights.",
		insertText: "hasAccessRight(String roleName)"
	},
	{
		label: "containsAnyRole",
		kind: CompletionItemKind.Function,
		documentation: "Check if the object contains any of the given roles.",
		insertText: "containsAnyRole(Collection<String> objectRoles)"
	}
];

export function locatePosition(
	completionItems: CompletionItem[],
	currentWord: string,
	position: Position
): CompletionItem[] {
	return completionItems.map(completion => {
		return {
			...completion,
			textEdit: {
				newText: completion.insertText as string,
				range: {
					start: Position.create(
						position.line,
						position.character - currentWord.length
					),
					end: position
				}
			}
		};
	});
}
