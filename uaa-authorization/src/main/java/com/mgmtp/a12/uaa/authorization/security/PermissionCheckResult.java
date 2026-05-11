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
package com.mgmtp.a12.uaa.authorization.security;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jakarta.annotation.Generated;

import com.mgmtp.a12.uaa.authorization.model.PolicyAware;

public class PermissionCheckResult<T extends PolicyAware> {

	private boolean passed;
	private List<PermissionEvaluationResult<T>> permissionEvaluationResult;
	/**
	 * When masking an immutable resource it is unable to modify directly the resource.
	 * This field is responsible to keep the modified resource after masking (if occurred).
	 * <p>
	 * Note: This field is only set when authorization is processed through {@link com.mgmtp.a12.uaa.authorization.AuthorizationService}
	 */
	private Object immutableResourceAfterMasking;

	@Generated("SparkTools")
	private PermissionCheckResult(Builder<T> builder) {
		this.passed = builder.passed;
		this.permissionEvaluationResult = builder.permissionEvaluationResult;
	}

	public boolean isPassed() {
		return passed;
	}

	public boolean isNotPassed() {
		return !passed;
	}

	public List<T> getPassedPermissions() {
		return permissionEvaluationResult.stream()
			.filter(PermissionEvaluationResult::isPassed)
			.map(PermissionEvaluationResult::getPermission)
			.collect(Collectors.toList());
	}

	public List<T> getFailedPermissions() {
		return permissionEvaluationResult.stream()
			.filter(Predicate.not(PermissionEvaluationResult::isPassed))
			.map(PermissionEvaluationResult::getPermission)
			.collect(Collectors.toList());
	}

	public List<PermissionEvaluationResult<T>> getPermissionEvaluationResult() {
		return permissionEvaluationResult;
	}

	public Object getImmutableResourceAfterMasking() {
		return immutableResourceAfterMasking;
	}

	public void setImmutableResourceAfterMasking(Object immutableResourceAfterMasking) {
		this.immutableResourceAfterMasking = immutableResourceAfterMasking;
	}

	@Override
	public String toString() {
		return "PermissionCheckResult [passed=" + passed + ", permissionEvaluationResult=" + permissionEvaluationResult + "]";
	}

	@Generated("SparkTools")
	public static <T extends PolicyAware> Builder<T> builderFrom(PermissionCheckResult<T> permissionCheckResult) {
		return new Builder<T>(permissionCheckResult);
	}

	@Generated("SparkTools")
	public static final class Builder<T extends PolicyAware> {
		private boolean passed;
		private List<PermissionEvaluationResult<T>> permissionEvaluationResult = Collections.emptyList();

		public Builder(boolean passed, List<PermissionEvaluationResult<T>> permissionEvaluationResult) {
			this.passed = passed;
			this.permissionEvaluationResult = permissionEvaluationResult;
		}

		private Builder(PermissionCheckResult<T> permissionCheckResult) {
			this.passed = permissionCheckResult.passed;
			this.permissionEvaluationResult = permissionCheckResult.permissionEvaluationResult;
		}

		public void addPermissionEvaluationResult(PermissionEvaluationResult<T> permissionEvaluationResult) {
			this.permissionEvaluationResult.add(permissionEvaluationResult);
		}

		public PermissionCheckResult<T> build() {
			return new PermissionCheckResult<T>(this);
		}
	}

}
