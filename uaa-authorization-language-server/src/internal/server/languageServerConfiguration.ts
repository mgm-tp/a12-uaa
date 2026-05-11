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
import fs from "node:fs";

import {
	Connection,
	DefinitionParams,
	InitializeParams,
	InitializeResult,
	ServerCapabilities,
	TextDocuments,
	TextDocumentSyncKind
} from "vscode-languageserver";
import {
	getLanguageService
} from "vscode-json-languageservice";
import { ClientCapabilities, TextDocument } from "vscode-json-languageservice/lib/esm/jsonLanguageTypes.js";

import { URI, Utils } from "vscode-uri";

import { RuntimeEnvironment } from "../interfaces/server.js";
import { schemasConfiguration } from "../utils/jsonSchemaLoader.js";
import { validateTextDocumentHandler } from "../action/validation/validateTextDocumentHandler.js";
import { onCompletionHandler } from "../action/suggestion/completion.js";
import { onHoverHandler } from "../action/hover/hover.js";
import { onDefinitionHandler } from "../action/definition/definition.js";
import { onSaveHandler } from "../action/save/save.js";
import {
	semanticTokenProvider,
	tokenModifiers,
	tokenTypes
} from "../action/color/semanticTokens.js";

const workspaceContext = {
	resolveRelativePath: (relativePath: string, resource: string) => {
		const base = resource.substring(0, resource.lastIndexOf("/") + 1);
		return Utils.resolvePath(URI.parse(base), relativePath).toString();
	}
};

export function startServer(
	connection: Connection,
	runtime: RuntimeEnvironment
) {
	// Create a text document manager.
	const documents = new TextDocuments(TextDocument);

	// create the language service
	const languageService = getLanguageService({
		schemaRequestService: (uri: string): Promise<string> => {
			return new Promise((resolve, reject) => {
				fs.readFile(uri, "utf-8", (err, content) => {
					if (err) {
						reject(`Unable to load schema from ${uri}`);
					} else {
						resolve(content);
					}
				});
			});
		},
		workspaceContext,
		contributions: [],
		clientCapabilities: ClientCapabilities.LATEST
	});

	languageService.configure({
		schemas: schemasConfiguration
	});

	let clientSnippetSupport = false;

	// After the server has started the client sends an initialize request. The server receives
	// in the past params the rootPath of the workspace plus the client capabilities.
	connection.onInitialize((params: InitializeParams): InitializeResult => {
		function getClientCapability<T>(name: string, def: T) {
			const keys = name.split(".");
			let c: any = params.capabilities;
			for (let i = 0; c && i < keys.length; i++) {
				if (!Object.prototype.hasOwnProperty.call(c, keys[i])) {
					return def;
				}
				c = c[keys[i]];
			}
			return c;
		}

		clientSnippetSupport = getClientCapability(
			"textDocument.completion.completionItem.snippetSupport",
			false
		);

		const capabilities: ServerCapabilities = {
			textDocumentSync: {
				change: TextDocumentSyncKind.Incremental,
				openClose: true,
				save: true
			},
			completionProvider: clientSnippetSupport
				? {
						completionItem: {
							labelDetailsSupport: true
						},
						resolveProvider: false, // turn off resolving as the current language service doesn't do anything on resolve. Also fixes #91747
						triggerCharacters: ['"', ":", ".", "#"]
					}
				: undefined,
			hoverProvider: true,
			definitionProvider: true,
			semanticTokensProvider: {
				full: true,
				legend: {
					tokenTypes: tokenTypes,
					tokenModifiers: tokenModifiers
				}
			}
		};
		return { capabilities };
	});
	documents.onDidOpen(event => {
		validateTextDocumentHandler(
			documents,
			event.document,
			languageService,
			connection
		);
	});

	// // The content of a text document has changed. This event is emitted
	// // when the text document first opened or when its content has changed.
	documents.onDidChangeContent(async event => {
		validateTextDocumentHandler(
			documents,
			event.document,
			languageService,
			connection
		);
		onCompletionHandler(documents, languageService);
	});

	documents.onDidSave(event => {
		onSaveHandler(event.document, connection);
	});

	connection.onCompletion(onCompletionHandler(documents, languageService));

	connection.onDefinition((definitionParams: DefinitionParams) =>
		onDefinitionHandler(documents, definitionParams, languageService)
	);

	connection.onHover(onHoverHandler(documents, languageService, runtime));

	connection.languages.semanticTokens.on(params => {
		const result = semanticTokenProvider(documents, languageService, params);
		return Promise.resolve(
			result || {
				data: []
			}
		);
	});

	documents.listen(connection);

	// Listen on the connection
	connection.listen();
}
