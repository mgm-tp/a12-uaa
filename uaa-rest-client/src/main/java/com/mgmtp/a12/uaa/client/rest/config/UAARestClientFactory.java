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
package com.mgmtp.a12.uaa.client.rest.config;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;

import com.mgmtp.a12.connector.rest.ResponseErrorHandler;
import com.mgmtp.a12.connector.rest.RestDeleteConnector;
import com.mgmtp.a12.connector.rest.RestGetConnector;
import com.mgmtp.a12.connector.rest.RestHeadConnector;
import com.mgmtp.a12.connector.rest.RestOptionsConnector;
import com.mgmtp.a12.connector.rest.RestPostConnector;
import com.mgmtp.a12.connector.rest.RestPutConnector;
import com.mgmtp.a12.connector.rest.RestServerConnectorFactory;
import com.mgmtp.a12.connector.rest.RestServerConnectorFactoryBuilder;
import com.mgmtp.a12.uaa.client.rest.AuthenticationRestClient;
import com.mgmtp.a12.uaa.client.rest.AuthorizationRestClient;
import com.mgmtp.a12.uaa.client.rest.UAARestClientAuthorizationConfig;
import com.mgmtp.a12.uaa.client.rest.UAARestClientInterceptorFactory;
import com.mgmtp.a12.uaa.client.rest.cache.CacheNameMapper;
import com.mgmtp.a12.uaa.client.rest.config.properties.CertificateProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;
import com.mgmtp.a12.uaa.client.rest.internal.UAAResponseErrorHandler;


/**
 * Allows manual UAA REST client configuration and initialization. It also initializes underlying REST server connector factory.
 *
 */
