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
import sinon, { SinonSandbox } from "sinon";
import { expect } from "chai";

import {
	AuthenticationType,
	TokenManagement,
	UaaClient
} from "../../src/index.js";

import {
	oidcConfiguration,
	serverURLCommon,
	uaaClientConfigurationWithoutOverride,
	uaaClientOidcMissingRequiredProperties,
	uaaClientOidcSelfConfigure
} from "./authenticating/appSetup.js";

describe("Uaa Client - API", function () {
	let sandbox: SinonSandbox;

	beforeEach(function () {
		sandbox = sinon.createSandbox();

		UaaClient["_selfConfigure"] = undefined;
	});

	afterEach(function () {
		sandbox.restore();
	});

	it("Configure just oidc self configuration", function () {
		UaaClient.init(uaaClientOidcSelfConfigure);
		expect(UaaClient.getOidcConfiguration().redirect_uri).to.deep.equal(
			oidcConfiguration.redirect_uri
		);
		expect(
			UaaClient.getOidcConfiguration().post_logout_redirect_uri
		).to.deep.equal(oidcConfiguration.post_logout_redirect_uri);
		expect(UaaClient.getOidcConfiguration().client_id).to.deep.equal(
			oidcConfiguration.client_id
		);
		expect(UaaClient.getOidcConfiguration().logoutIdp).to.deep.equal(
			oidcConfiguration.logoutIdp
		);
	});

	it("Configure just self configuration", function () {
		UaaClient.init(uaaClientConfigurationWithoutOverride);
		expect(UaaClient.getLocalConfiguration().serverURL).to.deep.equal(
			serverURLCommon
		);
		expect(UaaClient.getLocalConfiguration().loginRelativeUrl).to.deep.equal(
			"user/local/login"
		);
		expect(UaaClient.getLocalConfiguration().tokenType).to.deep.equal(
			"wrong type"
		);

		expect(UaaClient.getSamlConfiguration().serverURL).to.deep.equal(
			serverURLCommon
		);
		expect(UaaClient.getSamlConfiguration().loginRelativeUrl).to.deep.equal(
			"wrong login"
		);
		expect(UaaClient.getSamlConfiguration().logoutMethod).to.deep.equal("POST");

		expect(
			TokenManagement.getInstance().getTokenConfiguration(
				AuthenticationType.LOCAL
			)?.authorizationHeaderName
		).to.deep.equal("Authorization");
	});

	it("Configure oidc with just overrideClientConfigures", async function () {
		await UaaClient.init(uaaClientOidcMissingRequiredProperties).catch(
			error => {
				expect(error.message).to.match(
					new RegExp(
						"Please declare all required configurations of OIDC: authority, redirect_uri, client_id"
					)
				);
			}
		);
	});
});
