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

import java.util.Set;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.StringUtils;
import org.openrewrite.properties.PropertiesIsoVisitor;
import org.openrewrite.properties.search.FindProperties;
import org.openrewrite.properties.tree.Properties;

public class ChangeUaaSamlConfigurationKeys extends Recipe {

	private static final String REGISTRATION_ID_KEY = "mgmtp.a12.uaa.authentication.saml.registration.registration-id";
	private static final String REGISTRATION_ENTITY_ID_KEY = "mgmtp.a12.uaa.authentication.saml.registration.entity-id";
	private static final String METADATA_IDP_RESOURCE_KEY = "mgmtp.a12.uaa.authentication.saml.registration.metadata-idp-resource";
	private static final String CREDENTIAL_PRIVATE_PATTERN_KEY = "mgmtp.a12.uaa.authentication.saml.registration.(.*)-credential.(.*)";

	private String existingRegistrationIdValue;

	@Override
	public String getDisplayName() {
		return "Replace UAA with Spring configuration for SAML registration";
	}

	@Override
	public String getDescription() {
		return "The current registration-id value will be used to build a set of new properties key.";
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new PropertiesIsoVisitor<>() {
			@Override
			public Properties.Entry visitEntry(Properties.Entry entry, ExecutionContext ctx) {
				if (entry.getKey().matches(METADATA_IDP_RESOURCE_KEY)) {
					String targetKey = "spring.security.saml2.relyingparty.registration.%s.assertingparty.metadata-uri".formatted(existingRegistrationIdValue);
					return entry.withKey(entry.getKey().replaceAll(METADATA_IDP_RESOURCE_KEY, targetKey));
				} else if (entry.getKey().matches(REGISTRATION_ENTITY_ID_KEY)) {
					String targetKey = "spring.security.saml2.relyingparty.registration.%s.entity-id".formatted(existingRegistrationIdValue);
					return entry.withKey(entry.getKey().replaceAll(REGISTRATION_ENTITY_ID_KEY, targetKey));
				} else if (entry.getKey().matches(CREDENTIAL_PRIVATE_PATTERN_KEY)) {
					String targetPatternKey = "spring.security.saml2.relyingparty.registration.%s.$1.credentials[0].$2".formatted(existingRegistrationIdValue);
					return entry.withKey(entry.getKey().replaceAll(CREDENTIAL_PRIVATE_PATTERN_KEY, targetPatternKey));
				} else if (entry.getKey().matches(REGISTRATION_ID_KEY)) {
					Properties.Entry deleteEntry = entry.withKey(entry.getKey().replaceAll(REGISTRATION_ID_KEY, ""));
					deleteEntry = deleteEntry.withDelimiter(Properties.Entry.Delimiter.NONE);
					return deleteEntry.withValue(deleteEntry.getValue().withText(""));
				}
				return super.visitEntry(entry, ctx);
			}

			@Override
			public Properties.File visitFile(Properties.File file, ExecutionContext ctx) {
				Set<Properties.Entry> registerIdProperties = FindProperties.find(file, REGISTRATION_ID_KEY, false);
				if (StringUtils.isNullOrEmpty(existingRegistrationIdValue)) {
					existingRegistrationIdValue =
						String.valueOf(registerIdProperties.stream().findFirst().map(entry -> entry.getValue().getText()).orElse(null));
				}
				return super.visitFile(file, ctx);
			}
		};
	}
}
