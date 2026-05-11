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
package com.mgmtp.a12.uaa.authentication.principal.internal;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.mgmtp.a12.uaa.authentication.principal.ExternalPrincipal;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalConverter;

@Component
public class PrincipalConverterService {

	private static final String METHOD_NAME_CONVERTER = "convertPrincipal";
	private static final Logger LOGGER = LoggerFactory.getLogger(PrincipalConverterService.class);

	@Inject
	private List<PrincipalConverter<? extends UserDetails, ? extends ExternalPrincipal>> userConverters;

	public ExternalPrincipal convertPrincipal(UserDetails user) {
		LOGGER.debug("Using candidates [{}] for user external conversion", userConverters);
		Optional<PrincipalConverter<? extends UserDetails, ? extends ExternalPrincipal>> converterToUse =
			userConverters.stream()
				.filter(converter -> {
					Method method = findMethod(converter.getClass());
					Optional<Class<?>[]> matchedConverter = Optional.ofNullable(method)
						.map(Method::getParameterTypes)
						.filter(Objects::nonNull)
						.filter(parameters -> parameters.length == 1)
						.filter(parameters -> parameters[0].isAssignableFrom(user.getClass()));
					return matchedConverter.isPresent();
				})
				.findFirst();
		return converterToUse.map(convertor -> callConverter(convertor, user)).orElse(null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ExternalPrincipal callConverter(PrincipalConverter converter, UserDetails payload) {
		LOGGER.debug("Using converter [{}] for user [{}] conversion", converter.getClass().getCanonicalName(), payload.getClass().getCanonicalName());
		return converter.convertPrincipal(payload);
	}

	private Method findMethod(Class<?> clazz) {
		Method[] declaredMethods = ReflectionUtils.getDeclaredMethods(clazz);
		for (Method method : declaredMethods) {
			if ((METHOD_NAME_CONVERTER.equals(method.getName())) && (!method.isSynthetic())) {
				return method;
			}
		}
		return null;
	}
}
