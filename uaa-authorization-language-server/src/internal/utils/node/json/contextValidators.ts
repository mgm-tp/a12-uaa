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
	AUTHORIZATION_TYPE,
	POLICY_TYPE,
	PROPERTY_REFS,
	PROPERTY_RIGHT_TYPE,
	REPOSITORY_TYPE
} from "../../../constants/authorization.js";

export class ContextValidator {
	private constructor() {}

	static isInPolicyRefsContext(text: string): boolean {
		return text === PROPERTY_REFS.POLICY_REFS;
	}

	static isInRepositoryRefsContext(text: string): boolean {
		return text === PROPERTY_REFS.REPOSITORY_REFS;
	}

	static isInRightsRefsContext(text: string): boolean {
		return text === PROPERTY_REFS.RIGHTS_REFS;
	}

	static isInNameOfPolicyContext(text: string[]): boolean {
		return (
			text[1] === AUTHORIZATION_TYPE.POLICES && text[0] === POLICY_TYPE.NAME
		);
	}

	static isInNameOfRepositoryContext(text: string[]): boolean {
		return (
			text[1] === AUTHORIZATION_TYPE.REPOSITORY_POLICIES &&
			text[0] === REPOSITORY_TYPE.NAME
		);
	}

	static isInNameOfRightContext(text: string[]): boolean {
		return (
			text[1] === AUTHORIZATION_TYPE.PROPERTY_RIGHTS &&
			text[0] === PROPERTY_RIGHT_TYPE.NAME
		);
	}
}
