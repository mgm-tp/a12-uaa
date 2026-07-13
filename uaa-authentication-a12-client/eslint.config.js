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
import notice from "eslint-plugin-notice";
import typedReduxSaga from "@jambit/eslint-plugin-typed-redux-saga";
import mochaPlugin from "eslint-plugin-mocha";

import { strict as devtoolsStrict } from "@com.mgmtp.a12.devtools/eslint-config";

/** @type { import("eslint").Linter.Config[] } */
export default [
	...devtoolsStrict,
	mochaPlugin.configs.flat.recommended,
	{
		name: "uaa/ignores",
		ignores: ["**/lib/", "**/build/", "**/silent_renew.js"]
	},
	{
		name: "uaa/general",
		languageOptions: {
			parserOptions: {
				tsconfigRootDir: import.meta.dirname
			}
		},
		plugins: {
			notice,
			"typed-redux-saga": typedReduxSaga
		},
		rules: {
			"@typescript-eslint/no-namespace": "off",
			"@typescript-eslint/no-empty-object-type": "off",
			"@typescript-eslint/no-invalid-void-type": "off",
			"@typescript-eslint/no-explicit-any": "warn",
			"no-restricted-imports": [
				"error",
				{
					patterns: [
						"@com.mgmtp.a12*/**/internal/**",
						"@com.mgmtp.a12*/**/src/**"
					]
				}
			],

			"no-implicit-coercion": "off",
			"default-param-last": ["warn"],
			complexity: ["error", { max: 25 }],
			"no-underscore-dangle": [
				"error",
				{ allowAfterThis: true, allow: ["_roles"] }
			],
			"no-warning-comments": 0,
			"max-nested-callbacks": ["error", 3],
			"no-unused-vars": "off",
			"@typescript-eslint/no-unused-vars": [
				"error",
				{ vars: "all", args: "none", ignoreRestSiblings: true }
			],
			camelcase: ["warn"],
			"mocha/max-top-level-suites": ["warn", { limit: 2 }],
			"notice/notice": [
				"error",
				{
					templateFile: "license/copyright.js"
				}
			],

			// old overrides
			"no-shadow": "off",
			"no-undef": "off",
			"@typescript-eslint/no-use-before-define": "off",
			"no-use-before-define": "off",

			"typed-redux-saga/delegate-effects": "error",
			"typed-redux-saga/use-typed-effects": "error",
			"react/react-in-jsx-scope": "off"
		}
	},
	{
		name: "uaa/test",
		files: ["test/**"],
		rules: {
			"@typescript-eslint/ban-ts-comment": "off",
			"@typescript-eslint/no-non-null-assertion": "off"
		}
	}
];
