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
	ConnectorLocator,
	RestRequestPayload,
	RestServerConnector
} from "@com.mgmtp.a12.utils/utils-connector";
import { LoggerFactory } from "@com.mgmtp.a12.utils/utils-logging";

const logger = LoggerFactory.getLogger("UAA/Serverrequest");

const REDACTED_VALUE = "***";

export interface ErrorPayload {
	readonly operationType: "loading" | "saving";
	readonly error: unknown;
}

type TypeGuard<T, U extends T> = (obj: T) => obj is U;

/**
 * Returns a copy of the body where the values of the given field names are
 * replaced with a placeholder, leaving the rest of the body intact. Supports
 * JSON-string bodies and `URLSearchParams` bodies; any other body type is
 * returned unchanged.
 *
 * @param body
 * @param sensitiveFields
 */
function redactBody(
	body: unknown,
	sensitiveFields: readonly string[]
): unknown {
	if (body instanceof URLSearchParams) {
		const redacted = new URLSearchParams(body);
		for (const field of sensitiveFields) {
			if (redacted.has(field)) {
				redacted.set(field, REDACTED_VALUE);
			}
		}
		return redacted;
	}
	if (typeof body === "string") {
		try {
			const parsed: unknown = JSON.parse(body);
			if (parsed && typeof parsed === "object") {
				let changed = false;
				const record = parsed as Record<string, unknown>;
				for (const field of sensitiveFields) {
					if (field in record) {
						record[field] = REDACTED_VALUE;
						changed = true;
					}
				}
				return changed ? JSON.stringify(record) : body;
			}
		} catch {
			// Body is not JSON, nothing to redact field-wise.
		}
	}
	return body;
}

/**
 * Returns a copy of the request that is safe to log. Credential attributes
 * declared via `extendedData.sensitiveFields` (e.g. `password`) have their
 * value masked in the logged body, while the request actually sent to the
 * server stays untouched.
 *
 * @param request
 */
function toLoggableRequest(request: RestRequestPayload): RestRequestPayload {
	const extendedData = request.extendedData as
		| { readonly sensitiveFields?: readonly string[] }
		| undefined;
	const sensitiveFields = extendedData?.sensitiveFields;
	if (!sensitiveFields?.length || request.body === undefined) {
		return request;
	}
	return { ...request, body: redactBody(request.body, sensitiveFields) };
}

/**
 * @param request
 * @internal
 */
export async function fetchServerRequest(
	request: RestRequestPayload
): Promise<Response> {
	logger.log("Request", toLoggableRequest(request));
	let modifiedRequest: RestRequestPayload = request;
	const baseUrl = (
		ConnectorLocator.getInstance().getServerConnector() as RestServerConnector
	).getBaseUrl();
	if (baseUrl.slice(-1) !== "/") {
		modifiedRequest = {
			...request,
			relativeUrl: `/${request.relativeUrl}`
		};
	}
	const response = await (
		ConnectorLocator.getInstance().getServerConnector() as RestServerConnector
	).fetchData(modifiedRequest);
	logger.log("Response", response);

	/**
	 * NOTE: The RestServerConnector uses an error filter that will always reject
	 * if the fetch `response.ok` is `false`. Therefore, the following code will
	 * not be reached.
	 *
	 * However, we cannot guarantee this for the future therefore we will keep it.
	 */
	if (!response.ok) {
		throw new Error(response.statusText);
	}

	return response;
}

/**
 * @param request
 * @internal
 */
export async function dispatchServerRequest(
	request: RestRequestPayload
): Promise<string> {
	return (await fetchServerRequest(request)).text();
}

/**
 * @param request
 * @internal
 */
export async function dispatchBlobServerRequest(
	request: RestRequestPayload
): Promise<Blob> {
	return (await fetchServerRequest(request)).blob();
}

/**
 * @param request
 * @param responseChecker
 * @internal
 */
export async function dispatchAndCheckServerRequest<T>(
	request: RestRequestPayload,
	responseChecker: TypeGuard<T | Record<string, unknown>, T>
): Promise<T> {
	const response = await fetchServerRequest(request);
	const data = await response.json();
	if (!responseChecker(data)) {
		return Promise.reject(
			new Error("The server response cannot be interpreted!")
		);
	}
	return data;
}
