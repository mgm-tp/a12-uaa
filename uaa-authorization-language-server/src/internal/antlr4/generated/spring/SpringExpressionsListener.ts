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

import {
	ErrorNode,
	ParseTreeListener,
	ParserRuleContext,
	TerminalNode
} from "antlr4ng";

import { ExpressionContext } from "./SpringExpressionsParser.js";
import { PrintExpressionContext } from "./SpringExpressionsParser.js";
import { LogicalExpressionWithParenContext } from "./SpringExpressionsParser.js";
import { LogicalExpressionContext } from "./SpringExpressionsParser.js";
import { CompositeConditionContext } from "./SpringExpressionsParser.js";
import { ComplexConditionContext } from "./SpringExpressionsParser.js";
import { BaseConditionContext } from "./SpringExpressionsParser.js";
import { BasicExpressionContext } from "./SpringExpressionsParser.js";
import { ChainMethodCallContext } from "./SpringExpressionsParser.js";
import { MethodCallContext } from "./SpringExpressionsParser.js";
import { ConstructorExpContext } from "./SpringExpressionsParser.js";
import { ParameterListContext } from "./SpringExpressionsParser.js";
import { PropertyContext } from "./SpringExpressionsParser.js";
import { LiteralValueContext } from "./SpringExpressionsParser.js";
import { OperatorContext } from "./SpringExpressionsParser.js";

/**
 * This interface defines a complete listener for a parse tree produced by
 * `SpringExpressionsParser`.
 */
