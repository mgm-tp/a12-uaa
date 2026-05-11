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
package com.mgmtp.a12.uaa.client.rest.cache.internal;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;

import com.mgmtp.a12.uaa.client.rest.cache.CacheNameMapper;

public class PathCacheNameMapper implements CacheNameMapper {

	private static final Logger LOGGER = LoggerFactory.getLogger(PathCacheNameMapper.class);

	private PathMatcher requestMatcher;
	private String cachePathPattern;
	private String regionPattern;
	private String staticName;

	public PathCacheNameMapper(String cachePath, String regionPattern) {
		this(cachePath, regionPattern, null);
	}

	public PathCacheNameMapper(String cachePathPattern, String regionPattern, String staticName) {
		Assert.notNull(cachePathPattern, "Cache path pattern must be specified");
		this.cachePathPattern = StringUtils.trimToNull(cachePathPattern);
		this.regionPattern = StringUtils.trimToNull(regionPattern);
		this.staticName = StringUtils.trimToNull(staticName);
		Assert.isTrue(((regionPattern == null) || (staticName == null)), "Please specify regionPattern or static name");
		Assert.isTrue(((regionPattern != null) || (staticName != null)), "Please specify regionPattern or static name");
		this.requestMatcher = createMatcher();
	}

	@Override
	public boolean match(HttpRequest request) {
		String path = request.getURI().getPath();
		boolean matched = requestMatcher.match(cachePathPattern, path);
		LOGGER.debug("Matching request path [{}] with cache path [{}]: [{}]", path, cachePathPattern, matched);
		return matched;
	}

	@Override
	public String computeCacheName(HttpRequest request) {
		String path = request.getURI().getPath();
		String name = Optional.ofNullable(regionPattern)
			.map(pattern -> requestMatcher.extractPathWithinPattern(pattern, path))
			.orElse(staticName);
		LOGGER.debug("Computed cache name [{}] for request path [{}]", name, path);
		return name;
	}

	private AntPathMatcher createMatcher() {
		AntPathMatcher matcher = new AntPathMatcher();
		matcher.setTrimTokens(false);
		matcher.setCaseSensitive(false);
		return matcher;
	}

}
