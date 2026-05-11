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
package com.mgmtp.a12.uaa.client.rest.cache.internal;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

public class WrappedInputStream extends FilterInputStream {

	private byte[] body = new byte[] {};

	protected WrappedInputStream(InputStream in) {
		super(in);
	}

	@Override
	public int read() throws IOException {
		return super.read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		int readBytes = super.read(b);
		combineData(b, readBytes);
		return readBytes;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int readBytes = super.read(b, off, len);
		combineData(b, readBytes);
		return readBytes;
	}

	private void combineData(byte[] readBytes, int length) {
		byte[] effectiveBytes = Arrays.copyOf(readBytes, length + 1);
		body = ArrayUtils.addAll(body, effectiveBytes);
	}

	public byte[] getBody() {
		return body;
	}

}
