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

import jakarta.validation.Valid;

import com.mgmtp.a12.uaa.client.rest.config.AuthenticationType;
import com.mgmtp.a12.uaa.client.rest.config.common.UrlProperty;
import com.mgmtp.a12.uaa.client.rest.config.validation.NotNullForAuthenticationType;

public class UAARestClientAuthenticationProperties {

	@NotNullForAuthenticationType(authenticationTypes = { AuthenticationType.LOCAL,
		AuthenticationType.ACTIVE_DIRECTORY_LDAP }, message = "Missing configuration for relative login url")
	private UrlProperty loginRelative = new UrlProperty("user/local/login");
	@NotNullForAuthenticationType(authenticationTypes = { AuthenticationType.SAML, AuthenticationType.LOCAL, AuthenticationType.ACTIVE_DIRECTORY_LDAP,
		AuthenticationType.OAUTH2 }, message = "Missing configuration for username")
	private String username = "admin";
	@NotNullForAuthenticationType(authenticationTypes = { AuthenticationType.SAML, AuthenticationType.LOCAL, AuthenticationType.ACTIVE_DIRECTORY_LDAP,
		AuthenticationType.OAUTH2 }, message = "Missing configuration for password")
	private String password = "admin";
	@NotNullForAuthenticationType(authenticationTypes = { AuthenticationType.SAML }, message = "Missing configuration for Saml")
	private SamlProperties saml;
	@NotNullForAuthenticationType(authenticationTypes = { AuthenticationType.OAUTH2 }, message = "Missing configuration for Oauth2")
	private OidcProperties oidc = new OidcProperties();
	@Valid
	@NotNullForAuthenticationType(authenticationTypes = { AuthenticationType.CERTIFICATE }, message = "Missing configuration for properties of Certificate")
	private CertificateProperties certificate = new CertificateProperties();

	public UrlProperty getLoginRelative() {
		return loginRelative;
	}

	public void setLoginRelative(UrlProperty loginRelative) {
		this.loginRelative = loginRelative;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public SamlProperties getSaml() {
		return saml;
	}

	public void setSaml(SamlProperties saml) {
		this.saml = saml;
	}

	public OidcProperties getOidc() {
		return oidc;
	}

	public void setOidc(OidcProperties oidc) {
		this.oidc = oidc;
	}

	public CertificateProperties getCertificate() {
		return certificate;
	}

	public void setCertificate(CertificateProperties certificate) {
		this.certificate = certificate;
	}

	@Override
	public String toString() {
		return "UAARestClientAuthenticationProperties{" +
			"loginRelative=" + loginRelative +
			", username='*****'" +
			", password='*****'" +
			", saml=" + saml +
			", oidc=" + oidc +
			", certificate=" + certificate +
			'}';
	}
}
