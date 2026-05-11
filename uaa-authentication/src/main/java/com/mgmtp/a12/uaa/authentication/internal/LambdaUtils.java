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
package com.mgmtp.a12.uaa.authentication.internal;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class LambdaUtils {

	private LambdaUtils() {
	}

	@FunctionalInterface
	public interface Function_WithExceptions<T, R, E extends Exception> {
		R apply(T t) throws E;
	}

	@FunctionalInterface
	public interface Predicate_WithExceptions<T, E extends Exception> {
		boolean test(T t) throws E;
	}

	public interface ThrowingConsumer<T, E extends Throwable> {
		void accept(T t) throws E;

	}

	public static <T, E extends Throwable> Consumer<T> uncheckedConsumer(ThrowingConsumer<T, E> consumer) {
		return (t) -> {
			try {
				consumer.accept(t);
			} catch (Throwable e) {
				//throw new RuntimeException(e);
				throwAsUnchecked(e);
			}
		};
	}
	
	public static <T, E extends Throwable> Consumer<T> uncheckedConsumerWithNoException(ThrowingConsumer<T, E> consumer) {
		return (t) -> {
			try {
				consumer.accept(t);
			} catch (Throwable e) {
				//nothing here
			}
		};
	}


	public static <T, R, E extends Exception> Function<T, R> uncheckedFunction(Function_WithExceptions<T, R, E> function) throws E {
		return t -> {
			try {
				return function.apply(t);
			} catch (Exception exception) {
				throwAsUnchecked(exception);
				return null;
			}
		};
	}

	public static <T, R, E extends Exception> Function<T, R> uncheckedFunctionWithNoException(Function_WithExceptions<T, R, E> function) throws E {
		return t -> {
			try {
				return function.apply(t);
			} catch (Exception exception) {
				return null;
			}
		};
	}

	public static <T, E extends Exception> Predicate<T> uncheckedPredicateWithNoException(Predicate_WithExceptions<T, E> predicate) throws E {
		return t -> {
			try {
				return predicate.test(t);
			} catch (Exception exception) {
				return false;
			}
		};
	}

	@SuppressWarnings("unchecked")
	private static <E extends Throwable> void throwAsUnchecked(Throwable exception) throws E {
		throw (E) exception;
	}

}
