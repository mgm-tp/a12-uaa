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
package com.mgmtp.a12.uaa.client.rest.auth.token.internal;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodeExchangeUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(CodeExchangeUtils.class);

	public static String generateState() {
		return RandomStringUtils.random(32, 0, 0, true, true, null, new SecureRandom());
	}

	public static String generateCodeVerifier() {
		SecureRandom sr = new SecureRandom();
		byte[] code = new byte[32];
		sr.nextBytes(code);
		return Base64.encodeBase64URLSafeString(code);
	}

	public static String generateCodeChallenge(String codeVerifier) {
		String codeChallenge = null;
		try {
			byte[] bytes = codeVerifier.getBytes(StandardCharsets.US_ASCII);
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(bytes, 0, bytes.length);
			byte[] digest = md.digest();
			codeChallenge = Base64.encodeBase64URLSafeString(digest);
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("Code challenge generating failed.", e);
		}
		return codeChallenge;
	}
}
