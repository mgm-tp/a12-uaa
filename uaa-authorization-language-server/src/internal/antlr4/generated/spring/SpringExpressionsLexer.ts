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

export class SpringExpressionsLexer extends antlr.Lexer {
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

	public static readonly channelNames = ["DEFAULT_TOKEN_CHANNEL", "HIDDEN"];

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

	public static readonly modeNames = ["DEFAULT_MODE"];

	public static readonly ruleNames = [
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
		"WS",
		"ESC"
	];

	public constructor(input: antlr.CharStream) {
		super(input);
		this.interpreter = new antlr.LexerATNSimulator(
			this,
			SpringExpressionsLexer._ATN,
			SpringExpressionsLexer.decisionsToDFA,
			new antlr.PredictionContextCache()
		);
	}

	public get grammarFileName(): string {
		return "SpringExpressions.g4";
	}

	public get literalNames(): (string | null)[] {
		return SpringExpressionsLexer.literalNames;
	}
	public get symbolicNames(): (string | null)[] {
		return SpringExpressionsLexer.symbolicNames;
	}
	public get ruleNames(): string[] {
		return SpringExpressionsLexer.ruleNames;
	}

	public get serializedATN(): number[] {
		return SpringExpressionsLexer._serializedATN;
	}

	public get channelNames(): string[] {
		return SpringExpressionsLexer.channelNames;
	}

	public get modeNames(): string[] {
		return SpringExpressionsLexer.modeNames;
	}

