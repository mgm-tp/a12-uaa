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

import org.htmlunit.WebClient;
import org.htmlunit.cssparser.parser.CSSErrorHandler;
import org.htmlunit.cssparser.parser.CSSException;
import org.htmlunit.cssparser.parser.CSSParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebClientFactory {

	private static final Logger LOG = LoggerFactory.getLogger(WebClientFactory.class);

	public static WebClient createWebClient() {
		try {
			WebClient webClient = new WebClient();
			webClient.getOptions().setRedirectEnabled(true);
			webClient.getOptions().setJavaScriptEnabled(true);
			webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			webClient.setWebConnection(new UAAWebConnection());
			webClient.setCssErrorHandler(new CSSErrorHandler() {

				@Override
				public void warning(CSSParseException exception) throws CSSException {
					//stay silent

				}

				@Override
				public void fatalError(CSSParseException exception) throws CSSException {
					//stay silent

				}

				@Override
				public void error(CSSParseException exception) throws CSSException {
					//stay silent

				}
			});
			return webClient;
		} catch (Throwable t) {
			LOG.error("Error creating web client {}", t.getMessage(), t);
			throw t;
		}
	}

}
