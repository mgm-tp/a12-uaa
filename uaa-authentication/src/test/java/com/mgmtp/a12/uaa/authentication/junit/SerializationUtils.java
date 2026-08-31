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
package com.mgmtp.a12.uaa.authentication.junit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;

import com.fasterxml.jackson.annotation.JsonInclude;

import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

public class SerializationUtils {
	public static ObjectMapper MAPPER = createConfiguredObjectMapper();
	//Force newlines to be LF (default is system dependent)
	static private DefaultPrettyPrinter printer = new DefaultPrettyPrinter()
		.withObjectIndenter(new DefaultIndenter("  ", "\n"));

	public static void assertSerialization(final String expectJsonResource, Object objectToSerialize) throws IOException {
		String expected = IOUtils.toString(
			new InputStreamReader(Objects.requireNonNull(SerializationUtils.class.getResourceAsStream(expectJsonResource)), StandardCharsets.UTF_8));
		assertSerializationFromString(expected, objectToSerialize);
	}

	public static void assertSerializationFromString(final String expected, Object objectToSerialize) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		MAPPER.writer().with(printer).writeValue(outputStream, objectToSerialize);
		String actual = outputStream.toString(StandardCharsets.UTF_8);
		Assertions.assertEquals(expected.replace("\r\n", "\n"), actual.replace("\r\n", "\n"));
	}

	static private ObjectMapper createConfiguredObjectMapper() {
		return JsonMapper.builder()
			// Deserialization (json -> Object)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
			.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)

			// Serialization (Object -> json)
			.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
			.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
			.changeDefaultPropertyInclusion(incl ->
				incl.withValueInclusion(JsonInclude.Include.NON_NULL))
			.changeDefaultPropertyInclusion(incl ->
				incl.withContentInclusion(JsonInclude.Include.NON_NULL))
			.enable(SerializationFeature.INDENT_OUTPUT)
			.build();
	}
}
