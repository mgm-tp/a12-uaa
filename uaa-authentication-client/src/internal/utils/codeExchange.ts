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
/**
 * @param buffer
 */

export type CodeExchange = {
	state: string;
	code_v: string;
	code_c: string;
};

export function base64Encode(buffer: Uint8Array): string {
	return window
		.btoa(String.fromCharCode(...buffer))
		.replace(/\+/g, "-")
		.replace(/\//g, "_")
		.replace(/[=]/g, "");
}

/**
 * @param buffer
 */
export async function sha256(buffer: string): Promise<Uint8Array> {
	const textEncoded = new TextEncoder().encode(buffer);
	if (window.crypto.subtle !== undefined) {
		return new Uint8Array(
			await window.crypto.subtle.digest("SHA-256", textEncoded)
		);
	}
	throw Error("Crypto.subtle is available only in secure contexts (HTTPS).");
}

export async function generateCodeExchange(): Promise<CodeExchange> {
	// create random code_v
	const code_v = base64Encode(
		window.crypto.getRandomValues(new Uint8Array(32))
	);
	// create random state
	const state = base64Encode(window.crypto.getRandomValues(new Uint8Array(32)));
	// create code_c
	const code_c = base64Encode(await sha256(code_v));

	return {
		state,
		code_v,
		code_c
	};
}
