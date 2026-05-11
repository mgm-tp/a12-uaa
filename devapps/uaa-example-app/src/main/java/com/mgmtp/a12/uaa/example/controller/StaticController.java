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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StaticController {

	@GetMapping("/uri-based-authorization/forbidden")
	public String uriBasedAuthorizationForbidden() {
		return "If you can see me, there must be something wrong.";
	}

	@PostMapping("/uri-based-authorization/ok")
	public String uriBasedAuthorizationOk() {
		return "Successful";
	}

	@PreAuthorize("hasUAAPermission('Anonymous')")
	@GetMapping("/anonymousEndpoint")
	@ResponseBody
	public String anonymousEndpoint() {
		return "OK";
	}

	@GetMapping(value = "/image")
	public ResponseEntity<byte[]> getImage() throws IOException {
		ClassPathResource imageFile = new ClassPathResource("/images/background.jpg");
		byte[] imageBytes = Files.readAllBytes(Path.of(imageFile.getURI()));
		return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageBytes);
	}
}
