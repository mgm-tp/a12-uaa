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
package com.mgmtp.a12.uaa.client.rest.auth.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.htmlunit.WebConnection;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.WebResponseData;
import org.htmlunit.util.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

class UAAWebConnection implements WebConnection {

	private static final Logger LOGGER = LoggerFactory.getLogger(UAAWebConnection.class);

	private final RestClient restClient;

	public UAAWebConnection() {
		HttpClient httpClient = HttpClient.newBuilder()
			.followRedirects(HttpClient.Redirect.NEVER)
			.build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		restClient = RestClient.builder()
			.requestFactory(requestFactory)
			.defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
				// Pass all errors through without throwing exceptions
			})
			.build();
	}

	@Override
	public WebResponse getResponse(WebRequest webRequest) throws IOException {
		URL url = webRequest.getUrl();
		HttpHeaders headers = new HttpHeaders();
		webRequest.getAdditionalHeaders().entrySet().stream()
			.forEach(entry -> headers.putIfAbsent(entry.getKey(), Arrays.asList(entry.getValue())));

		HttpMethod httpMethod = HttpMethod.valueOf(webRequest.getHttpMethod().name());

		Object body = null;
		if ((HttpMethod.POST == httpMethod) || (HttpMethod.PUT == httpMethod)) {
			if (webRequest.getRequestBody() != null) {
				body = webRequest.getRequestBody();
			} else {
				MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
				webRequest.getParameters().stream()
					.forEach(parameter -> requestParams.add(parameter.getName(), parameter.getValue()));
				body = requestParams;
			}
		}

		try {
			URI uri = url.toURI();
			LOGGER.debug("Connecting to server URL=[{}], ContentType=[{}], AdditionalHeaders=[{}], Payload=[{}], Method=[{}]",
				url, headers.getContentType(), webRequest.getAdditionalHeaders(), body, httpMethod);

			final Object requestBody = body;
			ResponseEntity<String> responseEntity = restClient.method(httpMethod)
				.uri(uri)
				.headers(httpHeaders -> httpHeaders.addAll(headers))
				.body(requestBody != null ? requestBody : "")
				.retrieve()
				.toEntity(String.class);

			String responseBody = Optional.ofNullable(responseEntity.getBody()).orElse("");
			List<NameValuePair> htmlUnitHeaders = responseEntity.getHeaders().toSingleValueMap().entrySet().stream()
				.map(entry -> new NameValuePair(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
			WebResponseData responseData = new WebResponseData(responseBody.getBytes(),
				responseEntity.getStatusCode().value(), "NONE", htmlUnitHeaders);
			return new WebResponse(responseData, webRequest, System.currentTimeMillis());
		} catch (RestClientException | URISyntaxException e) {
			LOGGER.error("Unable to process request ", e);
		}
		return null;
	}

	@Override
	public void close() throws IOException {
		//nothing needed
	}

}