public class UAARestClientFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(UAARestClientFactory.class);

	RestServerConnectorFactory restServerConnectorFactory;

	private AuthenticationRestClient authenticationRestClient;
	private AuthorizationRestClient authorizationRestClient;
	private RestGetConnector getConnector;
	private RestPostConnector postConnector;
	private RestPutConnector putConnector;
	private RestDeleteConnector deleteConnector;
	private RestHeadConnector headConnector;
	private RestOptionsConnector optionsConnector;
	private UAARestClientAuthorizationConfig authorizationConfig;

	UAARestClientFactory(UAARestClientProperties uaaRestClientProperties, CloseableHttpClient httpClient, ClientHttpRequestInterceptor[] interceptors,
		ResponseErrorHandler[] errorHandlers, List<HttpMessageConverter<?>> messageConverters, CacheManager cacheManager, CacheNameMapper[] cacheNameMappers
	) throws GeneralSecurityException, IOException {

		this.authorizationConfig =
			UAARestClientInterceptorFactory.createAuthorizationInterceptorWithCaching(
				uaaRestClientProperties,
				cacheManager,
				cacheNameMappers
			);

		ClientHttpRequestInterceptor[] clientInterceptors = buildInterceptors(interceptors);
		ResponseErrorHandler[] clientErrorHandlers = buildErrorHandlers(errorHandlers);

		init(uaaRestClientProperties, httpClient, clientInterceptors, messageConverters, clientErrorHandlers);
	}

	UAARestClientFactory(UAARestClientProperties uaaRestClientProperties, CloseableHttpClient httpClient, ClientHttpRequestInterceptor[] interceptors,
		List<HttpMessageConverter<?>> messageConverters, ResponseErrorHandler[] errorHandlers)
		throws GeneralSecurityException, IOException {
		init(uaaRestClientProperties, httpClient, interceptors, messageConverters, errorHandlers);
	}

	private UAARestClientFactory(UAARestClientProperties uaaRestClientProperties, ClientHttpRequestInterceptor[] interceptors,
		ResponseErrorHandler[] errorHandlers, List<HttpMessageConverter<?>> messageConverters, CacheManager cacheManager, CacheNameMapper[] cacheNameMappers
	) throws GeneralSecurityException, IOException {
		this(uaaRestClientProperties, null, interceptors, errorHandlers, messageConverters, cacheManager, cacheNameMappers);
	}

	private void init(UAARestClientProperties uaaRestClientProperties, CloseableHttpClient httpClient, ClientHttpRequestInterceptor[] interceptors,
		List<HttpMessageConverter<?>> messageConverters, ResponseErrorHandler[] errorHandlers)
		throws GeneralSecurityException, IOException {
		RestServerConnectorFactoryBuilder restServerConnectorFactoryBuilder = RestServerConnectorFactoryBuilder.create()
			.withInterceptors(interceptors)
			.withErrorHandlers(errorHandlers)
			.withMessageConverters(messageConverters)
			.withHttpClient(createHttpClientContext(uaaRestClientProperties, httpClient));

		restServerConnectorFactory = restServerConnectorFactoryBuilder.build();

		getConnector = restServerConnectorFactory.createRestGetConnector();
		postConnector = restServerConnectorFactory.createRestPostConnector();
		putConnector = restServerConnectorFactory.createRestPutConnector();
		deleteConnector = restServerConnectorFactory.createRestDeleteConnector();
		headConnector = restServerConnectorFactory.createRestHeadConnector();
		optionsConnector = restServerConnectorFactory.createRestOptionsConnector();

		authenticationRestClient = new AuthenticationRestClient(uaaRestClientProperties.getUaaBase().getUrl(), getConnector, postConnector);
		authorizationRestClient = new AuthorizationRestClient(uaaRestClientProperties.getUaaBase().getUrl(), getConnector);
		LOGGER.info("Factory has been initialized with configuration[{}]", uaaRestClientProperties);
	}

	/**
	 * Get the {@link RestServerConnectorFactory} only if you need to create new connectors. Otherwise use getXYZConnectors
	 * @return RestServerConnectorFactory
	 */
	public RestServerConnectorFactory getRestServerConnectorFactory() {
		return restServerConnectorFactory;
	}

	public AuthenticationRestClient getAuthenticationRestClient() {
		return authenticationRestClient;
	}

	public AuthorizationRestClient getAuthorizationRestClient() {
		return authorizationRestClient;
	}

	public RestGetConnector getGetConnector() {
		return getConnector;
	}

	public RestPostConnector getPostConnector() {
		return postConnector;
	}

	public RestPutConnector getPutConnector() {
		return putConnector;
	}

	public RestDeleteConnector getDeleteConnector() {
		return deleteConnector;
	}

	public RestHeadConnector getHeadConnector() {
		return headConnector;
	}

	public RestOptionsConnector getOptionsConnector() {
		return optionsConnector;
	}

	public UAARestClientAuthorizationConfig getAuthorizationConfig() {
		return authorizationConfig;
	}

	private ClientHttpRequestInterceptor[] buildInterceptors(ClientHttpRequestInterceptor[] baseInterceptors) {
		ClientHttpRequestInterceptor[] clientInterceptors =
			ArrayUtils.addAll(
				baseInterceptors,
				UAARestClientInterceptorFactory.createAcceptHeaderInterceptor(),
				authorizationConfig.getAuthorizationInterceptor()
			);

		if (authorizationConfig.getCacheInterceptor() != null) {
			clientInterceptors = ArrayUtils.addAll(clientInterceptors, authorizationConfig.getCacheInterceptor());
		}

		return clientInterceptors;
	}

	private ResponseErrorHandler[] buildErrorHandlers(ResponseErrorHandler[] baseHandlers) {
		return ArrayUtils.add(baseHandlers, new UAAResponseErrorHandler());
	}

	private CloseableHttpClient createHttpClientContext(UAARestClientProperties uaaRestClientProperties, CloseableHttpClient httpClient)
		throws GeneralSecurityException, IOException {
		CertificateProperties certificateProperties = uaaRestClientProperties.getAuthenticationConfiguration().getCertificate();
		KeyStore keyStore = loadKeyStore(certificateProperties.getKeyStore(), certificateProperties.getKeyStorePassword());
		KeyStore trustStore = loadKeyStore(certificateProperties.getTrustStore(), certificateProperties.getTrustStorePassword());

		if (keyStore == null && trustStore == null) {
			return httpClient;
		}

		return createHttpClientWithSSLContext(httpClient, keyStore, certificateProperties.getKeyStorePassword(), trustStore);
	}

	private KeyStore loadKeyStore(String resourceLocation, String password)
		throws GeneralSecurityException, IOException {
		if (resourceLocation == null) {
			return null;
		}
		Resource resource = new DefaultResourceLoader().getResource(resourceLocation);
		KeyStore ks = KeyStore.getInstance("PKCS12");
		try (InputStream in = resource.getInputStream()) {
			ks.load(in, Optional.ofNullable(password).orElse("").toCharArray());
		}
		return ks;
	}

	private CloseableHttpClient createHttpClientWithSSLContext(CloseableHttpClient customHttpClient, KeyStore keyStore, String keyStorePassword,
		KeyStore trustStore)
		throws GeneralSecurityException {

		if (customHttpClient != null) {
			// If a custom client is provided, we can't easily modify it, so we create a new one with SSL
			LOGGER.warn("Custom HttpClient provided but SSL context needs to be configured. Creating new HttpClient with SSL.");
		}

		try {
			SSLContextBuilder sslContextBuilder = SSLContextBuilder.create();

			if (keyStore != null) {
				sslContextBuilder.loadKeyMaterial(keyStore, Optional.ofNullable(keyStorePassword).orElse("").toCharArray());
			}

			if (trustStore != null) {
				sslContextBuilder.loadTrustMaterial(trustStore, null);
			}

			SSLContext sslContext = sslContextBuilder.build();

			HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
				.setTlsSocketStrategy(new DefaultClientTlsStrategy(sslContext))
				.build();

			return HttpClients.custom()
				.setConnectionManager(connectionManager)
				.build();
		} catch (Exception e) {
			throw new GeneralSecurityException("Failed to create HttpClient with SSL context", e);
		}
	}

}
