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
import { reduxStore, sessionStorage } from "../utils/index.js";
import * as UaaActions from "../actions.js";

interface SessionStorageMap {
	[key: string]: string | undefined;
}

export class UaaSecureStorage {
	private static _instance: UaaSecureStorage;

	static createInstance(): void {
		this._instance = new UaaSecureStorage();
	}

	static getInstance(): UaaSecureStorage {
		if (!this._instance) {
			this._instance = new UaaSecureStorage();
		}
		return this._instance;
	}

	public initShareSecuredData(
		keys: string[],
		isSharingAllowed: () => boolean = () => true
	) {
		window.addEventListener("storage", (event: StorageEvent) => {
			if (!event.newValue) {
				return;
			}
			if (event.key === "requestSharedDataEvent") {
				reduxStore.dispatch(UaaActions.sessionStorageSharingData());
				const data: SessionStorageMap = {};
				for (const key in sessionStorage) {
					// eslint-disable-next-line no-prototype-builtins
					if (sessionStorage.hasOwnProperty(key) && keys?.includes(key)) {
						data[key] = sessionStorage.getItem(key) as string;
					}
				}
				// concat data from session storage and share in response
				const sharedDataPayload: string = JSON.stringify(data);
				localStorage.setItem("responseSharedDataEvent", sharedDataPayload);
				localStorage.removeItem("responseSharedDataEvent");
			} else if (event.key === "responseSharedDataEvent") {
				const data = JSON.parse(event.newValue);
				for (const key in data) {
					if (
						// eslint-disable-next-line no-prototype-builtins
						data.hasOwnProperty(key) &&
						keys?.includes(key) &&
						!sessionStorage.getItem(key)
					) {
						sessionStorage.setItem(key, data[key]);
					}
				}
				reduxStore.dispatch(UaaActions.sessionStorageSharedData());
			}
		});
		if (isSharingAllowed()) {
			localStorage.setItem(
				"requestSharedDataEvent",
				"Event triggers share data securely using local storage"
			);
			localStorage.removeItem("requestSharedDataEvent");
		}
	}
}
