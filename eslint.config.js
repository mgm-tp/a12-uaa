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
 * THIS SOFTWARE IS PROVIDED "AS IS" AND WITHOUT WARRANTY OF ANY KIND,
 * WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT, EXCEPT WHERE SUCH DISCLAIMERS ARE HELD TO BE
 * LEGALLY INVALID. SEE THE RESPECTIVE LICENSE TEXT FOR DETAILS.
 */

import Path from "node:path";
import Fs from "node:fs/promises";

import notice from "eslint-plugin-notice";
import stylistic from "@stylistic/eslint-plugin";
import { fixupPluginRules } from "@eslint/compat";
import unusedImports from "eslint-plugin-unused-imports";
import typedReduxSaga from "@jambit/eslint-plugin-typed-redux-saga";

import { reactStrict as devtoolsReactStrict } from "@com.mgmtp.a12.devtools/eslint-config";

/**
 * @param { import("eslint").Eslint.Plugin } plugin
 */
function injectRuleSchema(plugin) {
	const fixupPlugin = fixupPluginRules(plugin);
	const newRules = Object.fromEntries(
		Object.entries(fixupPlugin.rules).map(([ruleName, rule]) => [
			ruleName,
			{ ...rule, meta: { ...rule.meta, schema: false } }
		])
	);

	return { ...fixupPlugin, rules: newRules };
}

const rawLicense = await Fs.readFile(Path.join(import.meta.dirname, "licenses", "license-header.txt"), "utf-8");
const license =
	"/*\n" +
	rawLicense
		.replace(/\n$/, "")
		.split("\n")
		.map((line) => (line.length > 0 ? ` * ${line}` : " *"))
		.join("\n") +
	"\n */\n";

/** @type { import("eslint").Linter.Config[] } */
export default [
	...devtoolsReactStrict,
	{
		name: "uaa/ignores",
		ignores: [
			"**/lib/",
			"**/dist/",
			"**/build/",
			"**/target/",
			"**/typedoc/",
			"**/coverage/",
			"**/resources/",
			"**/generated/",
			"**/*.js",
			"**/*.mjs",
			"asciidoc/**",
			"devapps/deployment/**",
			"devapps/uaa-example-app/**",
			"devapps/uaa-example-helm/**",
			"devapps/uaa-example-rest-client/**",
			"devapps/uaa-example-server-side-rendering/**",
			"devapps/**/vite.config.ts",
			"jenkins/**",
			"gradle/**",
			"config/**",
			"performance-test/**",
			"uaa-regression-tests/**",
			"uaa-authorization-language-server/**"
		]
	},
	{
		name: "uaa/general",
		languageOptions: {
			parserOptions: {
				project: "tsconfig.json",
				tsconfigRootDir: import.meta.dirname
			}
		},
		linterOptions: {
			reportUnusedDisableDirectives: "off"
		},
		plugins: {
			notice,
			stylistic,
			"unused-imports": unusedImports,
			"typed-redux-saga": typedReduxSaga
		},
		rules: {
			"@typescript-eslint/no-namespace": "off",
			"@typescript-eslint/no-empty-object-type": "off",
			"@typescript-eslint/no-empty-function": "warn",
			"@typescript-eslint/no-explicit-any": "warn",
			"@typescript-eslint/no-unused-vars": ["warn", { vars: "all", args: "none", ignoreRestSiblings: true }],
			curly: "error",
			"no-inner-declarations": "off",
			"react/display-name": "off",
			"react/prop-types": "off",
			"react/react-in-jsx-scope": "off",
			"notice/notice": [
				"error",
				{
					template: license,
					onNonMatchingHeader: "replace",
					chars: license.length
				}
			],
			eqeqeq: "error",
			"no-console": "error",
			"import/order": "off",
			"import/no-extraneous-dependencies": "error",
			"unused-imports/no-unused-imports": "off",
			"no-restricted-imports": [
				"error",
				{
					patterns: ["@com.mgmtp.a12*/**/internal/**", "@com.mgmtp.a12*/**/src/**"]
				}
			],

			"no-implicit-coercion": "off",
			"default-param-last": ["warn"],
			complexity: ["error", { max: 25 }],
			"no-underscore-dangle": ["error", { allowAfterThis: true, allow: ["_roles"] }],
			"no-warning-comments": 0,
			"max-nested-callbacks": ["error", 3],

			"no-shadow": "off",
			"no-undef": "off",
			"@typescript-eslint/no-use-before-define": "off",
			"no-use-before-define": "off",

			"typed-redux-saga/delegate-effects": "error",
			"typed-redux-saga/use-typed-effects": "error",

			"@typescript-eslint/consistent-type-imports": "off",
			"stylistic/padding-line-between-statements": "off"
		}
	},
	{
		name: "uaa/test",
		files: ["**/test/**"],
		rules: {
			"@typescript-eslint/ban-ts-comment": "off",
			"@typescript-eslint/no-non-null-assertion": "off",
			"import/no-extraneous-dependencies": ["error", { devDependencies: true }],
			"no-console": "off",
			"no-restricted-imports": [
				"error",
				{
					patterns: ["@com.mgmtp.a12*/**/internal/**", "@com.mgmtp.a12*/**/src/**"]
				}
			]
		}
	},
	{
		name: "uaa/scripts",
		files: ["**/scripts/**", "**/*Main.ts", "**/utils/runner.ts", "**/utils/utils.ts"],
		rules: {
			"no-console": "off",
			"import/no-extraneous-dependencies": "off",
			"@typescript-eslint/no-require-imports": "off"
		}
	},
	{
		name: "uaa/devapps",
		files: ["devapps/**"],
		rules: {
			"no-console": "off",
			"no-underscore-dangle": "off"
		}
	}
];
