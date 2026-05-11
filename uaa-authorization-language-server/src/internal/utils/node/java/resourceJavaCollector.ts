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
	BaseJavaCstVisitorWithDefaults,
	ClassDeclarationCtx,
	FieldDeclarationCtx,
	MethodDeclarationCtx,
	parse
} from "java-parser";
import { Position } from "vscode-languageserver-types";

import { JavaClassMetadata } from "../../../interfaces/metadata.js";

/**
 * A custom visitor that traverses the CST to collect
 * field and method declarations.
 */
export class ResourceJavaCollector extends BaseJavaCstVisitorWithDefaults {
	private _className: string | null = null;
	private _classNamePosition: Position | null = null;
	private readonly _fields: string[] = [];
	private readonly _methods: string[] = [];

	constructor() {
		super();
		this.validateVisitor();
	}

	get fields(): string[] {
		return this._fields;
	}

	get methods(): string[] {
		return this._methods;
	}

	get className(): string | null {
		return this._className;
	}

	get classNamePosition(): { line: number; character: number } | null {
		return this._classNamePosition;
	}

	classDeclaration(ctx: ClassDeclarationCtx) {
		const normalClassDeclaration = ctx.normalClassDeclaration?.[0];
		if (normalClassDeclaration) {
			// Extract the class name
			const typeIdentifier =
				normalClassDeclaration.children.typeIdentifier?.[0];
			const identifierToken = typeIdentifier?.children.Identifier?.[0];
			if (identifierToken) {
				this._className = identifierToken.image;
				this._classNamePosition = {
					line: identifierToken.startLine - 1,
					character: identifierToken.startColumn - 1
				};
			}

			// Visit the class body to extract fields and methods
			const classBody = normalClassDeclaration.children.classBody?.[0];
			if (classBody) {
				this.visit(classBody);
			}
		}
	}

	fieldDeclaration(ctx: FieldDeclarationCtx) {
		const varDeclList = ctx.variableDeclaratorList[0];

		const declarators = varDeclList.children.variableDeclarator;
		for (const dec of declarators) {
			const variableDeclCtx = dec.children.variableDeclaratorId[0];
			const identifierToken = variableDeclCtx.children.Identifier?.[0];
			if (identifierToken) {
				this._fields.push(identifierToken.image);
			}
		}
	}

	methodDeclaration(ctx: MethodDeclarationCtx) {
		const methodHeader = ctx.methodHeader[0];
		const methodDeclarator = methodHeader.children.methodDeclarator[0];
		const methodNameToken = methodDeclarator.children.Identifier[0];
		this._methods.push(methodNameToken.image.concat("()"));
	}
}

export function getMetadataClass(javaPath: string): JavaClassMetadata {
	const cst = parse(javaPath);
	const collector = new ResourceJavaCollector();
	collector.visit(cst);
	return {
		className: collector.className,
		classNamePosition: collector.classNamePosition,
		fields: new Set(collector.fields),
		methods: new Set(collector.methods)
	};
}
