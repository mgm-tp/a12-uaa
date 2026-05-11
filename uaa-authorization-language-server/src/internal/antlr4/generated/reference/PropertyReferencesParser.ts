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

import * as antlr from "antlr4ng";

import { PropertyReferencesListener } from "./PropertyReferencesListener.js";
// for running tests with parameters, TODO: discuss strategy for typed parameters in CI

export class PropertyReferencesParser extends antlr.Parser {
	public static readonly AND = 1;
	public static readonly OR = 2;
	public static readonly EQUALS = 3;
	public static readonly NOT_EQUALS = 4;
	public static readonly GT = 5;
	public static readonly LT = 6;
	public static readonly GTE = 7;
	public static readonly LTE = 8;
	public static readonly LPAREN = 9;
	public static readonly RPAREN = 10;
	public static readonly PLUS = 11;
	public static readonly MINUS = 12;
	public static readonly DOT = 13;
	public static readonly COMMA = 14;
	public static readonly COLON = 15;
	public static readonly AT_SIGN = 16;
	public static readonly IDENTIFIER = 17;
	public static readonly STRING_LITERAL = 18;
	public static readonly NUMBER = 19;
	public static readonly BOOLEAN = 20;
	public static readonly WS = 21;
	public static readonly INVALID_TOKEN = 22;
	public static readonly RULE_references = 0;
	public static readonly RULE_stringReferences = 1;
	public static readonly RULE_sentences = 2;

	public static readonly literalNames = [
		null,
		"'&&'",
		"'||'",
		"'=='",
		"'!='",
		"'>'",
		"'<'",
		"'>='",
		"'<='",
		"'('",
		"')'",
		"'+'",
		"'-'",
		"'.'",
		"','",
		"':'",
		"'@'"
	];

	public static readonly symbolicNames = [
		null,
		"AND",
		"OR",
		"EQUALS",
		"NOT_EQUALS",
		"GT",
		"LT",
		"GTE",
		"LTE",
		"LPAREN",
		"RPAREN",
		"PLUS",
		"MINUS",
		"DOT",
		"COMMA",
		"COLON",
		"AT_SIGN",
		"IDENTIFIER",
		"STRING_LITERAL",
		"NUMBER",
		"BOOLEAN",
		"WS",
		"INVALID_TOKEN"
	];
	public static readonly ruleNames = [
		"references",
		"stringReferences",
		"sentences"
	];

	public get grammarFileName(): string {
		return "PropertyReferences.g4";
	}
	public get literalNames(): (string | null)[] {
		return PropertyReferencesParser.literalNames;
	}
	public get symbolicNames(): (string | null)[] {
		return PropertyReferencesParser.symbolicNames;
	}
	public get ruleNames(): string[] {
		return PropertyReferencesParser.ruleNames;
	}
	public get serializedATN(): number[] {
		return PropertyReferencesParser._serializedATN;
	}

	protected createFailedPredicateException(
		predicate?: string,
		message?: string
	): antlr.FailedPredicateException {
		return new antlr.FailedPredicateException(this, predicate, message);
	}

