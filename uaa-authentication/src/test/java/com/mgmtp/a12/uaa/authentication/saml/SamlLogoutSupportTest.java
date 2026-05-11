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
package com.mgmtp.a12.uaa.authentication.saml;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.saml2.provider.service.authentication.logout.Saml2LogoutRequest;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;

import com.mgmtp.a12.uaa.authentication.internal.TokenType;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenGenerator;
import com.mgmtp.a12.uaa.authentication.principal.UAAPrincipal;
import com.mgmtp.a12.uaa.authentication.utils.UserDataCreator;
import com.mgmtp.a12.uaa.authentication.utils.UserDataCreator.TestExtededData;

public class SamlLogoutSupportTest {

	private UaaSaml2LogoutRequestRepository saml2LogoutRequestRepository;
	private JwtTokenGenerator jwtTokenGenerator;

	public SamlLogoutSupportTest(UaaSaml2LogoutRequestRepository saml2LogoutRequestRepository, JwtTokenGenerator jwtTokenGenerator) {
		this.saml2LogoutRequestRepository = saml2LogoutRequestRepository;
		this.jwtTokenGenerator = jwtTokenGenerator;
	}

	Saml2LogoutRequest saveLogoutRequest(String relayState) {
		Saml2LogoutRequest request = createRequest(relayState);

		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		UAAPrincipal<TestExtededData> user = UserDataCreator.createUser("test", "N/A");
		httpRequest.addHeader("Authorization", TokenType.UAABEARER.name() + " " + jwtTokenGenerator.generateToken(user).getToken());
		MockHttpServletResponse httpResponse = new MockHttpServletResponse();

		saml2LogoutRequestRepository.saveLogoutRequest(request, httpRequest, httpResponse);
		return request;
	}

	Saml2LogoutRequest createRequest(String relaySaate) {
		RelyingPartyRegistration relyingPartyRegistration =
			RelyingPartyRegistration.withRegistrationId("uaa").entityId("id")
				.assertingPartyMetadata(t -> {
					t.entityId("assertinId");
					t.singleSignOnServiceLocation("SSO_Location");
				})
				.build();
		return Saml2LogoutRequest.withRelyingPartyRegistration(relyingPartyRegistration).id("test").location("testLocation").relayState(relaySaate).build();
	}

	LogoutRequestData findData(String relayState) {
		return saml2LogoutRequestRepository.loadAll().stream()
			.filter(data -> data.getLogoutRequest().getRelayState().equals(relayState))
			.findFirst().get();
	}

}
