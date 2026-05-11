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
import { useDispatch, useSelector } from "react-redux";

import { LocalizedModelText } from "@com.mgmtp.a12.utils/utils-localization/lib/main/index.js";
import { Select } from "@com.mgmtp.a12.widgets/widgets-core";
import { Localization } from "./localization.js";
import { LocalizerContext } from "@com.mgmtp.a12.utils/utils-localization-react/lib/main/index.js";
import React from "react";

const listSupportLocales: LocalizedModelText = [
	{
		locale: "de_DE",
		text: "Deutsch (Deutschland)"
	},
	{
		locale: "en_US",
		text: "English (USA)"
	},
	{
		locale: "vi_VN",
		text: "Vietnam"
	}
];
export const LocaleSelect = function () {
	const locale = useSelector((state: any) => state.appReducer.locale);
	const { localizer } = React.useContext(LocalizerContext);

	const dispatch = useDispatch();
	const label = localizer({ key: "label" });
	return (
		<Select
			label={label}
			value={locale}
			items={listSupportLocales.map(localized => ({
				label: localized.text,
				value: localized.locale
			}))}
			onValueChanged={value => {
				const option = listSupportLocales.find(
					({ locale }) => locale === value
				);
				if (option) {
					sessionStorage.setItem("locale", value);
					dispatch(Localization.setLocale(option.locale));
				}
			}}
		/>
	);
};
