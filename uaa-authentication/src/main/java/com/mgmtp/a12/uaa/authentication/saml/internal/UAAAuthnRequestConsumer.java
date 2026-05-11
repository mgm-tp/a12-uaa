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

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.springframework.security.saml2.provider.service.web.authentication.OpenSaml5AuthenticationRequestResolver.AuthnRequestContext;

import com.mgmtp.a12.uaa.authentication.saml.RequestContextDataGenerator;
import com.mgmtp.a12.uaa.authentication.saml.RequestExtension;
import com.mgmtp.a12.uaa.authentication.saml.RequestExtensionsDataGenerator;

public class UAAAuthnRequestConsumer implements Consumer<AuthnRequestContext> {

	private boolean forceAuth;
	private Optional<RequestExtensionsDataGenerator> extensionDataGenerator;
	private Optional<RequestContextDataGenerator> requestContextDataGenerator;
	private ExtensionDataConverter extensionsConverter = new ExtensionDataConverter();
	private AuthContextDataConverter authContextDataConverter = new AuthContextDataConverter();

	public UAAAuthnRequestConsumer(boolean forceAuth, Optional<RequestExtensionsDataGenerator> extensionDataGenerator,
		Optional<RequestContextDataGenerator> requestContextDataGenerator) {
		this.forceAuth = forceAuth;
		this.extensionDataGenerator = extensionDataGenerator;
		this.requestContextDataGenerator = requestContextDataGenerator;
	}

	@Override
	public void accept(AuthnRequestContext requestContext) {
		AuthnRequest authnRequest = requestContext.getAuthnRequest();
		authnRequest.setForceAuthn(forceAuth);
		extensionDataGenerator.ifPresent(generator -> {
			List<RequestExtension> extensionData = generator.generateExtensionData();
			authnRequest.setExtensions(extensionsConverter.buildExtensions(extensionData));
		});
		requestContextDataGenerator.ifPresent(generator -> {
			RequestedAuthnContext requestedAuthnContext = authContextDataConverter.buildRequestContext(generator);
			authnRequest.setRequestedAuthnContext(requestedAuthnContext);
		});
		UAAThreadLocalAuthnRequestDataStore.setAuthnRequestData(authnRequest);

	}

}
