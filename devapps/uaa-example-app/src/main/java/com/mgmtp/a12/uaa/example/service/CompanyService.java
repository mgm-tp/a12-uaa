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
package com.mgmtp.a12.uaa.example.service;

import java.util.List;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.mgmtp.a12.uaa.example.dto.CompanyDataExt;
import com.mgmtp.a12.uaa.example.dto.CompanyDataLoaderContext;
import com.mgmtp.a12.uaa.example.entity.Company;
import com.mgmtp.a12.uaa.example.repository.CompanyRepository;

@Service
public class CompanyService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompanyService.class);

	@Inject
	private CompanyRepository companyRepository;

	@Inject
	private ExternalCompanyService externalCompanyService;
	@Inject
	private CompanyPersister companyPersister;

	@PostFilter("hasUAAPropertyPermission(filterObject)")
	public List<Company> loadAllCompanies() {
		return companyRepository.findAll();
	}

	@PostAuthorize("hasUAAPropertyPermission(null, returnObject)")
	public Company saveCompany(Company company) {
		return company;
	}

	/**
	 * This method used to simulate the #resource
	 * authorization json file line 133 and 147
	 * @param company
	 */
	public void updateCompanyFromExternalSource(Company company) {
		Company currentCompany;
		if (company.getId() == null) {
			currentCompany = companyRepository.findByName(company.getName());
		} else {
			currentCompany = companyRepository.findById(company.getId()).get();
		}

		if (currentCompany == null) {
			LOGGER.info("Can not find any company following id: [] or name: []", company.getId(), company.getName());
			return;
		}

		CompanyDataLoaderContext dataLoaderContext = new CompanyDataLoaderContext();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		dataLoaderContext.setUserName(authentication.getName());
		dataLoaderContext.setCountryCode(company.getCountryCode());

		CompanyDataExt externalCountryData = externalCompanyService.loadExternalCountryData(dataLoaderContext, company);
		company.setName(externalCountryData.getName());
		company.setTaxNumber(externalCountryData.getTaxNumber());
		company.setCountryCode(externalCountryData.getCountryCode());
		company.setId(currentCompany.getId());

		companyPersister.updateCompany(currentCompany, company);
	}
}
