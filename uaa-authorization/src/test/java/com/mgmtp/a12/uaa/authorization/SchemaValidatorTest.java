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
package com.mgmtp.a12.uaa.authorization;

import java.util.List;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.uaa.authorization.schema.internal.SchemaValidator;

@ExtendWith(SpringExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class SchemaValidatorTest {

	private static final String PATH = "classpath:consistency/%s";
	@Inject
	private ResourceLoader resourceLoader;

	private SchemaValidator schemaValidator;

	@BeforeAll
	void setUp() {
		schemaValidator = new SchemaValidator(resourceLoader, new ObjectMapper());
	}

	@Test
	public void validate_OnlyParentFile_NoError() throws Exception {
		// Given
		String parentPath = PATH.formatted("noError/parentFile_NoError.json");

		// Actual
		List<String> actualErrors = schemaValidator.validateAuthorizationFile(parentPath, null);
		Assertions.assertEquals(0, actualErrors.size());
	}

	@Test
	public void validate_parentFile_child_NoError() throws Exception {
		// Given
		String parentPath = PATH.formatted("noError/parentFile_NoError.json");
		List<String> childPaths = List.of(PATH.formatted("noError/childFile_NoError.json"), PATH.formatted("noError/childFile1_NoError.json"));

		// Actual
		List<String> actualErrors = schemaValidator.validateAuthorizationFile(parentPath, childPaths);
		Assertions.assertEquals(0, actualErrors.size());
	}

	@Test
	public void validate_global_duplicate_policy_error() throws Exception {
		// Given
		String parentPath = PATH.formatted("noError/parentFile_NoError.json");
		List<String> childPaths = List.of(PATH.formatted("global_duplicated.json"));

		// Expected
		List<String> expectedErrors = List.of(
			"File: classpath:consistency/global_duplicated.json, Line: 4, Column: 5. " +
				"Error: The identity [name] with value [\"Policy 1\"] in the [classpath:consistency/global_duplicated.json] file " +
				"is a duplicate of the one in the [classpath:consistency/noError/parentFile_NoError.json] file.",
			"File: classpath:consistency/global_duplicated.json, Line: 12, Column: 5. " +
				"Error: The identity [name] with value [\"repository policy 1\"] in the [classpath:consistency/global_duplicated.json] file " +
				"is a duplicate of the one in the [classpath:consistency/noError/parentFile_NoError.json] file.",
			"File: classpath:consistency/global_duplicated.json, Line: 20, Column: 5. " +
				"Error: The identity [name] with value [\"Property Right 1\"] in the [classpath:consistency/global_duplicated.json] file " +
				"is a duplicate of the one in the [classpath:consistency/noError/parentFile_NoError.json] file.");

		// Actual
		List<String> actualErrors =
			schemaValidator.validateAuthorizationFile(parentPath, childPaths);
		Assertions.assertEquals(expectedErrors, actualErrors);
	}

	@Test
	public void validate_nonexistent_refs_error() throws Exception {
		// Given
		String parentPath = PATH.formatted("nonexistent_refs.json");

		// Expected
		List<String> expectedErrors = List.of(
			"File: classpath:consistency/nonexistent_refs.json, Line: 59, Column: 9. " +
				"Error: The reference [\"nonexistent 2\"] does not exist in Authorization Definition files.",
			"File: classpath:consistency/nonexistent_refs.json, Line: 59, Column: 9. " +
				"Error: The reference [\"nonexistent 1\"] does not exist in Authorization Definition files.",
			"File: classpath:consistency/nonexistent_refs.json, Line: 65, Column: 9. " +
				"Error: The reference [\"nonexistent 1\"] does not exist in Authorization Definition files.",
			"File: classpath:consistency/nonexistent_refs.json, Line: 85, Column: 9. " +
				"Error: The reference [\"nonexistent 1\"] does not exist in Authorization Definition files.");

		// Actual
		List<String> actualErrors = schemaValidator.validateAuthorizationFile(parentPath, null);

		Assertions.assertEquals(expectedErrors, actualErrors);
	}

	@Test
	public void validate_policy_refs_require_policies_errors1() throws Exception {
		// Given
		String parentPath = PATH.formatted("policy_refs_require_policies1.json");

		// Expected
		List<String> expectedErrors = List.of(
			"File: classpath:consistency/policy_refs_require_policies1.json, Line: 7, Column: 9. " +
				"Error: The reference [\"This ref does not existed\"] does not exist in Authorization Definition files.",
			"File: classpath:consistency/policy_refs_require_policies1.json, Line: 1, Column: 1. Error: Missing required property [policies].");

		// Actual
		List<String> actualErrors = schemaValidator.validateAuthorizationFile(parentPath, null);

		Assertions.assertEquals(expectedErrors, actualErrors);
	}

	@Test
	public void validate_policy_refs_require_policies_errors2() throws Exception {
		// Given
		String parentPath = PATH.formatted("policy_refs_require_policies2.json");

		// Expected
		List<String> expectedErrors = List.of(
			"File: classpath:consistency/policy_refs_require_policies2.json, Line: 18, Column: 9. " +
				"Error: The reference [\"property permissions policy ref\"] does not exist in Authorization Definition files.",
			"File: classpath:consistency/policy_refs_require_policies2.json, Line: 1, Column: 1. Error: Missing required property [policies].");

		// Actual
		List<String> actualErrors = schemaValidator.validateAuthorizationFile(parentPath, null);

		Assertions.assertEquals(expectedErrors, actualErrors);
	}

	@Test
	public void validate_policy_refs_require_policies_errors3() throws Exception {
		// Given
		String parentPath = PATH.formatted("policy_refs_require_policies3.json");

		// Expected
		List<String> expectedErrors = List.of(
			"File: classpath:consistency/policy_refs_require_policies3.json, Line: 7, Column: 9. " +
				"Error: The reference [\"This ref does not existed\"] does not exist in Authorization Definition files.",
			"File: classpath:consistency/policy_refs_require_policies3.json, Line: 18, Column: 9. " +
				"Error: The reference [\"property permissions policy ref\"] does not exist in Authorization Definition files.",
			"File: classpath:consistency/policy_refs_require_policies3.json, Line: 1, Column: 1. Error: Missing required property [policies].");

		// Actual
		List<String> actualErrors = schemaValidator.validateAuthorizationFile(parentPath, null);

		Assertions.assertEquals(expectedErrors, actualErrors);
	}

	@Test
	public void validate_repository_refs_required_repositoryPolicies_errors() throws Exception {
		// Given
		String parentPath = PATH.formatted("repository_refs_required_repositoryPolicies.json");

		// Expected
		List<String> expectedErrors = List.of(
			"File: classpath:consistency/repository_refs_required_repositoryPolicies.json, Line: 10, Column: 9. " +
				"Error: The reference [\"permission repository refs\"] does not exist in Authorization Definition files.",
			"File: classpath:consistency/repository_refs_required_repositoryPolicies.json, Line: 1, Column: 1. " +
				"Error: Missing required property [repositoryPolicies].");

		// Actual
		List<String> actualErrors = schemaValidator.validateAuthorizationFile(parentPath, null);

		Assertions.assertEquals(expectedErrors, actualErrors);
	}

	@Test
	public void validate_repository_refs_and_policy_refs_required_policies_and_repositoryPolicies_errors() throws Exception {
		// Give
		String parentPath = PATH.formatted("repository_refs_and_policy_refs_required_policies_and_repositoryPolicies.json");

		// Expected
		List<String> expectedErrors = List.of(
			"File: classpath:consistency/repository_refs_and_policy_refs_required_policies_and_repositoryPolicies.json, Line: 7, Column: 9. " +
				"Error: The reference [\"permissions repository refs\"] does not exist in Authorization Definition files.",
			"File: classpath:consistency/repository_refs_and_policy_refs_required_policies_and_repositoryPolicies.json, Line: 18, Column: 9. " +
				"Error: The reference [\"property permissions policy refs\"] does not exist in Authorization Definition files.",
			"File: classpath:consistency/repository_refs_and_policy_refs_required_policies_and_repositoryPolicies.json, Line: 1, Column: 1. " +
				"Error: Missing required property [policies].",
			"File: classpath:consistency/repository_refs_and_policy_refs_required_policies_and_repositoryPolicies.json, Line: 1, Column: 1. " +
				"Error: Missing required property [repositoryPolicies].");

		// Actual
		List<String> actualErrors = schemaValidator.validateAuthorizationFile(parentPath, null);

		Assertions.assertEquals(expectedErrors, actualErrors);
	}

	@Test
	public void validate_propertyPermission_required_propertyRights_errors() throws Exception {
		// Given
		String parentPath = PATH.formatted("propertyPermission_required_propertyRights.json");

		// Expected
		List<String> expectedErrors = List.of(
			"File: classpath:consistency/propertyPermission_required_propertyRights.json, Line: 6, Column: 23. " +
				"Error: The reference [\"rights 1\"] does not exist in Authorization Definition files.",
			"File: classpath:consistency/propertyPermission_required_propertyRights.json, Line: 1, Column: 1. " +
				"Error: Missing required property [propertyRights].");

		// Actual
		List<String> actualErrors = schemaValidator.validateAuthorizationFile(parentPath, null);

		Assertions.assertEquals(expectedErrors, actualErrors);
	}
}