export class SpringExpressionsListener implements ParseTreeListener {
	/**
	 * Enter a parse tree produced by `SpringExpressionsParser.expression`.
	 * @param ctx the parse tree
	 */
	enterExpression?: (ctx: ExpressionContext) => void;
	/**
	 * Exit a parse tree produced by `SpringExpressionsParser.expression`.
	 * @param ctx the parse tree
	 */
	exitExpression?: (ctx: ExpressionContext) => void;
	/**
	 * Enter a parse tree produced by `SpringExpressionsParser.printExpression`.
	 * @param ctx the parse tree
	 */
	enterPrintExpression?: (ctx: PrintExpressionContext) => void;
	/**
	 * Exit a parse tree produced by `SpringExpressionsParser.printExpression`.
	 * @param ctx the parse tree
	 */
	exitPrintExpression?: (ctx: PrintExpressionContext) => void;
	/**
	 * Enter a parse tree produced by `SpringExpressionsParser.logicalExpressionWithParen`.
	 * @param ctx the parse tree
	 */
	enterLogicalExpressionWithParen?: (
		ctx: LogicalExpressionWithParenContext
	) => void;
	/**
	 * Exit a parse tree produced by `SpringExpressionsParser.logicalExpressionWithParen`.
	 * @param ctx the parse tree
	 */
	exitLogicalExpressionWithParen?: (
		ctx: LogicalExpressionWithParenContext
	) => void;
	/**
	 * Enter a parse tree produced by `SpringExpressionsParser.logicalExpression`.
	 * @param ctx the parse tree
	 */
	enterLogicalExpression?: (ctx: LogicalExpressionContext) => void;
	/**
	 * Exit a parse tree produced by `SpringExpressionsParser.logicalExpression`.
	 * @param ctx the parse tree
	 */
	exitLogicalExpression?: (ctx: LogicalExpressionContext) => void;
	/**
	 * Enter a parse tree produced by `SpringExpressionsParser.compositeCondition`.
	 * @param ctx the parse tree
	 */
	enterCompositeCondition?: (ctx: CompositeConditionContext) => void;
	/**
	 * Exit a parse tree produced by `SpringExpressionsParser.compositeCondition`.
	 * @param ctx the parse tree
	 */
	exitCompositeCondition?: (ctx: CompositeConditionContext) => void;
	/**
	 * Enter a parse tree produced by `SpringExpressionsParser.complexCondition`.
	 * @param ctx the parse tree
	 */
	enterComplexCondition?: (ctx: ComplexConditionContext) => void;
	/**
	 * Exit a parse tree produced by `SpringExpressionsParser.complexCondition`.
	 * @param ctx the parse tree
	 */
	exitComplexCondition?: (ctx: ComplexConditionContext) => void;
	/**
	 * Enter a parse tree produced by `SpringExpressionsParser.baseCondition`.
	 * @param ctx the parse tree
	 */
	enterBaseCondition?: (ctx: BaseConditionContext) => void;
	/**
	 * Exit a parse tree produced by `SpringExpressionsParser.baseCondition`.
	 * @param ctx the parse tree
	 */
	exitBaseCondition?: (ctx: BaseConditionContext) => void;
	/**
	 * Enter a parse tree produced by `SpringExpressionsParser.basicExpression`.
	 * @param ctx the parse tree
	 */
	enterBasicExpression?: (ctx: BasicExpressionContext) => void;
	/**
	 * Exit a parse tree produced by `SpringExpressionsParser.basicExpression`.
	 * @param ctx the parse tree
	 */
	exitBasicExpression?: (ctx: BasicExpressionContext) => void;
	/**
	 * Enter a parse tree produced by `SpringExpressionsParser.chainMethodCall`.
	 * @param ctx the parse tree
	 */
	enterChainMethodCall?: (ctx: ChainMethodCallContext) => void;
	/**
	 * Exit a parse tree produced by `SpringExpressionsParser.chainMethodCall`.
	 * @param ctx the parse tree
	 */
	exitChainMethodCall?: (ctx: ChainMethodCallContext) => void;
	/**
	 * Enter a parse tree produced by `SpringExpressionsParser.methodCall`.
	 * @param ctx the parse tree
	 */
	enterMethodCall?: (ctx: MethodCallContext) => void;
	/**
	 * Exit a parse tree produced by `SpringExpressionsParser.methodCall`.
	 * @param ctx the parse tree
	 */
	exitMethodCall?: (ctx: MethodCallContext) => void;
	/**
	 * Enter a parse tree produced by `SpringExpressionsParser.constructorExp`.
	 * @param ctx the parse tree
	 */
	enterConstructorExp?: (ctx: ConstructorExpContext) => void;
	/**
	 * Exit a parse tree produced by `SpringExpressionsParser.constructorExp`.
	 * @param ctx the parse tree
	 */
	exitConstructorExp?: (ctx: ConstructorExpContext) => void;
	/**
	 * Enter a parse tree produced by `SpringExpressionsParser.parameterList`.
	 * @param ctx the parse tree
	 */
	enterParameterList?: (ctx: ParameterListContext) => void;
	/**
	 * Exit a parse tree produced by `SpringExpressionsParser.parameterList`.
	 * @param ctx the parse tree
	 */
	exitParameterList?: (ctx: ParameterListContext) => void;
	/**
	 * Enter a parse tree produced by `SpringExpressionsParser.property`.
	 * @param ctx the parse tree
	 */
	enterProperty?: (ctx: PropertyContext) => void;
	/**
	 * Exit a parse tree produced by `SpringExpressionsParser.property`.
	 * @param ctx the parse tree
	 */
	exitProperty?: (ctx: PropertyContext) => void;
	/**
	 * Enter a parse tree produced by `SpringExpressionsParser.literalValue`.
	 * @param ctx the parse tree
	 */
	enterLiteralValue?: (ctx: LiteralValueContext) => void;
	/**
	 * Exit a parse tree produced by `SpringExpressionsParser.literalValue`.
	 * @param ctx the parse tree
	 */
	exitLiteralValue?: (ctx: LiteralValueContext) => void;
	/**
	 * Enter a parse tree produced by `SpringExpressionsParser.operator`.
	 * @param ctx the parse tree
	 */
	enterOperator?: (ctx: OperatorContext) => void;
	/**
	 * Exit a parse tree produced by `SpringExpressionsParser.operator`.
	 * @param ctx the parse tree
	 */
	exitOperator?: (ctx: OperatorContext) => void;

	visitTerminal(node: TerminalNode): void {}
	visitErrorNode(node: ErrorNode): void {}
	enterEveryRule(node: ParserRuleContext): void {}
	exitEveryRule(node: ParserRuleContext): void {}
}
