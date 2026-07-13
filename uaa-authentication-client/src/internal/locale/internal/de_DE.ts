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
import { LocalizationTree } from "@com.mgmtp.a12.utils/utils-localization";

export const de_DE: LocalizationTree = {
	locale: {
		locale: "de_DE",
		text: "Deutsch (Deutschland)"
	},
	auth: {
		error: {
			authenticationfailed:
				"Authentifizierung ist fehlgeschlagen! Evtl. falsche Benutzer und/oder Passwort-Eingabe?",
			logoutfailed: "Abmeldung fehlgeschlagen!"
		},
		form: {
			title: "Login",
			username: {
				label: "Benutzername",
				error: "Geben Sie bitte den Benutzernamen an"
			},
			password: {
				label: "Passwort",
				error: "Geben Sie bitte das Passwort an",
				reset: {
					label: "Passwort vergesse"
				}
			},
			submit: {
				label: "Anmelden"
			}
		}
	}
};
