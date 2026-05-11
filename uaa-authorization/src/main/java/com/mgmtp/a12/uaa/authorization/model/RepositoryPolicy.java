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

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"templates",
	"template-refs"
})
public class RepositoryPolicy extends AbstractPolicy {

	@JsonDeserialize(as = LinkedHashSet.class)
	@JsonProperty("templates")
	private Set<String> templates = new LinkedHashSet<>();

	@Deprecated(since = "8.2.0")
	@JsonDeserialize(as = LinkedHashSet.class)
	@JsonProperty("template-refs")
	private Set<String> templateRefs = new LinkedHashSet<>();

	public Set<String> getTemplates() {
		return templates;
	}

	void setTemplates(Set<String> templates) {
		this.templates = templates;
	}

	@Deprecated(since = "8.2.0")
	public Set<String> getTemplateRefs() {
		return templateRefs;
	}

	@Deprecated(since = "8.2.0")
	void setTemplateRefs(Set<String> templateRefs) {
		this.templateRefs = templateRefs;
	}

	@Override
	public String toString() {
		return super.toString() + " RepositoryPolicy [templates=" + templates + ", templateRefs=" + templateRefs + "]";
	}
}
