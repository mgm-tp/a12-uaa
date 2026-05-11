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
package com.mgmtp.a12.uaa.authentication.benchmark;

import java.io.File;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

public class BenchmarkAuthenticationRunner {
	public static void main(String[] args) throws RunnerException {
		File targetDir = new File(BenchmarkAuthenticationRunner.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
		File destination = new File(targetDir, "results.csv");
		if (args.length > 0) {
			destination = new File(args[0]);
		}

		System.out.printf("Generating report to file [%s]%n", destination.getAbsolutePath());
		Options jmhRunnerOptions = new OptionsBuilder()
			// set the class name regex for benchmarks to search for to the current class
			.shouldFailOnError(true)
			.resultFormat(ResultFormatType.CSV)
			.result(destination.getAbsolutePath())
			.verbosity(VerboseMode.NORMAL)
			.include(".*")
			.build();
		new Runner(jmhRunnerOptions).run();
	}
}
