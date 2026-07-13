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
package com.mgmtp.a12.uaa.authentication.principal.apikey.internal;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mgmtp.a12.uaa.authentication.apikey.APIKeyConverter;
import com.mgmtp.a12.uaa.authentication.principal.AbstractExtendedPrincipal;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalProcessor;
import com.mgmtp.a12.uaa.authentication.principal.certificate.CertificateUtils;

public class APIKeyPrincipalConverter implements APIKeyConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(APIKeyPrincipalConverter.class);

	private String userNameField;
	private String userRoleField;

	@Inject
	private PrincipalProcessor principalProcessor;

	public APIKeyPrincipalConverter(String userNameField, String userRoleField) {
		this.userNameField = userNameField;
		this.userRoleField = userRoleField;
	}

	@Override
	public UserDetails convert(X509Certificate certificate) {
		String userNameValue = getAttributeValueFromCertificatePrincipal(certificate, userNameField);
		String roles = CertificateUtils.getAttributeValueFromCertificateExtension(certificate, userRoleField);

		Set<GrantedAuthority> grantedAuthorities = Arrays.asList(roles.split(",")).stream()
			.map(roleName -> new SimpleGrantedAuthority(roleName))
			.collect(Collectors.toSet());
		AbstractExtendedPrincipal<?> user = principalProcessor.createPrincipal(userNameValue, grantedAuthorities, certificate);
		return user;

	}

	private String getAttributeValueFromCertificatePrincipal(X509Certificate certificate, String attributeName) {
		try {
			String subjectName = certificate.getSubjectX500Principal().getName();
			LdapName ldapName = new LdapName(subjectName);
			return ldapName.getRdns()
				.stream()
				.filter(rdn -> rdn.getType().equals(attributeName))
				.findFirst().get().getValue().toString();
		} catch (InvalidNameException e) {
			LOGGER.error("Has error when get attribute value from certificate subject name", e);
		}
		return null;
	}
}
