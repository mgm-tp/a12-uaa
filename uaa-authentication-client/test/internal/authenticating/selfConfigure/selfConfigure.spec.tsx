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
import { expect } from "chai";
import fetchMock from "fetch-mock";
import { render, cleanup } from "@testing-library/react";
import { Unsubscribe } from "redux";

import { UaaClient } from "../../../../src/index.js";
import { UaaProvider } from "../../../../src/index.js";
import { sessionStorage } from "../../../../src/internal/utils/index.js";
import selfConfigure from "../../resources/self-configuration.json" with { type: "json" };

import createUaaStore from "../reduxSetup.js";

const unsubscribe: Unsubscribe = () => undefined;
let store = createUaaStore();

describe("SelfConfigure test", function () {
	beforeEach(function () {
		store = createUaaStore();
		render(<UaaProvider store={store} />);
	});

	afterEach(function () {
		// cleanup on exiting
		fetchMock.reset();
		cleanup();
		unsubscribe();
	});

	it("Initial UaaClient", async function () {
		fetchMock.get(
			"uaa/uaa-authentication/selfconfigure",
			JSON.stringify(selfConfigure)
		);
		await UaaClient.init({
			serverURL: "/uaa/"
		});
		const stored = sessionStorage.getItem("selfConfigure");
		expect(JSON.parse(stored!)).to.deep.include({
			applicationBaseUrl: "http://localhost:8080",
			uaaBaseUrl: "http://localhost:8080"
		});
	});
});
