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
import "../common/dev.config.js";
import "../common/ServiceWorker.js";
import "@com.mgmtp.a12.widgets/widgets-core/lib/theme/basic.css";

import { createRoot } from "react-dom/client";
import { Provider } from "react-redux";
import { uaaClientConfigure } from "../common/uaaConfiguration.js";
import appModel from "./appmodel.json" with { type: "json" };
import { Localization } from "../common/localization/localization.js";
import {
  logoutFailedSaga,
  withEditableRowTable,
  withFrameLayout,
  withLocalizationProvider,
  withLoginPage,
  withThemeProvider,
} from "./composables.js";
import { withReduxDevtool } from "../common/redux.js";
import {
  A12ApplicationConfig,
  addCustomSagas,
  combineFeatures,
  createA12ApplicationSetup,
  withModel,
  withReducerMap,
} from "@com.mgmtp.a12.client/client-core";
import { withPlatformModelLoader } from "@com.mgmtp.a12.client/client-core/modelLoader";
import { withUaa } from "@com.mgmtp.a12.uaa/uaa-authentication-a12-client";

const initialConfig: A12ApplicationConfig = {
  config: {},
  uaa: { configuration: uaaClientConfigure },
};

const { store, Component, initialActions } = createA12ApplicationSetup(
  combineFeatures(
    withModel(appModel),
    withReducerMap({ appReducer: Localization.localeReducer }),
    addCustomSagas(logoutFailedSaga),
    withPlatformModelLoader,

    withUaa,
    withFrameLayout,
    withLoginPage,

    combineFeatures(
      withThemeProvider,
      withLocalizationProvider,
      withReduxDevtool,
      withEditableRowTable,
    ),
  )(initialConfig),
);

initialActions().then(() => {
  const root = document.getElementById("root");

  if (root) {
    createRoot(root).render(<Provider store={store}>{Component}</Provider>);
  }
});
