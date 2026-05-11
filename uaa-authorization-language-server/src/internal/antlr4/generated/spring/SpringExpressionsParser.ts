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
// Generated from ./src/internal/antlr4/generated/g4/SpringExpressions.g4 by ANTLR 4.13.1

import * as antlr from "antlr4ng";

import { SpringExpressionsListener } from "./SpringExpressionsListener.js";

export class SpringExpressionsParser extends antlr.Parser {
	public static readonly AND = 1;
	public static readonly OR = 2;
	public static readonly EQUALS = 3;
	public static readonly NOT_EQUALS = 4;
	public static readonly NOT = 5;
	public static readonly GT = 6;
	public static readonly LT = 7;
	public static readonly GTE = 8;
	public static readonly LTE = 9;
	public static readonly PRINT = 10;
	public static readonly LPAREN = 11;
	public static readonly RPAREN = 12;
	public static readonly LSQUARE_BRACKET = 13;
	public static readonly RSQUARE_BRACKET = 14;
	public static readonly PLUS = 15;
	public static readonly MINUS = 16;
	public static readonly DOT = 17;
	public static readonly COMMA = 18;
	public static readonly AT_SIGN = 19;
	public static readonly ASSIGNMENT = 20;
	public static readonly INSTANCEOF = 21;
	public static readonly BETWEEN = 22;
	public static readonly MATCHES = 23;
	public static readonly NEW = 24;
	public static readonly IDENTIFIER = 25;
	public static readonly STRING_LITERAL = 26;
	public static readonly NUMBER = 27;
	public static readonly BOOLEAN = 28;
	public static readonly INVALID_TOKEN = 29;
	public static readonly WS = 30;
	public static readonly RULE_expression = 0;
	public static readonly RULE_printExpression = 1;
	public static readonly RULE_logicalExpressionWithParen = 2;
	public static readonly RULE_logicalExpression = 3;
	public static readonly RULE_compositeCondition = 4;
	public static readonly RULE_complexCondition = 5;
	public static readonly RULE_baseCondition = 6;
	public static readonly RULE_basicExpression = 7;
	public static readonly RULE_chainMethodCall = 8;
	public static readonly RULE_methodCall = 9;
	public static readonly RULE_constructorExp = 10;
	public static readonly RULE_parameterList = 11;
	public static readonly RULE_property = 12;
	public static readonly RULE_literalValue = 13;
	public static readonly RULE_operator = 14;

	public static readonly literalNames = [
		null,
		null,
		null,
		"'=='",
		"'!='",
		"'!'",
		"'>'",
		"'<'",
		"'>='",
		"'<='",
		"'print'",
		"'('",
		"')'",
		"'['",
		"']'",
		"'+'",
		"'-'",
		"'.'",
		"','",
		"'@'",
		"'='",
		"'instanceof'",
		"'between'",
		"'matches'",
		"'new'"
	];

	public static readonly symbolicNames = [
		null,
		"AND",
		"OR",
		"EQUALS",
		"NOT_EQUALS",
		"NOT",
		"GT",
		"LT",
		"GTE",
		"LTE",
		"PRINT",
		"LPAREN",
		"RPAREN",
		"LSQUARE_BRACKET",
		"RSQUARE_BRACKET",
		"PLUS",
		"MINUS",
		"DOT",
		"COMMA",
		"AT_SIGN",
		"ASSIGNMENT",
		"INSTANCEOF",
		"BETWEEN",
		"MATCHES",
		"NEW",
		"IDENTIFIER",
		"STRING_LITERAL",
		"NUMBER",
		"BOOLEAN",
		"INVALID_TOKEN",
		"WS"
	];
	public static readonly ruleNames = [
		"expression",
		"printExpression",
		"logicalExpressionWithParen",
		"logicalExpression",
		"compositeCondition",
		"complexCondition",
		"baseCondition",
		"basicExpression",
		"chainMethodCall",
		"methodCall",
		"constructorExp",
		"parameterList",
		"property",
		"literalValue",
		"operator"
	];

