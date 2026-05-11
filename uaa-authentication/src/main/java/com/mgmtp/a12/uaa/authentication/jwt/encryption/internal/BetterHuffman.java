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
package com.mgmtp.a12.uaa.authentication.jwt.encryption.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Original Huffman compression implementation.
 * Compresses text using variable-length prefix codes based on character frequency.
 * <p>
 * Algorithm: Characters appearing more frequently get shorter codes.
 * The tree structure and encoded data are stored in the compressed output.
 */
class BetterHuffman {

	private static final int MIN_COMPRESS_LENGTH = 3;

	/**
	 * Compresses a string into bytes using Huffman coding.
	 * For very short strings, returns uncompressed data.
	 *
	 * @param input String to compress
	 * @return Compressed byte array
	 */
	public static byte[] compress(final String input) {
		if (input == null || input.length() <= MIN_COMPRESS_LENGTH) {
			return input == null ? new byte[0] : input.getBytes(StandardCharsets.UTF_8);
		}

		// Count character frequencies
		Map<Character, Integer> frequencies = new HashMap<>();
		for (char c : input.toCharArray()) {
			frequencies.put(c, frequencies.getOrDefault(c, 0) + 1);
		}

		// Build Huffman tree
		TreeNode root = constructTree(frequencies);

		// Generate codes for each character
		Map<Character, String> codes = new HashMap<>();
		generateCodes(root, "", codes);

		// Write compressed data
		BitWriter writer = new BitWriter();
		serializeTree(root, writer);
		writer.writeInteger(input.length());

		for (char c : input.toCharArray()) {
			String code = codes.get(c);
			for (char bit : code.toCharArray()) {
				writer.writeBit(bit == '1');
			}
		}

		return writer.getBytes();
	}

	/**
	 * Decompresses bytes back to the original string.
	 *
	 * @param compressed Compressed byte array
	 * @return Original string
	 */
	public static String expand(final byte[] compressed) {
		if (compressed == null || compressed.length <= MIN_COMPRESS_LENGTH) {
			return compressed == null ? "" : new String(compressed, StandardCharsets.UTF_8);
		}

		BitReader reader = new BitReader(compressed);

		// Reconstruct the tree
		TreeNode root = deserializeTree(reader);

		// Read original length
		int length = reader.readInteger();

		// Decode characters
		StringBuilder result = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			TreeNode node = root;
			while (!node.isLeaf()) {
				node = reader.readBit() ? node.right : node.left;
			}
			result.append(node.character);
		}

