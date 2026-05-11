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
const BASE_TYPE = {
	NAME: "name",
	DESCRIPTION: "description"
} as const;

export const PROPERTY_REFS = {
	POLICY_REFS: "policy-refs",
	REPOSITORY_REFS: "repository-refs",
	RIGHTS_REFS: "rights-refs"
} as const;

export const AUTHORIZATION_TYPE = {
	...BASE_TYPE,
	POLICES: "policies",
	REPOSITORY_POLICIES: "repositoryPolicies",
	PERMISSIONS: "permissions",
	PROPERTY_RIGHTS: "propertyRights",
	PROPERTY_PERMISSIONS: "propertyPermissions"
} as const;

export const POLICY_TYPE = {
	...BASE_TYPE,
	TARGET: "target",
	DATA_PRELOAD: "dataPreload",
	RULES: "rules",
	TYPE: "type"
};

export const REPOSITORY_TYPE = {
	...BASE_TYPE,
	TARGET: "target",
	DATA_PRELOAD: "dataPreload",
	TEMPLATES: "templates"
};

export const PROPERTY_RIGHT_TYPE = {
	...BASE_TYPE,
	RIGHTS: "rights"
};