	public constructor(input: antlr.TokenStream) {
		super(input);
		this.interpreter = new antlr.ParserATNSimulator(
			this,
			PropertyReferencesParser._ATN,
			PropertyReferencesParser.decisionsToDFA,
			new antlr.PredictionContextCache()
		);
	}
	public references(): ReferencesContext {
		const localContext = new ReferencesContext(this.context, this.state);
		this.enterRule(localContext, 0, PropertyReferencesParser.RULE_references);
		try {
			this.enterOuterAlt(localContext, 1);
			{
				this.state = 6;
				this.stringReferences();
				this.state = 7;
				this.match(PropertyReferencesParser.EOF);
			}
		} catch (re) {
			if (re instanceof antlr.RecognitionException) {
				this.errorHandler.reportError(this, re);
				this.errorHandler.recover(this, re);
			} else {
				throw re;
			}
		} finally {
			this.exitRule();
		}
		return localContext;
	}
	public stringReferences(): StringReferencesContext {
		const localContext = new StringReferencesContext(this.context, this.state);
		this.enterRule(
			localContext,
			2,
			PropertyReferencesParser.RULE_stringReferences
		);
		let _la: number;
		try {
			this.enterOuterAlt(localContext, 1);
			{
				this.state = 9;
				this.sentences();
				this.state = 14;
				this.errorHandler.sync(this);
				_la = this.tokenStream.LA(1);
				while (_la === 1 || _la === 2) {
					{
						{
							this.state = 10;
							_la = this.tokenStream.LA(1);
							if (!(_la === 1 || _la === 2)) {
								this.errorHandler.recoverInline(this);
							} else {
								this.errorHandler.reportMatch(this);
								this.consume();
							}
							this.state = 11;
							this.sentences();
						}
					}
					this.state = 16;
					this.errorHandler.sync(this);
					_la = this.tokenStream.LA(1);
				}
			}
		} catch (re) {
			if (re instanceof antlr.RecognitionException) {
				this.errorHandler.reportError(this, re);
				this.errorHandler.recover(this, re);
			} else {
				throw re;
			}
		} finally {
			this.exitRule();
		}
		return localContext;
	}
	public sentences(): SentencesContext {
		const localContext = new SentencesContext(this.context, this.state);
		this.enterRule(localContext, 4, PropertyReferencesParser.RULE_sentences);
		let _la: number;
		try {
			this.enterOuterAlt(localContext, 1);
			{
				this.state = 17;
				this.match(PropertyReferencesParser.IDENTIFIER);
				this.state = 24;
				this.errorHandler.sync(this);
				_la = this.tokenStream.LA(1);
				while (_la === 17 || _la === 21) {
					{
						{
							this.state = 19;
							this.errorHandler.sync(this);
							_la = this.tokenStream.LA(1);
							if (_la === 21) {
								{
									this.state = 18;
									this.match(PropertyReferencesParser.WS);
								}
							}

							this.state = 21;
							this.match(PropertyReferencesParser.IDENTIFIER);
						}
					}
					this.state = 26;
					this.errorHandler.sync(this);
					_la = this.tokenStream.LA(1);
				}
			}
		} catch (re) {
			if (re instanceof antlr.RecognitionException) {
				this.errorHandler.reportError(this, re);
				this.errorHandler.recover(this, re);
			} else {
				throw re;
			}
		} finally {
			this.exitRule();
		}
		return localContext;
	}

	public static readonly _serializedATN: number[] = [
		4, 1, 22, 28, 2, 0, 7, 0, 2, 1, 7, 1, 2, 2, 7, 2, 1, 0, 1, 0, 1, 0, 1, 1, 1,
		1, 1, 1, 5, 1, 13, 8, 1, 10, 1, 12, 1, 16, 9, 1, 1, 2, 1, 2, 3, 2, 20, 8, 2,
		1, 2, 5, 2, 23, 8, 2, 10, 2, 12, 2, 26, 9, 2, 1, 2, 0, 0, 3, 0, 2, 4, 0, 1,
		1, 0, 1, 2, 27, 0, 6, 1, 0, 0, 0, 2, 9, 1, 0, 0, 0, 4, 17, 1, 0, 0, 0, 6, 7,
		3, 2, 1, 0, 7, 8, 5, 0, 0, 1, 8, 1, 1, 0, 0, 0, 9, 14, 3, 4, 2, 0, 10, 11,
		7, 0, 0, 0, 11, 13, 3, 4, 2, 0, 12, 10, 1, 0, 0, 0, 13, 16, 1, 0, 0, 0, 14,
		12, 1, 0, 0, 0, 14, 15, 1, 0, 0, 0, 15, 3, 1, 0, 0, 0, 16, 14, 1, 0, 0, 0,
		17, 24, 5, 17, 0, 0, 18, 20, 5, 21, 0, 0, 19, 18, 1, 0, 0, 0, 19, 20, 1, 0,
		0, 0, 20, 21, 1, 0, 0, 0, 21, 23, 5, 17, 0, 0, 22, 19, 1, 0, 0, 0, 23, 26,
		1, 0, 0, 0, 24, 22, 1, 0, 0, 0, 24, 25, 1, 0, 0, 0, 25, 5, 1, 0, 0, 0, 26,
		24, 1, 0, 0, 0, 3, 14, 19, 24
	];

	private static __ATN: antlr.ATN;
	public static get _ATN(): antlr.ATN {
		if (!PropertyReferencesParser.__ATN) {
			PropertyReferencesParser.__ATN = new antlr.ATNDeserializer().deserialize(
				PropertyReferencesParser._serializedATN
			);
		}

		return PropertyReferencesParser.__ATN;
	}

