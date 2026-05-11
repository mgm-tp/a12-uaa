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
package com.mgmtp.a12.uaa.client.rest.auth.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import com.mgmtp.a12.uaa.client.rest.auth.AuthenticationHandler;
import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationData;
import com.mgmtp.a12.uaa.client.rest.auth.AuthorizationDataStore;
import com.mgmtp.a12.uaa.client.rest.auth.TokenAcquirer;
import com.mgmtp.a12.uaa.client.rest.auth.token.internal.RefreshTokenScheduler;
import com.mgmtp.a12.uaa.client.rest.config.AuthenticationType;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

/**
 * This implementation is used when the client is responsible for authentication. It's valid for single thread applications.
 *
 * NOTE: This is not sufficient for multi thread application since it will login again and again for each thread.
 */
public class SingleThreadAuthenticationHandler implements AuthenticationHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(SingleThreadAuthenticationHandler.class);

	private final UAARestClientProperties clientConfiguration;
	private final AuthorizationDataStore authorizationDataStore;
	private TokenAcquirer tokenAcquirer;
	private boolean authenticationDataStored = false;

	public SingleThreadAuthenticationHandler(UAARestClientProperties configuration, AuthorizationDataStore authorizationDataStore,
		TokenAcquirer tokenAcquirer) {
		this.clientConfiguration = configuration;
		this.tokenAcquirer = tokenAcquirer;
		this.authorizationDataStore = authorizationDataStore;
		if (isNotCertificate() && (clientConfiguration.getAuthenticationConfiguration() == null)) {
			throw new RuntimeException(
				"Client is responsible for Auth and there is no configuration for it. " +
					"Please check the 'mgmtp.a12.uaa.authentication.client.rest.authentication-configuration.*' configuration.");
		}
	}

	@Override
	public AuthorizationData authenticate() {
		AuthorizationData authorizationData = authorizationDataStore.getAuthorizationData();
		AuthorizationData initialAuthorizationData = authorizationData;
		if (!isAuthenticationValid(initialAuthorizationData)) {
			LOGGER.info("Acquiring server token");
			authorizationData = tokenAcquirer.acquireToken();
		}
		if ((initialAuthorizationData != authorizationData) || !authenticationDataStored) {
			//Store authorizationData might change during validation check or 1-st login (generated for 1-st time)
			authorizationDataStore.setAuthorizationData(authorizationData);
			//We need to ensure at least 1 store in order to properly initialize the infrastructure
			authenticationDataStored = true;
		}
		return authorizationData;
	}

	@Override
	public void logout(HttpHeaders headers) {
		AuthorizationData authorizationData = authorizationDataStore.getAuthorizationData();
		authorizationDataStore.cleanUpAuthorizationStore();
		if (isNotCertificate()) {
			RefreshTokenScheduler.stopTokenRenewal();
		}
		tokenAcquirer.releaseToken(authorizationData, headers);
	}

	private boolean isAuthenticationValid(AuthorizationData credentialData) {
		return credentialData != null && credentialData.isValid();
	}

	private boolean isNotCertificate() {
		return AuthenticationType.CERTIFICATE != clientConfiguration.getAuthenticationType();
	}

}
