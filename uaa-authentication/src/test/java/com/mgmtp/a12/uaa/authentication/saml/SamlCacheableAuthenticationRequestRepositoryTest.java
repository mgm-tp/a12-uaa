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
import org.springframework.security.saml2.provider.service.authentication.AbstractSaml2AuthenticationRequest;
import org.springframework.security.saml2.provider.service.authentication.Saml2PostAuthenticationRequest;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mgmtp.a12.uaa.authentication.internal.CacheStorageType;
import com.mgmtp.a12.uaa.authentication.saml.internal.AuthenticationRequestData;
import com.mgmtp.a12.uaa.authentication.saml.internal.CacheableSamlAuthenticationRequestRepository;
import com.mgmtp.a12.uaa.authentication.saml.internal.UAASaml2AuthenticationRequestRepository;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SamlCacheableAuthenticationRequestRepositoryTest {

	@Inject
	private UAASaml2AuthenticationRequestRepository samlAuthenticationRequestRepository;
	@Inject
	private CacheManager cacheManager;

	@AfterAll
	public void cleanUp() throws IllegalArgumentException, IllegalAccessException {
		//we have to clear because static field is shared between tests
		samlAuthenticationRequestRepository.loadAll().stream()
			.forEach(samlAuthenticationRequestRepository::delete);
	}

	@Test
	public void saveRequest() {
		String relayState = "relayState_007";
		AbstractSaml2AuthenticationRequest request = createRequest(relayState);
		samlAuthenticationRequestRepository.saveAuthenticationRequest(request, new MockHttpServletRequest(), new MockHttpServletResponse());

		Cache cache = cacheManager.getCache(CacheStorageType.SAML_AUTHENTICATION_REQUEST);
		Cache.ValueWrapper valueWrapper = cache.get(relayState);
		Assertions.assertEquals(request, valueWrapper.get());

	}

	@Test
	public void loadEmpty() {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		httpRequest.addParameter(Saml2ParameterNames.RELAY_STATE, "NA");

		AbstractSaml2AuthenticationRequest request = samlAuthenticationRequestRepository.loadAuthenticationRequest(httpRequest);

		Assertions.assertNull(request);
	}

	@Test
	public void deleteRequest() {
		String relayState = "relayState_001";
		AbstractSaml2AuthenticationRequest request = createRequest(relayState);
		samlAuthenticationRequestRepository.saveAuthenticationRequest(request, new MockHttpServletRequest(), new MockHttpServletResponse());

		AuthenticationRequestData data = findData(request);

		samlAuthenticationRequestRepository.delete(data);

		Cache cache = cacheManager.getCache(CacheStorageType.SAML_AUTHENTICATION_REQUEST);
		Cache.ValueWrapper valueWrapper = cache.get(relayState);
		Assertions.assertNull(valueWrapper);
	}

	@Test
	public void removeRequest() {
		String relayState = "relayState_002";
		AbstractSaml2AuthenticationRequest request = createRequest(relayState);
		samlAuthenticationRequestRepository.saveAuthenticationRequest(request, new MockHttpServletRequest(), new MockHttpServletResponse());

		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		httpRequest.addParameter(Saml2ParameterNames.RELAY_STATE, relayState);
		MockHttpServletResponse httpResponse = new MockHttpServletResponse();

		samlAuthenticationRequestRepository.removeAuthenticationRequest(httpRequest, httpResponse);

		Cache cache = cacheManager.getCache(CacheStorageType.SAML_AUTHENTICATION_REQUEST);
		Cache.ValueWrapper valueWrapper = cache.get(relayState);
		Assertions.assertNull(valueWrapper);
	}

	@Test
	public void removeRequestByRequest() {
		String relayState = "relayState_003";

		AbstractSaml2AuthenticationRequest request = createRequest(relayState);
		samlAuthenticationRequestRepository.saveAuthenticationRequest(request, new MockHttpServletRequest(), new MockHttpServletResponse());

		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		httpRequest.addParameter(Saml2ParameterNames.RELAY_STATE, relayState);
		MockHttpServletResponse httpResponse = new MockHttpServletResponse();

		samlAuthenticationRequestRepository.removeAuthenticationRequest(httpRequest, httpResponse);

		Cache cache = cacheManager.getCache(CacheStorageType.SAML_AUTHENTICATION_REQUEST);
		Cache.ValueWrapper valueWrapper = cache.get(relayState);
		Assertions.assertNull(valueWrapper);
	}

	@Test
	public void loadExpired_Empty() {
		String relayState = "relayState_004";
		AbstractSaml2AuthenticationRequest request = createRequest(relayState);
		samlAuthenticationRequestRepository.saveAuthenticationRequest(request, new MockHttpServletRequest(), new MockHttpServletResponse());

		Collection<AuthenticationRequestData> allData = samlAuthenticationRequestRepository.loadAll();
		Assertions.assertTrue(allData.size() > 0);
	}

	private AbstractSaml2AuthenticationRequest createRequest(String relaySaate) {
		RelyingPartyRegistration relyingPartyRegistration =
			RelyingPartyRegistration.withRegistrationId("uaa").entityId("id")
				.assertingPartyMetadata(t -> {
					t.entityId("assertingId");
					t.singleSignOnServiceLocation("SSO_Location");
				})
				.build();
		return Saml2PostAuthenticationRequest.withRelyingPartyRegistration(relyingPartyRegistration).relayState(relaySaate).samlRequest("ABC").build();
	}

	private AuthenticationRequestData findData(AbstractSaml2AuthenticationRequest request) {
		return samlAuthenticationRequestRepository.loadAll().stream()
			.filter(data -> data.getSamlAuthenticationRequest().equals(request))
			.findFirst().get();
	}

	@Configuration
	@EnableCaching
	static class TestConfig {
		@Bean
		public UAASaml2AuthenticationRequestRepository createCacheableSamlLogoutRequestRepository() {
			return new CacheableSamlAuthenticationRequestRepository(new UAASaml2AuthenticationRequestRepository() {
			}, cacheManager());
		}

		@Bean
		public CacheManager cacheManager() {
			return new ConcurrentMapCacheManager(CacheStorageType.SAML_AUTHENTICATION_REQUEST);
		}

	}
}
