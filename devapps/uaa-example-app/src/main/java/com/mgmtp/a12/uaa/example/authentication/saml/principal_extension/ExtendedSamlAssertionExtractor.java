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
package com.mgmtp.a12.uaa.example.authentication.saml.principal_extension;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.inject.Inject;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider.ResponseToken;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.ConditionalOnAuthentication;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalFactory;
import com.mgmtp.a12.uaa.authentication.saml.SamlAssertionExtractor;
import com.mgmtp.a12.uaa.authentication.saml.SamlAssertionUtils;
import com.mgmtp.a12.uaa.authentication.saml.SamlGrantedAuthorityConverter;
import com.mgmtp.a12.uaa.authentication.saml.SamlPrincipal;
import com.mgmtp.a12.uaa.example.principal.PrincipalMetadata;
import com.mgmtp.a12.uaa.example.principal.extension.ExtendedPrincipal;

@Component
@ConditionalOnAuthentication(AuthenticationType.SAML)
@Profile("principal & !uaa_saml_custom_metadata")
public class ExtendedSamlAssertionExtractor implements SamlAssertionExtractor {

	@Inject
	Optional<SamlGrantedAuthorityConverter> samlGrantedAuthorityConverter;
	@Inject
	private PrincipalFactory userFactory;

	@Override
	public SamlPrincipal extractAssertion(ResponseToken samlResponse) {
		Response response = samlResponse.getResponse();
		Assertion assertion = CollectionUtils.firstElement(response.getAssertions());
		Map<String, List<Object>> attributes = SamlAssertionUtils.getAssertionAttributes(assertion);
		String userName = SamlAssertionUtils.getAttributeValue(attributes, "UserID", String.class).get();
		//depending on config nationality is overwritten  by persistent user thus makes no sense here - made it optional
		String nationality = SamlAssertionUtils.getAttributeValue(attributes, "nationality", String.class).orElse("NONE");
		Collection<GrantedAuthority> grantedAuthorities = samlGrantedAuthorityConverter.get().convert(assertion);
		PrincipalMetadata userMetadata = new PrincipalMetadata(nationality);

		ExtendedPrincipal principal = userFactory.createPrincipal(userName, "***", grantedAuthorities, userMetadata);
		principal.setNationality(nationality);
		principal.setRelayingPartyRegistration(samlResponse.getToken().getRelyingPartyRegistration().getRegistrationId());
		return principal;
	}

}
