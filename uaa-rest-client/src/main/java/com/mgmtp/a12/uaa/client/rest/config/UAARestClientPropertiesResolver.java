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
package com.mgmtp.a12.uaa.client.rest.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.a12.uaa.client.rest.auth.token.internal.oauth2.ClientType;
import com.mgmtp.a12.uaa.client.rest.config.common.UrlProperty;
import com.mgmtp.a12.uaa.client.rest.config.properties.OidcProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientAuthenticationProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

/**
 * Resolve proper client properties. Either load properties from self configuration URL or keep initial properties.
 */
public class UAARestClientPropertiesResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(UAARestClientPropertiesResolver.class);

	public static UAARestClientProperties resolve(UAARestClientProperties uaaRestClientProperties) {
		LOGGER.info("Resolve properties for: " + uaaRestClientProperties.getAuthenticationType());
		UAARestClientProperties finalRestClientProperties = uaaRestClientProperties;

		UrlProperty selfConfiguration = uaaRestClientProperties.getSelfconfiguration();
		if (selfConfiguration != null && StringUtils.isNotBlank(selfConfiguration.getUrl())) {
			String username = null;
			String password = null;
			String oauth2ClientSecret = null;
			ClientType oauth2ClientType = null;

			UAARestClientAuthenticationProperties authenticationConfiguration = uaaRestClientProperties.getAuthenticationConfiguration();
			if (authenticationConfiguration != null) {
				username = authenticationConfiguration.getUsername();
				password = authenticationConfiguration.getPassword();

				OidcProperties oidcProperties = authenticationConfiguration.getOidc();
				if (oidcProperties != null) {
					oauth2ClientType = oidcProperties.getClientType();
					oauth2ClientSecret = oidcProperties.getConfidentialClient() != null ? oidcProperties.getConfidentialClient().getClientSecret() : null;
				}
			}

			ClientSelfconfigurationReader reader = new ClientSelfconfigurationReader();
			finalRestClientProperties = reader.readSelfconfiguration(
				selfConfiguration.getUrl(),
				null,
				username,
				password,
				uaaRestClientProperties.getAuthenticationType().name(),
				oauth2ClientSecret,
				oauth2ClientType,
				uaaRestClientProperties.getApiKeyResource(),
				uaaRestClientProperties.getAuthorizationHeaderName());
		}

		return finalRestClientProperties;
	}

}
