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
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;

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

import okhttp3.OkHttpClient;

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

	UAARestClientFactory(UAARestClientProperties uaaRestClientProperties, OkHttpClient okHttpClient, ClientHttpRequestInterceptor[] interceptors,
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

		init(uaaRestClientProperties, okHttpClient, clientInterceptors, messageConverters, clientErrorHandlers);
	}

	UAARestClientFactory(UAARestClientProperties uaaRestClientProperties, OkHttpClient okHttpClient, ClientHttpRequestInterceptor[] interceptors,
		List<HttpMessageConverter<?>> messageConverters, ResponseErrorHandler[] errorHandlers)
		throws GeneralSecurityException, IOException {
		init(uaaRestClientProperties, okHttpClient, interceptors, messageConverters, errorHandlers);
	}

	/**
	 * @deprecated This constructor will be made private in a future release.
	 *             Use {@link UAARestClientFactoryBuilder} to create instances instead.
	 */
	@Deprecated(since = "9.2.0")
	public UAARestClientFactory(UAARestClientProperties uaaRestClientProperties, ClientHttpRequestInterceptor[] interceptors,
		ResponseErrorHandler[] errorHandlers, List<HttpMessageConverter<?>> messageConverters)
		throws GeneralSecurityException, IOException {
		this(uaaRestClientProperties, interceptors, errorHandlers, messageConverters, null, new CacheNameMapper[0]);
	}

	/**
	 * @deprecated This constructor will be made private in a future release.
	 *             Use {@link UAARestClientFactoryBuilder} to create instances instead.
	 */
	@Deprecated(since = "9.2.0")
	public UAARestClientFactory(UAARestClientProperties uaaRestClientProperties, ClientHttpRequestInterceptor[] interceptors,
		List<HttpMessageConverter<?>> messageConverters, ResponseErrorHandler[] errorHandlers)
		throws GeneralSecurityException, IOException {
		init(uaaRestClientProperties, null, interceptors, messageConverters, errorHandlers);
	}

	/**
	 * @deprecated This constructor will be made private in a future release.
	 *             Use {@link UAARestClientFactoryBuilder} to create instances instead.
	 */
	@Deprecated(since = "9.2.0")
	public UAARestClientFactory(UAARestClientProperties uaaRestClientProperties, ClientHttpRequestInterceptor[] interceptors,
		ResponseErrorHandler[] errorHandlers, List<HttpMessageConverter<?>> messageConverters, CacheManager cacheManager, CacheNameMapper[] cacheNameMappers
	) throws GeneralSecurityException, IOException {
		this(uaaRestClientProperties, null, interceptors, errorHandlers, messageConverters, cacheManager, cacheNameMappers);
	}

	private void init(UAARestClientProperties uaaRestClientProperties, OkHttpClient okHttpClient, ClientHttpRequestInterceptor[] interceptors,
		List<HttpMessageConverter<?>> messageConverters, ResponseErrorHandler[] errorHandlers)
		throws GeneralSecurityException, IOException {
		RestServerConnectorFactoryBuilder restServerConnectorFactoryBuilder = RestServerConnectorFactoryBuilder.create()
			.withInterceptors(interceptors)
			.withErrorHandlers(errorHandlers)
			.withMessageConverters(messageConverters)
			.withOkHttpClient(createHttpClientContext(uaaRestClientProperties, okHttpClient));

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

	private OkHttpClient createHttpClientContext(UAARestClientProperties uaaRestClientProperties, OkHttpClient okHttpClient)
		throws GeneralSecurityException, IOException {
		CertificateProperties certificateProperties = uaaRestClientProperties.getAuthenticationConfiguration().getCertificate();
		KeyStore keyStore = loadKeyStore(certificateProperties.getKeyStore(), certificateProperties.getKeyStorePassword());
		KeyStore trustStore = loadKeyStore(certificateProperties.getTrustStore(), certificateProperties.getTrustStorePassword());

		if (keyStore == null && trustStore == null) {
			return okHttpClient;
		}

		return getOkHttpClientWithSSLContext(okHttpClient, keyStore, certificateProperties.getKeyStorePassword(), trustStore);
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

	private OkHttpClient getOkHttpClientWithSSLContext(OkHttpClient customOkHttpClient, KeyStore keyStore, String keyStorePassword,
		KeyStore trustStore)
		throws GeneralSecurityException {
		OkHttpClient.Builder builder = customOkHttpClient != null ?
			customOkHttpClient.newBuilder() :
			new OkHttpClient.Builder()
				.connectTimeout(0, TimeUnit.MILLISECONDS)
				.readTimeout(0, TimeUnit.MILLISECONDS)
				.writeTimeout(0, TimeUnit.MILLISECONDS);
		SSLContext sslContext = SSLContext.getInstance("TLS");

		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(keyStore, Optional.ofNullable(keyStorePassword).orElse("").toCharArray());

		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(trustStore);
		X509TrustManager trustManager = (X509TrustManager) tmf.getTrustManagers()[0];

		sslContext.init(keyStore != null ? kmf.getKeyManagers() : null,
			trustStore != null ? tmf.getTrustManagers() : null,
			new SecureRandom());

		builder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
		return builder.build();
	}

}
