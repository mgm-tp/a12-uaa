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
	CancellationToken,
	LSPErrorCodes,
	ResponseError
} from "vscode-languageserver";

import { RuntimeEnvironment } from "../interfaces/server.js";

/*
 * ===== BEGIN THIRD-PARTY SOURCE: aws-toolkit-vscode (https://github.com/aws/aws-toolkit-vscode),
 * https://github.com/microsoft/vscode-extension-samples/blob/main/lsp-embedded-language-service/server/src/languageModelCache.ts
 * Licensed under the Apache-2.0 License.
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the MIT License.
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 * Modified by mgm technology partners on [2025-01-09].
 */
export function formatError(message: string, err: any): string {
	if (err instanceof Error) {
		const error = <Error>err;
		return `${message}: ${error.message}
${error.stack}`;
	} else if (typeof err === "string") {
		return `${message}: ${err}`;
	} else if (err) {
		return `${message}: ${err.toString()}`;
	}
	return message;
}

export function runSafeAsync<T>(
	runtime: RuntimeEnvironment,
	func: () => Thenable<T>,
	errorVal: T,
	errorMessage: string,
	token: CancellationToken
): Thenable<T | ResponseError<any>> {
	return new Promise<T | ResponseError<any>>(resolve => {
		runtime.timer.setImmediate(() => {
			if (token.isCancellationRequested) {
				resolve(cancelValue());
				return;
			}
			return func().then(
				result => {
					if (token.isCancellationRequested) {
						resolve(cancelValue());
						return;
					} else {
						resolve(result);
					}
				},
				e => {
					console.error(formatError(errorMessage, e));
					resolve(errorVal);
				}
			);
		});
	});
}

export function runSafe<T, E>(
	runtime: RuntimeEnvironment,
	func: () => T,
	errorVal: T,
	errorMessage: string,
	token: CancellationToken
): Thenable<T | ResponseError<E>> {
	return new Promise<T | ResponseError<E>>(resolve => {
		runtime.timer.setImmediate(() => {
			if (token.isCancellationRequested) {
				resolve(cancelValue());
			} else {
				try {
					const result = func();
					if (token.isCancellationRequested) {
						resolve(cancelValue());
						return;
					} else {
						resolve(result);
					}
				} catch (e) {
					console.error(formatError(errorMessage, e));
					resolve(errorVal);
				}
			}
		});
	});
}

function cancelValue<E>() {
	return new ResponseError<E>(
		LSPErrorCodes.RequestCancelled,
		"Request cancelled"
	);
}
// ===== END THIRD-PARTY SOURCE =====
