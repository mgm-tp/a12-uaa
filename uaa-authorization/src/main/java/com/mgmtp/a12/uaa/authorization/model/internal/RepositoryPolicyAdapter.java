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
package com.mgmtp.a12.uaa.authorization.model.internal;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.Gson;
import com.mgmtp.a12.uaa.authorization.exception.InvalidAuthorizationDefinitionException;
import com.mgmtp.a12.uaa.authorization.model.RepositoryPolicy;

public class RepositoryPolicyAdapter extends RepositoryPolicy {
	private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryPolicyAdapter.class);
	private static final String TEMPLATES_FIELD = "templates";

	@JsonDeserialize(as = LinkedHashSet.class)
	@JsonProperty("templates")
	private Set<Object> templatesAdapter = new LinkedHashSet<>();

	public Set<Object> getTemplatesAdapter() {
		return templatesAdapter;
	}

	void setTemplatesAdapter(Set<Object> templatesAdapter) {
		this.templatesAdapter = templatesAdapter;
	}

	public RepositoryPolicy toRepositoryPolicy() {
		try {
			Field templatesField = this.getClass().getSuperclass().getDeclaredField(TEMPLATES_FIELD);
			templatesField.setAccessible(true);
			templatesField.set(this, this.templatesAdapter.stream().map(tem ->
				(tem instanceof Map) ? new Gson().toJson(tem, Map.class) : tem.toString()
			).collect(Collectors.toCollection(LinkedHashSet::new)));
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new InvalidAuthorizationDefinitionException();
		}
		return this;
	}
}
