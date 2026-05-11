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
package com.mgmtp.a12.uaa.authentication.saml;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Generated;

import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;

/**
 * Data class for an extension object stored in {@link AuthnRequest} Extensions element.  
 *
 */
public class RequestExtension {

	private String namespace;
	private String name;
	private String prefix;
	private String value;
	private List<RequestExtension> attributes;
	private List<RequestExtension> childrens;

	@Generated("SparkTools")
	private RequestExtension(Builder builder) {
		this.namespace = builder.namespace;
		this.name = builder.name;
		this.prefix = builder.prefix;
		this.value = builder.value;
		this.childrens = builder.childrens;
		this.attributes = builder.attributes;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getName() {
		return name;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getValue() {
		return value;
	}

	public List<RequestExtension> getChildrens() {
		return childrens;
	}

	public List<RequestExtension> getAttributes() {
		return attributes;
	}

	@Override
	public String toString() {
		return "RequestExtension [namespace=" + namespace + ", name=" + name + ", prefix=" + prefix + ", value=" + value + ", attributes=" + attributes
			+ ", childrens=" + childrens + "]";
	}

	/**
	 * Builder to build {@link RequestExtension}.
	 */
	@Generated("SparkTools")
	public static final class Builder {
		private String namespace;
		private String name;
		private String prefix;
		private String value;
		private List<RequestExtension> attributes = new ArrayList<>();
		private List<RequestExtension> childrens = new ArrayList<>();

		public Builder(String name, String prefix, String value) {
			this.name = name;
			this.prefix = StringUtils.trimToEmpty(prefix);
			this.value = value;
		}

		public Builder withNamespace(String namespace) {
			this.namespace = namespace;
			return this;
		}

		public Builder witDefultEmptyNamespace() {
			this.namespace = SAMLConstants.SAML20P_NS;
			return this;
		}

		public Builder withChild(RequestExtension child) {
			return addChild(child);
		}

		public Builder addChild(RequestExtension child) {
			this.childrens.add(child);
			return this;
		}

		public Builder withAttributes(List<RequestExtension> attributes) {
			this.attributes = attributes;
			return this;
		}

		public Builder addAttribute(RequestExtension attribute) {
			this.attributes.add(attribute);
			return this;
		}

		public RequestExtension build() {
			return new RequestExtension(this);
		}
	}

}
