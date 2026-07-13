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
package com.mgmtp.a12.uaa.authorization.schema.internal.location;

import java.util.Objects;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.NumericNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

public class LocationJsonNodeFactory extends JsonNodeFactory {
	private final JsonParser jsonParser;

	public LocationJsonNodeFactory(JsonParser jsonParser) {
		super();
		this.jsonParser = jsonParser;
	}

	@Override
	public ArrayNode arrayNode() {
		return new LocationArrayNode(this, jsonParser.currentTokenLocation(), jsonParser.currentLocation());
	}

	@Override
	public BooleanNode booleanNode(boolean v) {
		return new LocationBooleanNode(v, jsonParser.currentTokenLocation(), jsonParser.currentLocation());
	}

	@Override
	public NumericNode numberNode(int v) {
		return new LocationIntNode(v, jsonParser.currentTokenLocation(), jsonParser.currentLocation());
	}

	@Override
	public NullNode nullNode() {
		return new LocationNullNode(jsonParser.currentTokenLocation(), jsonParser.currentLocation());
	}

	@Override
	public ObjectNode objectNode() {
		return new LocationObjectNode(this, jsonParser.currentTokenLocation(), jsonParser.currentLocation());
	}

	@Override
	public StringNode stringNode(String text) {
		return Objects.nonNull(text) ? new LocationStringNode(text, jsonParser.currentTokenLocation(), jsonParser.currentLocation()) : null;
	}
}
