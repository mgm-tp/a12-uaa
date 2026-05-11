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
import { UaaActions } from "@com.mgmtp.a12.uaa/uaa-authentication-client";
import type { UaaClientConfiguration } from "@com.mgmtp.a12.uaa/uaa-authentication-client";

import {
	withUaa,
	type UaaConfig,
	type ApplicationWithUaaConfig
} from "../../src/index.js";

const mockUaaConfig: UaaClientConfiguration = {
	serverURL: "http://localhost:8080"
};

const createBaseConfig = (): ApplicationWithUaaConfig => ({
	config: {},
	uaa: {
		configuration: mockUaaConfig
	}
});

describe("withUaa", function () {
	it("should mark uaa as configured", function () {
		const result = withUaa(createBaseConfig());

		expect(result.configured).toHaveProperty("uaa", true);
	});

	it("should add UAA reducer to config", function () {
		const result = withUaa(createBaseConfig());
		const cfg = result.config as Record<string, unknown>;

		expect(cfg).toHaveProperty("reducerMap");
		const reducerMap = cfg.reducerMap as Record<string, unknown>;
		expect(reducerMap).toHaveProperty("uaa");
		expect(reducerMap.uaa).toBeTypeOf("function");
	});

	it("should add UAA middlewares to config", function () {
		const result = withUaa(createBaseConfig());
		const cfg = result.config as Record<string, unknown>;

		expect(cfg).toHaveProperty("additionalMiddlewares");
		const middlewares = cfg.additionalMiddlewares as unknown[];
		expect(middlewares).toBeInstanceOf(Array);
		expect(middlewares.length).toBeGreaterThan(0);
	});

	it("should add busy triggers for login actions", function () {
		const result = withUaa(createBaseConfig());
		const cfg = result.config as Record<string, unknown>;
		const busyTrigger = cfg.applicationBusyTriggers as {
			start: unknown[];
			end: unknown[];
		};

		expect(busyTrigger.start).toContain(UaaActions.loggingInLocal);
		expect(busyTrigger.start).toContain(UaaActions.loggingInLDAP);
		expect(busyTrigger.start).toContain(UaaActions.loggingInOIDC);
		expect(busyTrigger.start).toContain(UaaActions.loggingInSAML);
		expect(busyTrigger.end).toContain(UaaActions.loggedIn);
		expect(busyTrigger.end).toContain(UaaActions.loginFailed);
	});

	it("should add reset triggers for logout actions", function () {
		const result = withUaa(createBaseConfig());
		const cfg = result.config as Record<string, unknown>;
		const resetTriggers = cfg.applicationResetTriggers as {
			resetRequested: unknown[];
			reset: unknown[];
		};

		expect(resetTriggers.resetRequested).toContain(UaaActions.logoutRequested);
		expect(resetTriggers.reset).toContain(UaaActions.loggedOut);
	});

	it("should preserve existing config properties", function () {
		const config = { ...createBaseConfig(), existingProperty: "test" };
		const result = withUaa(config);

		expect(result).toHaveProperty("existingProperty", "test");
	});

	it("should preserve uaa configuration in result", function () {
		const config = createBaseConfig();
		const result = withUaa(config);

		expect(result.uaa).toEqual(config.uaa);
	});

	it("should work with override configurations", function () {
		const config: ApplicationWithUaaConfig = {
			config: {},
			uaa: {
				configuration: {
					serverURL: "http://localhost:8080",
					automaticallyLogin: true,
					overrideClientConfigures: {
						local: { serverURL: "http://localhost:9090" }
					}
				}
			}
		};

		const result = withUaa(config);

		expect(result.configured).toHaveProperty("uaa", true);
		expect(result.uaa!.configuration.automaticallyLogin).toBe(true);
	});
});

describe("UaaConfig type", function () {
	it("should require configuration with serverURL", function () {
		const config: UaaConfig = {
			configuration: { serverURL: "http://localhost:8080" }
		};

		expect(config.configuration.serverURL).toBeTypeOf("string");
	});

	it("should accept optional serverSelfConfigureUrl", function () {
		const config: UaaConfig = {
			configuration: {
				serverURL: "http://localhost:8080",
				serverSelfConfigureUrl: "http://localhost:8080/uaa/self-configuration"
			}
		};

		expect(config.configuration.serverSelfConfigureUrl).toBe(
			"http://localhost:8080/uaa/self-configuration"
		);
	});
});
