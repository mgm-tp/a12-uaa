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
import { ASTNode } from "vscode-json-languageservice";

import { NodeData, PropertyNodeLocation } from "../../../interfaces/nodeJson.js";

import { parseComplexExpressions } from "./expressions.js";

/*
 * Find all offsets of property-ref (policy-refs, repository-refs and rights-refs) based on name of main property (policy, repositoryPolicy and propertyRights)
 * Example: When click one of the name of policy, it will return all offsets of property has policy-refs include this name
 */
export function findReferenceNodeOffsets(
	root: ASTNode,
	propertyRefKey: string,
	searchName: string
): PropertyNodeLocation[] {
	const offsetPropertiesRef: PropertyNodeLocation[] = [];

	if (
		root.type === "array" &&
		root.parent?.type === "property" &&
		root.parent.keyNode.value === propertyRefKey
	) {
		const isPropertyRefHasSearchNAme = root.children.some(child => {
			return parseComplexExpressions(child.value as string).find(
				({ expression }) => expression === searchName
			);
		});

		if (isPropertyRefHasSearchNAme && root.parent.parent?.type === "object") {
			for (const property of root.parent.parent.properties) {
				if (property.keyNode.value === "name" && property.valueNode) {
					offsetPropertiesRef.push({
						offset: property.valueNode.offset,
						lengthText: property.valueNode.length
					});
				}
			}
		}
	}

	for (const child of root.children || []) {
		offsetPropertiesRef.push(
			...findReferenceNodeOffsets(child, propertyRefKey, searchName)
		);
	}

	return offsetPropertiesRef;
}

/*
 * Find all offsets of main property (policy, repositoryPolicy and propertyRights) based on name of property-ref (policy-refs, repository-refs and rights-refs)
 * Example: When click one of the name of policy-refs, it will return all offsets of policy has the name be equaled
 */
export function findNamePropertyOffsets(
	root: ASTNode,
	propertyKey: string,
	searchName: string
): number[] | null {
	if (
		root.type === "property" &&
		root.keyNode.value === propertyKey &&
		root.valueNode
	) {
		const offsetProperties: number[] = [];
		for (const child of root.valueNode.children || []) {
			const nameNode = child.children?.find(
				n =>
					n.type === "property" &&
					n.keyNode.value === "name" &&
					n.valueNode &&
					n.valueNode.value === searchName
			);
			if (nameNode) {
				offsetProperties.push(nameNode.offset);
			}
		}
		return offsetProperties;
	}
	for (const child of root.children || []) {
		const found = findNamePropertyOffsets(child, propertyKey, searchName);
		if (found) return found;
	}

	return null;
}

/**
 * Get list of key names from a node and its ancestors
 */
export function getKeyNamesOfNode(node: ASTNode): NodeData | null {
	if (!node.parent?.parent) {
		return null;
	}

	const nodeData: NodeData = {
		keys: [],
		name: null
	};

	let parentNode: ASTNode | undefined;

	switch (node.parent?.type) {
		case "property": {
			const keyValue = node.parent.keyNode?.value;
			if (keyValue) {
				nodeData.keys.push(keyValue);
			}
			nodeData.name = getNameOfNode(node);
			parentNode = node.parent;
			break;
		}
		case "array": {
			const arrayParent = node.parent.parent;
			if (arrayParent?.type === "property") {
				const arrayKey = arrayParent.keyNode?.value;
				if (arrayKey) {
					nodeData.keys.push(arrayKey);
				}
				parentNode = arrayParent;
			}
			break;
		}
		case "object":
			parentNode = node.parent;
			break;
	}

	if (!parentNode) {
		return nodeData;
	}

	const parentData = getKeyNamesOfNode(parentNode);
	if (parentData) {
		nodeData.keys.push(...parentData.keys);
		if (parentData.name != null) {
			nodeData.name = parentData.name;
		}
	}

	return nodeData;
}

function getNameOfNode(node: ASTNode): string {
	const parentChildren = node.parent?.parent?.children;
	if (!parentChildren) return "";

	for (const child of parentChildren) {
		if (
			child.type === "property" &&
			child.keyNode?.value === "name" &&
			child.valueNode?.type === "string"
		) {
			return child.valueNode.value;
		}
	}

	return "";
}
