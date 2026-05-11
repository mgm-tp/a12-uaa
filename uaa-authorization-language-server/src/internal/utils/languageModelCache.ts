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
import { TextDocument } from "vscode-languageserver";

/*
 * ===== BEGIN THIRD-PARTY SOURCE: vscode-extension-samples (https://github.com/microsoft/vscode-extension-samples),
 * https://github.com/microsoft/vscode-extension-samples/blob/main/lsp-embedded-language-service/server/src/languageModelCache.ts
 * Licensed under the MIT License.
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
export interface LanguageModelCache<T> {
	get(document: TextDocument): T;

	onDocumentRemoved(document: TextDocument): void;

	dispose(): void;
}

export function getLanguageModelCache<T>(
	maxEntries: number,
	cleanupIntervalTimeInSec: number,
	parse: (document: TextDocument) => T
): LanguageModelCache<T> {
	let languageModels: {
		[uri: string]: {
			version: number;
			languageId: string;
			cTime: number;
			languageModel: T;
		};
	} = {};
	let nModels = 0;

	let cleanupInterval: NodeJS.Timeout | undefined = undefined;
	if (cleanupIntervalTimeInSec > 0) {
		cleanupInterval = setInterval(() => {
			const cutoffTime = Date.now() - cleanupIntervalTimeInSec * 1000;
			const uris = Object.keys(languageModels);
			for (const uri of uris) {
				const languageModelInfo = languageModels[uri];
				if (languageModelInfo.cTime < cutoffTime) {
					delete languageModels[uri];
					nModels--;
				}
			}
		}, cleanupIntervalTimeInSec * 1000);
	}

	return {
		get(document: TextDocument): T {
			const version = document.version;
			const languageId = document.languageId;
			const languageModelInfo = languageModels[document.uri];
			if (
				languageModelInfo &&
				languageModelInfo.version === version &&
				languageModelInfo.languageId === languageId
			) {
				languageModelInfo.cTime = Date.now();
				return languageModelInfo.languageModel;
			}
			const languageModel = parse(document);
			languageModels[document.uri] = {
				languageModel,
				version,
				languageId,
				cTime: Date.now()
			};
			if (!languageModelInfo) {
				nModels++;
			}

			if (nModels === maxEntries) {
				let oldestTime = Number.MAX_VALUE;
				let oldestUri = null;
				for (const uri in languageModels) {
					const languageModelInfo = languageModels[uri];
					if (languageModelInfo.cTime < oldestTime) {
						oldestUri = uri;
						oldestTime = languageModelInfo.cTime;
					}
				}
				if (oldestUri) {
					delete languageModels[oldestUri];
					nModels--;
				}
			}
			return languageModel;
		},
		onDocumentRemoved(document: TextDocument) {
			const uri = document.uri;
			if (languageModels[uri]) {
				delete languageModels[uri];
				nModels--;
			}
		},
		dispose() {
			if (typeof cleanupInterval !== "undefined") {
				clearInterval(cleanupInterval);
				cleanupInterval = undefined;
				languageModels = {};
				nModels = 0;
			}
		}
	};
}
// ===== END THIRD-PARTY SOURCE =====
