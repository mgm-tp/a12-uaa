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
package com.mgmtp.a12.uaa.authorization.integration.documentation;

import java.util.Arrays;
import java.util.Collections;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionService;
import com.mgmtp.a12.uaa.authorization.AuthorizationService;
import com.mgmtp.a12.uaa.authorization.integration.AbstractIntegrationTest;
import com.mgmtp.a12.uaa.authorization.internal.RuntimeAuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.model.Permission;
import com.mgmtp.a12.uaa.authorization.model.PropertyPermission;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyRightsValidator;
import com.mgmtp.a12.uaa.authorization.property.internal.UAADataMasking;
import com.mgmtp.a12.uaa.authorization.security.DataMasking;
import com.mgmtp.a12.uaa.authorization.security.PermissionCheckResult;
import com.mgmtp.a12.uaa.authorization.security.PolicyProcessorFactory;
import com.mgmtp.a12.uaa.authorization.security.PropertyChangesChecker;
import com.mgmtp.a12.uaa.authorization.security.spel.internal.SpelPolicyProcessorFactory;

@ExtendWith(SpringExtension.class)
public class DocumentationAuthorizationDefinitionServiceTest extends AbstractIntegrationTest {

	@Inject
	private AuthorizationService authorizationService;

	@WithUserDetails("docAdmin")
	@Test
	public void checkRestEndpointSaveAdmin() {
		PermissionCheckResult<Permission> savePermissions = authorizationService.checkPermissions("CompanyController.saveCompany", "rest endpoint");
		Assertions.assertTrue(savePermissions.isPassed());
		Assertions.assertEquals(1, savePermissions.getPassedPermissions().size());
		Assertions.assertEquals("allow access rest endpoint", savePermissions.getPassedPermissions().get(0).getName());
	}

	@WithUserDetails("docAdmin")
	@Test
	public void checkRestEndpointLoadAdmin() {
		PermissionCheckResult<Permission> loadPermissions = authorizationService.checkPermissions("CompanyController.loadCompany", "rest endpoint");
		Assertions.assertTrue(loadPermissions.isPassed());
		Assertions.assertEquals(1, loadPermissions.getPassedPermissions().size());
		Assertions.assertEquals("allow access rest endpoint", loadPermissions.getPassedPermissions().get(0).getName());
	}

	@WithUserDetails("docGuest")
	@Test
	public void checkRestEndpointSaveGuest() {
		PermissionCheckResult<Permission> savePermissions = authorizationService.checkPermissions("CompanyController.saveCompany", "rest endpoint");
		Assertions.assertFalse(savePermissions.isPassed());

	}

	@WithUserDetails("docGuest")
	@Test
	public void checkRestEndpointLoadGuest() {
		PermissionCheckResult<Permission> loadPermissions = authorizationService.checkPermissions("CompanyController.loadCompany", "rest endpoint");
		Assertions.assertTrue(loadPermissions.isPassed());
		Assertions.assertEquals(1, loadPermissions.getPassedPermissions().size());
		Assertions.assertEquals("allow access rest endpoint", loadPermissions.getPassedPermissions().get(0).getName());
	}

	@WithUserDetails("docAnonymous")
	@Test
	public void checkRestEndpointLoadAnonymous() {
		PermissionCheckResult<Permission> loadPermissions = authorizationService.checkPermissions("CompanyController.loadCompany", "rest endpoint");
		Assertions.assertFalse(loadPermissions.isPassed());
		Assertions.assertEquals(0, loadPermissions.getPassedPermissions().size());
	}

	@WithUserDetails("docAdmin")
	@Test
	public void checkServiceLoadCompanyAdmin() {
		Country country = new Country("AN", "AN Country");
		Company company = new Company(1L, "Angel company", "valid", country);
		PermissionCheckResult<Permission> loadPermissions = authorizationService.checkPermissions(company, "load company");
		Assertions.assertTrue(loadPermissions.isPassed());
	}

	@WithUserDetails("docAdmin")
	@Test
	public void checkServiceLoadCompanyAdminWithPropertyAuth() {
		Country country = new Country("AN", "AN Country");
		Company company = new Company(1L, "Angel company", "valid", country);
		company
			.addOffice(new Country("DE", "DE Country"))
			.addOffice(new Country("CZ", "CZ Country"));

		PermissionCheckResult<PropertyPermission> checkResult = authorizationService.checkPropertyPermissionsAndMaskData(company);

		Assertions.assertTrue(checkResult.isPassed());
		Assertions.assertNotNull(company.getId());
		Assertions.assertNotNull(company.getName());
		Assertions.assertNotNull(company.getCountry().getCode());
		Assertions.assertNotNull(company.getTaxNumber());
		Assertions.assertNotNull(company.getOffices());
		Assertions.assertNotNull(company.getOffices().get(0).getCode());
		Assertions.assertNotNull(company.getOffices().get(0).getName());
		Assertions.assertNotNull(company.getOffices().get(1).getCode());
		Assertions.assertNotNull(company.getOffices().get(1).getName());

	}

