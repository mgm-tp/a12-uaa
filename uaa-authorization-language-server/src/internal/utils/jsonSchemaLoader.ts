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
import path from "node:path";
import fs from "node:fs";

import { JSONSchema } from "vscode-json-languageservice";
import { fileURLToPath } from 'node:url';
import { dirname as pathDirname } from 'node:path';

const __filename = fileURLToPath(import.meta.url);
const __dirname  = pathDirname(__filename);
// Load the schemas
const basePath = path.resolve(__dirname);
const authorizationSchemaPath = path.join(
	basePath,
	"../assets/schemas/authorization-definition-schema.json"
);
const permissionSchemaPath = path.join(
	basePath,
	"../assets/schemas/permission-schema.json"
);
const policySchemaPath = path.join(
	basePath,
	"../assets/schemas/policy-schema.json"
);
const propertyPermissionSchemaPath = path.join(
	basePath,
	"../assets/schemas/propertyPermission-schema.json"
);
const propertyRightSchemaPath = path.join(
	basePath,
	"../assets/schemas/propertyRight-schema.json"
);
const repositoryPolicySchemaPath = path.join(
	basePath,
	"../assets/schemas/repositoryPolicy-schema.json"
);

// Reading and storing schema content
const schemas: { [uri: string]: JSONSchema } = {
	"file://authorization-definition-schema.json": JSON.parse(
		fs.readFileSync(authorizationSchemaPath, "utf-8")
	),
	"file://permission-schema.json": JSON.parse(
		fs.readFileSync(permissionSchemaPath, "utf-8")
	),
	"file://policy-schema.json": JSON.parse(
		fs.readFileSync(policySchemaPath, "utf-8")
	),
	"file://propertyPermission-schema.json": JSON.parse(
		fs.readFileSync(propertyPermissionSchemaPath, "utf-8")
	),
	"file://propertyRight-schema.json": JSON.parse(
		fs.readFileSync(propertyRightSchemaPath, "utf-8")
	),
	"file://repositoryPolicy-schema.json": JSON.parse(
		fs.readFileSync(repositoryPolicySchemaPath, "utf-8")
	)
};

export const schemasConfiguration = [
	{
		uri: "file://authorization-definition-schema.json",
		fileMatch: ["*.json"],
		schema: schemas["file://authorization-definition-schema.json"]
	},
	{
		uri: "file://permission-schema.json",
		schema: schemas["file://permission-schema.json"]
	},
	{
		uri: "file://policy-schema.json",
		schema: schemas["file://policy-schema.json"]
	},
	{
		uri: "file://propertyPermission-schema.json",
		schema: schemas["file://propertyPermission-schema.json"]
	},
	{
		uri: "file://propertyRight-schema.json",
		schema: schemas["file://propertyRight-schema.json"]
	},
	{
		uri: "file://repositoryPolicy-schema.json",
		schema: schemas["file://repositoryPolicy-schema.json"]
	}
];
