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
package com.mgmtp.a12.uaa.example.controller;

import java.util.List;

import jakarta.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mgmtp.a12.uaa.example.entity.Company;
import com.mgmtp.a12.uaa.example.service.CompanyService;

@RestController
public class CompanyController {

	@Inject
	private CompanyService companyService;

	@PreAuthorize("hasUAAPermission('Load company')")
	@GetMapping("/loadAllCompanies")
	@ResponseBody
	public List<Company> loadAllCompanies() {
		return companyService.loadAllCompanies();
	}

	@PreAuthorize("hasUAAPermission('Company save')")
	@PostMapping("/saveCompany")
	@ResponseBody
	public String saveCompany(@RequestBody Company company) {
		companyService.saveCompany(company);
		return "CREATED";
	}

	@PreAuthorize("hasUAAPermission('Company update')")
	@PostMapping("/updateCompany")
	@ResponseBody
	public String updateCompany(@RequestBody Company company) {
		companyService.updateCompanyFromExternalSource(company);
		return "UPDATED";
	}
}
