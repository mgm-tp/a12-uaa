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
package com.mgmtp.a12.uaa.authorization.security.spel.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.ObjectUtils;

/**
 * Base UAA root object. It's container for all shared methods used by SpEL.
 *
 */
public class UAABaseSpelSecurityExpressionRoot extends SecurityExpressionRoot {

	private static final Logger LOGGER = LoggerFactory.getLogger(UAABaseSpelSecurityExpressionRoot.class.getCanonicalName() + "_Print");

	public UAABaseSpelSecurityExpressionRoot(Authentication authentication) {
		super(authentication);
	}

	public boolean isResourceName(Object resource, String name) {
		if (resource == null) {
			throw new RuntimeException("No resource object");
		}
		Object realResource = ObjectResolver.resolveDataObject(resource);
		return Optional.ofNullable(realResource).map(r -> r.getClass().getCanonicalName().equals(name)).orElse(false);
	}

	public boolean print(Object message) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(ObjectUtils.nullSafeToString(message));
		}
		return true;
	}

	public boolean hasObjectWithPropertyValue(Collection<Object> collection, String propertyName, String properyValue) {
		return collection.stream()
			.filter(element -> hasElementWithPropertyValue(element, propertyName, properyValue))
			.findAny().isPresent();
	}

	public boolean hasNestedObjectWithPropertyValue(Collection<? extends Object> collection, String collectionName, String propertyName, String properyValue) {
		return collection.stream()
			.map(element -> getObjectProperty(element, collectionName))
			.filter(Objects::nonNull)
			.filter(element -> Collection.class.isAssignableFrom(element.getClass())) //check if it's a collection
			.map(Collection.class::cast)
			.filter(nestedCollection -> hasObjectWithPropertyValue(nestedCollection, propertyName, properyValue))
			.findAny()
			.isPresent();
	}

	/**
	 * Replacement of
	 * principal.authorities.?[!accessRights.?[name == 'MODEL_WRITE'].isEmpty()]
	 */

	public boolean hasAccessRight(String roleName) {
		return hasNestedObjectWithPropertyValue(resolveAuthorities(), "accessRights", "name", roleName);
	}

	/**
	 * Replacement of:
	 * principal.authorities.?[#resource.objectRoles.contains(name)].isEmpty()
	 */
	public boolean containsAnyRole(Collection<String> objectRoles) {
		return resolveAuthorities().stream()
			.filter(authority -> objectRoles.contains(authority.getAuthority()))
			.findAny().isPresent();

	}

	private Collection<? extends GrantedAuthority> resolveAuthorities() {
		Collection<? extends GrantedAuthority> authorities = getAuthentication().getAuthorities();
		UserDetails principal = resolvePrincipal();

		if (principal != null) {
			authorities = principal.getAuthorities();
		}
		return authorities;
	}

	protected UserDetails resolvePrincipal() {
		Object principal = getAuthentication().getPrincipal();

		if (principal instanceof UserDetails details) {
			return details;
		}
		return null;
	}

	private boolean hasElementWithPropertyValue(Object element, String propertyName, String properyValue) {
		try {
			Object property = getObjectProperty(element, propertyName);
			return StringUtils.equals(ObjectUtils.nullSafeToString(property), properyValue);
		} catch (Exception e) {
			LOGGER.debug("Missing property [{}] on object [{}]", propertyName, ClassUtils.getName(element));
		}
		return false;
	}

	private Object getObjectProperty(Object element, String propertyName) {
		try {
			return PropertyUtils.getProperty(element, propertyName);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			LOGGER.debug("Missing property [{}] on object [{}]", propertyName, ClassUtils.getName(element));
		}
		return null;
	}
}
