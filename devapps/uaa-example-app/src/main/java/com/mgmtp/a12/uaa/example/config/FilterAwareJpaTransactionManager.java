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
package com.mgmtp.a12.uaa.example.config;

import java.util.Set;

import jakarta.persistence.EntityManager;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.mgmtp.a12.uaa.authorization.AuthorizationService;

/**
 * An approach to work with repositoryPolicies is to take advantage of {@link Filter} to define custom filters built up from template in repositoryPolicies
 */
public class FilterAwareJpaTransactionManager extends JpaTransactionManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(FilterAwareJpaTransactionManager.class);

	private AuthorizationService authorizationService;

	public FilterAwareJpaTransactionManager(AuthorizationService authorizationService) {
		this.authorizationService = authorizationService;
	}

	@Override
	protected EntityManager createEntityManagerForTransaction() {
		EntityManager entityManager = super.createEntityManagerForTransaction();
		Session session = entityManager.unwrap(Session.class);
		enableFilterByGroup(session);

		return entityManager;
	}

	@Override
	protected Object doGetTransaction() {
		Object txResult = super.doGetTransaction();
		EntityManagerHolder emHolder = (EntityManagerHolder) TransactionSynchronizationManager.getResource(obtainEntityManagerFactory());
		if (emHolder != null) {
			EntityManager entityManager = emHolder.getEntityManager();
			Session session = entityManager.unwrap(Session.class);
			enableFilterByGroup(session);
		}
		return txResult;
	}

	private void enableFilterByGroup(Session session) {
		try {
			Set<String> repositoryPermissions = authorizationService.generateRepositoryPermissions();
			if (CollectionUtils.isNotEmpty(repositoryPermissions)) {
				Filter companyFilter = session.enableFilter("filterByCountry");
				repositoryPermissions
					.forEach(filter -> {
						String[] split = StringUtils.split(filter, "=");
						companyFilter.setParameter(StringUtils.trim(split[0]), StringUtils.trim(split[1]));
					});
			}
		} catch (Throwable t) {
			LOGGER.error("Unable to enable filter", t);
		}

	}
}
