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
import { ExpressionResult } from "../../../interfaces/nodeJson.js";

export function parseComplexExpressions(
	content: string,
	cursorPosition: number = 0,
	lengthText: number = 0
): ExpressionResult[] {
	const operators: string[] = ["||", "&&"];
	const operatorRegex = new RegExp(
		operators
			.map(op => `${op.replace(/([.*+?^=!:${}()|[\]/\\])/g, "\\$1")}`)
			.join("|"),
		"g"
	);

	let currentIndex = cursorPosition - lengthText;
	const expressionsResults: ExpressionResult[] = [];

	// split content based on operator
	content.split(operatorRegex).forEach(expr => {
		const trimmedExpr = expr.replace(/[",]/g, "").trim();
		const { leading, trailing } = countLeadingAndTrailingCharacters(expr);
		const startIndex = currentIndex + leading;
		const endIndex = startIndex + trimmedExpr.length;
		// check if cursor is in expression
		const isCursorInside =
			cursorPosition >= startIndex && cursorPosition <= endIndex;

		expressionsResults.push({
			expression: trimmedExpr,
			startIndex,
			endIndex,
			isCursorInside
		});

		// update currentIndex, plus the length of operator
		currentIndex = endIndex + trailing + 2;
	});
	return expressionsResults;
}

function countLeadingAndTrailingCharacters(input: string): {
	leading: number;
	trailing: number;
} {
	// find the position of first character
	const leadingMatch = input.match(/[^a-zA-Z]*([a-zA-Z])/);
	const leading = leadingMatch ? leadingMatch[0].length - 1 : 0;

	// find the position of last character
	const trailingMatch = input.match(/([a-zA-Z])[^a-zA-Z]*$/);
	const trailing = trailingMatch ? input.length - trailingMatch.index! - 1 : 0;

	return { leading, trailing };
}
