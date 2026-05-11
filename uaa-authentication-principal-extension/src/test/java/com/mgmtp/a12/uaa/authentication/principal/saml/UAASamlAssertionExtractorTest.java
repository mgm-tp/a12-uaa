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
package com.mgmtp.a12.uaa.authentication.principal.saml;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider.ResponseToken;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;

import com.mgmtp.a12.uaa.authentication.principal.AbstractExtendedPrincipal;
import com.mgmtp.a12.uaa.authentication.principal.ExtendedPrincipal;
import com.mgmtp.a12.uaa.authentication.principal.PrincipalProcessor;
import com.mgmtp.a12.uaa.authentication.principal.saml.internal.UAASamlAssertionExtractor;
import com.mgmtp.a12.uaa.authentication.saml.SamlGrantedAuthorityConverter;
import com.mgmtp.a12.uaa.authentication.saml.SamlPrincipal;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestInstance(Lifecycle.PER_CLASS)
public class UAASamlAssertionExtractorTest {

	@Mock
	private ResponseToken responseToken;
	@Mock
	private Saml2AuthenticationToken saml2AuthenticationToken;
	@Mock
	private RelyingPartyRegistration relyingPartyRegistration;
	@Mock
	private Response response;
	@Mock
	private Assertion assertion;
	@Mock
	private AttributeStatement statement;
	@Mock
	private Attribute attribute;
	@Mock
	private XSString value;
	@Mock
	private SamlGrantedAuthorityConverter authorityConverter;
	@Mock
	private PrincipalProcessor principalProcessor;
	@InjectMocks
	private UAASamlAssertionExtractor assertionExtractor = new UAASamlAssertionExtractor("UserID");
	@InjectMocks
	private UAASamlAssertionExtractor failingAssertionExtractor = new UAASamlAssertionExtractor("UserID-DUMY");

	@BeforeEach
	public void setIUp() {
		Mockito.when(value.getValue()).thenReturn("mimasadmin");

		Mockito.when(attribute.getName()).thenReturn("UserID");
		Mockito.when(attribute.getAttributeValues()).thenReturn(Arrays.asList(value));

		Mockito.when(statement.getAttributes()).thenReturn(Arrays.asList(attribute));

		Mockito.when(assertion.getAttributeStatements()).thenReturn(Arrays.asList(statement));

		Mockito.when(response.getAssertions()).thenReturn(Arrays.asList(assertion));

		Mockito.when(responseToken.getResponse()).thenReturn(response);
		Mockito.when(relyingPartyRegistration.getRegistrationId()).thenReturn("registrationID");
		Mockito.when(saml2AuthenticationToken.getRelyingPartyRegistration()).thenReturn(relyingPartyRegistration);
		Mockito.when(responseToken.getToken()).thenReturn(saml2AuthenticationToken);

		Mockito.when(authorityConverter.convert(Mockito.any())).thenReturn(Arrays.asList(new SimpleGrantedAuthority("test")));
		Mockito.when(principalProcessor.createPrincipal(Mockito.anyString(), Mockito.anyCollection(), Mockito.any()))
			.thenReturn((AbstractExtendedPrincipal) new ExtendedPrincipal("mimasadmin", "***",
				Arrays.asList(new SimpleGrantedAuthority("test"))));
	}

	@Test
	public void testGetUserDetailsSupport() {
		SamlPrincipal userDetails = assertionExtractor.extractAssertion(responseToken);
		Assertions.assertEquals(userDetails.getPassword(), "***");
		Assertions.assertEquals(userDetails.getUsername(), "mimasadmin");
		Assertions.assertEquals(1, userDetails.getAuthorities().size());
		Assertions.assertEquals(userDetails.getRelyingPartyRegistrationId(), "registrationID");
	}

	@Test
	public void testUserIdDoesNotFoundSupport() {
		Assertions.assertThrows(UsernameNotFoundException.class, () -> failingAssertionExtractor.extractAssertion(responseToken));
	}

}
