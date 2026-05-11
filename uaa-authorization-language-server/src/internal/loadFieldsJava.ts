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
import { allCompletionFields } from "../uaaAuthorizationServerMain.js";

import { loadMetadata, resolveMetadataClass } from "./utils/utils.js";
import { JavaClassMetadata } from "./interfaces/metadata.js";

export const fieldsOfJavaResource: Set<string> = new Set();

export const resourcesMetadata: JavaClassMetadata[] = [];

export function preloadFieldsJava(): void {
	try {
		const metadataObject = loadMetadata();
		if (metadataObject === undefined) {
			return;
		}
		const principalPath = metadataObject.principal;
		const resourcePaths = metadataObject.resource;
		if (principalPath) {
			const principalMetadata = resolveMetadataClass(principalPath);

			// Get all fields of principal
			const fieldsOfPrincipal: Set<string> = new Set();
			Array.from(principalMetadata.fields).forEach(value => {
				fieldsOfJavaResource.add(value);
				fieldsOfPrincipal.add(value);
			});

			// Get all methods of principal
			const methodsOfPrincipal: Set<string> = new Set();
			Array.from(principalMetadata.methods).forEach(value =>
				methodsOfPrincipal.add(value)
			);

			allCompletionFields.push({
				name: principalMetadata.className || "",
				completionLabels: fieldsOfPrincipal
			});
		}
		if (resourcePaths) {
			resourcePaths.forEach(resourceJavaPath => {
				const fieldOfResource: Set<string> = new Set();
				const resourceJava = resolveMetadataClass(resourceJavaPath.path);
				Array.from(resourceJava.fields).forEach(value => {
					fieldsOfJavaResource.add(value);
					fieldOfResource.add(value);
				});
				const methodsOfResource: Set<string> = new Set();
				Array.from(resourceJava.methods).forEach(value =>
					methodsOfResource.add(value)
				);
				allCompletionFields.push({
					name: resourceJava.className || "",
					completionLabels: fieldOfResource
				});

				resourcesMetadata.push({
					className: resourceJava.className,
					classNamePosition: resourceJava.classNamePosition,
					fields: fieldOfResource,
					methods: methodsOfResource
				});
			});
		}
	} catch (err) {
		console.error(`Error during preload: ${err}`);
	}
}
