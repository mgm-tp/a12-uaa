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
	afterEach,
	beforeEach,
	describe,
	expect,
	it,
	MockInstance,
	vi
} from "vitest";
import fetchMock from "fetch-mock";
import { RestRequestPayload } from "@com.mgmtp.a12.utils/utils-connector";
import { LoggerFactory } from "@com.mgmtp.a12.utils/utils-logging";

import { UaaClient } from "../../../src/index.js";
import {
	dispatchAndCheckServerRequest,
	dispatchBlobServerRequest,
	dispatchServerRequest,
	fetchServerRequest
} from "../../../src/internal/utils/dispatchServerRequest.js";

const REDACTED_VALUE = "***";

const logger = LoggerFactory.getLogger("UAA/Serverrequest");

let logSpy: MockInstance<typeof logger.log>;

/**
 * Returns the payload of the most recent `logger.log("Request", ...)` call.
 */
function lastLoggedRequest(): RestRequestPayload {
	const requestCalls = logSpy.mock.calls.filter(call => call[0] === "Request");
	expect(requestCalls.length).toBeGreaterThan(0);
	return requestCalls[requestCalls.length - 1][1] as RestRequestPayload;
}

const isWithId = (obj: Record<string, unknown>): obj is { id: number } =>
	typeof obj.id === "number";

describe("dispatchServerRequest", function () {
	beforeEach(function () {
		UaaClient.init({ serverURL: "uaaServer" });
		logSpy = vi.spyOn(logger, "log");
	});

	afterEach(function () {
		fetchMock.reset();
		vi.restoreAllMocks();
	});

	it("masks sensitive fields in a JSON-string body before logging", async function () {
		fetchMock.post(() => true, { status: 200 });
		const request: RestRequestPayload = {
			relativeUrl: "user/local/login",
			method: "POST",
			body: JSON.stringify({ username: "admin", password: "secret" }),
			extendedData: { sensitiveFields: ["password"] }
		};

		await fetchServerRequest(request);

		expect(JSON.parse(lastLoggedRequest().body as string)).toEqual({
			username: "admin",
			password: REDACTED_VALUE
		});
		// The request actually sent to the server is left untouched.
		expect(JSON.parse(request.body as string)).toEqual({
			username: "admin",
			password: "secret"
		});
	});

	it("masks sensitive fields in a URLSearchParams body before logging", async function () {
		fetchMock.post(() => true, { status: 200 });
		const body = new URLSearchParams();
		body.set("username", "admin");
		body.set("password", "secret");
		const request: RestRequestPayload = {
			relativeUrl: "user/local/login",
			method: "POST",
			body,
			extendedData: { sensitiveFields: ["password"] }
		};

		await fetchServerRequest(request);

		const loggedBody = lastLoggedRequest().body as URLSearchParams;
		expect(loggedBody.get("password")).toBe(REDACTED_VALUE);
		expect(loggedBody.get("username")).toBe("admin");
		// The original body keeps the real credential.
		expect(body.get("password")).toBe("secret");
	});

	it("masks multiple sensitive fields at once", async function () {
		fetchMock.post(() => true, { status: 200 });
		const request: RestRequestPayload = {
			relativeUrl: "user/local/login",
			method: "POST",
			body: JSON.stringify({
				username: "admin",
				password: "secret",
				clientSecret: "topsecret"
			}),
			extendedData: { sensitiveFields: ["password", "clientSecret"] }
		};

		await fetchServerRequest(request);

		expect(JSON.parse(lastLoggedRequest().body as string)).toEqual({
			username: "admin",
			password: REDACTED_VALUE,
			clientSecret: REDACTED_VALUE
		});
	});

	it("logs the request unchanged when no sensitiveFields are declared", async function () {
		fetchMock.post(() => true, { status: 200 });
		const request: RestRequestPayload = {
			relativeUrl: "user/local/login",
			method: "POST",
			body: JSON.stringify({ username: "admin", password: "secret" })
		};

		await fetchServerRequest(request);

		expect(lastLoggedRequest()).toBe(request);
	});

	it("logs the request unchanged when sensitiveFields is empty", async function () {
		fetchMock.post(() => true, { status: 200 });
		const request: RestRequestPayload = {
			relativeUrl: "user/local/login",
			method: "POST",
			body: JSON.stringify({ password: "secret" }),
			extendedData: { sensitiveFields: [] }
		};

		await fetchServerRequest(request);

		expect(lastLoggedRequest()).toBe(request);
	});

	it("logs the request unchanged when there is no body", async function () {
		fetchMock.get(() => true, { status: 200 });
		const request: RestRequestPayload = {
			relativeUrl: "user/info",
			method: "GET",
			extendedData: { sensitiveFields: ["password"] }
		};

		await fetchServerRequest(request);

		expect(lastLoggedRequest()).toBe(request);
	});

	it("leaves a non-JSON string body untouched", async function () {
		fetchMock.post(() => true, { status: 200 });
		const request: RestRequestPayload = {
			relativeUrl: "user/local/login",
			method: "POST",
			body: "not-json-password=secret",
			extendedData: { sensitiveFields: ["password"] }
		};

		await fetchServerRequest(request);

		expect(lastLoggedRequest().body).toBe("not-json-password=secret");
	});

	it("leaves the body untouched when none of the sensitive fields are present", async function () {
		fetchMock.post(() => true, { status: 200 });
		const original = JSON.stringify({ username: "admin" });
		const request: RestRequestPayload = {
			relativeUrl: "user/local/login",
			method: "POST",
			body: original,
			extendedData: { sensitiveFields: ["password"] }
		};

		await fetchServerRequest(request);

		// Unchanged body is returned as the very same string instance.
		expect(lastLoggedRequest().body).toBe(original);
	});

	it("dispatchServerRequest resolves with the response text", async function () {
		fetchMock.post(() => true, { status: 200, body: "plain-text" });
		const request: RestRequestPayload = {
			relativeUrl: "some/resource",
			method: "POST"
		};

		await expect(dispatchServerRequest(request)).resolves.toBe("plain-text");
	});

	it("dispatchBlobServerRequest resolves with a Blob of the response", async function () {
		fetchMock.post(() => true, { status: 200, body: "blob-content" });
		const request: RestRequestPayload = {
			relativeUrl: "some/resource",
			method: "POST"
		};

		const blob = await dispatchBlobServerRequest(request);

		// The fetch-mock Response yields a Blob from a different realm than
		// jsdom's global, so assert on behaviour rather than `instanceof`.
		expect(typeof blob.text).toBe("function");
		await expect(blob.text()).resolves.toBe("blob-content");
	});

	it("dispatchAndCheckServerRequest resolves when the response passes the type guard", async function () {
		fetchMock.post(() => true, { status: 200, body: { id: 42 } });
		const request: RestRequestPayload = {
			relativeUrl: "some/resource",
			method: "POST"
		};

		await expect(
			dispatchAndCheckServerRequest(request, isWithId)
		).resolves.toEqual({ id: 42 });
	});

	it("dispatchAndCheckServerRequest rejects when the response fails the type guard", async function () {
		fetchMock.post(() => true, { status: 200, body: { unexpected: true } });
		const request: RestRequestPayload = {
			relativeUrl: "some/resource",
			method: "POST"
		};

		await expect(
			dispatchAndCheckServerRequest(request, isWithId)
		).rejects.toThrow("The server response cannot be interpreted!");
	});
});
