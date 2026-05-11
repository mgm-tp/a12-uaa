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

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.saml2.core.Saml2ParameterNames;
import org.springframework.security.saml2.provider.service.authentication.logout.Saml2LogoutRequest;

import com.mgmtp.a12.uaa.authentication.internal.AuthenticationTokenLocator;
import com.mgmtp.a12.uaa.authentication.jwt.JwtTokenData;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenGenerator;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenVerifier;
import com.mgmtp.a12.uaa.authentication.saml.internal.SimpleSamlLogoutRequestRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SamlSimpleLogoutRequestStorageImplTest {

	@InjectMocks
	private SimpleSamlLogoutRequestRepository simpleSamlLogoutRequestRepository;
	@Mock
	private AuthenticationTokenLocator tokenLocator;
	@Mock
	private JwtTokenGenerator jwtTokenGenerator;

	@Mock
	private JwtTokenVerifier jwtTokenVerifier;

	private SamlLogoutSupportTest samlLogoutSupport;

	@BeforeEach
	public void init() {
		Mockito.when(tokenLocator.locateToken(Mockito.any())).thenReturn(Optional.of("XYZ"));
		JwtTokenData jwtTokenData = new JwtTokenData.Builder("user")
			.withIssuedTime(Instant.now())
			.withExpirationSeconds(60)
			.withTokenRenewThresholdInSeconds(15)
			.build();
		Mockito.when(jwtTokenGenerator.generateToken(Mockito.any())).thenReturn(jwtTokenData);
		Mockito.when(jwtTokenVerifier.unpackToken(Mockito.anyString())).thenReturn(jwtTokenData);
		samlLogoutSupport = new SamlLogoutSupportTest(simpleSamlLogoutRequestRepository, jwtTokenGenerator);
	}

	@AfterAll
	public void cleanUp() throws IllegalArgumentException, IllegalAccessException {
		//we have to clear because static field is shared between tests
		simpleSamlLogoutRequestRepository.loadAll().stream()
			.forEach(simpleSamlLogoutRequestRepository::delete);
	}

	@Test
	public void saveRequest() {
		String relayState = "relayState_007";
		Saml2LogoutRequest request = samlLogoutSupport.saveLogoutRequest(relayState);

		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		httpRequest.addParameter(Saml2ParameterNames.RELAY_STATE, relayState);
		Saml2LogoutRequest loadedRequest = simpleSamlLogoutRequestRepository.loadLogoutRequest(httpRequest);

		Assertions.assertEquals(request, loadedRequest);
	}

	@Test
	public void loadEmpty() {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		httpRequest.addParameter(Saml2ParameterNames.RELAY_STATE, "NA");

		Saml2LogoutRequest request = simpleSamlLogoutRequestRepository.loadLogoutRequest(httpRequest);

		Assertions.assertNull(request);
	}

	@Test
	public void deleteRequest() {
		String relayState = "relayState_001";
		samlLogoutSupport.saveLogoutRequest(relayState);

		simpleSamlLogoutRequestRepository.delete(samlLogoutSupport.findData(relayState));

		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		httpRequest.addParameter(Saml2ParameterNames.RELAY_STATE, relayState);
		Saml2LogoutRequest loadedRequest = simpleSamlLogoutRequestRepository.loadLogoutRequest(httpRequest);

		Assertions.assertNull(loadedRequest);
	}

	@Test
	public void removeRequest() {
		String relayState = "relayState_002";
		Saml2LogoutRequest request = samlLogoutSupport.saveLogoutRequest(relayState);

		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		httpRequest.addParameter(Saml2ParameterNames.RELAY_STATE, relayState);
		MockHttpServletResponse httpResponse = new MockHttpServletResponse();

		Saml2LogoutRequest loadedRequest = simpleSamlLogoutRequestRepository.loadLogoutRequest(httpRequest);

		simpleSamlLogoutRequestRepository.removeLogoutRequest(httpRequest, httpResponse);

		Assertions.assertEquals(request, loadedRequest);
	}

	@Test
	public void removeRequestAndgetToken() {
		String relayState = "relayState_003";
		samlLogoutSupport.saveLogoutRequest(relayState);

		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		httpRequest.addParameter(Saml2ParameterNames.RELAY_STATE, relayState);
		MockHttpServletResponse httpResponse = new MockHttpServletResponse();

		simpleSamlLogoutRequestRepository.removeLogoutRequestAndGetToken(httpRequest, httpResponse);

		Saml2LogoutRequest loadedRequest = simpleSamlLogoutRequestRepository.loadLogoutRequest(httpRequest);

		Assertions.assertNull(loadedRequest);
	}

	@Test
	public void loadExpired_Empty() {
		String relayState = "relayState_004";
		samlLogoutSupport.saveLogoutRequest(relayState);

		Collection<LogoutRequestData> allData = simpleSamlLogoutRequestRepository.loadAll();
		Assertions.assertTrue(allData.size() > 0);
	}
}