	public get grammarFileName(): string {
		return "SpringExpressions.g4";
	}
	public get literalNames(): (string | null)[] {
		return SpringExpressionsParser.literalNames;
	}
	public get symbolicNames(): (string | null)[] {
		return SpringExpressionsParser.symbolicNames;
	}
	public get ruleNames(): string[] {
		return SpringExpressionsParser.ruleNames;
	}
	public get serializedATN(): number[] {
		return SpringExpressionsParser._serializedATN;
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
			SpringExpressionsParser._ATN,
			SpringExpressionsParser.decisionsToDFA,
			new antlr.PredictionContextCache()
		);
	}
	public expression(): ExpressionContext {
		const localContext = new ExpressionContext(this.context, this.state);
		this.enterRule(localContext, 0, SpringExpressionsParser.RULE_expression);
		try {
			this.state = 36;
			this.errorHandler.sync(this);
			switch (this.tokenStream.LA(1)) {
				case SpringExpressionsParser.PRINT:
					this.enterOuterAlt(localContext, 1);
					{
						this.state = 30;
						this.printExpression();
						this.state = 31;
						this.match(SpringExpressionsParser.EOF);
					}
					break;
				case SpringExpressionsParser.NOT:
				case SpringExpressionsParser.LPAREN:
				case SpringExpressionsParser.AT_SIGN:
				case SpringExpressionsParser.NEW:
				case SpringExpressionsParser.IDENTIFIER:
				case SpringExpressionsParser.STRING_LITERAL:
				case SpringExpressionsParser.NUMBER:
				case SpringExpressionsParser.BOOLEAN:
					this.enterOuterAlt(localContext, 2);
					{
						this.state = 33;
						this.logicalExpressionWithParen();
						this.state = 34;
						this.match(SpringExpressionsParser.EOF);
					}
					break;
				default:
					throw new antlr.NoViableAltException(this);
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
	public printExpression(): PrintExpressionContext {
		const localContext = new PrintExpressionContext(this.context, this.state);
		this.enterRule(
			localContext,
			2,
			SpringExpressionsParser.RULE_printExpression
		);
		let _la: number;
		try {
			this.enterOuterAlt(localContext, 1);
			{
				this.state = 38;
				this.match(SpringExpressionsParser.PRINT);
				this.state = 39;
				this.match(SpringExpressionsParser.LPAREN);
				this.state = 43;
				this.errorHandler.sync(this);
				switch (
					this.interpreter.adaptivePredict(this.tokenStream, 1, this.context)
				) {
					case 1:
						{
							this.state = 40;
							this.match(SpringExpressionsParser.STRING_LITERAL);
						}
						break;
					case 2:
						{
							this.state = 41;
							this.chainMethodCall();
						}
						break;
					case 3:
						{
							this.state = 42;
							this.match(SpringExpressionsParser.IDENTIFIER);
						}
						break;
				}
				this.state = 54;
				this.errorHandler.sync(this);
				_la = this.tokenStream.LA(1);
				while (_la === 15) {
					{
						{
							this.state = 45;
							this.match(SpringExpressionsParser.PLUS);
							this.state = 50;
							this.errorHandler.sync(this);
							switch (
								this.interpreter.adaptivePredict(
									this.tokenStream,
									2,
									this.context
								)
							) {
								case 1:
									{
										this.state = 46;
										this.match(SpringExpressionsParser.STRING_LITERAL);
									}
									break;
								case 2:
									{
										this.state = 47;
										this.chainMethodCall();
									}
									break;
								case 3:
									{
										this.state = 48;
										this.match(SpringExpressionsParser.IDENTIFIER);
									}
									break;
								case 4:
									{
										this.state = 49;
										this.property();
									}
									break;
							}
						}
					}
					this.state = 56;
					this.errorHandler.sync(this);
					_la = this.tokenStream.LA(1);
				}
				this.state = 57;
				this.match(SpringExpressionsParser.RPAREN);
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
	public logicalExpressionWithParen(): LogicalExpressionWithParenContext {
		const localContext = new LogicalExpressionWithParenContext(
			this.context,
			this.state
		);
		this.enterRule(
			localContext,
			4,
			SpringExpressionsParser.RULE_logicalExpressionWithParen
		);
		let _la: number;
		try {
			this.enterOuterAlt(localContext, 1);
			{
				this.state = 64;
				this.errorHandler.sync(this);
				switch (
					this.interpreter.adaptivePredict(this.tokenStream, 4, this.context)
				) {
					case 1:
						{
							this.state = 59;
							this.logicalExpression();
						}
						break;
					case 2:
						{
							{
								this.state = 60;
								this.match(SpringExpressionsParser.LPAREN);
								this.state = 61;
								this.logicalExpressionWithParen();
								this.state = 62;
								this.match(SpringExpressionsParser.RPAREN);
							}
						}
						break;
				}
				this.state = 76;
				this.errorHandler.sync(this);
				_la = this.tokenStream.LA(1);
				while (_la === 1 || _la === 2) {
					{
						{
							this.state = 66;
							_la = this.tokenStream.LA(1);
							if (!(_la === 1 || _la === 2)) {
								this.errorHandler.recoverInline(this);
							} else {
								this.errorHandler.reportMatch(this);
								this.consume();
							}
							this.state = 72;
							this.errorHandler.sync(this);
							switch (
								this.interpreter.adaptivePredict(
									this.tokenStream,
									5,
									this.context
								)
							) {
								case 1:
									{
										{
											this.state = 67;
											this.match(SpringExpressionsParser.LPAREN);
											this.state = 68;
											this.logicalExpressionWithParen();
											this.state = 69;
											this.match(SpringExpressionsParser.RPAREN);
										}
									}
									break;
								case 2:
									{
										this.state = 71;
										this.logicalExpression();
									}
									break;
							}
						}
					}
					this.state = 78;
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
	public logicalExpression(): LogicalExpressionContext {
		const localContext = new LogicalExpressionContext(this.context, this.state);
		this.enterRule(
			localContext,
			6,
			SpringExpressionsParser.RULE_logicalExpression
		);
		let _la: number;
		try {
			let alternative: number;
			this.enterOuterAlt(localContext, 1);
			{
				this.state = 79;
				this.compositeCondition();
				this.state = 84;
				this.errorHandler.sync(this);
				alternative = this.interpreter.adaptivePredict(
					this.tokenStream,
					7,
					this.context
				);
				while (
					alternative !== 2 &&
					alternative !== antlr.ATN.INVALID_ALT_NUMBER
				) {
					if (alternative === 1) {
						{
							{
								this.state = 80;
								_la = this.tokenStream.LA(1);
								if (!(_la === 1 || _la === 2)) {
									this.errorHandler.recoverInline(this);
								} else {
									this.errorHandler.reportMatch(this);
									this.consume();
								}
								this.state = 81;
								this.compositeCondition();
							}
						}
					}
					this.state = 86;
					this.errorHandler.sync(this);
					alternative = this.interpreter.adaptivePredict(
						this.tokenStream,
						7,
						this.context
					);
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
	public compositeCondition(): CompositeConditionContext {
		const localContext = new CompositeConditionContext(
			this.context,
			this.state
		);
		this.enterRule(
			localContext,
			8,
			SpringExpressionsParser.RULE_compositeCondition
		);
		let _la: number;
		try {
			this.state = 115;
			this.errorHandler.sync(this);
			switch (
				this.interpreter.adaptivePredict(this.tokenStream, 11, this.context)
			) {
				case 1:
					this.enterOuterAlt(localContext, 1);
					{
						this.state = 90;
						this.errorHandler.sync(this);
						_la = this.tokenStream.LA(1);
						while (_la === 5) {
							{
								{
									this.state = 87;
									this.match(SpringExpressionsParser.NOT);
								}
							}
							this.state = 92;
							this.errorHandler.sync(this);
							_la = this.tokenStream.LA(1);
						}
						this.state = 93;
						this.baseCondition();
					}
					break;
				case 2:
					this.enterOuterAlt(localContext, 2);
					{
						this.state = 97;
						this.errorHandler.sync(this);
						_la = this.tokenStream.LA(1);
						while (_la === 5) {
							{
								{
									this.state = 94;
									this.match(SpringExpressionsParser.NOT);
								}
							}
							this.state = 99;
							this.errorHandler.sync(this);
							_la = this.tokenStream.LA(1);
						}
						this.state = 100;
						this.match(SpringExpressionsParser.LPAREN);
						this.state = 101;
						this.baseCondition();
						this.state = 102;
						this.match(SpringExpressionsParser.RPAREN);
					}
					break;
				case 3:
					this.enterOuterAlt(localContext, 3);
					{
						this.state = 107;
						this.errorHandler.sync(this);
						_la = this.tokenStream.LA(1);
						while (_la === 5) {
							{
								{
									this.state = 104;
									this.match(SpringExpressionsParser.NOT);
								}
							}
							this.state = 109;
							this.errorHandler.sync(this);
							_la = this.tokenStream.LA(1);
						}
						this.state = 110;
						this.match(SpringExpressionsParser.LPAREN);
						this.state = 111;
						this.complexCondition();
						this.state = 112;
						this.match(SpringExpressionsParser.RPAREN);
					}
					break;
				case 4:
					this.enterOuterAlt(localContext, 4);
					{
						this.state = 114;
						this.complexCondition();
					}
					break;
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
	public complexCondition(): ComplexConditionContext {
		const localContext = new ComplexConditionContext(this.context, this.state);
		this.enterRule(
			localContext,
			10,
			SpringExpressionsParser.RULE_complexCondition
		);
		try {
			this.state = 129;
			this.errorHandler.sync(this);
			switch (
				this.interpreter.adaptivePredict(this.tokenStream, 12, this.context)
			) {
				case 1:
					this.enterOuterAlt(localContext, 1);
					{
						this.state = 117;
						this.basicExpression();
						this.state = 118;
						this.operator();
						this.state = 119;
						this.basicExpression();
					}
					break;
				case 2:
					this.enterOuterAlt(localContext, 2);
					{
						this.state = 121;
						this.chainMethodCall();
						this.state = 122;
						this.operator();
						this.state = 123;
						this.basicExpression();
					}
					break;
				case 3:
					this.enterOuterAlt(localContext, 3);
					{
						this.state = 125;
						this.basicExpression();
						this.state = 126;
						this.operator();
						this.state = 127;
						this.chainMethodCall();
					}
					break;
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
	public baseCondition(): BaseConditionContext {
		const localContext = new BaseConditionContext(this.context, this.state);
		this.enterRule(
			localContext,
			12,
			SpringExpressionsParser.RULE_baseCondition
		);
		try {
			this.state = 137;
			this.errorHandler.sync(this);
			switch (
				this.interpreter.adaptivePredict(this.tokenStream, 13, this.context)
			) {
				case 1:
					this.enterOuterAlt(localContext, 1);
					{
						this.state = 131;
						this.chainMethodCall();
					}
					break;
				case 2:
					this.enterOuterAlt(localContext, 2);
					{
						this.state = 132;
						this.basicExpression();
					}
					break;
				case 3:
					this.enterOuterAlt(localContext, 3);
					{
						this.state = 133;
						this.property();
						this.state = 134;
						this.match(SpringExpressionsParser.DOT);
						this.state = 135;
						this.chainMethodCall();
					}
					break;
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
	public basicExpression(): BasicExpressionContext {
		const localContext = new BasicExpressionContext(this.context, this.state);
		this.enterRule(
			localContext,
			14,
			SpringExpressionsParser.RULE_basicExpression
		);
		try {
			this.state = 145;
			this.errorHandler.sync(this);
			switch (
				this.interpreter.adaptivePredict(this.tokenStream, 14, this.context)
			) {
				case 1:
					this.enterOuterAlt(localContext, 1);
					{
						this.state = 139;
						this.property();
					}
					break;
				case 2:
					this.enterOuterAlt(localContext, 2);
					{
						this.state = 140;
						this.literalValue();
					}
					break;
				case 3:
					this.enterOuterAlt(localContext, 3);
					{
						this.state = 141;
						this.constructorExp();
					}
					break;
				case 4:
					this.enterOuterAlt(localContext, 4);
					{
						this.state = 142;
						this.match(SpringExpressionsParser.STRING_LITERAL);
						this.state = 143;
						this.match(SpringExpressionsParser.ASSIGNMENT);
						this.state = 144;
						this.basicExpression();
					}
					break;
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
	public chainMethodCall(): ChainMethodCallContext {
		const localContext = new ChainMethodCallContext(this.context, this.state);
		this.enterRule(
			localContext,
			16,
			SpringExpressionsParser.RULE_chainMethodCall
		);
		let _la: number;
		try {
			this.enterOuterAlt(localContext, 1);
			{
				this.state = 147;
				this.methodCall();
				this.state = 152;
				this.errorHandler.sync(this);
				_la = this.tokenStream.LA(1);
				while (_la === 17) {
					{
						{
							this.state = 148;
							this.match(SpringExpressionsParser.DOT);
							this.state = 149;
							this.methodCall();
						}
					}
					this.state = 154;
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
	public methodCall(): MethodCallContext {
		const localContext = new MethodCallContext(this.context, this.state);
		this.enterRule(localContext, 18, SpringExpressionsParser.RULE_methodCall);
		let _la: number;
		try {
			this.enterOuterAlt(localContext, 1);
			{
				this.state = 156;
				this.errorHandler.sync(this);
				_la = this.tokenStream.LA(1);
				if (_la === 19) {
					{
						this.state = 155;
						this.match(SpringExpressionsParser.AT_SIGN);
					}
				}

				this.state = 158;
				this.property();
				this.state = 159;
				this.match(SpringExpressionsParser.LPAREN);
				this.state = 161;
				this.errorHandler.sync(this);
				_la = this.tokenStream.LA(1);
				if ((_la & ~0x1f) === 0 && ((1 << _la) & 520093696) !== 0) {
					{
						this.state = 160;
						this.parameterList();
					}
				}

				this.state = 163;
				this.match(SpringExpressionsParser.RPAREN);
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
	public constructorExp(): ConstructorExpContext {
		const localContext = new ConstructorExpContext(this.context, this.state);
		this.enterRule(
			localContext,
			20,
			SpringExpressionsParser.RULE_constructorExp
		);
		let _la: number;
		try {
			this.enterOuterAlt(localContext, 1);
			{
				this.state = 165;
				this.match(SpringExpressionsParser.NEW);
				this.state = 166;
				this.property();
				this.state = 167;
				this.match(SpringExpressionsParser.LPAREN);
				{
					this.state = 169;
					this.errorHandler.sync(this);
					_la = this.tokenStream.LA(1);
					if ((_la & ~0x1f) === 0 && ((1 << _la) & 520093696) !== 0) {
						{
							this.state = 168;
							this.parameterList();
						}
					}
				}
				this.state = 171;
				this.match(SpringExpressionsParser.RPAREN);
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
	public parameterList(): ParameterListContext {
		const localContext = new ParameterListContext(this.context, this.state);
		this.enterRule(
			localContext,
			22,
			SpringExpressionsParser.RULE_parameterList
		);
		let _la: number;
		try {
			this.enterOuterAlt(localContext, 1);
			{
				this.state = 176;
				this.errorHandler.sync(this);
				switch (this.tokenStream.LA(1)) {
					case SpringExpressionsParser.STRING_LITERAL:
					case SpringExpressionsParser.NUMBER:
					case SpringExpressionsParser.BOOLEAN:
						{
							this.state = 173;
							this.literalValue();
						}
						break;
					case SpringExpressionsParser.IDENTIFIER:
						{
							this.state = 174;
							this.property();
						}
						break;
					case SpringExpressionsParser.NEW:
						{
							this.state = 175;
							this.constructorExp();
						}
						break;
					default:
						throw new antlr.NoViableAltException(this);
				}
				this.state = 186;
				this.errorHandler.sync(this);
				_la = this.tokenStream.LA(1);
				while (_la === 18) {
					{
						{
							this.state = 178;
							this.match(SpringExpressionsParser.COMMA);
							this.state = 182;
							this.errorHandler.sync(this);
							switch (this.tokenStream.LA(1)) {
								case SpringExpressionsParser.STRING_LITERAL:
								case SpringExpressionsParser.NUMBER:
								case SpringExpressionsParser.BOOLEAN:
									{
										this.state = 179;
										this.literalValue();
									}
									break;
								case SpringExpressionsParser.IDENTIFIER:
									{
										this.state = 180;
										this.property();
									}
									break;
								case SpringExpressionsParser.NEW:
									{
										this.state = 181;
										this.constructorExp();
									}
									break;
								default:
									throw new antlr.NoViableAltException(this);
							}
						}
					}
					this.state = 188;
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
	public property(): PropertyContext {
		const localContext = new PropertyContext(this.context, this.state);
		this.enterRule(localContext, 24, SpringExpressionsParser.RULE_property);
		let _la: number;
		try {
			let alternative: number;
			this.enterOuterAlt(localContext, 1);
			{
				this.state = 189;
				this.match(SpringExpressionsParser.IDENTIFIER);
				this.state = 204;
				this.errorHandler.sync(this);
				alternative = this.interpreter.adaptivePredict(
					this.tokenStream,
					25,
					this.context
				);
				while (
					alternative !== 2 &&
					alternative !== antlr.ATN.INVALID_ALT_NUMBER
				) {
					if (alternative === 1) {
						{
							this.state = 202;
							this.errorHandler.sync(this);
							switch (
								this.interpreter.adaptivePredict(
									this.tokenStream,
									24,
									this.context
								)
							) {
								case 1:
									{
										{
											this.state = 191;
											this.errorHandler.sync(this);
											_la = this.tokenStream.LA(1);
											if (_la === 17) {
												{
													this.state = 190;
													this.match(SpringExpressionsParser.DOT);
												}
											}

											this.state = 193;
											this.match(SpringExpressionsParser.LSQUARE_BRACKET);
											this.state = 195;
											this.errorHandler.sync(this);
											alternative = 1 + 1;
											do {
												switch (alternative) {
													case 1 + 1:
														{
															{
																this.state = 194;
																this.matchWildcard();
															}
														}
														break;
													default:
														throw new antlr.NoViableAltException(this);
												}
												this.state = 197;
												this.errorHandler.sync(this);
												alternative = this.interpreter.adaptivePredict(
													this.tokenStream,
													23,
													this.context
												);
											} while (
												alternative !== 1 &&
												alternative !== antlr.ATN.INVALID_ALT_NUMBER
											);
											this.state = 199;
											this.match(SpringExpressionsParser.RSQUARE_BRACKET);
										}
									}
									break;
								case 2:
									{
										{
											this.state = 200;
											this.match(SpringExpressionsParser.DOT);
											this.state = 201;
											this.match(SpringExpressionsParser.IDENTIFIER);
										}
									}
									break;
							}
						}
					}
					this.state = 206;
					this.errorHandler.sync(this);
					alternative = this.interpreter.adaptivePredict(
						this.tokenStream,
						25,
						this.context
					);
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
	public literalValue(): LiteralValueContext {
		const localContext = new LiteralValueContext(this.context, this.state);
		this.enterRule(localContext, 26, SpringExpressionsParser.RULE_literalValue);
		let _la: number;
		try {
			this.enterOuterAlt(localContext, 1);
			{
				this.state = 207;
				_la = this.tokenStream.LA(1);
				if (!((_la & ~0x1f) === 0 && ((1 << _la) & 469762048) !== 0)) {
					this.errorHandler.recoverInline(this);
				} else {
					this.errorHandler.reportMatch(this);
					this.consume();
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
	public operator(): OperatorContext {
		const localContext = new OperatorContext(this.context, this.state);
		this.enterRule(localContext, 28, SpringExpressionsParser.RULE_operator);
		let _la: number;
		try {
			this.enterOuterAlt(localContext, 1);
			{
				this.state = 209;
				_la = this.tokenStream.LA(1);
				if (!((_la & ~0x1f) === 0 && ((1 << _la) & 14681048) !== 0)) {
					this.errorHandler.recoverInline(this);
				} else {
					this.errorHandler.reportMatch(this);
					this.consume();
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
		4, 1, 30, 212, 2, 0, 7, 0, 2, 1, 7, 1, 2, 2, 7, 2, 2, 3, 7, 3, 2, 4, 7, 4,
		2, 5, 7, 5, 2, 6, 7, 6, 2, 7, 7, 7, 2, 8, 7, 8, 2, 9, 7, 9, 2, 10, 7, 10, 2,
		11, 7, 11, 2, 12, 7, 12, 2, 13, 7, 13, 2, 14, 7, 14, 1, 0, 1, 0, 1, 0, 1, 0,
		1, 0, 1, 0, 3, 0, 37, 8, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 44, 8, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 51, 8, 1, 5, 1, 53, 8, 1, 10, 1, 12, 1, 56,
		9, 1, 1, 1, 1, 1, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 3, 2, 65, 8, 2, 1, 2, 1, 2,
		1, 2, 1, 2, 1, 2, 1, 2, 3, 2, 73, 8, 2, 5, 2, 75, 8, 2, 10, 2, 12, 2, 78, 9,
		2, 1, 3, 1, 3, 1, 3, 5, 3, 83, 8, 3, 10, 3, 12, 3, 86, 9, 3, 1, 4, 5, 4, 89,
		8, 4, 10, 4, 12, 4, 92, 9, 4, 1, 4, 1, 4, 5, 4, 96, 8, 4, 10, 4, 12, 4, 99,
		9, 4, 1, 4, 1, 4, 1, 4, 1, 4, 1, 4, 5, 4, 106, 8, 4, 10, 4, 12, 4, 109, 9,
		4, 1, 4, 1, 4, 1, 4, 1, 4, 1, 4, 3, 4, 116, 8, 4, 1, 5, 1, 5, 1, 5, 1, 5, 1,
		5, 1, 5, 1, 5, 1, 5, 1, 5, 1, 5, 1, 5, 1, 5, 3, 5, 130, 8, 5, 1, 6, 1, 6, 1,
		6, 1, 6, 1, 6, 1, 6, 3, 6, 138, 8, 6, 1, 7, 1, 7, 1, 7, 1, 7, 1, 7, 1, 7, 3,
		7, 146, 8, 7, 1, 8, 1, 8, 1, 8, 5, 8, 151, 8, 8, 10, 8, 12, 8, 154, 9, 8, 1,
		9, 3, 9, 157, 8, 9, 1, 9, 1, 9, 1, 9, 3, 9, 162, 8, 9, 1, 9, 1, 9, 1, 10, 1,
		10, 1, 10, 1, 10, 3, 10, 170, 8, 10, 1, 10, 1, 10, 1, 11, 1, 11, 1, 11, 3,
		11, 177, 8, 11, 1, 11, 1, 11, 1, 11, 1, 11, 3, 11, 183, 8, 11, 5, 11, 185,
		8, 11, 10, 11, 12, 11, 188, 9, 11, 1, 12, 1, 12, 3, 12, 192, 8, 12, 1, 12,
		1, 12, 4, 12, 196, 8, 12, 11, 12, 12, 12, 197, 1, 12, 1, 12, 1, 12, 5, 12,
		203, 8, 12, 10, 12, 12, 12, 206, 9, 12, 1, 13, 1, 13, 1, 14, 1, 14, 1, 14,
		1, 197, 0, 15, 0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 0, 3,
		1, 0, 1, 2, 1, 0, 26, 28, 3, 0, 3, 4, 6, 9, 21, 23, 233, 0, 36, 1, 0, 0, 0,
		2, 38, 1, 0, 0, 0, 4, 64, 1, 0, 0, 0, 6, 79, 1, 0, 0, 0, 8, 115, 1, 0, 0, 0,
		10, 129, 1, 0, 0, 0, 12, 137, 1, 0, 0, 0, 14, 145, 1, 0, 0, 0, 16, 147, 1,
		0, 0, 0, 18, 156, 1, 0, 0, 0, 20, 165, 1, 0, 0, 0, 22, 176, 1, 0, 0, 0, 24,
		189, 1, 0, 0, 0, 26, 207, 1, 0, 0, 0, 28, 209, 1, 0, 0, 0, 30, 31, 3, 2, 1,
		0, 31, 32, 5, 0, 0, 1, 32, 37, 1, 0, 0, 0, 33, 34, 3, 4, 2, 0, 34, 35, 5, 0,
		0, 1, 35, 37, 1, 0, 0, 0, 36, 30, 1, 0, 0, 0, 36, 33, 1, 0, 0, 0, 37, 1, 1,
		0, 0, 0, 38, 39, 5, 10, 0, 0, 39, 43, 5, 11, 0, 0, 40, 44, 5, 26, 0, 0, 41,
		44, 3, 16, 8, 0, 42, 44, 5, 25, 0, 0, 43, 40, 1, 0, 0, 0, 43, 41, 1, 0, 0,
		0, 43, 42, 1, 0, 0, 0, 44, 54, 1, 0, 0, 0, 45, 50, 5, 15, 0, 0, 46, 51, 5,
		26, 0, 0, 47, 51, 3, 16, 8, 0, 48, 51, 5, 25, 0, 0, 49, 51, 3, 24, 12, 0,
		50, 46, 1, 0, 0, 0, 50, 47, 1, 0, 0, 0, 50, 48, 1, 0, 0, 0, 50, 49, 1, 0, 0,
		0, 51, 53, 1, 0, 0, 0, 52, 45, 1, 0, 0, 0, 53, 56, 1, 0, 0, 0, 54, 52, 1, 0,
		0, 0, 54, 55, 1, 0, 0, 0, 55, 57, 1, 0, 0, 0, 56, 54, 1, 0, 0, 0, 57, 58, 5,
		12, 0, 0, 58, 3, 1, 0, 0, 0, 59, 65, 3, 6, 3, 0, 60, 61, 5, 11, 0, 0, 61,
		62, 3, 4, 2, 0, 62, 63, 5, 12, 0, 0, 63, 65, 1, 0, 0, 0, 64, 59, 1, 0, 0, 0,
		64, 60, 1, 0, 0, 0, 65, 76, 1, 0, 0, 0, 66, 72, 7, 0, 0, 0, 67, 68, 5, 11,
		0, 0, 68, 69, 3, 4, 2, 0, 69, 70, 5, 12, 0, 0, 70, 73, 1, 0, 0, 0, 71, 73,
		3, 6, 3, 0, 72, 67, 1, 0, 0, 0, 72, 71, 1, 0, 0, 0, 73, 75, 1, 0, 0, 0, 74,
		66, 1, 0, 0, 0, 75, 78, 1, 0, 0, 0, 76, 74, 1, 0, 0, 0, 76, 77, 1, 0, 0, 0,
		77, 5, 1, 0, 0, 0, 78, 76, 1, 0, 0, 0, 79, 84, 3, 8, 4, 0, 80, 81, 7, 0, 0,
		0, 81, 83, 3, 8, 4, 0, 82, 80, 1, 0, 0, 0, 83, 86, 1, 0, 0, 0, 84, 82, 1, 0,
		0, 0, 84, 85, 1, 0, 0, 0, 85, 7, 1, 0, 0, 0, 86, 84, 1, 0, 0, 0, 87, 89, 5,
		5, 0, 0, 88, 87, 1, 0, 0, 0, 89, 92, 1, 0, 0, 0, 90, 88, 1, 0, 0, 0, 90, 91,
		1, 0, 0, 0, 91, 93, 1, 0, 0, 0, 92, 90, 1, 0, 0, 0, 93, 116, 3, 12, 6, 0,
		94, 96, 5, 5, 0, 0, 95, 94, 1, 0, 0, 0, 96, 99, 1, 0, 0, 0, 97, 95, 1, 0, 0,
		0, 97, 98, 1, 0, 0, 0, 98, 100, 1, 0, 0, 0, 99, 97, 1, 0, 0, 0, 100, 101, 5,
		11, 0, 0, 101, 102, 3, 12, 6, 0, 102, 103, 5, 12, 0, 0, 103, 116, 1, 0, 0,
		0, 104, 106, 5, 5, 0, 0, 105, 104, 1, 0, 0, 0, 106, 109, 1, 0, 0, 0, 107,
		105, 1, 0, 0, 0, 107, 108, 1, 0, 0, 0, 108, 110, 1, 0, 0, 0, 109, 107, 1, 0,
		0, 0, 110, 111, 5, 11, 0, 0, 111, 112, 3, 10, 5, 0, 112, 113, 5, 12, 0, 0,
		113, 116, 1, 0, 0, 0, 114, 116, 3, 10, 5, 0, 115, 90, 1, 0, 0, 0, 115, 97,
		1, 0, 0, 0, 115, 107, 1, 0, 0, 0, 115, 114, 1, 0, 0, 0, 116, 9, 1, 0, 0, 0,
		117, 118, 3, 14, 7, 0, 118, 119, 3, 28, 14, 0, 119, 120, 3, 14, 7, 0, 120,
		130, 1, 0, 0, 0, 121, 122, 3, 16, 8, 0, 122, 123, 3, 28, 14, 0, 123, 124, 3,
		14, 7, 0, 124, 130, 1, 0, 0, 0, 125, 126, 3, 14, 7, 0, 126, 127, 3, 28, 14,
		0, 127, 128, 3, 16, 8, 0, 128, 130, 1, 0, 0, 0, 129, 117, 1, 0, 0, 0, 129,
		121, 1, 0, 0, 0, 129, 125, 1, 0, 0, 0, 130, 11, 1, 0, 0, 0, 131, 138, 3, 16,
		8, 0, 132, 138, 3, 14, 7, 0, 133, 134, 3, 24, 12, 0, 134, 135, 5, 17, 0, 0,
		135, 136, 3, 16, 8, 0, 136, 138, 1, 0, 0, 0, 137, 131, 1, 0, 0, 0, 137, 132,
		1, 0, 0, 0, 137, 133, 1, 0, 0, 0, 138, 13, 1, 0, 0, 0, 139, 146, 3, 24, 12,
		0, 140, 146, 3, 26, 13, 0, 141, 146, 3, 20, 10, 0, 142, 143, 5, 26, 0, 0,
		143, 144, 5, 20, 0, 0, 144, 146, 3, 14, 7, 0, 145, 139, 1, 0, 0, 0, 145,
		140, 1, 0, 0, 0, 145, 141, 1, 0, 0, 0, 145, 142, 1, 0, 0, 0, 146, 15, 1, 0,
		0, 0, 147, 152, 3, 18, 9, 0, 148, 149, 5, 17, 0, 0, 149, 151, 3, 18, 9, 0,
		150, 148, 1, 0, 0, 0, 151, 154, 1, 0, 0, 0, 152, 150, 1, 0, 0, 0, 152, 153,
		1, 0, 0, 0, 153, 17, 1, 0, 0, 0, 154, 152, 1, 0, 0, 0, 155, 157, 5, 19, 0,
		0, 156, 155, 1, 0, 0, 0, 156, 157, 1, 0, 0, 0, 157, 158, 1, 0, 0, 0, 158,
		159, 3, 24, 12, 0, 159, 161, 5, 11, 0, 0, 160, 162, 3, 22, 11, 0, 161, 160,
		1, 0, 0, 0, 161, 162, 1, 0, 0, 0, 162, 163, 1, 0, 0, 0, 163, 164, 5, 12, 0,
		0, 164, 19, 1, 0, 0, 0, 165, 166, 5, 24, 0, 0, 166, 167, 3, 24, 12, 0, 167,
		169, 5, 11, 0, 0, 168, 170, 3, 22, 11, 0, 169, 168, 1, 0, 0, 0, 169, 170, 1,
		0, 0, 0, 170, 171, 1, 0, 0, 0, 171, 172, 5, 12, 0, 0, 172, 21, 1, 0, 0, 0,
		173, 177, 3, 26, 13, 0, 174, 177, 3, 24, 12, 0, 175, 177, 3, 20, 10, 0, 176,
		173, 1, 0, 0, 0, 176, 174, 1, 0, 0, 0, 176, 175, 1, 0, 0, 0, 177, 186, 1, 0,
		0, 0, 178, 182, 5, 18, 0, 0, 179, 183, 3, 26, 13, 0, 180, 183, 3, 24, 12, 0,
		181, 183, 3, 20, 10, 0, 182, 179, 1, 0, 0, 0, 182, 180, 1, 0, 0, 0, 182,
		181, 1, 0, 0, 0, 183, 185, 1, 0, 0, 0, 184, 178, 1, 0, 0, 0, 185, 188, 1, 0,
		0, 0, 186, 184, 1, 0, 0, 0, 186, 187, 1, 0, 0, 0, 187, 23, 1, 0, 0, 0, 188,
		186, 1, 0, 0, 0, 189, 204, 5, 25, 0, 0, 190, 192, 5, 17, 0, 0, 191, 190, 1,
		0, 0, 0, 191, 192, 1, 0, 0, 0, 192, 193, 1, 0, 0, 0, 193, 195, 5, 13, 0, 0,
		194, 196, 9, 0, 0, 0, 195, 194, 1, 0, 0, 0, 196, 197, 1, 0, 0, 0, 197, 198,
		1, 0, 0, 0, 197, 195, 1, 0, 0, 0, 198, 199, 1, 0, 0, 0, 199, 203, 5, 14, 0,
		0, 200, 201, 5, 17, 0, 0, 201, 203, 5, 25, 0, 0, 202, 191, 1, 0, 0, 0, 202,
		200, 1, 0, 0, 0, 203, 206, 1, 0, 0, 0, 204, 202, 1, 0, 0, 0, 204, 205, 1, 0,
		0, 0, 205, 25, 1, 0, 0, 0, 206, 204, 1, 0, 0, 0, 207, 208, 7, 1, 0, 0, 208,
		27, 1, 0, 0, 0, 209, 210, 7, 2, 0, 0, 210, 29, 1, 0, 0, 0, 26, 36, 43, 50,
		54, 64, 72, 76, 84, 90, 97, 107, 115, 129, 137, 145, 152, 156, 161, 169,
		176, 182, 186, 191, 197, 202, 204
	];

	private static __ATN: antlr.ATN;
	public static get _ATN(): antlr.ATN {
		if (!SpringExpressionsParser.__ATN) {
			SpringExpressionsParser.__ATN = new antlr.ATNDeserializer().deserialize(
				SpringExpressionsParser._serializedATN
			);
		}

		return SpringExpressionsParser.__ATN;
	}

	private static readonly vocabulary = new antlr.Vocabulary(
		SpringExpressionsParser.literalNames,
		SpringExpressionsParser.symbolicNames,
		[]
	);

	public override get vocabulary(): antlr.Vocabulary {
		return SpringExpressionsParser.vocabulary;
	}

	private static readonly decisionsToDFA =
		SpringExpressionsParser._ATN.decisionToState.map(
			(ds: antlr.DecisionState, index: number) => new antlr.DFA(ds, index)
		);
}

export class ExpressionContext extends antlr.ParserRuleContext {
	public constructor(
		parent: antlr.ParserRuleContext | null,
		invokingState: number
	) {
		super(parent, invokingState);
	}
	public printExpression(): PrintExpressionContext | null {
		return this.getRuleContext(0, PrintExpressionContext);
	}
	public EOF(): antlr.TerminalNode {
		return this.getToken(SpringExpressionsParser.EOF, 0)!;
	}
	public logicalExpressionWithParen(): LogicalExpressionWithParenContext | null {
		return this.getRuleContext(0, LogicalExpressionWithParenContext);
	}
	public override get ruleIndex(): number {
		return SpringExpressionsParser.RULE_expression;
	}
	public override enterRule(listener: SpringExpressionsListener): void {
		if (listener.enterExpression) {
			listener.enterExpression(this);
		}
	}
	public override exitRule(listener: SpringExpressionsListener): void {
		if (listener.exitExpression) {
			listener.exitExpression(this);
		}
	}
}

export class PrintExpressionContext extends antlr.ParserRuleContext {
	public constructor(
		parent: antlr.ParserRuleContext | null,
		invokingState: number
	) {
		super(parent, invokingState);
	}
	public PRINT(): antlr.TerminalNode {
		return this.getToken(SpringExpressionsParser.PRINT, 0)!;
	}
	public LPAREN(): antlr.TerminalNode {
		return this.getToken(SpringExpressionsParser.LPAREN, 0)!;
	}
	public RPAREN(): antlr.TerminalNode {
		return this.getToken(SpringExpressionsParser.RPAREN, 0)!;
	}
	public STRING_LITERAL(): antlr.TerminalNode[];
	public STRING_LITERAL(i: number): antlr.TerminalNode | null;
	public STRING_LITERAL(
		i?: number
	): antlr.TerminalNode | null | antlr.TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SpringExpressionsParser.STRING_LITERAL);
		} else {
			return this.getToken(SpringExpressionsParser.STRING_LITERAL, i);
		}
	}
	public chainMethodCall(): ChainMethodCallContext[];
	public chainMethodCall(i: number): ChainMethodCallContext | null;
	public chainMethodCall(
		i?: number
	): ChainMethodCallContext[] | ChainMethodCallContext | null {
		if (i === undefined) {
			return this.getRuleContexts(ChainMethodCallContext);
		}

		return this.getRuleContext(i, ChainMethodCallContext);
	}
	public IDENTIFIER(): antlr.TerminalNode[];
	public IDENTIFIER(i: number): antlr.TerminalNode | null;
	public IDENTIFIER(
		i?: number
	): antlr.TerminalNode | null | antlr.TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SpringExpressionsParser.IDENTIFIER);
		} else {
			return this.getToken(SpringExpressionsParser.IDENTIFIER, i);
		}
	}
	public PLUS(): antlr.TerminalNode[];
	public PLUS(i: number): antlr.TerminalNode | null;
	public PLUS(i?: number): antlr.TerminalNode | null | antlr.TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SpringExpressionsParser.PLUS);
		} else {
			return this.getToken(SpringExpressionsParser.PLUS, i);
		}
	}
	public property(): PropertyContext[];
	public property(i: number): PropertyContext | null;
	public property(i?: number): PropertyContext[] | PropertyContext | null {
		if (i === undefined) {
			return this.getRuleContexts(PropertyContext);
		}

		return this.getRuleContext(i, PropertyContext);
	}
	public override get ruleIndex(): number {
		return SpringExpressionsParser.RULE_printExpression;
	}
	public override enterRule(listener: SpringExpressionsListener): void {
		if (listener.enterPrintExpression) {
			listener.enterPrintExpression(this);
		}
	}
	public override exitRule(listener: SpringExpressionsListener): void {
		if (listener.exitPrintExpression) {
			listener.exitPrintExpression(this);
		}
	}
}

export class LogicalExpressionWithParenContext extends antlr.ParserRuleContext {
	public constructor(
		parent: antlr.ParserRuleContext | null,
		invokingState: number
	) {
		super(parent, invokingState);
	}
	public logicalExpression(): LogicalExpressionContext[];
	public logicalExpression(i: number): LogicalExpressionContext | null;
	public logicalExpression(
		i?: number
	): LogicalExpressionContext[] | LogicalExpressionContext | null {
		if (i === undefined) {
			return this.getRuleContexts(LogicalExpressionContext);
		}

		return this.getRuleContext(i, LogicalExpressionContext);
	}
	public LPAREN(): antlr.TerminalNode[];
	public LPAREN(i: number): antlr.TerminalNode | null;
	public LPAREN(i?: number): antlr.TerminalNode | null | antlr.TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SpringExpressionsParser.LPAREN);
		} else {
			return this.getToken(SpringExpressionsParser.LPAREN, i);
		}
	}
	public logicalExpressionWithParen(): LogicalExpressionWithParenContext[];
	public logicalExpressionWithParen(
		i: number
	): LogicalExpressionWithParenContext | null;
	public logicalExpressionWithParen(
		i?: number
	):
		| LogicalExpressionWithParenContext[]
		| LogicalExpressionWithParenContext
		| null {
		if (i === undefined) {
			return this.getRuleContexts(LogicalExpressionWithParenContext);
		}

		return this.getRuleContext(i, LogicalExpressionWithParenContext);
	}
	public RPAREN(): antlr.TerminalNode[];
	public RPAREN(i: number): antlr.TerminalNode | null;
	public RPAREN(i?: number): antlr.TerminalNode | null | antlr.TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SpringExpressionsParser.RPAREN);
		} else {
			return this.getToken(SpringExpressionsParser.RPAREN, i);
		}
	}
	public AND(): antlr.TerminalNode[];
	public AND(i: number): antlr.TerminalNode | null;
	public AND(i?: number): antlr.TerminalNode | null | antlr.TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SpringExpressionsParser.AND);
		} else {
			return this.getToken(SpringExpressionsParser.AND, i);
		}
	}
	public OR(): antlr.TerminalNode[];
	public OR(i: number): antlr.TerminalNode | null;
	public OR(i?: number): antlr.TerminalNode | null | antlr.TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SpringExpressionsParser.OR);
		} else {
			return this.getToken(SpringExpressionsParser.OR, i);
		}
	}
	public override get ruleIndex(): number {
		return SpringExpressionsParser.RULE_logicalExpressionWithParen;
	}
	public override enterRule(listener: SpringExpressionsListener): void {
		if (listener.enterLogicalExpressionWithParen) {
			listener.enterLogicalExpressionWithParen(this);
		}
	}
	public override exitRule(listener: SpringExpressionsListener): void {
		if (listener.exitLogicalExpressionWithParen) {
			listener.exitLogicalExpressionWithParen(this);
		}
	}
}

export class LogicalExpressionContext extends antlr.ParserRuleContext {
	public constructor(
		parent: antlr.ParserRuleContext | null,
		invokingState: number
	) {
		super(parent, invokingState);
	}
	public compositeCondition(): CompositeConditionContext[];
	public compositeCondition(i: number): CompositeConditionContext | null;
	public compositeCondition(
		i?: number
	): CompositeConditionContext[] | CompositeConditionContext | null {
		if (i === undefined) {
			return this.getRuleContexts(CompositeConditionContext);
		}

		return this.getRuleContext(i, CompositeConditionContext);
	}
	public AND(): antlr.TerminalNode[];
	public AND(i: number): antlr.TerminalNode | null;
	public AND(i?: number): antlr.TerminalNode | null | antlr.TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SpringExpressionsParser.AND);
		} else {
			return this.getToken(SpringExpressionsParser.AND, i);
		}
	}
	public OR(): antlr.TerminalNode[];
	public OR(i: number): antlr.TerminalNode | null;
	public OR(i?: number): antlr.TerminalNode | null | antlr.TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SpringExpressionsParser.OR);
		} else {
			return this.getToken(SpringExpressionsParser.OR, i);
		}
	}
	public override get ruleIndex(): number {
		return SpringExpressionsParser.RULE_logicalExpression;
	}
	public override enterRule(listener: SpringExpressionsListener): void {
		if (listener.enterLogicalExpression) {
			listener.enterLogicalExpression(this);
		}
	}
	public override exitRule(listener: SpringExpressionsListener): void {
		if (listener.exitLogicalExpression) {
			listener.exitLogicalExpression(this);
		}
	}
}

export class CompositeConditionContext extends antlr.ParserRuleContext {
	public constructor(
		parent: antlr.ParserRuleContext | null,
		invokingState: number
	) {
		super(parent, invokingState);
	}
	public baseCondition(): BaseConditionContext | null {
		return this.getRuleContext(0, BaseConditionContext);
	}
	public NOT(): antlr.TerminalNode[];
	public NOT(i: number): antlr.TerminalNode | null;
	public NOT(i?: number): antlr.TerminalNode | null | antlr.TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SpringExpressionsParser.NOT);
		} else {
			return this.getToken(SpringExpressionsParser.NOT, i);
		}
	}
	public LPAREN(): antlr.TerminalNode | null {
		return this.getToken(SpringExpressionsParser.LPAREN, 0);
	}
	public RPAREN(): antlr.TerminalNode | null {
		return this.getToken(SpringExpressionsParser.RPAREN, 0);
	}
	public complexCondition(): ComplexConditionContext | null {
		return this.getRuleContext(0, ComplexConditionContext);
	}
	public override get ruleIndex(): number {
		return SpringExpressionsParser.RULE_compositeCondition;
	}
	public override enterRule(listener: SpringExpressionsListener): void {
		if (listener.enterCompositeCondition) {
			listener.enterCompositeCondition(this);
		}
	}
	public override exitRule(listener: SpringExpressionsListener): void {
		if (listener.exitCompositeCondition) {
			listener.exitCompositeCondition(this);
		}
	}
}

export class ComplexConditionContext extends antlr.ParserRuleContext {
	public constructor(
		parent: antlr.ParserRuleContext | null,
		invokingState: number
	) {
		super(parent, invokingState);
	}
	public basicExpression(): BasicExpressionContext[];
	public basicExpression(i: number): BasicExpressionContext | null;
	public basicExpression(
		i?: number
	): BasicExpressionContext[] | BasicExpressionContext | null {
		if (i === undefined) {
			return this.getRuleContexts(BasicExpressionContext);
		}

		return this.getRuleContext(i, BasicExpressionContext);
	}
	public operator(): OperatorContext {
		return this.getRuleContext(0, OperatorContext)!;
	}
	public chainMethodCall(): ChainMethodCallContext | null {
		return this.getRuleContext(0, ChainMethodCallContext);
	}
	public override get ruleIndex(): number {
		return SpringExpressionsParser.RULE_complexCondition;
	}
	public override enterRule(listener: SpringExpressionsListener): void {
		if (listener.enterComplexCondition) {
			listener.enterComplexCondition(this);
		}
	}
	public override exitRule(listener: SpringExpressionsListener): void {
		if (listener.exitComplexCondition) {
			listener.exitComplexCondition(this);
		}
	}
}

export class BaseConditionContext extends antlr.ParserRuleContext {
	public constructor(
		parent: antlr.ParserRuleContext | null,
		invokingState: number
	) {
		super(parent, invokingState);
	}
	public chainMethodCall(): ChainMethodCallContext | null {
		return this.getRuleContext(0, ChainMethodCallContext);
	}
	public basicExpression(): BasicExpressionContext | null {
		return this.getRuleContext(0, BasicExpressionContext);
	}
	public property(): PropertyContext | null {
		return this.getRuleContext(0, PropertyContext);
	}
	public DOT(): antlr.TerminalNode | null {
		return this.getToken(SpringExpressionsParser.DOT, 0);
	}
	public override get ruleIndex(): number {
		return SpringExpressionsParser.RULE_baseCondition;
	}
	public override enterRule(listener: SpringExpressionsListener): void {
		if (listener.enterBaseCondition) {
			listener.enterBaseCondition(this);
		}
	}
	public override exitRule(listener: SpringExpressionsListener): void {
		if (listener.exitBaseCondition) {
			listener.exitBaseCondition(this);
		}
	}
}

export class BasicExpressionContext extends antlr.ParserRuleContext {
	public constructor(
		parent: antlr.ParserRuleContext | null,
		invokingState: number
	) {
		super(parent, invokingState);
	}
	public property(): PropertyContext | null {
		return this.getRuleContext(0, PropertyContext);
	}
	public literalValue(): LiteralValueContext | null {
		return this.getRuleContext(0, LiteralValueContext);
	}
	public constructorExp(): ConstructorExpContext | null {
		return this.getRuleContext(0, ConstructorExpContext);
	}
	public STRING_LITERAL(): antlr.TerminalNode | null {
		return this.getToken(SpringExpressionsParser.STRING_LITERAL, 0);
	}
	public ASSIGNMENT(): antlr.TerminalNode | null {
		return this.getToken(SpringExpressionsParser.ASSIGNMENT, 0);
	}
	public basicExpression(): BasicExpressionContext | null {
		return this.getRuleContext(0, BasicExpressionContext);
	}
	public override get ruleIndex(): number {
		return SpringExpressionsParser.RULE_basicExpression;
	}
	public override enterRule(listener: SpringExpressionsListener): void {
		if (listener.enterBasicExpression) {
			listener.enterBasicExpression(this);
		}
	}
	public override exitRule(listener: SpringExpressionsListener): void {
		if (listener.exitBasicExpression) {
			listener.exitBasicExpression(this);
		}
	}
}

export class ChainMethodCallContext extends antlr.ParserRuleContext {
	public constructor(
		parent: antlr.ParserRuleContext | null,
		invokingState: number
	) {
		super(parent, invokingState);
	}
	public methodCall(): MethodCallContext[];
	public methodCall(i: number): MethodCallContext | null;
	public methodCall(
		i?: number
	): MethodCallContext[] | MethodCallContext | null {
		if (i === undefined) {
			return this.getRuleContexts(MethodCallContext);
		}

		return this.getRuleContext(i, MethodCallContext);
	}
	public DOT(): antlr.TerminalNode[];
	public DOT(i: number): antlr.TerminalNode | null;
	public DOT(i?: number): antlr.TerminalNode | null | antlr.TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SpringExpressionsParser.DOT);
		} else {
			return this.getToken(SpringExpressionsParser.DOT, i);
		}
	}
	public override get ruleIndex(): number {
		return SpringExpressionsParser.RULE_chainMethodCall;
	}
	public override enterRule(listener: SpringExpressionsListener): void {
		if (listener.enterChainMethodCall) {
			listener.enterChainMethodCall(this);
		}
	}
	public override exitRule(listener: SpringExpressionsListener): void {
		if (listener.exitChainMethodCall) {
			listener.exitChainMethodCall(this);
		}
	}
}

export class MethodCallContext extends antlr.ParserRuleContext {
	public constructor(
		parent: antlr.ParserRuleContext | null,
		invokingState: number
	) {
		super(parent, invokingState);
	}
	public property(): PropertyContext {
		return this.getRuleContext(0, PropertyContext)!;
	}
	public LPAREN(): antlr.TerminalNode {
		return this.getToken(SpringExpressionsParser.LPAREN, 0)!;
	}
	public RPAREN(): antlr.TerminalNode {
		return this.getToken(SpringExpressionsParser.RPAREN, 0)!;
	}
	public AT_SIGN(): antlr.TerminalNode | null {
		return this.getToken(SpringExpressionsParser.AT_SIGN, 0);
	}
	public parameterList(): ParameterListContext | null {
		return this.getRuleContext(0, ParameterListContext);
	}
	public override get ruleIndex(): number {
		return SpringExpressionsParser.RULE_methodCall;
	}
	public override enterRule(listener: SpringExpressionsListener): void {
		if (listener.enterMethodCall) {
			listener.enterMethodCall(this);
		}
	}
	public override exitRule(listener: SpringExpressionsListener): void {
		if (listener.exitMethodCall) {
			listener.exitMethodCall(this);
		}
	}
}

export class ConstructorExpContext extends antlr.ParserRuleContext {
	public constructor(
		parent: antlr.ParserRuleContext | null,
		invokingState: number
	) {
		super(parent, invokingState);
	}
	public NEW(): antlr.TerminalNode {
		return this.getToken(SpringExpressionsParser.NEW, 0)!;
	}
	public property(): PropertyContext {
		return this.getRuleContext(0, PropertyContext)!;
	}
	public LPAREN(): antlr.TerminalNode {
		return this.getToken(SpringExpressionsParser.LPAREN, 0)!;
	}
	public RPAREN(): antlr.TerminalNode {
		return this.getToken(SpringExpressionsParser.RPAREN, 0)!;
	}
	public parameterList(): ParameterListContext | null {
		return this.getRuleContext(0, ParameterListContext);
	}
	public override get ruleIndex(): number {
		return SpringExpressionsParser.RULE_constructorExp;
	}
	public override enterRule(listener: SpringExpressionsListener): void {
		if (listener.enterConstructorExp) {
			listener.enterConstructorExp(this);
		}
	}
	public override exitRule(listener: SpringExpressionsListener): void {
		if (listener.exitConstructorExp) {
			listener.exitConstructorExp(this);
		}
	}
}

export class ParameterListContext extends antlr.ParserRuleContext {
	public constructor(
		parent: antlr.ParserRuleContext | null,
		invokingState: number
	) {
		super(parent, invokingState);
	}
	public literalValue(): LiteralValueContext[];
	public literalValue(i: number): LiteralValueContext | null;
	public literalValue(
		i?: number
	): LiteralValueContext[] | LiteralValueContext | null {
		if (i === undefined) {
			return this.getRuleContexts(LiteralValueContext);
		}

		return this.getRuleContext(i, LiteralValueContext);
	}
	public property(): PropertyContext[];
	public property(i: number): PropertyContext | null;
	public property(i?: number): PropertyContext[] | PropertyContext | null {
		if (i === undefined) {
			return this.getRuleContexts(PropertyContext);
		}

		return this.getRuleContext(i, PropertyContext);
	}
	public constructorExp(): ConstructorExpContext[];
	public constructorExp(i: number): ConstructorExpContext | null;
	public constructorExp(
		i?: number
	): ConstructorExpContext[] | ConstructorExpContext | null {
		if (i === undefined) {
			return this.getRuleContexts(ConstructorExpContext);
		}

		return this.getRuleContext(i, ConstructorExpContext);
	}
	public COMMA(): antlr.TerminalNode[];
	public COMMA(i: number): antlr.TerminalNode | null;
	public COMMA(i?: number): antlr.TerminalNode | null | antlr.TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SpringExpressionsParser.COMMA);
		} else {
			return this.getToken(SpringExpressionsParser.COMMA, i);
		}
	}
	public override get ruleIndex(): number {
		return SpringExpressionsParser.RULE_parameterList;
	}
	public override enterRule(listener: SpringExpressionsListener): void {
		if (listener.enterParameterList) {
			listener.enterParameterList(this);
		}
	}
	public override exitRule(listener: SpringExpressionsListener): void {
		if (listener.exitParameterList) {
			listener.exitParameterList(this);
		}
	}
}

export class PropertyContext extends antlr.ParserRuleContext {
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
			return this.getTokens(SpringExpressionsParser.IDENTIFIER);
		} else {
			return this.getToken(SpringExpressionsParser.IDENTIFIER, i);
		}
	}
	public LSQUARE_BRACKET(): antlr.TerminalNode[];
	public LSQUARE_BRACKET(i: number): antlr.TerminalNode | null;
	public LSQUARE_BRACKET(
		i?: number
	): antlr.TerminalNode | null | antlr.TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SpringExpressionsParser.LSQUARE_BRACKET);
		} else {
			return this.getToken(SpringExpressionsParser.LSQUARE_BRACKET, i);
		}
	}
	public RSQUARE_BRACKET(): antlr.TerminalNode[];
	public RSQUARE_BRACKET(i: number): antlr.TerminalNode | null;
	public RSQUARE_BRACKET(
		i?: number
	): antlr.TerminalNode | null | antlr.TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SpringExpressionsParser.RSQUARE_BRACKET);
		} else {
			return this.getToken(SpringExpressionsParser.RSQUARE_BRACKET, i);
		}
	}
	public DOT(): antlr.TerminalNode[];
	public DOT(i: number): antlr.TerminalNode | null;
	public DOT(i?: number): antlr.TerminalNode | null | antlr.TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(SpringExpressionsParser.DOT);
		} else {
			return this.getToken(SpringExpressionsParser.DOT, i);
		}
	}
	public override get ruleIndex(): number {
		return SpringExpressionsParser.RULE_property;
	}
	public override enterRule(listener: SpringExpressionsListener): void {
		if (listener.enterProperty) {
			listener.enterProperty(this);
		}
	}
	public override exitRule(listener: SpringExpressionsListener): void {
		if (listener.exitProperty) {
			listener.exitProperty(this);
		}
	}
}

export class LiteralValueContext extends antlr.ParserRuleContext {
	public constructor(
		parent: antlr.ParserRuleContext | null,
		invokingState: number
	) {
		super(parent, invokingState);
	}
	public STRING_LITERAL(): antlr.TerminalNode | null {
		return this.getToken(SpringExpressionsParser.STRING_LITERAL, 0);
	}
	public NUMBER(): antlr.TerminalNode | null {
		return this.getToken(SpringExpressionsParser.NUMBER, 0);
	}
	public BOOLEAN(): antlr.TerminalNode | null {
		return this.getToken(SpringExpressionsParser.BOOLEAN, 0);
	}
	public override get ruleIndex(): number {
		return SpringExpressionsParser.RULE_literalValue;
	}
	public override enterRule(listener: SpringExpressionsListener): void {
		if (listener.enterLiteralValue) {
			listener.enterLiteralValue(this);
		}
	}
	public override exitRule(listener: SpringExpressionsListener): void {
		if (listener.exitLiteralValue) {
			listener.exitLiteralValue(this);
		}
	}
}

export class OperatorContext extends antlr.ParserRuleContext {
	public constructor(
		parent: antlr.ParserRuleContext | null,
		invokingState: number
	) {
		super(parent, invokingState);
	}
	public EQUALS(): antlr.TerminalNode | null {
		return this.getToken(SpringExpressionsParser.EQUALS, 0);
	}
	public NOT_EQUALS(): antlr.TerminalNode | null {
		return this.getToken(SpringExpressionsParser.NOT_EQUALS, 0);
	}
	public GT(): antlr.TerminalNode | null {
		return this.getToken(SpringExpressionsParser.GT, 0);
	}
	public LT(): antlr.TerminalNode | null {
		return this.getToken(SpringExpressionsParser.LT, 0);
	}
	public GTE(): antlr.TerminalNode | null {
		return this.getToken(SpringExpressionsParser.GTE, 0);
	}
	public LTE(): antlr.TerminalNode | null {
		return this.getToken(SpringExpressionsParser.LTE, 0);
	}
	public INSTANCEOF(): antlr.TerminalNode | null {
		return this.getToken(SpringExpressionsParser.INSTANCEOF, 0);
	}
	public BETWEEN(): antlr.TerminalNode | null {
		return this.getToken(SpringExpressionsParser.BETWEEN, 0);
	}
	public MATCHES(): antlr.TerminalNode | null {
		return this.getToken(SpringExpressionsParser.MATCHES, 0);
	}
	public override get ruleIndex(): number {
		return SpringExpressionsParser.RULE_operator;
	}
	public override enterRule(listener: SpringExpressionsListener): void {
		if (listener.enterOperator) {
			listener.enterOperator(this);
		}
	}
	public override exitRule(listener: SpringExpressionsListener): void {
		if (listener.exitOperator) {
			listener.exitOperator(this);
		}
	}
}
