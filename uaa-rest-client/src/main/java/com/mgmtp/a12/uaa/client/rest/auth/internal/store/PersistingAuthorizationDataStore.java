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
package com.mgmtp.a12.uaa.client.rest.auth.internal.store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.TokenRefresher;
import com.mgmtp.a12.uaa.client.rest.auth.TokenValidator;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.RefreshTokenScheduler;

public class PersistingAuthorizationDataStore implements AuthorizationDataStore {

	private static final Logger LOGGER = LoggerFactory.getLogger(PersistingAuthorizationDataStore.class);

	private static final Integer MAXIMUM_RETRIES = 2;

	private ObjectMapper objectMapper;
	private Path store;
	private TokenRefresher tokenRefresher;
	private TokenValidator tokenValidator;

	public PersistingAuthorizationDataStore(Path store, TokenRefresher tokenRefresher, TokenValidator tokenValidator) {
		this.store = store;
		this.tokenRefresher = tokenRefresher;
		this.tokenValidator = tokenValidator;
		this.objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.registerModule(new ParameterNamesModule());
	}

	@Override
	public AuthorizationData getAuthorizationData() {
		try {
			byte[] storageBytes = Files.readAllBytes(store);
			if (storageBytes.length > 0) {
				AuthorizationData authorizationData = objectMapper.readValue(storageBytes, AuthorizationData.class);
				String token = authorizationData.getAuthenticationToken();
				//Check if the token itself is valid on server.
				if (authorizationData.isValid() && tokenValidator.isTokenValid(token)) {
					ScheduledFuture<?> renewalFuture =
						RefreshTokenScheduler.scheduleTokenRenewal(tokenRefresher, null, authorizationData.getTokenRenewInSeconds(), MAXIMUM_RETRIES);
					if (renewalFuture != null) {
						LOGGER.info("Loaded stored token from file [{}]", store.toAbsolutePath());
						return authorizationData;
					}
				}
			}
		} catch (NoSuchFileException e) {
			//stay silent
		} catch (Exception e) {
			LOGGER.error("Unable to deserialize authorization store [{}]", store.toAbsolutePath(), e);
		}
		return null;
	}

	@Override
	public void setAuthorizationData(AuthorizationData authorizationData) {
		try {
			objectMapper.writeValue(store.toFile(), authorizationData);
			LOGGER.info("Persisting authorization data to file [{}]", store.toAbsolutePath());
		} catch (Exception e) {
			LOGGER.error("Unable to persist authorization store [{}]", store.toAbsolutePath(), e);
		}
	}

	@Override
	public void cleanUpAuthorizationStore() {
		try {
			Files.write(store, new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			LOGGER.error("Unable to clean authorization store [{}]", store.toAbsolutePath(), e);
		}
	}
}
