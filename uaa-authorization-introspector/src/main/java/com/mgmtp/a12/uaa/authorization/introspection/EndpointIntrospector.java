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
package com.mgmtp.a12.uaa.authorization.introspection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import com.mgmtp.a12.uaa.authorization.internal.introspection.AbstractAuthorizationIntrospector;

public class EndpointIntrospector extends AbstractAuthorizationIntrospector {

	private final List<Class<? extends Annotation>> POSSIBLE_AUTHORIZATION_ANNOTATIONS =
		List.of(PreAuthorize.class, PostAuthorize.class, PreFilter.class, PostFilter.class);
	private final String WHITELIST_ENDPOINTS_PROPERTY = "mgmtp.a12.uaa.authorization.web.introspection.whitelist-endpoints";
	private final String URI_SECURED_CONTEXT_PROPERTY = "mgmtp.a12.uaa.authorization.web.uri-secured-contexts";
	private final String UN_SECURED_URL_PROPERTY = "mgmtp.a12.uaa.authentication.unsecured.urls";
	private final List<String> UN_SECURED_URLS_UAA = List.of(
		"/**/uaa-authentication/selfconfigure",
		"/**/uaa-authentication/tokenValid",
		"/**/uaa-authentication/oauth2TokenValid",
		"/**/uaa-authentication/authorize",
		"/**/uaa-authentication/token",
		"/**/uaa-authentication/exchangeAuthorizationCodeToToken/authorize",
		"/**/uaa-authentication/exchangeAuthorizationCodeToToken"
	);
	private final AntPathMatcher pathMatcher = createPathMatcher();
	private final Map<RequestMappingInfo, HandlerMethod> existingEndpoints;

	public EndpointIntrospector(Environment environment, Map<RequestMappingInfo, HandlerMethod> existingEndpoints) {
		super(null, environment);
		this.existingEndpoints = existingEndpoints;
	}

	@Override
	public boolean process() {
		List<String> whitelistEndpoints = environment.getProperty(WHITELIST_ENDPOINTS_PROPERTY, List.class);
		List<String> uriSecuredContexts = environment.getProperty(URI_SECURED_CONTEXT_PROPERTY, List.class);
		List<String> unSecuredUrls = environment.getProperty(UN_SECURED_URL_PROPERTY, List.class);
		List<String> bypassUrls =
			Optional.ofNullable(uriSecuredContexts).map(uris -> uris.stream().map(u -> u.split("::")[0]).collect(Collectors.toList()))
				.orElse(new ArrayList<>());
		bypassUrls.addAll(UN_SECURED_URLS_UAA);
		bypassUrls.addAll(Optional.ofNullable(whitelistEndpoints).orElse(new ArrayList<>()));
		bypassUrls.addAll(Optional.ofNullable(unSecuredUrls).orElse(new ArrayList<>()));
		Set<String> errorEndpoints = new HashSet<>();
		Set<RequestMappingInfo> unAuthorizedEndpoints = getUnAuthorizedEndpoints();

		unAuthorizedEndpoints.forEach(endpoint -> {
			Method method = existingEndpoints.get(endpoint).getMethod();
			if (isAnnotatedByAuthorization(method) || isAnnotatedByInterfaces(method)) {
				return;
			}

			if (isNoneUriMatch(endpoint.getPatternValues(), bypassUrls)) {
				errorEndpoints.addAll(endpoint.getPatternValues());
			}
		});

		if (CollectionUtils.isNotEmpty(errorEndpoints)) {
			throw new RuntimeException("Please configure authorization policy for these endpoints: %s".formatted(errorEndpoints));
		}

		return true;
	}

	private AntPathMatcher createPathMatcher() {
		AntPathMatcher matcher = new AntPathMatcher();
		matcher.setTrimTokens(false);
		matcher.setCaseSensitive(false);
		return matcher;
	}

	private boolean isAnnotatedByAuthorization(Method method) {
		return POSSIBLE_AUTHORIZATION_ANNOTATIONS.stream().anyMatch(annotation -> isAnnotationPresent(method, annotation));
	}

	private boolean isAnnotatedByInterfaces(Method method) {
		for (Class<?> inheritedInterfaces : Arrays.stream(method.getDeclaringClass().getInterfaces()).toList()) {
			try {
				Method inheritedMethod = inheritedInterfaces.getDeclaredMethod(method.getName(), method.getParameterTypes());
				if (isAnnotatedByAuthorization(inheritedMethod)) {
					return true;
				}
			} catch (Exception e) {
				// no method found and do nothing
			}
		}

		return false;
	}

	private boolean isNoneUriMatch(Set<String> patterns, List<String> uris) {
		if (CollectionUtils.isEmpty(patterns) || CollectionUtils.isEmpty(uris)) {
			return true;
		}
		return patterns.stream().noneMatch(pattern -> uris.stream().anyMatch(uri -> pathMatcher.match(uri, pattern)));
	}

	private Set<RequestMappingInfo> getUnAuthorizedEndpoints() {
		Set<Class<?>> allTypes = existingEndpoints.values().stream()
			.map(method -> {
				Class<?> original = method.getMethod().getDeclaringClass();
				List<Class<?>> inheritedInterfaces = new ArrayList<>(Arrays.stream(original.getInterfaces()).toList());
				inheritedInterfaces.add(original);
				return inheritedInterfaces;
			})
			.flatMap(Collection::stream)
			.collect(Collectors.toSet());

		Set<Class<?>> authorizedTypes = allTypes.stream().filter(type -> POSSIBLE_AUTHORIZATION_ANNOTATIONS.stream()
			.anyMatch(annotation -> isAnnotationPresent(type, annotation)))
			.collect(Collectors.toSet());

		return existingEndpoints.keySet().stream().filter(endpoint -> {
			Class<?> declaringClass = existingEndpoints.get(endpoint).getMethod().getDeclaringClass();
			Class<?>[] inheritedInterfaces = declaringClass.getInterfaces();
			return !CollectionUtils.containsAny(authorizedTypes, inheritedInterfaces, declaringClass);
		}).collect(Collectors.toSet());
	}

	private <T extends AnnotatedElement> boolean isAnnotationPresent(T type, Class<? extends Annotation> clazz) {
		if (type.isAnnotationPresent(clazz)) {
			return true;
		}

		for (Annotation annotation : Arrays.stream(type.getAnnotations()).toList()) {
			if (annotation.annotationType().isAnnotationPresent(clazz)) {
				return true;
			}
		}

		return false;
	}
}
