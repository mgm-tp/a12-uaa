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

import tools.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"READ",
	"MASK",
	"WRITE"
})
public class Rights {

	@JsonProperty("READ")
	@JsonDeserialize(as = LinkedHashSet.class)
	private Set<String> read = new LinkedHashSet<>();
	@JsonProperty("MASK")
	@JsonDeserialize(as = LinkedHashSet.class)
	private Set<String> mask = new LinkedHashSet<>();
	@JsonProperty("WRITE")
	@JsonDeserialize(as = LinkedHashSet.class)
	private Set<String> write = new LinkedHashSet<>();

	public Set<String> getRead() {
		return read;
	}

	void setRead(Set<String> read) {
		this.read = read;
	}

	public Set<String> getMask() {
		return mask;
	}

	void setMask(Set<String> mask) {
		this.mask = mask;
	}

	public Set<String> getWrite() {
		return write;
	}

	void setWrite(Set<String> write) {
		this.write = write;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(Rights.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
		sb.append("read");
		sb.append('=');
		sb.append(((this.read == null) ? "<null>" : this.read));
		sb.append(',');
		sb.append("mask");
		sb.append('=');
		sb.append(((this.mask == null) ? "<null>" : this.mask));
		sb.append(',');
		sb.append("write");
		sb.append('=');
		sb.append(((this.write == null) ? "<null>" : this.write));
		sb.append(',');
		if (sb.charAt((sb.length() - 1)) == ',') {
			sb.setCharAt((sb.length() - 1), ']');
		} else {
			sb.append(']');
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = ((result * 31) + ((this.write == null) ? 0 : this.write.hashCode()));
		result = ((result * 31) + ((this.read == null) ? 0 : this.read.hashCode()));
		result = ((result * 31) + ((this.mask == null) ? 0 : this.mask.hashCode()));
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if ((other instanceof Rights) == false) {
			return false;
		}
		Rights rhs = ((Rights) other);
		return (((this.write == rhs.write) || ((this.write != null) && this.write.equals(rhs.write)))
			&& ((this.read == rhs.read) || ((this.read != null) && this.read.equals(rhs.read)))
			&& ((this.mask == rhs.mask) || ((this.mask != null) && this.mask.equals(rhs.mask))));
	}

}
