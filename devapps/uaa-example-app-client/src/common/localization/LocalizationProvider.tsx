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
import { Container } from "@com.mgmtp.a12.widgets/widgets-core";
import {
  defaultDataFormats,
  defaultLocalizerFactory,
  defaultValueConversion,
  Locale,
} from "@com.mgmtp.a12.utils/utils-localization";
import { useSelector } from "react-redux";
import { LocalizerContext } from "@com.mgmtp.a12.utils/utils-localization-react";
import {
  A11YLanguageContext,
  getA11yResource,
} from "@com.mgmtp.a12.widgets/widgets-core";
import { localeJsonResource } from "../localization/resources.js";
import { Localization } from "../localization/localization.js";

export const LocalizationProvider: React.FC<Container> = ({ children }) => {
  const localeString = useSelector(Localization.selectLocale()) as unknown as string;
  const locale = Locale.fromString(localeString) as Locale;

  const localizer = defaultLocalizerFactory({
    locale,
    fallbackLocales: [Locale.fromString("en_US")],
    translationSource: localeJsonResource,
  });
  const dataFormats = defaultDataFormats(locale);
  const conversion = defaultValueConversion(dataFormats);

  return (
    <LocalizerContext.Provider
      value={{ locale, dataFormats, localizer, conversion }}
    >
      <A11YLanguageContext.Provider
        value={getA11yResource(locale.language || "en")}
      >
        {children}
      </A11YLanguageContext.Provider>
    </LocalizerContext.Provider>
  );
};
