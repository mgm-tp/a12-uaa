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

import org.junit.jupiter.api.Test;
import org.openrewrite.config.Environment;
import org.openrewrite.java.Assertions;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

/**
 * Verifies the {@code UpgradeUaa_10_0} recipe (2026.06 / UAA 10.0 breaking changes).
 *
 * <p>The removed UAA APIs are provided as compile stubs via {@link JavaParser#dependsOn} so that
 * type attribution resolves the old method names without bundling binary jars.</p>
 *
 * <p>Only the getter/setter renames that the recipe actually performs are asserted here
 * (see {@code META-INF/rewrite/uaa-10_0_0.yml}). Removals that require non-mechanical changes
 * are documented as manual migration steps in the migration guide and are deliberately not
 * covered by the recipe.</p>
 */
public class UpgradeUaa_10_0Test implements RewriteTest {

	// Minimal stubs of the removed (pre-10.0) public APIs, only for type attribution.
	private static final String EXTERNAL_PRINCIPAL_STUB = """
		package com.mgmtp.a12.uaa.authentication.principal;
		public class ExternalPrincipalImpl {
			public String geteMail() { return null; }
			public void seteMail(String email) { }
		}
		""";

	private static final String TOKEN_DATA_STUB = """
		package com.mgmtp.a12.uaa.client.rest.auth.internal.data;
		public class TokenData {
			public String getAccessTokenExpiration() { return null; }
			public void setAccessTokenExpiration(String value) { }
		}
		""";

	private static final String REST_CLIENT_PROPERTIES_STUB = """
		package com.mgmtp.a12.uaa.client.rest.config.properties;
		public class UAARestClientProperties {
			public String getGeneratedTokenExpirationHeaderName() { return null; }
			public void setGeneratedTokenExpirationHeaderName(String value) { }
		}
		""";

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.parser(JavaParser.fromJavaVersion()
				.dependsOn(EXTERNAL_PRINCIPAL_STUB, TOKEN_DATA_STUB, REST_CLIENT_PROPERTIES_STUB)
				.logCompilationWarningsAndErrors(true))
			.recipe(Environment.builder()
				.scanRuntimeClasspath("com.mgmtp")
				.build()
				.activateRecipes("com.mgmtp.a12.uaa.openrewrite.UpgradeUaa_10_0"));
	}

	@Test
	void renamesRemovedGettersAndSetters() {
		rewriteRun(
			Assertions.java(
				"""
					import com.mgmtp.a12.uaa.authentication.principal.ExternalPrincipalImpl;
					import com.mgmtp.a12.uaa.client.rest.auth.internal.data.TokenData;
					import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

					class Consumer {
						void use(ExternalPrincipalImpl principal, TokenData token, UAARestClientProperties props) {
							principal.geteMail();
							principal.seteMail("a@b.c");
							token.getAccessTokenExpiration();
							token.setAccessTokenExpiration("1");
							props.getGeneratedTokenExpirationHeaderName();
							props.setGeneratedTokenExpirationHeaderName("h");
						}
					}
					""",
				"""
					import com.mgmtp.a12.uaa.authentication.principal.ExternalPrincipalImpl;
					import com.mgmtp.a12.uaa.client.rest.auth.internal.data.TokenData;
					import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

					class Consumer {
						void use(ExternalPrincipalImpl principal, TokenData token, UAARestClientProperties props) {
							principal.getEmail();
							principal.setEmail("a@b.c");
							token.getTokenRenewInSeconds();
							token.setTokenRenewInSeconds("1");
							props.getGeneratedTokenRenewInSecondsHeaderName();
							props.setGeneratedTokenRenewInSecondsHeaderName("h");
						}
					}
					"""));
	}
}