		return result.toString();
	}

	/**
	 * Builds Huffman tree from character frequencies using a priority queue.
	 */
	private static TreeNode constructTree(Map<Character, Integer> frequencies) {
		PriorityQueue<TreeNode> queue = new PriorityQueue<>(
			Comparator.comparingInt(a -> a.frequency)
		);

		// Create leaf nodes
		for (Map.Entry<Character, Integer> entry : frequencies.entrySet()) {
			queue.offer(new TreeNode(entry.getKey(), entry.getValue()));
		}

		// Special case: single unique character
		if (queue.size() == 1) {
			queue.offer(new TreeNode('\0', 0));
		}

		// Merge nodes until one remains
		while (queue.size() > 1) {
			TreeNode left = queue.poll();
			TreeNode right = queue.poll();
			TreeNode parent = new TreeNode(left.frequency + right.frequency, left, right);
			queue.offer(parent);
		}

		return queue.poll();
	}

	/**
	 * Recursively generates binary codes for each character.
	 */
	private static void generateCodes(TreeNode node, String prefix, Map<Character, String> codes) {
		if (node.isLeaf()) {
			codes.put(node.character, prefix.isEmpty() ? "0" : prefix);
		} else {
			if (node.left != null) {
				generateCodes(node.left, prefix + "0", codes);
			}
			if (node.right != null) {
				generateCodes(node.right, prefix + "1", codes);
			}
		}
	}

	/**
	 * Writes tree structure to bit stream.
	 * Leaf nodes: bit 1 + UTF-8 character
	 * Internal nodes: bit 0 + left subtree + right subtree
	 */
	private static void serializeTree(TreeNode node, BitWriter writer) {
		if (node.isLeaf()) {
			writer.writeBit(true);
			byte[] charBytes = Character.toString(node.character).getBytes(StandardCharsets.UTF_8);
			for (byte b : charBytes) {
				writer.writeByte(b & 0xFF);
			}
		} else {
			writer.writeBit(false);
			serializeTree(node.left, writer);
			serializeTree(node.right, writer);
		}
	}

	/**
	 * Reconstructs tree from bit stream.
	 */
	private static TreeNode deserializeTree(BitReader reader) {
		boolean isLeaf = reader.readBit();
		if (isLeaf) {
			char character = reader.readUtf8Character();
			return new TreeNode(character, 0);
		} else {
			TreeNode left = deserializeTree(reader);
			TreeNode right = deserializeTree(reader);
			return new TreeNode(0, left, right);
		}
	}

	/**
	 * Represents a node in the Huffman tree.
	 */
	private static class TreeNode {
		final char character;
		final int frequency;
		final TreeNode left;
		final TreeNode right;

		// Leaf node constructor
		TreeNode(char character, int frequency) {
			this.character = character;
			this.frequency = frequency;
			this.left = null;
			this.right = null;
		}

		// Internal node constructor
		TreeNode(int frequency, TreeNode left, TreeNode right) {
			this.character = '\0';
			this.frequency = frequency;
			this.left = left;
			this.right = right;
		}

		boolean isLeaf() {
			return left == null && right == null;
		}
	}

	/**
	 * Utility for writing individual bits to a byte stream.
	 */
	private static class BitWriter {
		private final ByteArrayOutputStream output;
		private int currentByte;
		private int bitCount;

		BitWriter() {
			this.output = new ByteArrayOutputStream();
			this.currentByte = 0;
			this.bitCount = 0;
		}

		void writeBit(boolean bit) {
			currentByte = (currentByte << 1) | (bit ? 1 : 0);
			bitCount++;
			if (bitCount == 8) {
				output.write(currentByte);
				currentByte = 0;
				bitCount = 0;
			}
		}

		void writeByte(int value) {
			for (int i = 7; i >= 0; i--) {
				writeBit(((value >> i) & 1) == 1);
			}
		}

		void writeInteger(int value) {
			for (int i = 7; i >= 0; i--) {
				writeByte((value >> (i * 8)) & 0xFF);
			}
		}

		byte[] getBytes() {
			// Flush remaining bits
			if (bitCount > 0) {
				output.write(currentByte << (8 - bitCount));
			}
			return output.toByteArray();
		}
	}

	/**
	 * Utility for reading individual bits from a byte stream.
	 */
	private static class BitReader {
		private final ByteArrayInputStream input;
		private int currentByte;
		private int bitsLeft;

		BitReader(byte[] data) {
			this.input = new ByteArrayInputStream(data);
			this.currentByte = 0;
			this.bitsLeft = 0;
		}

		boolean readBit() {
			if (bitsLeft == 0) {
				currentByte = input.read();
				bitsLeft = 8;
			}
			bitsLeft--;
			return ((currentByte >> bitsLeft) & 1) == 1;
		}

		int readByte() {
			int value = currentByte << (8 - bitsLeft);

			if (bitsLeft == 8) {
				currentByte = input.read();
			} else {
				int savedBits = bitsLeft;
				currentByte = input.read();
				bitsLeft = 8;
				value |= (currentByte >> savedBits);
				bitsLeft = savedBits;
			}

			return value & 0xFF;
		}

		char readUtf8Character() {
			int firstByte = readByte();

			// ASCII (single byte)
			if ((firstByte & 0x80) == 0) {
				return (char) firstByte;
			}

			// Determine number of bytes in UTF-8 sequence
			int numBytes;
			if ((firstByte & 0xE0) == 0xC0) {
				numBytes = 2;
			} else if ((firstByte & 0xF0) == 0xE0) {
				numBytes = 3;
			} else if ((firstByte & 0xF8) == 0xF0) {
				numBytes = 4;
			} else {
				throw new IllegalStateException("Invalid UTF-8 sequence");
			}

			// Read continuation bytes
			byte[] bytes = new byte[numBytes];
			bytes[0] = (byte) firstByte;
			for (int i = 1; i < numBytes; i++) {
				bytes[i] = (byte) readByte();
			}

			// Convert to character
			String str = new String(bytes, StandardCharsets.UTF_8);
			if (str.length() != 1) {
				throw new IllegalStateException("Multi-codepoint character not supported");
			}
			return str.charAt(0);
		}

		int readInteger() {
			int value = 0;
			for (int i = 0; i < 8; i++) {
				value = (value << 8) | readByte();
			}
			return value;
		}
	}
}