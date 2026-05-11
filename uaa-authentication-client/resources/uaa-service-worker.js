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
let idToken;
let authenticationType;
const urls = new Set();

self.addEventListener("message", event => {
	if (event.data && event.data.action === "POST_URL") {
		event.data.data.forEach(url => urls.add(url));
	}

	if (event.data && event.data.action === "POST_TOKEN") {
		idToken = event.data.data ?? idToken;
		authenticationType = event.data.type ?? authenticationType;
	}
});

self.addEventListener("fetch", fetchEvent => {
	const url = new URL(fetchEvent.request.url);
	const checkUrl = urls.has(url.href);
	const tokenAuthentication =
		authenticationType === "OAUTH2" ? "Bearer" : "UAABearer";
	const checkAuthorHeader = fetchEvent.request.headers.get("Authorization");
	if (idToken && checkUrl && !checkAuthorHeader) {
		const headers = fetchEvent.request.headers;
		const headersAppend = new Headers(headers);

		headersAppend.append("Authorization", `${tokenAuthentication} ${idToken}`);

		const modifiedRequest = new Request(fetchEvent.request, {
			headers: headersAppend,
			mode: "cors"
		});

		fetchEvent.respondWith(fetch(modifiedRequest));
	}
});

self.addEventListener("install", function (event) {
	event.waitUntil(self.skipWaiting());
});

self.addEventListener("activate", function (event) {
	event.waitUntil(self.clients.claim());
});
