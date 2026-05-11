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
package com.mgmtp.a12.uaa.client.rest.config.validation;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.core.env.Environment;

import com.mgmtp.a12.uaa.client.rest.config.AuthenticationType;

public abstract class AbstractTypeStringValidator<T extends Annotation, V> implements ConstraintValidator<T, V> {

	private static final String UAA_AUTHENTICATION_TYPE = "mgmtp.a12.uaa.authentication.client.rest.authentication-type";

	private AuthenticationType[] types;

	@Inject
	private Environment env;

	public AbstractTypeStringValidator(AuthenticationType... type) {
		this.types = type;
	}

	AuthenticationType[] getTypes() {
		return types;
	}

	void setTypes(AuthenticationType... types) {
		this.types = types;
	}

	@Override
	public boolean isValid(V value, ConstraintValidatorContext context) {
		String configuredTypeValue = Optional.ofNullable(env.getProperty(UAA_AUTHENTICATION_TYPE)).orElse(AuthenticationType.LOCAL.name());
		AuthenticationType configuredType = AuthenticationType.valueOf(configuredTypeValue);

		if (Arrays.asList(types).contains(configuredType)) {
			return validateValue(value, context);
		}
		return true;
	}

	abstract boolean validateValue(V value, ConstraintValidatorContext context);

}
