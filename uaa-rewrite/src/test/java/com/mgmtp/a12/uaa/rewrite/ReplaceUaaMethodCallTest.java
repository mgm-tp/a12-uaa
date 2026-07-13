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

import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.config.Environment;
import org.openrewrite.java.Assertions;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

public class ReplaceUaaMethodCallTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.parser(JavaParser
				.fromJavaVersion()
				//for classpath from gradle
				//				.classpath("uaa-authentication", "spring-security-core")
				.classpathFromResources(new InMemoryExecutionContext(), "uaa-authentication-7.5.0", "spring-security-core-6.2.3")
				.logCompilationWarningsAndErrors(true))
			.recipe(Environment.builder()
				.scanRuntimeClasspath("com.mgmtp")
				.build()
				.activateRecipes())
			.recipe(new ReplaceMethodCall(
				"com.mgmtp.a12.uaa.authentication.jwt.TokenSupport generateToken(org.springframework.security.core.userdetails.UserDetails)",
				"#{any(com.mgmtp.a12.uaa.authentication.jwt.TokenSupport)}" +
					".generateToken(#{any(org.springframework.security.core.userdetails.UserDetails)}).getToken()",
				Arrays.asList(Integer.valueOf(0))));

	}

	/*
	 * This test is failed even when the rewrite function runs properly.
	 * Because of: https://github.com/openrewrite/rewrite-testing-frameworks/issues/502
	 */
	//@Test
	void checkUaaMethodChainReplacement() {
		rewriteRun(
			Assertions.java(
				// The Java source file before the recipe is run:
				"""
					
					import java.util.Collections;
					
					import com.mgmtp.a12.uaa.authentication.jwt.TokenSupport;
					import com.mgmtp.a12.uaa.authentication.user.internal.UAAUser;
					
					public class ChainTestClass {
					
						private TokenSupport tokenSupport;
					
						public String testToken() {
							UAAUser<?> user = new UAAUser<>("test", "", Collections.emptyList());
							String token = tokenSupport.generateToken(user);
							return token;
						}
					}
					
					""",
				// The expected Java source file after the recipe is run:
				"""
					
					import java.util.Collections;
					
					import com.mgmtp.a12.uaa.authentication.jwt.TokenSupport;
					import com.mgmtp.a12.uaa.authentication.user.internal.UAAUser;
					
					public class ChainTestClass {
					
						private TokenSupport tokenSupport;
					
						public String testToken() {
							UAAUser<?> user = new UAAUser<>("test", "", Collections.emptyList());
							String token = tokenSupport.generateToken(user).getToken();
							return token;
						}
					}
					
					"""));
	}

}
