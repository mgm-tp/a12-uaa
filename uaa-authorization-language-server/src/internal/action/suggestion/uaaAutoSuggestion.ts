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
import { Position } from "vscode-languageserver-types";
import { CompletionItem } from "vscode-languageserver/node.js";
import { CompletionItemKind, Range } from "vscode-languageserver";

import { loadMetadata, resolveMetadataClass } from "../../utils/utils.js";
import { resourcesMetadata } from "../../loadFieldsJava.js";

export function resolveCompletionItemsBasedOnMetaData(
	currentWordDetected: string,
	position: Position
): CompletionItem[] {
	const metadataObject = loadMetadata();
	if (metadataObject === undefined) {
		return [];
	}
	const principalJavaClass = metadataObject.principal;

	const createCompletionItem = (
		label: string,
		kind: CompletionItemKind,
		newText: string,
		range: Range,
		detail: string
	): CompletionItem => ({ label, kind, textEdit: { newText, range }, detail });

	const createRange = (
		startLine: number,
		startChar: number,
		end: Position
	): Range => ({
		start: Position.create(startLine, startChar),
		end
	});

	if (
		principalJavaClass &&
		(currentWordDetected === "principal." ||
			currentWordDetected.startsWith("principal."))
	) {
		const principalMetadata = resolveMetadataClass(principalJavaClass);
		const completions: CompletionItem[] = [];

		const prefix = "principal.";
		const rangeStart = Position.create(
			position.line,
			position.character - currentWordDetected.length
		);

		const filteredFields = Array.from(principalMetadata.fields).filter(
			field =>
				currentWordDetected === prefix ||
				`${prefix}${field}`.startsWith(currentWordDetected)
		);

		const filteredMethods = Array.from(principalMetadata.methods).filter(
			method =>
				currentWordDetected === prefix ||
				`${prefix}${method}`.startsWith(currentWordDetected)
		);

		completions.push(
			...filteredFields.map(field =>
				createCompletionItem(
					`${prefix}${field}`,
					CompletionItemKind.Field,
					`${prefix}${field}`,
					createRange(position.line, rangeStart.character, position),
					principalMetadata.className || ""
				)
			),
			...filteredMethods.map(method =>
				createCompletionItem(
					`${prefix}${method}`,
					CompletionItemKind.Method,
					`${prefix}${method}`,
					createRange(position.line, rangeStart.character, position),
					principalMetadata.className || ""
				)
			)
		);

		return completions;
	}

	if (
		currentWordDetected === "#resource." ||
		currentWordDetected.startsWith("#resource.")
	) {
		const completions: CompletionItem[] = [];

		const prefix = "#resource.";
		const rangeStart = Position.create(
			position.line,
			position.character - currentWordDetected.length
		);

		resourcesMetadata.forEach(javaClass => {
			completions.push(
				...Array.from(javaClass.fields)
					.filter(
						field =>
							currentWordDetected === prefix ||
							`${prefix}${field}`.startsWith(currentWordDetected)
					)
					.map(field =>
						createCompletionItem(
							`${prefix}${field}`,
							CompletionItemKind.Field,
							`${prefix}${field}`,
							createRange(position.line, rangeStart.character, position),
							javaClass.className || ""
						)
					),

				...Array.from(javaClass.methods)
					.filter(
						method =>
							currentWordDetected === prefix ||
							`${prefix}${method}`.startsWith(currentWordDetected)
					)
					.map(method =>
						createCompletionItem(
							`${prefix}${method}`,
							CompletionItemKind.Method,
							`${prefix}${method}`,
							createRange(position.line, rangeStart.character, position),
							javaClass.className || ""
						)
					)
			);
		});

		return completions;
	}

	if (currentWordDetected.startsWith("#")) {
		return [
			createCompletionItem(
				"#resource",
				CompletionItemKind.Text,
				"#resource",
				createRange(
					position.line,
					position.character - currentWordDetected.length,
					position
				),
				""
			)
		];
	}

	return [];
}
