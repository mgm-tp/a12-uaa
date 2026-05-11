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
package com.mgmtp.a12.uaa.authorization.internal.introspection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;

import com.mgmtp.a12.uaa.authorization.internal.AuthorizationConfigurationScanner;
import com.mgmtp.a12.uaa.authorization.internal.RuntimeAuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.model.Permission;

public class ScopeIntrospector extends AbstractAuthorizationIntrospector {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScopeIntrospector.class);

	private final String[] DEFAULT_SCANNED_PACKAGES = {"com.mgmtp"};
	private final Map<String, String> SCANNED_PROPERTIES_AND_DEFAULT_VALUES = Map.of(
		"mgmtp.a12.uaa.authorization.web.uri-secured-contexts", "/actuator/**::RelativePath"
	);
	private final Map<Class<? extends Annotation>, AuthorizationConfigurationScanner.AnnotationExpressionGetter<?>>
		SCANNED_ANNOTATIONS = Map.of(
		PreAuthorize.class, (PreAuthorize annotation) -> annotation.value(),
		PostAuthorize.class, (PostAuthorize annotation) -> annotation.value(),
		PreFilter.class, (PreFilter annotation) -> annotation.value(),
		PostFilter.class, (PostFilter annotation) -> annotation.value()
	);

	private AuthorizationConfigurationScanner authorizationScanner;

	public ScopeIntrospector(RuntimeAuthorizationDefinitionRepository authorizationDefRepository, Environment environment) {
		super(authorizationDefRepository, environment);

		List<String> configPackages = environment.getProperty("mgmtp.a12.uaa.authorization.scan-entity-packages", List.class);
		String[] scannedPackages = CollectionUtils.isNotEmpty(configPackages) ? configPackages.toArray(new String[]{}) : DEFAULT_SCANNED_PACKAGES;
		authorizationScanner = new AuthorizationConfigurationScanner(scannedPackages);
	}

	@Override
	public boolean process() {
		Set<Permission> allPermissions = authorizationDefRepository.getPermissions();

		if (CollectionUtils.isEmpty(allPermissions)) {
			return true;
		}

		Set<String> annotationExpressions = getAuthorizationExpressions();
		Set<String> configPropertyExpressions = getAuthorizationProperties();
		Map<Permission, List<String>> orphanScopesMap = new HashMap<>();

		allPermissions.forEach(permission -> {
			for (var scope : permission.getScopes()) {
				if (annotationExpressions.stream().noneMatch(p -> p.contains("'%s'".formatted(scope))) &&
					configPropertyExpressions.stream().noneMatch(p -> p.contains(scope))) {
					List<String> existingScopes = orphanScopesMap.computeIfAbsent(permission, p -> new ArrayList<>());
					existingScopes.add(scope);
				}
			}
		});

		if (MapUtils.isNotEmpty(orphanScopesMap)) {
			List<List<String>> rows = new ArrayList<>();
			for (var entry : orphanScopesMap.entrySet()) {
				rows.add(List.of(entry.getKey().getName(), String.join("; ", entry.getValue())));
			}
			LOGGER.warn("\nThese permissions have orphan scopes (skip if they are used by java functions):" +
				TableLoggingUtils.generateTable(List.of("Permission", "Scopes"), rows));
			return false;
		}

		return true;
	}

	private Set<String> getAuthorizationExpressions() {
		Set<String> expressions = new HashSet<>();
		for (var entry : SCANNED_ANNOTATIONS.entrySet()) {
			Set<Method> annotatedMethods = authorizationScanner.scanAnnotatedMethods(entry.getKey());
			Set<Class<?>> annotatedTypes = authorizationScanner.scanAnnotatedTypes(entry.getKey());
			expressions.addAll(authorizationScanner.getAnnotationExpressionsOfMethods(annotatedMethods, entry.getKey(), entry.getValue()));
			expressions.addAll(authorizationScanner.getAnnotationExpressionsOfTypes(annotatedTypes, entry.getKey(), entry.getValue()));
		}
		return expressions;
	}

	private Set<String> getAuthorizationProperties() {
		return SCANNED_PROPERTIES_AND_DEFAULT_VALUES.keySet().stream()
			.map(prop -> {
				String value = environment.getProperty(prop);
				return StringUtils.isNotEmpty(value) ? value : SCANNED_PROPERTIES_AND_DEFAULT_VALUES.get(prop);
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}
}
