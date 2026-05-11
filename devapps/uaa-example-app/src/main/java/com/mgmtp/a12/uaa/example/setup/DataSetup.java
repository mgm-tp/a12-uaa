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
package com.mgmtp.a12.uaa.example.setup;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import jakarta.inject.Inject;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mgmtp.a12.uaa.authorization.UAASecurityBypass;
import com.mgmtp.a12.uaa.example.entity.Company;
import com.mgmtp.a12.uaa.example.entity.Employee;
import com.mgmtp.a12.uaa.example.entity.Privilege;
import com.mgmtp.a12.uaa.example.entity.User;
import com.mgmtp.a12.uaa.example.repository.CompanyRepository;
import com.mgmtp.a12.uaa.example.repository.EmployeeRepository;
import com.mgmtp.a12.uaa.example.repository.PrivilegeRepository;
import com.mgmtp.a12.uaa.example.repository.UserRepository;

@Component
public class DataSetup {

	private static String[] listCompany = {
		"mgm partners ",
		"Vin-group ",
		"Pepsico ",
		"Mercedes-Benz ",
		"Toyota ",
		"Cocacola ",
		"Honda ",
		"Bose "
	};

	private static String[][] listCountry = {
		{ "US", "America" },
		{ "VN", "Vietnam" },
		{ "CZ", "Czech" },
		{ "FR", "France" },
		{ "DE", "Germany" },
		{ "IT", "Italy" },
	};

	@Inject
	private UserRepository userRepository;
	@Inject
	private CompanyRepository companyRepository;
	@Inject
	private EmployeeRepository employeeRepository;
	@Inject
	private PrivilegeRepository privilegeRepository;
	@Inject
	private PasswordEncoder encoder;
	@Inject
	private UAASecurityBypass securityBypass;

	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void init() {
		securityBypass.runWithSecurityBypass(() -> {
			initPrivileges();
			initCompanies();
			initUsers();
		});
	}

	private void initPrivileges() {
		Privilege privilege1 = new Privilege("Reviewer");
		privilegeRepository.save(privilege1);

		Privilege privilege2 = new Privilege("Manager");
		privilegeRepository.save(privilege2);

		Privilege privilegeAdmin = new Privilege("Admin");
		privilegeRepository.save(privilegeAdmin);

	}

	private User createUser(String userName, String password, String nationality, Privilege... privilege) {
		User user = new User();
		user.setUsername(userName);
		user.setPassword(encoder.encode(password));
		user.setNationality(nationality);
		user.setPrivileges(new HashSet<Privilege>(Arrays.asList(privilege)));
		return user;
	}

	private void initUsers() {
		User user1 = createUser("admin", "admin", "VN", privilegeRepository.findByName("Manager"), privilegeRepository.findByName("Admin"));
		User user2 = createUser("tom", "tom", "CZ", privilegeRepository.findByName("Reviewer"));
		userRepository.saveAll(Arrays.asList(user1, user2));
	}

	private Company generateRandomCompany(int countryIndex) {
		Random rand = new Random();
		int nameIndex = rand.nextInt(8);
		String[] countryInfo = countryIndex != -1 ? listCountry[countryIndex] : listCountry[rand.nextInt(6)];

		Company company = new Company();
		company.setName(listCompany[nameIndex] + countryInfo[1]);
		company.setCountryCode(countryInfo[0]);
		int ranNum = rand.nextInt(999999) + 100000;
		company.setTaxNumber(countryInfo[0] + ranNum);
		addEmployees(company);
		return company;
	}

	private void initCompanies() {

		// This fixed data for api testing purpose
		Company company1 = new Company();
		company1.setName("Fixed VN Company");
		company1.setCountryCode("VN");
		company1.setTaxNumber("VN123456");
		addEmployees(company1);
		companyRepository.save(company1);

		Company company2 = new Company();
		company2.setName("Fixed CZ Company");
		company2.setCountryCode("CZ");
		company2.setTaxNumber("CZ112233");
		addEmployees(company2);
		companyRepository.save(company2);

		int limit = 15;
		int VN = 1;
		int CZ = 2;
		int OTHER = -1;
		for (int i = 0; i < limit; i++) {
			companyRepository.save(generateRandomCompany(VN));
			companyRepository.save(generateRandomCompany(CZ));
			companyRepository.save(generateRandomCompany(OTHER));
		}
	}

	private void addEmployees(Company company) {
		Employee employee1 = new Employee();
		employee1.setFirstName("Peter");
		employee1.setLastName("From " + company.getName());
		employee1.setAge(30);

		Employee employee2 = new Employee();
		employee2.setFirstName("Alfons");
		employee2.setLastName("From " + company.getName());
		employee2.setAge(40);
		
		company.addEmplyee(employee1);
		company.addEmplyee(employee2);

	}
}
