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
import * as antlr from "antlr4ng";
import { CharStream, CommonTokenStream, ParseTreeWalker } from "antlr4ng";
import { ASTNode, TextDocument } from "vscode-json-languageservice";
import { Diagnostic } from "vscode-languageserver";

import { UaaExpressionListener } from "./uaaExpressionListener.js";
import { SpringExpressionsLexer } from "./generated/spring/SpringExpressionsLexer.js";
import { SpringExpressionsParser } from "./generated/spring/SpringExpressionsParser.js";
import { PropertyReferencesLexer } from "./generated/reference/PropertyReferencesLexer.js";
import { PropertyReferencesParser } from "./generated/reference/PropertyReferencesParser.js";

const initializeListener = (document: TextDocument, node: ASTNode) => {
	const listener = new UaaExpressionListener();
	const startPosition = document.positionAt(node.offset);

	listener.overallLine = startPosition.line;
	listener.overallColumn = startPosition.character;
	listener.documentOffset = node.offset;

	return listener;
};

const parseGrammar = <
	T extends {
		removeErrorListeners: () => void;
		addErrorListener: (listener: any) => void;
	}
>(
	parser: T,
	listener: UaaExpressionListener,
	parseMethod: () => antlr.ParserRuleContext
): Diagnostic[] => {
	parser.removeErrorListeners();
	parser.addErrorListener(listener);

	try {
		new ParseTreeWalker().walk(listener, parseMethod());
	} catch (error) {
		console.error("Parsing error:", error);
	}

	return listener.getSyntaxErrors();
};

export const springExpressionGrammarCheck = (
	document: TextDocument,
	node: ASTNode,
	content: string
): Diagnostic[] => {
	if (!content) return [];

	const chars = CharStream.fromString(content);
	const lexer = new SpringExpressionsLexer(chars);
	const tokens = new CommonTokenStream(lexer);
	const parser = new SpringExpressionsParser(tokens);
	const listener = initializeListener(document, node);
	return parseGrammar(parser, listener, () => parser.expression());
};

export const propertyReferenceGrammarCheck = (
	document: TextDocument,
	node: ASTNode,
	content: string
): Diagnostic[] => {
	if (!content) return [];

	const chars = CharStream.fromString(content);
	const lexer = new PropertyReferencesLexer(chars);
	const tokens = new CommonTokenStream(lexer);
	const parser = new PropertyReferencesParser(tokens);
	const listener = initializeListener(document, node);

	return parseGrammar(parser, listener, () => parser.references());
};