	@WithUserDetails("docGuest")
	@Test
	public void checkServiceLoadCompanyGuestWithValidCountry() {
		Country country = new Country("HE", "HE Country");
		Company company = new Company(2L, "Hell company", "valid", country);
		PermissionCheckResult<Permission> loadPermissions = authorizationService.checkPermissions(company, "load company");
		Assertions.assertTrue(loadPermissions.isPassed());

	}

	@WithUserDetails("docGuest")
	@Test
	public void checkPropertyPermissionCompanyGuestWithValidCountry() {
		Country country = new Country("HE", "HE Country");
		Company company = new Company(2L, "Hell company", "valid", country);
		company
		.addOffice(new Country("DE", "DE Country"))
		.addOffice(new Country("CZ", "CZ Country"));

		PermissionCheckResult<PropertyPermission> checkResult = authorizationService.checkPropertyPermissionsAndMaskData(company);
		Assertions.assertTrue(checkResult.isPassed());
		Assertions.assertEquals(1, checkResult.getPassedPermissions().size());
		Assertions.assertEquals(1, checkResult.getFailedPermissions().size());

		Assertions.assertNotNull(company.getId());
		Assertions.assertNotNull(company.getName());
		Assertions.assertNotNull(company.getTaxNumber());
		Assertions.assertEquals("masked:[val...]", company.getTaxNumber());
		Assertions.assertNotNull(company.getCountry());
		Assertions.assertNotNull(company.getCountry().getName());
		Assertions.assertNotNull(company.getCountry().getCode());
		Assertions.assertEquals("Access Denied", company.getCountry().getCode());
		Assertions.assertNotNull(company.getOffices());
		Assertions.assertNotNull(company.getOffices().get(0).getCode());
		Assertions.assertNull(company.getOffices().get(0).getName());
		Assertions.assertNotNull(company.getOffices().get(1).getCode());
		Assertions.assertNull(company.getOffices().get(1).getName());
		
	}

	@WithUserDetails("docGuest")
	@Test
	public void checkServiceLoadCompanyGuestWithInvalidCountry() {
		Country country = new Country("XX", "XX Country");
		Company company = new Company(1L, "Angel company", "valid", country);
		PermissionCheckResult<Permission> loadPermissions = authorizationService.checkPermissions(company, "load company");
		Assertions.assertFalse(loadPermissions.isPassed());
	}

	@WithUserDetails("docAdmin")
	@Test
	public void checkServiceSaveCompanyAdminWithValidTaxnumberAndCountry() {
		Country country = new Country("AN", "AN Country");
		Company company = new Company(1L, "Angel company", "valid", country);
		PermissionCheckResult<Permission> loadPermissions = authorizationService.checkPermissions(company, "save company");
		Assertions.assertTrue(loadPermissions.isPassed());
	}

	@WithUserDetails("docGuest")
	@Test
	public void checkServiceSaveCompanyGuestWithValidTaxnumberAndCountry() {
		Country country = new Country("HE", "HE Country");
		Company company = new Company(1L, "Hell company", "valid", country);
		PermissionCheckResult<Permission> loadPermissions = authorizationService.checkPermissions(company, "save company");
		Assertions.assertTrue(loadPermissions.isPassed());
	}

	@WithUserDetails("docGuest")
	@Test
	public void checkServiceSaveCompanyGuestWithValidTaxnumberAndCountryAndPropertyAuth() {
		Country country = new Country("HE", "HE Country");
		Company companyPersisted = new Company(1L, "Hell company", "valid", country);
		Company companyUpdated = new Company(1L, "Hell company updated", "valid", country);
		Boolean checkPropertyPermissionsForChanges = authorizationService.checkPropertyPermissionsForChanges(companyPersisted, companyUpdated);
		Assertions.assertFalse(checkPropertyPermissionsForChanges);
	}
	
