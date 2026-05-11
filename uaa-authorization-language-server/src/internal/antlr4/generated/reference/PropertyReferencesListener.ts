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
// Generated from ./src/internal/antlr4/generated/g4/PropertyReferences.g4 by ANTLR 4.13.1

import {
	ErrorNode,
	ParseTreeListener,
	ParserRuleContext,
	TerminalNode
} from "antlr4ng";

import { ReferencesContext } from "./PropertyReferencesParser.js";
import { StringReferencesContext } from "./PropertyReferencesParser.js";
import { SentencesContext } from "./PropertyReferencesParser.js";

/**
 * This interface defines a complete listener for a parse tree produced by
 * `PropertyReferencesParser`.
 */
export class PropertyReferencesListener implements ParseTreeListener {
	/**
	 * Enter a parse tree produced by `PropertyReferencesParser.references`.
	 * @param ctx the parse tree
	 */
	enterReferences?: (ctx: ReferencesContext) => void;
	/**
	 * Exit a parse tree produced by `PropertyReferencesParser.references`.
	 * @param ctx the parse tree
	 */
	exitReferences?: (ctx: ReferencesContext) => void;
	/**
	 * Enter a parse tree produced by `PropertyReferencesParser.stringReferences`.
	 * @param ctx the parse tree
	 */
	enterStringReferences?: (ctx: StringReferencesContext) => void;
	/**
	 * Exit a parse tree produced by `PropertyReferencesParser.stringReferences`.
	 * @param ctx the parse tree
	 */
	exitStringReferences?: (ctx: StringReferencesContext) => void;
	/**
	 * Enter a parse tree produced by `PropertyReferencesParser.sentences`.
	 * @param ctx the parse tree
	 */
	enterSentences?: (ctx: SentencesContext) => void;
	/**
	 * Exit a parse tree produced by `PropertyReferencesParser.sentences`.
	 * @param ctx the parse tree
	 */
	exitSentences?: (ctx: SentencesContext) => void;

	visitTerminal(node: TerminalNode): void {}
	visitErrorNode(node: ErrorNode): void {}
	enterEveryRule(node: ParserRuleContext): void {}
	exitEveryRule(node: ParserRuleContext): void {}
}
