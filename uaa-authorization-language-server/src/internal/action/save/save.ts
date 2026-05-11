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
	Connection,
	Range,
	TextDocument,
	TextEdit
} from "vscode-languageserver";

export async function onSaveHandler(
	document: TextDocument,
	connection: Connection
) {
	try {
		const originalText = document.getText();
		if (!originalText.trim()) {
			return;
		}

		let jsonData: any;
		try {
			jsonData = JSON.parse(originalText);
		} catch (parseError) {
			connection.console.warn("File JSON is invalid.");
			return;
		}

		let newText = JSON.stringify(jsonData, null, 2);

		if (!newText.endsWith("\n")) {
			newText += "\n";
		}

		const fullRange = Range.create(
			document.positionAt(0),
			document.positionAt(originalText.length)
		);

		const edit: TextEdit = {
			range: fullRange,
			newText
		};

		await connection.workspace.applyEdit({
			changes: {
				[document.uri]: [edit]
			}
		});
	} catch (error) {
		connection.console.error(`Error while formatting JSON: ${error}`);
	}
}
