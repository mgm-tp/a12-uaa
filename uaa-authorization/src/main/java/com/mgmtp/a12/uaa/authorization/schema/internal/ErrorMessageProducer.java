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
package com.mgmtp.a12.uaa.authorization.schema.internal;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.uaa.authorization.schema.internal.location.LocationDetails;
import com.mgmtp.a12.uaa.authorization.schema.internal.location.LocationProvider;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;

public class ErrorMessageProducer {
	private static final String ERROR_PATTERN = "File: %s, Line: %s, Column: %s. Error: %s.";
	private final Set<ValidationMessage> messages;
	private final String fileName;

	public ErrorMessageProducer(Set<ValidationMessage> messages, JsonNode rootNode) {
		this.messages = messages;
		this.fileName = rootNode.get(SchemaValidator.FILENAME_FIELD).asText();
	}

	public List<String> generateMessages() {
		List<String> result = new ArrayList<>();
		messages.forEach(message -> {
			LocationDetails locationDetails = getLocationDetails(message);
			String error = createReadableMessage(message);
			result.add(ERROR_PATTERN.formatted(fileName, locationDetails.getStartLineNumber(), locationDetails.getStartColumnNumber(), error));
		});
		return result;
	}

	private LocationDetails getLocationDetails(ValidationMessage message) {
		JsonNode node = message.getInstanceNode();
		if (node instanceof LocationProvider provider) {
			return provider.getLocationDetails();
		}
		return new LocationDetails(null, null);
	}

	private String createReadableMessage(ValidationMessage error) {
		try {
			ValidatorTypeCode type = ValidatorTypeCode.fromValue(error.getType());
			switch (type) {
				case ADDITIONAL_PROPERTIES -> {
					return new MessageFormat("Property [{0}] is not allowed")
						.format(error.getArguments());
				}
				case REQUIRED -> {
					return new MessageFormat("Missing required property [{0}]")
						.format(error.getArguments());
				}
				default -> {
					return getDefaultMessage(error.getMessage());
				}
			}
		} catch (IllegalArgumentException ex) {
			return getDefaultMessage(error.getMessage());
		}
	}

	private String getDefaultMessage(String message) {
		return StringUtils.substringAfter(message, ":").trim();
	}

}