	public static readonly _serializedATN: number[] = [
		4, 0, 30, 207, 6, -1, 2, 0, 7, 0, 2, 1, 7, 1, 2, 2, 7, 2, 2, 3, 7, 3, 2, 4,
		7, 4, 2, 5, 7, 5, 2, 6, 7, 6, 2, 7, 7, 7, 2, 8, 7, 8, 2, 9, 7, 9, 2, 10, 7,
		10, 2, 11, 7, 11, 2, 12, 7, 12, 2, 13, 7, 13, 2, 14, 7, 14, 2, 15, 7, 15, 2,
		16, 7, 16, 2, 17, 7, 17, 2, 18, 7, 18, 2, 19, 7, 19, 2, 20, 7, 20, 2, 21, 7,
		21, 2, 22, 7, 22, 2, 23, 7, 23, 2, 24, 7, 24, 2, 25, 7, 25, 2, 26, 7, 26, 2,
		27, 7, 27, 2, 28, 7, 28, 2, 29, 7, 29, 2, 30, 7, 30, 1, 0, 1, 0, 1, 0, 1, 0,
		1, 0, 3, 0, 69, 8, 0, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 75, 8, 1, 1, 2, 1, 2, 1,
		2, 1, 3, 1, 3, 1, 3, 1, 4, 1, 4, 1, 5, 1, 5, 1, 6, 1, 6, 1, 7, 1, 7, 1, 7,
		1, 8, 1, 8, 1, 8, 1, 9, 1, 9, 1, 9, 1, 9, 1, 9, 1, 9, 1, 10, 1, 10, 1, 11,
		1, 11, 1, 12, 1, 12, 1, 13, 1, 13, 1, 14, 1, 14, 1, 15, 1, 15, 1, 16, 1, 16,
		1, 17, 1, 17, 1, 18, 1, 18, 1, 19, 1, 19, 1, 20, 1, 20, 1, 20, 1, 20, 1, 20,
		1, 20, 1, 20, 1, 20, 1, 20, 1, 20, 1, 20, 1, 21, 1, 21, 1, 21, 1, 21, 1, 21,
		1, 21, 1, 21, 1, 21, 1, 22, 1, 22, 1, 22, 1, 22, 1, 22, 1, 22, 1, 22, 1, 22,
		1, 23, 1, 23, 1, 23, 1, 23, 1, 24, 1, 24, 5, 24, 154, 8, 24, 10, 24, 12, 24,
		157, 9, 24, 1, 25, 1, 25, 1, 25, 1, 25, 1, 25, 1, 25, 5, 25, 165, 8, 25, 10,
		25, 12, 25, 168, 9, 25, 1, 25, 1, 25, 1, 26, 4, 26, 173, 8, 26, 11, 26, 12,
		26, 174, 1, 26, 1, 26, 4, 26, 179, 8, 26, 11, 26, 12, 26, 180, 3, 26, 183,
		8, 26, 1, 27, 1, 27, 1, 27, 1, 27, 1, 27, 1, 27, 1, 27, 1, 27, 1, 27, 3, 27,
		194, 8, 27, 1, 28, 1, 28, 1, 29, 4, 29, 199, 8, 29, 11, 29, 12, 29, 200, 1,
		29, 1, 29, 1, 30, 1, 30, 1, 30, 0, 0, 31, 1, 1, 3, 2, 5, 3, 7, 4, 9, 5, 11,
		6, 13, 7, 15, 8, 17, 9, 19, 10, 21, 11, 23, 12, 25, 13, 27, 14, 29, 15, 31,
		16, 33, 17, 35, 18, 37, 19, 39, 20, 41, 21, 43, 22, 45, 23, 47, 24, 49, 25,
		51, 26, 53, 27, 55, 28, 57, 29, 59, 30, 61, 0, 1, 0, 12, 2, 0, 65, 65, 97,
		97, 2, 0, 78, 78, 110, 110, 2, 0, 68, 68, 100, 100, 2, 0, 79, 79, 111, 111,
		2, 0, 82, 82, 114, 114, 2, 0, 65, 90, 97, 122, 3, 0, 48, 57, 65, 90, 97,
		122, 2, 0, 39, 39, 92, 92, 1, 0, 39, 39, 1, 0, 48, 57, 2, 0, 38, 38, 124,
		124, 3, 0, 9, 10, 13, 13, 32, 32, 217, 0, 1, 1, 0, 0, 0, 0, 3, 1, 0, 0, 0,
		0, 5, 1, 0, 0, 0, 0, 7, 1, 0, 0, 0, 0, 9, 1, 0, 0, 0, 0, 11, 1, 0, 0, 0, 0,
		13, 1, 0, 0, 0, 0, 15, 1, 0, 0, 0, 0, 17, 1, 0, 0, 0, 0, 19, 1, 0, 0, 0, 0,
		21, 1, 0, 0, 0, 0, 23, 1, 0, 0, 0, 0, 25, 1, 0, 0, 0, 0, 27, 1, 0, 0, 0, 0,
		29, 1, 0, 0, 0, 0, 31, 1, 0, 0, 0, 0, 33, 1, 0, 0, 0, 0, 35, 1, 0, 0, 0, 0,
		37, 1, 0, 0, 0, 0, 39, 1, 0, 0, 0, 0, 41, 1, 0, 0, 0, 0, 43, 1, 0, 0, 0, 0,
		45, 1, 0, 0, 0, 0, 47, 1, 0, 0, 0, 0, 49, 1, 0, 0, 0, 0, 51, 1, 0, 0, 0, 0,
		53, 1, 0, 0, 0, 0, 55, 1, 0, 0, 0, 0, 57, 1, 0, 0, 0, 0, 59, 1, 0, 0, 0, 1,
		68, 1, 0, 0, 0, 3, 74, 1, 0, 0, 0, 5, 76, 1, 0, 0, 0, 7, 79, 1, 0, 0, 0, 9,
		82, 1, 0, 0, 0, 11, 84, 1, 0, 0, 0, 13, 86, 1, 0, 0, 0, 15, 88, 1, 0, 0, 0,
		17, 91, 1, 0, 0, 0, 19, 94, 1, 0, 0, 0, 21, 100, 1, 0, 0, 0, 23, 102, 1, 0,
		0, 0, 25, 104, 1, 0, 0, 0, 27, 106, 1, 0, 0, 0, 29, 108, 1, 0, 0, 0, 31,
		110, 1, 0, 0, 0, 33, 112, 1, 0, 0, 0, 35, 114, 1, 0, 0, 0, 37, 116, 1, 0, 0,
		0, 39, 118, 1, 0, 0, 0, 41, 120, 1, 0, 0, 0, 43, 131, 1, 0, 0, 0, 45, 139,
		1, 0, 0, 0, 47, 147, 1, 0, 0, 0, 49, 151, 1, 0, 0, 0, 51, 158, 1, 0, 0, 0,
		53, 172, 1, 0, 0, 0, 55, 193, 1, 0, 0, 0, 57, 195, 1, 0, 0, 0, 59, 198, 1,
		0, 0, 0, 61, 204, 1, 0, 0, 0, 63, 64, 5, 38, 0, 0, 64, 69, 5, 38, 0, 0, 65,
		66, 7, 0, 0, 0, 66, 67, 7, 1, 0, 0, 67, 69, 7, 2, 0, 0, 68, 63, 1, 0, 0, 0,
		68, 65, 1, 0, 0, 0, 69, 2, 1, 0, 0, 0, 70, 71, 5, 124, 0, 0, 71, 75, 5, 124,
		0, 0, 72, 73, 7, 3, 0, 0, 73, 75, 7, 4, 0, 0, 74, 70, 1, 0, 0, 0, 74, 72, 1,
		0, 0, 0, 75, 4, 1, 0, 0, 0, 76, 77, 5, 61, 0, 0, 77, 78, 5, 61, 0, 0, 78, 6,
		1, 0, 0, 0, 79, 80, 5, 33, 0, 0, 80, 81, 5, 61, 0, 0, 81, 8, 1, 0, 0, 0, 82,
		83, 5, 33, 0, 0, 83, 10, 1, 0, 0, 0, 84, 85, 5, 62, 0, 0, 85, 12, 1, 0, 0,
		0, 86, 87, 5, 60, 0, 0, 87, 14, 1, 0, 0, 0, 88, 89, 5, 62, 0, 0, 89, 90, 5,
		61, 0, 0, 90, 16, 1, 0, 0, 0, 91, 92, 5, 60, 0, 0, 92, 93, 5, 61, 0, 0, 93,
		18, 1, 0, 0, 0, 94, 95, 5, 112, 0, 0, 95, 96, 5, 114, 0, 0, 96, 97, 5, 105,
		0, 0, 97, 98, 5, 110, 0, 0, 98, 99, 5, 116, 0, 0, 99, 20, 1, 0, 0, 0, 100,
		101, 5, 40, 0, 0, 101, 22, 1, 0, 0, 0, 102, 103, 5, 41, 0, 0, 103, 24, 1, 0,
		0, 0, 104, 105, 5, 91, 0, 0, 105, 26, 1, 0, 0, 0, 106, 107, 5, 93, 0, 0,
		107, 28, 1, 0, 0, 0, 108, 109, 5, 43, 0, 0, 109, 30, 1, 0, 0, 0, 110, 111,
		5, 45, 0, 0, 111, 32, 1, 0, 0, 0, 112, 113, 5, 46, 0, 0, 113, 34, 1, 0, 0,
		0, 114, 115, 5, 44, 0, 0, 115, 36, 1, 0, 0, 0, 116, 117, 5, 64, 0, 0, 117,
		38, 1, 0, 0, 0, 118, 119, 5, 61, 0, 0, 119, 40, 1, 0, 0, 0, 120, 121, 5,
		105, 0, 0, 121, 122, 5, 110, 0, 0, 122, 123, 5, 115, 0, 0, 123, 124, 5, 116,
		0, 0, 124, 125, 5, 97, 0, 0, 125, 126, 5, 110, 0, 0, 126, 127, 5, 99, 0, 0,
		127, 128, 5, 101, 0, 0, 128, 129, 5, 111, 0, 0, 129, 130, 5, 102, 0, 0, 130,
		42, 1, 0, 0, 0, 131, 132, 5, 98, 0, 0, 132, 133, 5, 101, 0, 0, 133, 134, 5,
		116, 0, 0, 134, 135, 5, 119, 0, 0, 135, 136, 5, 101, 0, 0, 136, 137, 5, 101,
		0, 0, 137, 138, 5, 110, 0, 0, 138, 44, 1, 0, 0, 0, 139, 140, 5, 109, 0, 0,
		140, 141, 5, 97, 0, 0, 141, 142, 5, 116, 0, 0, 142, 143, 5, 99, 0, 0, 143,
		144, 5, 104, 0, 0, 144, 145, 5, 101, 0, 0, 145, 146, 5, 115, 0, 0, 146, 46,
		1, 0, 0, 0, 147, 148, 5, 110, 0, 0, 148, 149, 5, 101, 0, 0, 149, 150, 5,
		119, 0, 0, 150, 48, 1, 0, 0, 0, 151, 155, 7, 5, 0, 0, 152, 154, 7, 6, 0, 0,
		153, 152, 1, 0, 0, 0, 154, 157, 1, 0, 0, 0, 155, 153, 1, 0, 0, 0, 155, 156,
		1, 0, 0, 0, 156, 50, 1, 0, 0, 0, 157, 155, 1, 0, 0, 0, 158, 166, 5, 39, 0,
		0, 159, 165, 3, 61, 30, 0, 160, 165, 8, 7, 0, 0, 161, 165, 8, 8, 0, 0, 162,
		163, 5, 39, 0, 0, 163, 165, 5, 39, 0, 0, 164, 159, 1, 0, 0, 0, 164, 160, 1,
		0, 0, 0, 164, 161, 1, 0, 0, 0, 164, 162, 1, 0, 0, 0, 165, 168, 1, 0, 0, 0,
		166, 164, 1, 0, 0, 0, 166, 167, 1, 0, 0, 0, 167, 169, 1, 0, 0, 0, 168, 166,
		1, 0, 0, 0, 169, 170, 5, 39, 0, 0, 170, 52, 1, 0, 0, 0, 171, 173, 7, 9, 0,
		0, 172, 171, 1, 0, 0, 0, 173, 174, 1, 0, 0, 0, 174, 172, 1, 0, 0, 0, 174,
		175, 1, 0, 0, 0, 175, 182, 1, 0, 0, 0, 176, 178, 5, 46, 0, 0, 177, 179, 7,
		9, 0, 0, 178, 177, 1, 0, 0, 0, 179, 180, 1, 0, 0, 0, 180, 178, 1, 0, 0, 0,
		180, 181, 1, 0, 0, 0, 181, 183, 1, 0, 0, 0, 182, 176, 1, 0, 0, 0, 182, 183,
		1, 0, 0, 0, 183, 54, 1, 0, 0, 0, 184, 185, 5, 116, 0, 0, 185, 186, 5, 114,
		0, 0, 186, 187, 5, 117, 0, 0, 187, 194, 5, 101, 0, 0, 188, 189, 5, 102, 0,
		0, 189, 190, 5, 97, 0, 0, 190, 191, 5, 108, 0, 0, 191, 192, 5, 115, 0, 0,
		192, 194, 5, 101, 0, 0, 193, 184, 1, 0, 0, 0, 193, 188, 1, 0, 0, 0, 194, 56,
		1, 0, 0, 0, 195, 196, 7, 10, 0, 0, 196, 58, 1, 0, 0, 0, 197, 199, 7, 11, 0,
		0, 198, 197, 1, 0, 0, 0, 199, 200, 1, 0, 0, 0, 200, 198, 1, 0, 0, 0, 200,
		201, 1, 0, 0, 0, 201, 202, 1, 0, 0, 0, 202, 203, 6, 29, 0, 0, 203, 60, 1, 0,
		0, 0, 204, 205, 5, 92, 0, 0, 205, 206, 9, 0, 0, 0, 206, 62, 1, 0, 0, 0, 11,
		0, 68, 74, 155, 164, 166, 174, 180, 182, 193, 200, 1, 6, 0, 0
	];

	private static __ATN: antlr.ATN;
	public static get _ATN(): antlr.ATN {
		if (!SpringExpressionsLexer.__ATN) {
			SpringExpressionsLexer.__ATN = new antlr.ATNDeserializer().deserialize(
				SpringExpressionsLexer._serializedATN
			);
		}

		return SpringExpressionsLexer.__ATN;
	}

	private static readonly vocabulary = new antlr.Vocabulary(
		SpringExpressionsLexer.literalNames,
		SpringExpressionsLexer.symbolicNames,
		[]
	);

	public override get vocabulary(): antlr.Vocabulary {
		return SpringExpressionsLexer.vocabulary;
	}

	private static readonly decisionsToDFA =
		SpringExpressionsLexer._ATN.decisionToState.map(
			(ds: antlr.DecisionState, index: number) => new antlr.DFA(ds, index)
		);
}
