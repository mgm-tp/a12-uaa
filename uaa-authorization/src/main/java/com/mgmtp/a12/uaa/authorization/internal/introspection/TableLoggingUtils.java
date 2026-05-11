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
package com.mgmtp.a12.uaa.authorization.internal.introspection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableLoggingUtils {

	private static final int PADDING_SIZE = 2;
	private static final String NEW_LINE = "\n";
	private static final String TABLE_JOINT_SYMBOL = "+";
	private static final String TABLE_V_SPLIT_SYMBOL = "|";
	private static final String TABLE_H_SPLIT_SYMBOL = "-";
	private static final String RESET = "\u001B[0m";
	private static final String YELLOW = "\u001B[33m";

	private TableLoggingUtils() {
	}

	public static String generateTable(List<String> headersList, List<List<String>> rowsList) {
		StringBuilder stringBuilder = new StringBuilder();
		Map<Integer, Integer> columnMaxWidthMapping = getMaximumWidthOfTable(headersList, rowsList);

		// top table border
		stringBuilder.append(NEW_LINE);
		createRowLine(stringBuilder, headersList.size(), columnMaxWidthMapping);
		stringBuilder.append(NEW_LINE);

		for (int headerIndex = 0; headerIndex < headersList.size(); headerIndex++) {
			fillCellContent(stringBuilder, headersList.get(headerIndex), headerIndex, columnMaxWidthMapping, YELLOW);
		}

		// end header section
		stringBuilder.append(NEW_LINE);
		createRowLine(stringBuilder, headersList.size(), columnMaxWidthMapping);

		for (List<String> row : rowsList) {
			stringBuilder.append(NEW_LINE);
			for (int cellIndex = 0; cellIndex < row.size(); cellIndex++) {
				String rowValue = String.valueOf(row.get(cellIndex));
				fillCellContent(stringBuilder, rowValue, cellIndex, columnMaxWidthMapping);
			}

			stringBuilder.append(NEW_LINE);
			createRowLine(stringBuilder, headersList.size(), columnMaxWidthMapping);
		}

		return stringBuilder.toString();
	}

	private static void fillSpace(StringBuilder stringBuilder, int length) {
		stringBuilder.append(" ".repeat(Math.max(0, length)));
	}

	private static void createRowLine(StringBuilder stringBuilder, int numberOfColumns, Map<Integer, Integer> columnMaxWidthMapping) {
		stringBuilder.append(TABLE_JOINT_SYMBOL);
		for (int i = 0; i < numberOfColumns; i++) {
			stringBuilder.append(TABLE_H_SPLIT_SYMBOL.repeat(columnMaxWidthMapping.get(i) + PADDING_SIZE * 2));
			stringBuilder.append(TABLE_JOINT_SYMBOL);
		}
	}

	private static Map<Integer, Integer> getMaximumWidthOfTable(List<String> headersList, List<List<String>> rowsList) {
		Map<Integer, Integer> columnMaxWidthMapping = new HashMap<>();

		for (int columnIndex = 0; columnIndex < headersList.size(); columnIndex++) {
			columnMaxWidthMapping.put(columnIndex, headersList.get(columnIndex).length());
		}

		for (List<String> row : rowsList) {
			for (int columnIndex = 0; columnIndex < row.size(); columnIndex++) {
				String value = row.get(columnIndex);
				if (value.length() > columnMaxWidthMapping.get(columnIndex)) {
					columnMaxWidthMapping.put(columnIndex, value.length());
				}
			}
		}

		return columnMaxWidthMapping;
	}

	private static int getCellPadding(int dataLength, int maxCellLength) {
		if (dataLength < maxCellLength) {
			return PADDING_SIZE + (maxCellLength - dataLength) / 2;
		}

		return PADDING_SIZE;
	}

	private static void fillCellContent(StringBuilder stringBuilder, String cell, int cellIndex, Map<Integer, Integer> columnMaxWidthMapping, String ...color) {
		int cellPaddingSize = getCellPadding(cell.length(), columnMaxWidthMapping.get(cellIndex));

		if (cellIndex == 0) {
			stringBuilder.append(TABLE_V_SPLIT_SYMBOL);
		}

		fillSpace(stringBuilder, cellPaddingSize);
		stringBuilder.append(color.length > 0 ? color[0] + cell + RESET : cell);
		fillSpace(stringBuilder, cellPaddingSize);

		int totalCellLength = cell.length() + 2 * cellPaddingSize;
		if (totalCellLength < columnMaxWidthMapping.get(cellIndex) + 2 * PADDING_SIZE) {
			fillSpace(stringBuilder, columnMaxWidthMapping.get(cellIndex) + 2 * PADDING_SIZE - totalCellLength);
		}

		stringBuilder.append(TABLE_V_SPLIT_SYMBOL);
	}
}
