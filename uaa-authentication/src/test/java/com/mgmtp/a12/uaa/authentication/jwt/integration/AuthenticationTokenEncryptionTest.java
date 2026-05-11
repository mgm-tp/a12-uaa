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
package com.mgmtp.a12.uaa.authentication.jwt.integration;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.a12.uaa.authentication.jwt.encryption.DataEncoder;
import com.mgmtp.a12.uaa.authentication.jwt.encryption.internal.BypassingEncoder;
import com.mgmtp.a12.uaa.authentication.jwt.encryption.internal.HuffmanEncoder;

public class AuthenticationTokenEncryptionTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationTokenEncryptionTest.class);
	private DataEncoder bypassingEncoder = new BypassingEncoder();
	private DataEncoder huffmanEncoder = new HuffmanEncoder();
	private String realMessage =
		"{  \"@class\": \"com.mgmtp.a12.uaa.authentication.jwt.internal.UserWrapper\",  \"user\": " +
			"{\"@class\": \"com.mgmtp.a12.uaa.authentication.user.internal.UAAUser\",\"password\": null," +
			"\"username\": \"ek-ff2e73aa80f06248866aa6f99ccb6701def15660\",\"authorities\": [  \"java.util.Collections$UnmodifiableSet\"," +
			"  [{  \"@class\": \"org.springframework.security.core.authority.SimpleGrantedAuthority\",  \"authority\": \"citizen\"}," +
			"{  \"@class\": \"org.springframework.security.core.authority.SimpleGrantedAuthority\",  \"authority\": \"guest\"}  ]]," +
			"\"accountNonExpired\": true,\"accountNonLocked\": true,\"credentialsNonExpired\": true,\"enabled\": true,\"extendedUserData\": " +
			"{  \"@class\": \"com.mgmtp.mycase.server.security.dto.MyCaseUserDto\",  \"dataForPrepopulate\": " +
			"{\"@class\": \"com.mgmtp.mycase.server.security.dto.CitizenPrepopulateDataDto\",\"elsterId\": \"ek-ff2e73aa80f06248866aa6f99ccb6701def15660\"," +
			"\"vorname\": \"Yiğit\",\"nachname\": \"Şahin Kılıç\",\"name\": \"Yiğit Şahin Kılıç\",\"adresstyp\": \"Inland\",\"strasse\": \"Finkenweg\"," +
			"\"hausnummer\": \"99\",\"plz\": \"80333\",\"ort\": \"Hintertupfing\",\"land\": \"DE\",\"identificationTrustLevel\": \"substanziell\"," +
			"\"authenticationTrustLevel\": \"substanziell\",\"identifikationsnummer\": \"\",\"geburtsdatum\": \"1978-01-27\",\"geburtsland\": \"\"," +
			"\"geburtsname\": \"\"},\"metadata\": {\"duebelId\": \"\"}}  },  \"random\": 1014500141} special char: ěščřž๗ปἍὃ";

	@Test
	public void checkBypassingEncoder() {
		checkMessageEncodingAndDecoding(bypassingEncoder);
	}

	@Test
	public void checkHuffmanEncoder() {
		checkMessageEncodingAndDecoding(huffmanEncoder);
	}

	@Test
	public void checkEncodedSize() {
		String huffmanEncrypted = checkMessageEncodingAndDecoding(huffmanEncoder, realMessage);

		LOGGER.info("Message sizes: Original=[{}], huffman=[{}]",
			realMessage.length(),
			huffmanEncrypted.length());
	}

	private void checkMessageEncodingAndDecoding(DataEncoder encoder) {
		String testMessage = "";
		int size = 1;
		for (int i = 0; i < size; i++) {
			checkMessageEncodingAndDecoding(encoder, testMessage);
			testMessage += RandomStringUtils.randomAlphabetic(1);
		}
		checkMessageEncodingAndDecoding(encoder, realMessage);
	}

	private String checkMessageEncodingAndDecoding(DataEncoder encoder, String message) {
		String encrypted = encoder.encrypt(message);
		String decrypted = encoder.decrypt(encrypted);
		System.out.println("Encrypted size [%s], decrypted size [%s]".formatted(encrypted.length(), decrypted.length()));
		Assertions.assertEquals(message, decrypted);
		return encrypted;
	}
}
