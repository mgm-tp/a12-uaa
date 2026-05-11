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
package com.mgmtp.a12.uaa.authentication.saml.internal;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import jakarta.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider.ResponseToken;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.saml.SamlAssertionExtractor;
import com.mgmtp.a12.uaa.authentication.security.login.internal.TypedUsernamePasswordAuthenticationToken;

/**
 * Create {@link TypedUsernamePasswordAuthenticationToken} which is used by spring security to identify users.
 *
 */
public class SamlAuthenticationConverter implements Converter<ResponseToken, UsernamePasswordAuthenticationToken> {

	private static final Logger LOGGER = LoggerFactory.getLogger(SamlAuthenticationConverter.class);
	private static final XPath xpath = XPathFactory.newInstance().newXPath();
	private static final ConcurrentHashMap<String, XPathExpression> xpathCache = new ConcurrentHashMap<>();
	private static final String SEPARATOR = "##";

	private List<String> uniqueElementXpaths;
	private List<String> sameValueElementXpaths;

	@Inject
	private SamlAssertionExtractor samlAssertionExtractor;

	public SamlAuthenticationConverter(List<String> uniqueElementXpaths, List<String> sameValueElementsXpaths) {
		this.uniqueElementXpaths = uniqueElementXpaths;
		this.sameValueElementXpaths = sameValueElementsXpaths;
	}

	@Override
	public UsernamePasswordAuthenticationToken convert(ResponseToken response) {
		List<String> errors = validate(response);
		if (CollectionUtils.isNotEmpty(errors)) {
			throw new AccessDeniedException(StringUtils.join(errors, ","));
		}
		try {
			UserDetails userDetails = samlAssertionExtractor.extractAssertion(response);
			TypedUsernamePasswordAuthenticationToken<ResponseToken> authentication =
				new TypedUsernamePasswordAuthenticationToken<>(userDetails, "", AuthenticationType.SAML, userDetails.getAuthorities());
			authentication.setAuthenticationData(response);
			return authentication;
		} catch (Exception e) {
			LOGGER.error("Unable to extract SAML assertion(s)", e);
			throw e;
		}
	}

	public List<String> validate(ResponseToken responseToken) {
		List<String> errors = new LinkedList<>();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliant
			DocumentBuilder documentBuilder = factory.newDocumentBuilder();
			Document samlResponseDocument = documentBuilder.parse(new InputSource(new StringReader(responseToken.getToken().getSaml2Response())));

			uniqueElementXpaths.stream()
				.map(xpathExpression -> checkUniqueOccurence(samlResponseDocument, xpathExpression))
				.flatMap(Optional::stream)
				.forEach(errors::add);

			sameValueElementXpaths.stream()
				.map(pair -> Pair.of(StringUtils.substringBefore(pair, SEPARATOR), StringUtils.substringAfter(pair, SEPARATOR)))
				.map(pair -> equalsXpathValues(samlResponseDocument, pair))
				.flatMap(Optional::stream)
				.forEach(errors::add);

		} catch (ParserConfigurationException | SAXException | IOException e) {
			errors.add(e.getMessage());
		}
		return errors;
	}

	private Optional<String> checkUniqueOccurence(Node node, String xpathExpression) {
		NodeList nodes = (NodeList) getXpathValue(node, xpathExpression, XPathConstants.NODESET);
		String message = "Xpath expression [%s] finds %s nodes".formatted(xpathExpression, nodes.getLength());
		LOGGER.debug(message);
		if (nodes.getLength() != 1) {
			return Optional.of(message);
		}
		return Optional.empty();
	}

	private Optional<String> equalsXpathValues(Node node, Pair<String, String> xpathExpressions) {
		String leftValue = (String) getXpathValue(node, xpathExpressions.getLeft(), XPathConstants.STRING);
		String rightValue = (String) getXpathValue(node, xpathExpressions.getRight(), XPathConstants.STRING);
		LOGGER.debug("Comparing xpath expression [{}]=[{}] with [{}]=[{}]", xpathExpressions.getLeft(), leftValue, xpathExpressions.getRight(), rightValue);
		if (!StringUtils.equals(leftValue, rightValue)) {
			return Optional.of("Xpath expression [%s]=[%s] and [%s]=[%s] has different values".formatted(xpathExpressions.getLeft(),
				leftValue, xpathExpressions.getRight(), rightValue));
		}
		return Optional.empty();
	}

	private Object getXpathValue(Node node, String xpathExpression, QName type) {
		try {
			return getXpathExpression(xpathExpression).evaluate(node, type);
		} catch (Exception e) {
			throw new AccessDeniedException("Unable to resolve xpath expression [%s]".formatted(xpathExpression), e);
		}
	}

	private XPathExpression getXpathExpression(String xpath) {
		XPathExpression xpathExpr = xpathCache.computeIfAbsent(xpath, expression -> {
			try {
				return SamlAuthenticationConverter.xpath.compile(expression);
			} catch (XPathExpressionException e) {
				LOGGER.error("Unable to create xpath expression for path: " + xpath, e);
			}
			return null;
		});
		return xpathExpr;
	}

}
