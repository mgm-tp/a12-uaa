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
package com.mgmtp.a12.uaa.authorization.schema.internal.listener;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mgmtp.a12.uaa.authorization.schema.internal.collector.GlobalRefsCollector;
import com.networknt.schema.CollectorContext;
import com.networknt.schema.Error;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;
import com.networknt.schema.walk.WalkListener;

public class RefsListener implements WalkListener {

	@Override
	public WalkFlow onWalkStart(WalkEvent walkEvent) {

		String instancePath = walkEvent.getInstanceLocation().toString();
		CollectorContext collectorContext = walkEvent.getExecutionContext().getCollectorContext();
		Object collector = collectorContext.get(instancePath);
		if (collector != null) {
			combine((GlobalRefsCollector) collector, (ArrayNode) walkEvent.getInstanceNode());
		}
		return WalkFlow.SKIP;
	}

	@Override public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
		// Empty body
	}

	private void combine(GlobalRefsCollector collector, ArrayNode arrayNode) {
		if (Objects.nonNull(arrayNode)) {
			Set<JsonNode> result = new LinkedHashSet<>();
			arrayNode.forEach(result::add);

			collector.combine(result);
		}
	}
}
