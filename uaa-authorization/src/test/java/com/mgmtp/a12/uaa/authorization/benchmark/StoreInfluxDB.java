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
package com.mgmtp.a12.uaa.authorization.benchmark;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import okhttp3.OkHttpClient;

public class StoreInfluxDB {
	private static String DATABASE = "jmeter";
	private static String CSV_FILE = "results.csv";
	private static String INFLUXDB_URL = "http://localhost:8086";
	public static void main(String[] args) {
		File targetDir = new File(StoreInfluxDB.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
		File destination = new File(targetDir, CSV_FILE);
		if (args.length > 0) {
			destination = new File(args[0]);
		}

		InfluxDB influxDB = InfluxDBFactory.connect(INFLUXDB_URL, new OkHttpClient.Builder());
		influxDB.setDatabase(DATABASE);
		try (Reader reader = new FileReader(destination);
			CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT)) {
			Pattern pattern = Pattern.compile("[^:.]+(:.*)*$");
			influxDB.write(Point.measurement("events")
				.time(System.currentTimeMillis(), TimeUnit.NANOSECONDS)
				.tag("application", "UAA")
				.tag("release", "7")
				.tag("title", "ApacheJMeter")
				.tag("version", "7.3.3")
				.addField("text", "Test name started")
				.build());
			for (CSVRecord csvRecord : csvParser) {
				if (csvParser.getCurrentLineNumber() == 0 || csvParser.getCurrentLineNumber() == 1)
					continue;
				Matcher matcher = pattern.matcher(csvRecord.get(0));
				String function = "Nothing";
				if (matcher.find()) {
					function = matcher.group(0);
				}
				String mode = csvRecord.get(1);
				//				Double threads = Double.valueOf(csvRecord.get(2));
				//				Double samples = Double.valueOf(csvRecord.get(3));
				Double score = Double.valueOf(csvRecord.get(4));
				Double scoreError = 0.0;
				if (!csvRecord.get(5).equals("NaN")) {
					scoreError = Double.valueOf(csvRecord.get(5));
				}
				//				String unit = csvRecord.get(6);
				System.out.println(function + " " + mode + " " + score + " " + scoreError);

				influxDB.write(Point.measurement("jmeter")
					.time(System.currentTimeMillis(), TimeUnit.NANOSECONDS)
					.tag("transaction", function)
					.tag("mode", mode)
					.tag("application", "UAA")
					.tag("release", "7")
					.tag("version", "7.3.3")
					.tag("statut", "ok")
					//					.tag("threads", threads)
					//					.tag("samples", samples)
					.addField("avg", score)
					.addField("score error", scoreError)
					.build());
			}
			influxDB.write(Point.measurement("events")
				.time(System.currentTimeMillis(), TimeUnit.NANOSECONDS)
				.tag("application", "UAA")
				.tag("release", "7")
				.tag("title", "ApacheJMeter")
				.tag("version", "7.3.3")
				.addField("text", "Test name ended")
				.build());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