	private static readonly vocabulary = new antlr.Vocabulary(
		PropertyReferencesParser.literalNames,
		PropertyReferencesParser.symbolicNames,
		[]
	);

	public override get vocabulary(): antlr.Vocabulary {
		return PropertyReferencesParser.vocabulary;
	}

	private static readonly decisionsToDFA =
		PropertyReferencesParser._ATN.decisionToState.map(
			(ds: antlr.DecisionState, index: number) => new antlr.DFA(ds, index)
		);
}

export class ReferencesContext extends antlr.ParserRuleContext {
	public constructor(
		parent: antlr.ParserRuleContext | null,
		invokingState: number
	) {
		super(parent, invokingState);
	}
	public stringReferences(): StringReferencesContext {
		return this.getRuleContext(0, StringReferencesContext)!;
	}
	public EOF(): antlr.TerminalNode {
		return this.getToken(PropertyReferencesParser.EOF, 0)!;
	}
	public override get ruleIndex(): number {
		return PropertyReferencesParser.RULE_references;
	}
	public override enterRule(listener: PropertyReferencesListener): void {
		if (listener.enterReferences) {
			listener.enterReferences(this);
		}
	}
	public override exitRule(listener: PropertyReferencesListener): void {
		if (listener.exitReferences) {
			listener.exitReferences(this);
		}
	}
}

export class StringReferencesContext extends antlr.ParserRuleContext {
	public constructor(
		parent: antlr.ParserRuleContext | null,
		invokingState: number
	) {
		super(parent, invokingState);
	}
	public sentences(): SentencesContext[];
	public sentences(i: number): SentencesContext | null;
	public sentences(i?: number): SentencesContext[] | SentencesContext | null {
		if (i === undefined) {
			return this.getRuleContexts(SentencesContext);
		}

		return this.getRuleContext(i, SentencesContext);
	}
	public AND(): antlr.TerminalNode[];
	public AND(i: number): antlr.TerminalNode | null;
	public AND(i?: number): antlr.TerminalNode | null | antlr.TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(PropertyReferencesParser.AND);
		} else {
			return this.getToken(PropertyReferencesParser.AND, i);
		}
	}
	public OR(): antlr.TerminalNode[];
	public OR(i: number): antlr.TerminalNode | null;
	public OR(i?: number): antlr.TerminalNode | null | antlr.TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(PropertyReferencesParser.OR);
		} else {
			return this.getToken(PropertyReferencesParser.OR, i);
		}
	}
	public override get ruleIndex(): number {
		return PropertyReferencesParser.RULE_stringReferences;
	}
	public override enterRule(listener: PropertyReferencesListener): void {
		if (listener.enterStringReferences) {
			listener.enterStringReferences(this);
		}
	}
	public override exitRule(listener: PropertyReferencesListener): void {
		if (listener.exitStringReferences) {
			listener.exitStringReferences(this);
		}
	}
}

export class SentencesContext extends antlr.ParserRuleContext {
	public constructor(
		parent: antlr.ParserRuleContext | null,
		invokingState: number
	) {
		super(parent, invokingState);
	}
	public IDENTIFIER(): antlr.TerminalNode[];
	public IDENTIFIER(i: number): antlr.TerminalNode | null;
	public IDENTIFIER(
		i?: number
	): antlr.TerminalNode | null | antlr.TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(PropertyReferencesParser.IDENTIFIER);
		} else {
			return this.getToken(PropertyReferencesParser.IDENTIFIER, i);
		}
	}
	public WS(): antlr.TerminalNode[];
	public WS(i: number): antlr.TerminalNode | null;
	public WS(i?: number): antlr.TerminalNode | null | antlr.TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(PropertyReferencesParser.WS);
		} else {
			return this.getToken(PropertyReferencesParser.WS, i);
		}
	}
	public override get ruleIndex(): number {
		return PropertyReferencesParser.RULE_sentences;
	}
	public override enterRule(listener: PropertyReferencesListener): void {
		if (listener.enterSentences) {
			listener.enterSentences(this);
		}
	}
	public override exitRule(listener: PropertyReferencesListener): void {
		if (listener.exitSentences) {
			listener.exitSentences(this);
		}
	}
}
