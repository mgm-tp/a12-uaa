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
package com.mgmtp.a12.uaa.authentication.saml.internal;

import java.io.Serializable;
import java.time.Instant;

import jakarta.annotation.Generated;

import org.springframework.security.saml2.provider.service.authentication.AbstractSaml2AuthenticationRequest;

public class AuthenticationRequestData implements Serializable {

	private AbstractSaml2AuthenticationRequest samlAuthenticationRequest;
	private Instant creationDate;

	@Generated("SparkTools")
	private AuthenticationRequestData(Builder builder) {
		this.samlAuthenticationRequest = builder.samlauAuthenticationRequest;
		this.creationDate = builder.creationDate;
	}

	public AbstractSaml2AuthenticationRequest getSamlAuthenticationRequest() {
		return samlAuthenticationRequest;
	}

	public Instant getCreationDate() {
		return creationDate;
	}

	@Generated("SparkTools")
	public static Builder builder() {
		return new Builder();
	}

	@Generated("SparkTools")
	public static Builder builderFrom(AuthenticationRequestData authenticationRequestData) {
		return new Builder(authenticationRequestData);
	}

	@Generated("SparkTools")
	public static final class Builder {
		private AbstractSaml2AuthenticationRequest samlauAuthenticationRequest;
		private Instant creationDate;

		private Builder() {
		}

		private Builder(AuthenticationRequestData authenticationRequestData) {
			this.samlauAuthenticationRequest = authenticationRequestData.samlAuthenticationRequest;
			this.creationDate = authenticationRequestData.creationDate;
		}

		public Builder withSamlauAuthenticationRequest(AbstractSaml2AuthenticationRequest samlauAuthenticationRequest) {
			this.samlauAuthenticationRequest = samlauAuthenticationRequest;
			return this;
		}

		public Builder withCreationDate(Instant creationDate) {
			this.creationDate = creationDate;
			return this;
		}

		public AuthenticationRequestData build() {
			return new AuthenticationRequestData(this);
		}
	}

}
