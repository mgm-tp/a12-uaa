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

import java.util.Collection;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.saml2.core.Saml2ParameterNames;
import org.springframework.security.saml2.provider.service.authentication.logout.Saml2LogoutRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mgmtp.a12.uaa.authentication.internal.AuthenticationTokenLocator;
import com.mgmtp.a12.uaa.authentication.internal.CacheStorageType;
import com.mgmtp.a12.uaa.authentication.internal.HeaderAuthenticationTokenLocator;
import com.mgmtp.a12.uaa.authentication.internal.TokenType;
import com.mgmtp.a12.uaa.authentication.jwt.integration.BaseTestConfig;
import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenGenerator;
import com.mgmtp.a12.uaa.authentication.saml.internal.CacheableSamlLogoutRequestRepository;
import com.mgmtp.a12.uaa.authentication.saml.internal.SimpleSamlLogoutRequestRepository;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SamlCacheableLogoutRequestStorageImplTest {

	@Inject
	private UaaSaml2LogoutRequestRepository cacheableSamlLogoutRequestRepository;
	@Inject
	private CacheManager cacheManager;
	@Inject
	private JwtTokenGenerator jwtTokenGenerator;

	private SamlLogoutSupportTest samlLogoutSupport;

	@BeforeEach
	void setUp() {
		samlLogoutSupport = new SamlLogoutSupportTest(cacheableSamlLogoutRequestRepository, jwtTokenGenerator);
	}

	@AfterAll
	public void cleanUp() throws IllegalArgumentException, IllegalAccessException {
		//we have to clear because static field is shared between tests
		cacheableSamlLogoutRequestRepository.loadAll().stream()
			.forEach(cacheableSamlLogoutRequestRepository::delete);

	}

	@Test
	public void saveRequest() {
		String relayState = "relayState_9_007";
		Saml2LogoutRequest request = samlLogoutSupport.saveLogoutRequest(relayState);

		Cache cache = cacheManager.getCache(CacheStorageType.SAML_LOGOUT_REQUEST);
		Cache.ValueWrapper valueWrapper = cache.get(relayState);
		Assertions.assertEquals(request, valueWrapper.get());
	}

	@Test
	public void loadEmpty() {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		httpRequest.addParameter(Saml2ParameterNames.RELAY_STATE, "NA");

		Saml2LogoutRequest request = cacheableSamlLogoutRequestRepository.loadLogoutRequest(httpRequest);

		Assertions.assertNull(request);
	}

	@Test
	public void deleteRequest() {
		String relayState = "relayState_9_001";
		samlLogoutSupport.saveLogoutRequest(relayState);

		cacheableSamlLogoutRequestRepository.delete(samlLogoutSupport.findData(relayState));
		Cache cache = cacheManager.getCache(CacheStorageType.SAML_LOGOUT_REQUEST);
		Cache.ValueWrapper valueWrapper = cache.get(relayState);
		Assertions.assertNull(valueWrapper);
	}

	@Test
	public void removeRequest() {
		String relayState = "relayState_9_002";
		samlLogoutSupport.saveLogoutRequest(relayState);

		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		httpRequest.addParameter(Saml2ParameterNames.RELAY_STATE, relayState);
		MockHttpServletResponse httpResponse = new MockHttpServletResponse();

		cacheableSamlLogoutRequestRepository.removeLogoutRequest(httpRequest, httpResponse);
		Cache cache = cacheManager.getCache(CacheStorageType.SAML_LOGOUT_REQUEST);
		Cache.ValueWrapper valueWrapper = cache.get(relayState);
		Assertions.assertNotNull(valueWrapper);
	}

	@Test
	public void removeRequestAndgetToken() {
		String relayState = "relayState_9_003";
		samlLogoutSupport.saveLogoutRequest(relayState);

		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		httpRequest.addParameter(Saml2ParameterNames.RELAY_STATE, relayState);
		MockHttpServletResponse httpResponse = new MockHttpServletResponse();

		cacheableSamlLogoutRequestRepository.removeLogoutRequestAndGetToken(httpRequest, httpResponse);
		Cache cache = cacheManager.getCache(CacheStorageType.SAML_LOGOUT_REQUEST);
		Cache.ValueWrapper valueWrapper = cache.get(relayState);
		Assertions.assertNull(valueWrapper);
	}

	@Test
	public void loadExpired_Empty() throws Exception {
		String relayState = "relayState_9_004";
		samlLogoutSupport.saveLogoutRequest(relayState);
		Collection<LogoutRequestData> allData = cacheableSamlLogoutRequestRepository.loadAll();
		Assertions.assertTrue(allData.size() > 0);
	}

	//	private Saml2LogoutRequest saveLogoutRequest(String relayState) {
	//		Saml2LogoutRequest request = createRequest(relayState);
	//
	//		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
	//		UAAUser<TestExtededData> user = UserDataCreator.createUser("test", "N/A");
	//		httpRequest.addHeader("Authorization", TokenType.UAABEARER.name() + " " + jwtTokenSupport.generateToken(user));
	//		MockHttpServletResponse httpResponse = new MockHttpServletResponse();
	//
	//		cacheableSamlLogoutRequestRepository.saveLogoutRequest(request, httpRequest, httpResponse);
	//		return request;
	//	}
	//
	//	private Saml2LogoutRequest createRequest(String relaySaate) {
	//		RelyingPartyRegistration relyingPartyRegistration =
	//			RelyingPartyRegistration.withRegistrationId("uaa").entityId("id")
	//				.assertingPartyDetails(t -> {
	//					t.entityId("assertinId");
	//					t.singleSignOnServiceLocation("SSO_Location");
	//				})
	//				.build();
	//		return Saml2LogoutRequest.withRelyingPartyRegistration(relyingPartyRegistration).id("test").location("testLocation").relayState(relaySaate).build();
	//	}

	//	private LogoutRequestData findData(String relayState) {
	//		return cacheableSamlLogoutRequestRepository.loadAll().stream()
	//			.filter(data -> data.getLogoutRequest().getRelayState().equals(relayState))
	//			.findFirst().get();
	//	}

	@Configuration
	@EnableCaching
	static class TestConfig extends BaseTestConfig {
		@Bean
		public CacheableSamlLogoutRequestRepository createCacheableSamlLogoutRequestRepository() {
			return new CacheableSamlLogoutRequestRepository(
				new SimpleSamlLogoutRequestRepository(createauAuthenticationTokenLocator(), createJwtTokenVerifierSupport()), cacheManager());
		}

		@Bean
		public CacheManager cacheManager() {
			return new ConcurrentMapCacheManager(CacheStorageType.SAML_LOGOUT_REQUEST);
		}

		@Bean
		public AuthenticationTokenLocator createauAuthenticationTokenLocator() {
			return new HeaderAuthenticationTokenLocator("Authorization", TokenType.UAABEARER);
		}

	}
}
