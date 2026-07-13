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
import { SagaGenerator, takeEvery } from "typed-redux-saga";
import createSagaMiddleware, { Task } from "redux-saga";

import type { Action } from "@com.mgmtp.a12.client/typescript-fsa-redux-5-compat";

import { UaaSagaDescriptor } from "../interfaces/index.js";

import localSaga from "./local.js";
import samlSaga from "./saml.js";
import commonSaga from "./common.js";
import oidcSaga from "./oidc.js";

/** @internal */
export class UaaSaga {
	private _task: Task | undefined;
	private _overrideSagas: UaaSagaDescriptor[] = [];

	public get overrideSagas(): UaaSagaDescriptor[] {
		return this._overrideSagas;
	}

	public set overrideSagas(value: UaaSagaDescriptor[]) {
		this._overrideSagas = value;
	}

	// @ts-expect-error FIXME ESM issue
	private _middleware = createSagaMiddleware();

	private rootSaga: UaaSagaDescriptor[] = [
		...commonSaga,
		...samlSaga,
		...localSaga,
		...oidcSaga
	];

	public get middleware() {
		return this._middleware;
	}

	public set middleware(value) {
		this._middleware = value;
	}

	public get task(): Task | undefined {
		return this._task;
	}

	public set task(value: Task | undefined) {
		this._task = value;
	}

	public init() {
		if (this.task) {
			this.task.cancel();
		}
		this.task = this.middleware.run(watchDispatchSaga, [
			...this.overrideSagas,
			...this.rootSaga
		]);
	}
}

/**
 *
 * @param sagas
 */
function* watchDispatchSaga(sagas: UaaSagaDescriptor[]): SagaGenerator<void> {
	yield* takeEvery(() => true, dispatch, sagas);
}

/**
 *
 * @param sagas
 * @param action
 */
function* dispatch(sagas: UaaSagaDescriptor[], action: Action<void>) {
	const firstMatchingSaga = sagas.find(saga => saga.canHandle(action));
	if (firstMatchingSaga) {
		yield firstMatchingSaga.handle(action);
	}
}

/** @internal */
export const uaaSaga = new UaaSaga();
