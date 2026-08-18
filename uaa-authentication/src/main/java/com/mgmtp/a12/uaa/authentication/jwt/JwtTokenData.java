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
package com.mgmtp.a12.uaa.authentication.jwt;

import java.time.Instant;
import java.util.Collection;

import jakarta.annotation.Generated;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtTokenData {

	private String token;
	private String username;
	private Collection<? extends GrantedAuthority> authorities;
	private UserDetails principal;
	private Instant issuedTime;
	/** Time of the first login. Preserved across token renewals so {@code user-lifetime-seconds} is measured from it. */
	private Instant loginTime;
	@Deprecated(since = "8.2.2", forRemoval = true)
	private Instant expirationTime;
	private Integer expirationSeconds;
	private Integer tokenRenewThresholdInSeconds;

	@Generated("SparkTools")
	private JwtTokenData(Builder builder) {
		this.token = builder.token;
		this.username = builder.username;
		this.authorities = builder.authorities;
		this.principal = builder.principal;
		this.loginTime = builder.loginTime;
		this.issuedTime = builder.issuedTime;
		this.expirationTime = builder.expirationTime;
		this.expirationSeconds = builder.expirationSeconds;
		this.tokenRenewThresholdInSeconds = builder.tokenRenewThresholdInSeconds;
	}

	public String getUsername() {
		return username;
	}

	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public UserDetails getPrincipal() {
		return principal;
	}

	/**
	 * Time of the first login. Unlike {@link #getIssuedTime()} this value is preserved across token renewals.
	 */
	public Instant getLoginTime() {
		return loginTime;
	}

	public Instant getIssuedTime() {
		return issuedTime;
	}

	public Instant getExpirationTime() {
		return issuedTime.plusSeconds(expirationSeconds);
	}

	public Integer getExpirationSeconds() {
		return expirationSeconds;
	}

	public Integer getTokenRenewThresholdInSeconds() {
		return tokenRenewThresholdInSeconds;
	}

	public String getToken() {
		return token;
	}

	@Override
	public String toString() {
		return "JwtTokenData [token=" + token + ", username=" + username + ", authorities=" + authorities + ", principal=" + principal + ", loginTime="
			+ loginTime + ", expirationTime=" + expirationTime + ", expirationSeconds=" + expirationSeconds + "]";
	}

	@Generated("SparkTools")
	public static Builder builderFrom(JwtTokenData jwtTokenData) {
		return new Builder(jwtTokenData);
	}

	@Generated("SparkTools")
	public static final class Builder {
		private String token;
		private String username;
		private Collection<? extends GrantedAuthority> authorities;
		private UserDetails principal;
		private Instant issuedTime;
		private Instant loginTime;
		@Deprecated(since = "8.2.2", forRemoval = true)
		private Instant expirationTime;
		private Integer expirationSeconds;
		private Integer tokenRenewThresholdInSeconds;

		public Builder(String username) {
			this.username = username;
		}

		private Builder(JwtTokenData jwtTokenData) {
			this.username = jwtTokenData.username;
			this.authorities = jwtTokenData.authorities;
			this.principal = jwtTokenData.principal;
			this.loginTime = jwtTokenData.loginTime;
			this.issuedTime = jwtTokenData.issuedTime;
			this.expirationTime = jwtTokenData.expirationTime;
			this.expirationSeconds = jwtTokenData.expirationSeconds;
			this.tokenRenewThresholdInSeconds = jwtTokenData.tokenRenewThresholdInSeconds;
		}

		public Builder withToken(String token) {
			this.token = token;
			return this;
		}

		public Builder withAuthorities(Collection<? extends GrantedAuthority> authorities) {
			this.authorities = authorities;
			return this;
		}

		public Builder withPrincipal(UserDetails principal) {
			this.principal = principal;
			return this;
		}

		public Builder withIssuedTime(Instant issuedTime) {
			this.issuedTime = issuedTime;
			return this;
		}

		public Builder withLoginTime(Instant loginTime) {
			this.loginTime = loginTime;
			return this;
		}

		@Deprecated(since = "8.2.2", forRemoval = true)
		public Builder withExpirationTime(Instant expirationTime) {
			this.expirationTime = expirationTime;
			return this;
		}

		public Builder withExpirationSeconds(Integer expirationSeconds) {
			this.expirationSeconds = expirationSeconds;
			return this;
		}

		public Builder withTokenRenewThresholdInSeconds(Integer tokenRenewThresholdInSeconds) {
			this.tokenRenewThresholdInSeconds = tokenRenewThresholdInSeconds;
			return this;
		}

		public JwtTokenData build() {
			return new JwtTokenData(this);
		}
	}

}
