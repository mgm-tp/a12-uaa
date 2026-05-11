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
import * as fs from "node:fs";

import { createConnection, Disposable } from "vscode-languageserver/node.js";
import { createRequire } from 'module';
const require = createRequire(import.meta.url);
const requestLight = require('request-light') as typeof import('request-light');
const {
	configure: configureHttpRequests,
	getErrorStatusDescription,
	xhr,
} = requestLight;

import { URI as Uri } from "vscode-uri";

import { formatError } from "./internal/utils/runner.js";
import {
	RequestService,
	RuntimeEnvironment
} from "./internal/interfaces/server.js";
import { startServer } from "./internal/server/languageServerConfiguration.js";
import {
	fieldsOfAllDocument,
	preloadFieldOfAllDocument
} from "./internal/loadFieldsDocument.js";
import {
	fieldsOfJavaResource,
	preloadFieldsJava
} from "./internal/loadFieldsJava.js";
import { CompletionField } from "./internal/interfaces/nodeJson.js";
import { XHRResponse } from "request-light";

// Create a connection for the server. The connection uses Node's IPC as transport.
// Also include all preview / proposed LSP features.
// let connection = createConnection(ProposedFeatures.all);
const connection = createConnection(process.stdin, process.stdout);
console.log = connection.console.log.bind(connection.console);
console.error = connection.console.error.bind(connection.console);

process.on("unhandledRejection", (e: any) => {
	connection.console.error(formatError(`Unhandled exception`, e));
});

export const allCompletionFields: CompletionField[] = [];

function getHTTPRequestService(): RequestService {
	return {
		getContent(uri: string, _encoding?: string) {
			const headers = { "Accept-Encoding": "gzip, deflate" };
			return xhr({ url: uri, followRedirects: 5, headers }).then(
				(response: XHRResponse) => {
					return response.responseText;
				},
				(error: XHRResponse) => {
					return Promise.reject(
						error.responseText ||
							getErrorStatusDescription(error.status) ||
							error.toString()
					);
				}
			);
		}
	};
}

/*
 * ===== BEGIN THIRD-PARTY SOURCE: vscode (https://github.com/microsoft/vscode),
 * https://github.com/microsoft/vscode/blob/1.66.0/extensions/json-language-features/server/src/node/jsonServerMain.ts
 * Licensed under the MIT License.
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
function getFileRequestService(): RequestService {
	return {
		getContent(location: string, encoding?: BufferEncoding) {
			return new Promise((c, e) => {
				const uri = Uri.parse(location);
				fs.readFile(uri.fsPath, encoding, (err, buf) => {
					if (err) {
						return e(err);
					}
					c(buf.toString());
				});
			});
		}
	};
}

const runtime: RuntimeEnvironment = {
	timer: {
		setImmediate(
			callback: (...args: any[]) => void,
			...args: any[]
		): Disposable {
			const handle = setImmediate(callback, ...args);
			return { dispose: () => clearImmediate(handle) };
		},
		setTimeout(
			callback: (...args: any[]) => void,
			ms: number,
			...args: any[]
		): Disposable {
			const handle = setTimeout(callback, ms, ...args);
			return { dispose: () => clearTimeout(handle) };
		}
	},
	file: getFileRequestService(),
	http: getHTTPRequestService(),
	configureHttpRequests
};

startServer(connection, runtime);
// ===== END THIRD-PARTY SOURCE =====

preloadFieldOfAllDocument();
preloadFieldsJava();

allCompletionFields.push({
	name: "",
	completionLabels: new Set([...fieldsOfAllDocument, ...fieldsOfJavaResource])
});
