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
package com.mgmtp.a12.uaa.client.rest.config.properties;

import com.mgmtp.a12.uaa.client.rest.config.AuthenticationType;
import com.mgmtp.a12.uaa.client.rest.config.validation.NotNullForAuthenticationType;

public class CertificateProperties {
	@NotNullForAuthenticationType(authenticationTypes = {
		AuthenticationType.CERTIFICATE }, message = "Certificate authentication requires 'keyStore' to be configured")
	private String keyStore;
	@NotNullForAuthenticationType(authenticationTypes = {
		AuthenticationType.CERTIFICATE }, message = "Certificate authentication requires 'keyStorePassword' to be configured")
	private String keyStorePassword;
	@NotNullForAuthenticationType(authenticationTypes = {
		AuthenticationType.CERTIFICATE }, message = "Certificate authentication requires 'trustStore' to be configured")
	private String trustStore;
	@NotNullForAuthenticationType(authenticationTypes = {
		AuthenticationType.CERTIFICATE }, message = "Certificate authentication requires 'trustStorePassword' to be configured")
	private String trustStorePassword;

	public String getKeyStore() {
		return keyStore;
	}

	public void setKeyStore(String keyStore) {
		this.keyStore = keyStore;
	}

	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	public String getTrustStore() {
		return trustStore;
	}

	public void setTrustStore(String trustStore) {
		this.trustStore = trustStore;
	}

	public String getTrustStorePassword() {
		return trustStorePassword;
	}

	public void setTrustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}

	@Override public String toString() {
		return "CertificateProperties{" +
			"keyStore='" + keyStore + '\'' +
			", keyStorePassword='" + keyStorePassword + '\'' +
			", trustStore='" + trustStore + '\'' +
			", trustStorePassword='" + trustStorePassword + '\'' +
			'}';
	}
}
