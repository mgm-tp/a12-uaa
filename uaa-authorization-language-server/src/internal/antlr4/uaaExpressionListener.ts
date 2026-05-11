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
	ATNConfigSet,
	BaseErrorListener,
	BitSet,
	DFA,
	ErrorNode,
	Parser,
	ParserRuleContext,
	ParseTreeListener,
	TerminalNode
} from "antlr4ng";
import { Diagnostic, DiagnosticSeverity, Range } from "vscode-languageserver";

import { SpringExpressionsListener } from "./generated/spring/SpringExpressionsListener.js";

export class UaaExpressionListener
	implements SpringExpressionsListener, ParseTreeListener, BaseErrorListener
{
	enterEveryRule(node: ParserRuleContext): void {}

	exitEveryRule(node: ParserRuleContext): void {}

	visitErrorNode(node: ErrorNode): void {}

	visitTerminal(node: TerminalNode): void {}
	reportAmbiguity(
		recognizer: Parser,
		dfa: DFA,
		startIndex: number,
		stopIndex: number,
		exact: boolean,
		ambigAlts: BitSet | undefined,
		configs: ATNConfigSet
	): void {}

	reportAttemptingFullContext(
		recognizer: Parser,
		dfa: DFA,
		startIndex: number,
		stopIndex: number,
		conflictingAlts: BitSet | undefined,
		configs: ATNConfigSet
	): void {}

	reportContextSensitivity(
		recognizer: Parser,
		dfa: DFA,
		startIndex: number,
		stopIndex: number,
		prediction: number,
		configs: ATNConfigSet
	): void {}
	stacks: Diagnostic[] = [];
	private _overallLine: number | undefined;
	private _overallColumn: number | undefined;
	private _documentOffset: number = 0; // Thêm offset của document

	public getSyntaxErrors(): Diagnostic[] {
		return this.stacks;
	}
	get overallColumn(): number | undefined {
		return this._overallColumn;
	}

	set overallColumn(value: number | undefined) {
		this._overallColumn = value;
	}

	get overallLine(): number | undefined {
		return this._overallLine;
	}

	set overallLine(value: number | undefined) {
		this._overallLine = value;
	}

	// Thêm setter/getter cho document offset
	set documentOffset(value: number) {
		this._documentOffset = value;
	}

	get documentOffset(): number {
		return this._documentOffset;
	}

	syntaxError(
		recognizer: any,
		offendingSymbol: any,
		line: number,
		column: number,
		msg: string,
		e: any
	): void {
		const startColumn = this._overallColumn
			? this._overallColumn + column
			: column;

		const symbolLength = offendingSymbol?.text?.length || 1;

		this.stacks.push({
			severity: DiagnosticSeverity.Error,
			range: Range.create(
				this._overallLine ?? line,
				startColumn,
				this._overallLine ?? line,
				startColumn + symbolLength
			),
			message: msg,
			source: "spring-expression-validator"
		});
	}
}
