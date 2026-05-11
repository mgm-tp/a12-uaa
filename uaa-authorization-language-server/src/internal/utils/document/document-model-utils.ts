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
import type { DocumentModel } from "@com.mgmtp.a12.kernel/kernel-md-facade";
import { type ModelPath } from "@com.mgmtp.a12.base/base-model-api/lib/main/model/index.js";

import { allDocumentModels } from "../../loadFieldsDocument.js";

export interface DocumentElementReference<
	Element extends DocumentModel.Element
> {
	readonly path: ModelPath;
	readonly element: Element;
}

export namespace DocumentModelUtils {
	export function filterChildElements<T extends DocumentModel.Element>(
		elementRef: DocumentElementReference<DocumentModel.Group>,
		predicate: (element: DocumentModel.Element) => element is T,
		ignore: (element: DocumentModel.Element) => boolean
		// options?: { traverseRepeatableGroups?: boolean }
	): DocumentElementReference<T>[] {
		// const traverseRepeatableGroups = options?.traverseRepeatableGroups ?? false;
		if (ignore(elementRef.element)) {
			return [];
		}
		const result: DocumentElementReference<T>[] = [];
		for (const element of elementRef.element.elements) {
			let isRepeatableMoreThan1 = false;
			if (element.type === "Group" && element.repeatability > 1) {
				isRepeatableMoreThan1 = true;
			}

			const nextPath: ModelPath = [
				...elementRef.path,
				isRepeatableMoreThan1
					? { elementName: element.name + "[]" }
					: { elementName: element.name }
			];

			if (element.type === "Group" && element.modelAlias) {
				const modelAlias = element.modelAlias;
				allDocumentModels.forEach(document => {
					if (document.header.id && document.header.id === modelAlias) {
						result.push(
							...filterChildElements(
								{
									element: document.content.modelRoot
										.elements[0] as DocumentModel.Group,
									path: nextPath
								},
								predicate,
								ignore
							)
						);
					}
				});
			}
			if (predicate(element)) {
				result.push({ element, path: nextPath });
			}

			if (element.type === "Field") {
				continue;
			}

			// if (element.repeatability == 1 || (element.repeatability > 1 && traverseRepeatableGroups)) {
			result.push(
				...filterChildElements({ element, path: nextPath }, predicate, ignore)
			);
			// }
		}

		return result;
	}
}
