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
import { Store, UnknownAction } from "redux";

import { Action } from "@com.mgmtp.a12.client/typescript-fsa-redux-5-compat";

/**
 * @param sliceName
 * @param test
 * @param selector
 * @internal
 */
export function createSliceSelector<T, M>(
	sliceName: string,
	test: (slice: unknown) => slice is T,
	selector: (slice: T) => M
): (state: Record<string, unknown>) => M | undefined {
	return state => {
		if (state && sliceName in state) {
			const slice: unknown = state[sliceName];
			if (!test(slice)) {
				throw new Error(`State contains an invalid ${sliceName} slice.`);
			}

			return selector(slice);
		}
		throw new Error(`State does not contain a ${sliceName} slice.`);
	};
}

export type Selector<R> = (state: {}) => R;

export interface ConfigurableSelector<R> extends Selector<R> {
	withConfig: (params: { defaultValue: R }) => Selector<R>;
}

class ReduxStore {
	private store: Store | undefined;

	public setStore(store: Store): void {
		this.store = store;
	}

	public getStore(): Store<Record<string, unknown>, UnknownAction> | undefined {
		return this.store;
	}

	public dispatch(action: Action<unknown>): void {
		this.store?.dispatch(action);
	}
}

export const reduxStore = new ReduxStore();
export const sessionStorage = window.sessionStorage;
