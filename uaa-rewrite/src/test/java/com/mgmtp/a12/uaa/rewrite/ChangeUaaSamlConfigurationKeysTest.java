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
import org.openrewrite.properties.Assertions;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

public class ChangeUaaSamlConfigurationKeysTest implements RewriteTest {
	@Override
	public void defaults(RecipeSpec spec) {
		spec.recipe(new ChangeUaaSamlConfigurationKeys());
	}

	@Test
	void propertiesChanged() {
		rewriteRun(
			Assertions.properties(
				// Before
				"""
					mgmtp.a12.uaa.authentication.saml.registration.entity-id=urn:com:mgm:UAA
					mgmtp.a12.uaa.authentication.saml.registration.metadata-idp-resource=classpath:static/ekona.xml
					mgmtp.a12.uaa.authentication.saml.registration.signing-credential.private-key-location=classpath:encryption/sign_private.key
					mgmtp.a12.uaa.authentication.saml.registration.signing-credential.certificate-location=classpath:encryption/sign_certificate.crt
					mgmtp.a12.uaa.authentication.saml.registration.decryption-credential.private-key-location=classpath:encryption/enc_private.key
					mgmtp.a12.uaa.authentication.saml.registration.decryption-credential.certificate-location=classpath:encryption/enc_certificate.crt
					mgmtp.a12.uaa.authentication.saml.registration.registration-id=uaa
					""",
				// After
				"""
					spring.security.saml2.relyingparty.registration.uaa.entity-id=urn:com:mgm:UAA
					spring.security.saml2.relyingparty.registration.uaa.assertingparty.metadata-uri=classpath:static/ekona.xml
					spring.security.saml2.relyingparty.registration.uaa.signing.credentials[0].private-key-location=classpath:encryption/sign_private.key
					spring.security.saml2.relyingparty.registration.uaa.signing.credentials[0].certificate-location=classpath:encryption/sign_certificate.crt
					spring.security.saml2.relyingparty.registration.uaa.decryption.credentials[0].private-key-location=classpath:encryption/enc_private.key
					spring.security.saml2.relyingparty.registration.uaa.decryption.credentials[0].certificate-location=classpath:encryption/enc_certificate.crt
					
					""",
				s -> s.path("src/main/resources/application.properties")
			)
		);
	}
}
