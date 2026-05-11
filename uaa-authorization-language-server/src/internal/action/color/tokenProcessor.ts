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
import { ASTNode, TextDocument } from "vscode-json-languageservice";
import { SemanticTokensBuilder } from "vscode-languageserver/node.js";
import { CharStream, CommonTokenStream } from "antlr4ng";
import { SemanticTokenTypes } from "vscode-languageserver";

import { SpringExpressionsLexer } from "../../antlr4/generated/spring/SpringExpressionsLexer.js";
import { SpringExpressionsParser } from "../../antlr4/generated/spring/SpringExpressionsParser.js";
import { PropertyReferencesLexer } from "../../antlr4/generated/reference/PropertyReferencesLexer.js";
import { PropertyReferencesParser } from "../../antlr4/generated/reference/PropertyReferencesParser.js";

import { tokenTypes } from "./semanticTokens.js";

interface TokenProcessorConfig {
	lexer: any;
	parser: any;
	parserMethod: (parserInstance: any) => void;
	tokenTypesMap: Record<number, string>;
	defaultTokenType: string;
}

function processTokens(
	document: TextDocument,
	node: ASTNode,
	builder: SemanticTokensBuilder,
	content: string,
	config: TokenProcessorConfig
): void {
	if (!content) return;

	const chars = CharStream.fromString(content);
	const lexerInstance = new config.lexer(chars);
	const tokens = new CommonTokenStream(lexerInstance);
	const parserInstance = new config.parser(tokens);

	config.parserMethod(parserInstance);

	tokens.getTokens().forEach(token => {
		if (!config.tokenTypesMap[token.type]) {
			return;
		}
		const tokenPosition = document.positionAt(node.offset);
		const line = tokenPosition.line;
		const startChar = tokenPosition.character + token.start + 1;
		const length = token.text?.length || 0;

		const tokenType = config.tokenTypesMap[token.type];

		builder.push(line, startChar, length, tokenTypes.indexOf(tokenType), 0);
	});

	const basePosition = document.positionAt(node.offset);

	builder.push(
		basePosition.line,
		basePosition.character + 1,
		content.length,
		tokenTypes.indexOf(config.defaultTokenType),
		0
	);
}

export function processSpELExpression(
	document: TextDocument,
	node: ASTNode,
	builder: SemanticTokensBuilder,
	content: string
): void {
	processTokens(document, node, builder, content, {
		lexer: SpringExpressionsLexer,
		parser: SpringExpressionsParser,
		parserMethod: parser => parser.expression(),
		tokenTypesMap: {
			[SpringExpressionsLexer.AND]: SemanticTokenTypes.operator,
			[SpringExpressionsLexer.OR]: SemanticTokenTypes.operator,
			[SpringExpressionsLexer.EQUALS]: SemanticTokenTypes.operator,
			[SpringExpressionsLexer.NOT_EQUALS]: SemanticTokenTypes.operator,
			[SpringExpressionsLexer.NOT]: SemanticTokenTypes.operator,
			[SpringExpressionsLexer.GT]: SemanticTokenTypes.operator,
			[SpringExpressionsLexer.LT]: SemanticTokenTypes.operator,
			[SpringExpressionsLexer.GTE]: SemanticTokenTypes.operator,
			[SpringExpressionsLexer.LTE]: SemanticTokenTypes.operator,
			[SpringExpressionsLexer.PLUS]: SemanticTokenTypes.operator,
			[SpringExpressionsLexer.MINUS]: SemanticTokenTypes.operator,
			[SpringExpressionsLexer.DOT]: SemanticTokenTypes.operator,
			[SpringExpressionsLexer.COMMA]: SemanticTokenTypes.operator,
			[SpringExpressionsLexer.INSTANCEOF]: SemanticTokenTypes.operator,
			[SpringExpressionsLexer.BETWEEN]: SemanticTokenTypes.operator,
			[SpringExpressionsLexer.MATCHES]: SemanticTokenTypes.operator
		},
		defaultTokenType: SemanticTokenTypes.regexp
	});
}

export function processPropertyReferences(
	document: TextDocument,
	node: ASTNode,
	builder: SemanticTokensBuilder,
	content: string
): void {
	processTokens(document, node, builder, content, {
		lexer: PropertyReferencesLexer,
		parser: PropertyReferencesParser,
		parserMethod: parser => parser.references(),
		tokenTypesMap: {
			[PropertyReferencesLexer.AND]: SemanticTokenTypes.operator,
			[PropertyReferencesLexer.OR]: SemanticTokenTypes.operator,
			[PropertyReferencesLexer.DOT]: SemanticTokenTypes.operator,
			[PropertyReferencesLexer.PLUS]: SemanticTokenTypes.operator,
			[PropertyReferencesLexer.COLON]: SemanticTokenTypes.operator
		},
		defaultTokenType: SemanticTokenTypes.keyword
	});
}
