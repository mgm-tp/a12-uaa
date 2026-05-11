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

import java.time.Instant;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import com.mgmtp.a12.uaa.authentication.saml.UaaSaml2LogoutRequestRepository;

public class SamlLogoutRequestCleaner {

	private static final Logger LOGGER = LoggerFactory.getLogger(SamlAuthorizationCodeCleaner.class);

	@Inject
	private UaaSaml2LogoutRequestRepository uaaSaml2LogoutRequestRepository;

	/**
	 * Run every 30 seconds
	 */
	@Scheduled(cron = "*/30 * * * * *")
	public void cleanExpiredAuthorizationCode() {
		LOGGER.info("Cleaning job has been started");
		long deletedItems = uaaSaml2LogoutRequestRepository.loadAll()
			.stream()
			.filter(data -> Instant.now().isAfter(data.getExpirationDate()))
			.map(data -> {
				uaaSaml2LogoutRequestRepository.delete(data);
				return Boolean.TRUE;

			})
			.count();
		LOGGER.info("Clean-up job finished. [{}] items has been deleted.", deletedItems);
	}
}
