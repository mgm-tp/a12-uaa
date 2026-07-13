/*
 * (c) copyright 2012-2025 mgm technology partners GmbH
 *
 * This software, the underlying source code and other artifacts are protected by copyright.
 * All rights, in particular the right to use, reproduce, publish and edit are reserved.
 * A simple right of use (license) can be acquired for use, duplication, publication, editing etc..
 *
 * Requests for this can be made at A12-license@mgm-tp.com or other official channels of the copyright holder.
 */
package com.mgmtp.a12.uaa.rewrite;

import java.util.Arrays;

import org.openrewrite.config.Environment;
import org.openrewrite.java.Assertions;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

public class ReplaceJavaMethodCallTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.parser(JavaParser
				.fromJavaVersion()
				.classpath("uaa-rewrite")
				.logCompilationWarningsAndErrors(true))
			.recipe(Environment.builder()
				.scanRuntimeClasspath("com.test")
				.build().activateRecipes())
			.recipe(new ReplaceMethodCall(
				"com.test.TestService testMethod(java.lang.String)",
				"#{any(com.test.TestService)}.testMethod(#{any(java.lang.String)}).getData()",
				Arrays.asList(Integer.valueOf(0))));

	}
	/*
	* This test is failed even when the rewrite function runs properly.
	* Because of: https://github.com/openrewrite/rewrite-testing-frameworks/issues/502
	*/
//	@Test
	void checkJavaMethodChainReplacement() {
		rewriteRun(

			Assertions.java(
				// The Java source file before the recipe is run:
				"""
					import com.test.TestService;

					public class TestConsumer {

						private TestService testService;

						public String aMethod() {
							String data = "nothing";
							return testService.testMethod(data);
						}

					}

					""",
				// The expected Java source file after the recipe is run:
				"""
					import com.test.TestService;

					public class TestConsumer {

						private TestService testService;

						public String aMethod() {
							String data = "nothing";
							return testService.testMethod(data).getData();
						}

					}

					"""));
	}

}
