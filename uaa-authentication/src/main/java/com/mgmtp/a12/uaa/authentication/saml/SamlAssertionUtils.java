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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.XSBoolean;
import org.opensaml.core.xml.schema.XSBooleanValue;
import org.opensaml.core.xml.schema.XSDateTime;
import org.opensaml.core.xml.schema.XSInteger;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.XSURI;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.springframework.util.CollectionUtils;

public class SamlAssertionUtils {

	public static Map<String, List<Object>> getAssertionAttributes(Assertion assertion) {
		Map<String, List<Object>> attributeMap = new LinkedHashMap<>();
		for (AttributeStatement attributeStatement : assertion.getAttributeStatements()) {
			for (Attribute attribute : attributeStatement.getAttributes()) {
				List<Object> attributeValues = new ArrayList<>();
				for (XMLObject xmlObject : attribute.getAttributeValues()) {
					Object attributeValue = getXmlObjectValue(xmlObject);
					if (attributeValue != null) {
						attributeValues.add(attributeValue);
					}
				}
				attributeMap.merge(attribute.getName(), attributeValues, (existingValue, newValue) -> {
					existingValue.addAll(attributeValues);
					return existingValue;
				});
			}
		}
		return attributeMap;
	}

	public static <T> Optional<T> getAttributeValue(Map<String, List<Object>> attributes, String attributeName, Class<T> requiredType) {
		Optional<?> attributeOpt = attributes.entrySet().stream()
			.filter(entry -> entry.getKey().equals(attributeName))
			.map(entry -> CollectionUtils.firstElement(entry.getValue()))
			.findFirst();
		return (Optional<T>) attributeOpt;
	}

	private static Object getXmlObjectValue(XMLObject xmlObject) {
		if (xmlObject instanceof XSAny any) {
			return any.getTextContent();
		}
		if (xmlObject instanceof XSString string) {
			return string.getValue();
		}
		if (xmlObject instanceof XSInteger integer) {
			return integer.getValue();
		}
		if (xmlObject instanceof XSURI rI) {
			return rI.getURI();
		}
		if (xmlObject instanceof XSBoolean boolean1) {
			XSBooleanValue xsBooleanValue = boolean1.getValue();
			return (xsBooleanValue != null) ? xsBooleanValue.getValue() : null;
		}
		if (xmlObject instanceof XSDateTime time) {
			return  time.getValue();
		}
		return xmlObject;
	}

}
