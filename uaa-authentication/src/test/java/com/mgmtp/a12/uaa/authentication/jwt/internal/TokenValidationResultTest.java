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
package com.mgmtp.a12.uaa.authentication.jwt.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

import com.mgmtp.a12.uaa.authentication.jwt.internal.JwtTokenVerifier.TokenValidationResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenValidationResultTest {

	@Mock
	private Logger logger;

	@Mock
	private LoggingEventBuilder loggingEventBuilder;

	@Test
	public void allChecksPassingIsValid() {
		TokenValidationResult result = new TokenValidationResult(false, false, false, false, false, null);
		assertTrue(result.valid());
	}

	@Test
	public void expiredTokenIsInvalid() {
		TokenValidationResult result = new TokenValidationResult(false, false, true, false, false, null);
		assertFalse(result.valid());
		assertTrue(result.expired());
	}

	@Test
	public void loggedOutTokenIsInvalid() {
		TokenValidationResult result = new TokenValidationResult(false, false, false, true, false, null);
		assertFalse(result.valid());
		assertTrue(result.loggedOut());
	}

	@Test
	public void malformedTokenIsInvalid() {
		TokenValidationResult result = new TokenValidationResult(true, false, false, false, false, null);
		assertFalse(result.valid());
		assertTrue(result.malformed());
	}

	@Test
	public void parsingFailureKeepsCause() {
		RuntimeException cause = new RuntimeException("boom");
		TokenValidationResult result = new TokenValidationResult(false, false, false, false, true, cause);
		assertFalse(result.valid());
		assertTrue(result.parsingFailed());
		assertSame(cause, result.cause());
	}

	@Test
	public void doErrorLogDoesNotLogForValidToken() {
		TokenValidationResult result = new TokenValidationResult(false, false, false, false, false, null);
		result.doErrorLog(logger, Level.WARN, "should not be logged");
		verifyNoInteractions(logger);
	}

	@Test
	public void doErrorLogWritesSingleEntryWithoutCause() {
		when(logger.atLevel(Level.WARN)).thenReturn(loggingEventBuilder);
		TokenValidationResult result = new TokenValidationResult(false, false, true, false, false, null);

		result.doErrorLog(logger, Level.WARN, "Token rejected");

		verify(loggingEventBuilder).log("Token rejected " + result.describe());
		verify(loggingEventBuilder, never()).setCause(any());
	}

	@Test
	public void doErrorLogAttachesCauseWhenParsingFailed() {
		RuntimeException cause = new RuntimeException("boom");
		when(logger.atLevel(Level.WARN)).thenReturn(loggingEventBuilder);
		when(loggingEventBuilder.setCause(cause)).thenReturn(loggingEventBuilder);
		TokenValidationResult result = new TokenValidationResult(false, false, false, false, true, cause);

		result.doErrorLog(logger, Level.WARN, "Token rejected");

		verify(loggingEventBuilder).setCause(cause);
		verify(loggingEventBuilder).log("Token rejected " + result.describe());
	}
}
