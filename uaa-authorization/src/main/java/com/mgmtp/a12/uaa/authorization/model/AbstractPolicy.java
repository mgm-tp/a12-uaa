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
package com.mgmtp.a12.uaa.authorization.model;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"name",
	"description",
	"target",
	"dataPreload",
})
public abstract class AbstractPolicy extends NamedElement implements PolicyExecutionCallback {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPolicy.class);

	@JsonProperty("description")
	private String description;

	@JsonProperty("target")
	private String target;

	@JsonProperty("dataPreload")
	private List<String> dataPreload = null;

	public String getDescription() {
		return description;
	}

	void setDescription(String description) {
		this.description = description;
	}

	public String getTarget() {
		return target;
	}

	void setTarget(String target) {
		this.target = target;
	}

	public List<String> getDataPreload() {
		return dataPreload;
	}
	void setDataPreload(List<String> dataPreload) {
		this.dataPreload = dataPreload;
	}

	@Override
	public void policyExecuted(String name, Object result) {
		LOGGER.debug("Rule [%s] executed with result=[%s]".formatted(name, result));
	}

	@Override
	public void targetExecuted(String name, boolean result) {
		LOGGER.debug("Rule [%s] executed target with result=[%s]".formatted(name, result));
	}

	@Override
	public String toString() {
		return "AbstractPolicy [description=" + description + ", target=" + target + ", dataPreload=" + dataPreload + "]";
	}

}