	@WithUserDetails("docGuest")
	@Test
	public void checkServiceSaveCompanyGuestWithValidTaxnumberAndCountryNameAndPropertyAuth() {
		Country country = new Country("HE", "HE Country");
		Company companyPersisted = new Company(1L, "Hell company", "valid", country);
		Country countryUpdated = new Country("HE", "HE Country Updated");
		Company companyUpdated = new Company(1L, "Hell company", "valid", countryUpdated);
		Boolean checkPropertyPermissionsForChanges = authorizationService.checkPropertyPermissionsForChanges(companyPersisted, companyUpdated);
		Assertions.assertTrue(checkPropertyPermissionsForChanges);
	}
	
	@WithUserDetails("docGuest")
	@Test
	public void checkServiceSaveCompanyGuestWithValidTaxnumberAndCountryNameAndPropertyAuthFail() {
		Country country = new Country("HE", "HE Country");
		Company companyPersisted = new Company(1L, "Hell company", "valid", country);
		Country countryUpdated = new Country("HE Updated", "HE Country Updated");
		Company companyUpdated = new Company(1L, "Hell company", "valid", countryUpdated);
		Boolean checkPropertyPermissionsForChanges = authorizationService.checkPropertyPermissionsForChanges(companyPersisted, companyUpdated);
		Assertions.assertFalse(checkPropertyPermissionsForChanges);
	}

	@WithUserDetails("docAdmin")
	@Test
	public void checkServiceSaveCompanyAdminWithInvalidTaxnumber() {
		Country country = new Country("AN", "AN Country");
		Company company = new Company(1L, "Angel company", "invalid", country);
		PermissionCheckResult<Permission> loadPermissions = authorizationService.checkPermissions(company, "save company");
		Assertions.assertTrue(loadPermissions.isPassed());
	}

	@WithUserDetails("docGuest")
	@Test
	public void checkServiceSaveCompanyGuestWithInvalidTaxnumber() {
		Country country = new Country("HE", "HE Country");
		Company company = new Company(1L, "Hell company", "crappy", country);
		PermissionCheckResult<Permission> loadPermissions = authorizationService.checkPermissions(company, "save company");
		Assertions.assertFalse(loadPermissions.isPassed());
	}

	@WithUserDetails("docAdmin")
	@Test
	public void checkServiceSaveCompanyAdminWithInvalidTaxnumberAndCountry() {
		Country country = new Country("XX", "XX Country");
		Company company = new Company(1L, "Angel company", "crappy", country);
		PermissionCheckResult<Permission> loadPermissions = authorizationService.checkPermissions(company, "save company");
		Assertions.assertTrue(loadPermissions.isPassed());
	}

	@WithUserDetails("docGuest")
	@Test
	public void checkServiceSaveCompanyGuestWithInvalidTaxnumberAndCountry() {
		Country country = new Country("XX", "XX Country");
		Company company = new Company(1L, "Hell company", "crappy", country);
		PermissionCheckResult<Permission> loadPermissions = authorizationService.checkPermissions(company, "save company");
		Assertions.assertFalse(loadPermissions.isPassed());
	}

	public static class TaxService {

		public boolean isValid(String taxNumber) {
			return "valid".equals(taxNumber);
		}
	}

	public static class CountryRepository {

		public Country findByCountryCode(String countryCode) {
			if ("AN".equals(countryCode)) {
				return new Country("AN", "Angel");
			} else if ("HE".equals(countryCode)) {
				return new Country("HE", "Hell");
			}
			return null;
		}

	}

	@Configuration
	static class TestConfig {
		@Bean
		public AuthorizationDefinitionRepository createAuthorizationDefinitionRepository() {
			return new RuntimeAuthorizationDefinitionRepository();
		}

		@Bean
		public AuthorizationService creatAuthorizationService() {
			return new AuthorizationService();
		}

		@Bean
		public AuthorizationDefinitionService crAuthorizationDefinitionService() {
			return new AuthorizationDefinitionService("classpath:documentationAuthorizationDefinition.json", Collections.emptyList());
		}

		@Bean
		public UserDetailsService createUserDetailsService() {
			return new DocumentationTestUserDetailsService();
		}

		@Bean
		public PolicyProcessorFactory spelFactory() {
			return new SpelPolicyProcessorFactory();
		}

		@Bean
		public TaxService taxService() {
			return new TaxService();
		}

		@Bean
		public CountryRepository countryRepository() {
			return new CountryRepository();
		}

		@Bean
		public PropertyRightsValidator createPropertyPermissionValidator() {
			return new PropertyRightsValidator();
		}
		
		@Bean
		public DataMasking createDataMasking() {
			return new UAADataMasking(createAuthorizationDefinitionRepository());
		}
		
		@Bean
		public PropertyChangesChecker createPropertyChangesChecker() {
			return new PropertyChangesChecker(Arrays.asList("com.mgmtp"));
		}

	}
}
