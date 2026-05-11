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
import { LoggerFactory } from "@com.mgmtp.a12.utils/utils-logging/lib/factory.js";

import { AuthenticationType } from "./interfaces/index.js";

const logger = LoggerFactory.getLogger("UAA/Service-worker");

export namespace UAAServiceWorker {
	/**
	 *
	 * @param urls
	 */
	export function register(urls: string[]) {
		const scriptUrl = "uaa-service-worker.js";
		const message = {
			action: "POST_URL",
			data: urls
		};
		const msg = new MessageChannel();
		if ("serviceWorker" in navigator) {
			window.addEventListener("load", function () {
				navigator.serviceWorker.register(scriptUrl).catch(error => {
					logger.error("Register service worker failed: ", error);
				});
			});
			navigator.serviceWorker.ready.then(registration => {
				registration.active?.postMessage(message, [msg.port2]);
			});
		}
	}

	/**
	 * @param token
	 * @param authenticationType
	 * @internal
	 */
	export function postToken(
		token: string,
		authenticationType: AuthenticationType
	) {
		const msg = new MessageChannel();
		const message = {
			action: "POST_TOKEN",
			data: token,
			type: authenticationType
		};
		if ("serviceWorker" in navigator) {
			navigator.serviceWorker.ready.then(registration => {
				registration.active?.postMessage(message, [msg.port2]);
			});
		}
	}

	/**
	 *
	 */
	export function unregister() {
		if ("serviceWorker" in navigator) {
			navigator.serviceWorker.ready
				.then(registration => {
					registration.unregister();
				})
				.catch(error => {
					logger.error(error.message);
				});
		}
	}
}
