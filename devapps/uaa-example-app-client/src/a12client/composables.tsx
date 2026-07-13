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
import { useSelector } from "react-redux";

import {
  type A12ApplicationConfig,
  addLayout,
  addWrapper,
  modifyLayout,
} from "@com.mgmtp.a12.client/client-core";
import { FrameViews } from "@com.mgmtp.a12.client/client-core";
import {
  AuthenticationState,
  UaaActions,
  UaaSelectors,
  UserInfoHeader,
} from "@com.mgmtp.a12.uaa/uaa-authentication-client";
import { ProgressIndicator } from "@com.mgmtp.a12.widgets/widgets-core";
import { ThemeContextProvider } from "../common/components/ThemeContextProvider.js";
import { EditableRowTable } from "../common/components/EditableRowTable.js";
import { SampleLoginPage } from "../common/components/SampleLoginPage.js";
import { LocalizationProvider } from "../common/localization/LocalizationProvider.js";
import { HeaderLabel } from "../common/components/HeaderLabel.js";
import { ImageComponent } from "../common/components/ImageComponent.js";
import { put, takeEvery } from "typed-redux-saga";

export const withThemeProvider = <T extends A12ApplicationConfig>(cfg: T) =>
  addWrapper<T>(ThemeContextProvider, "outer")(cfg);

export const withLocalizationProvider = <T extends A12ApplicationConfig>(
  cfg: T,
) => addWrapper<T>(LocalizationProvider, "outer")(cfg);

export const withLoginPage = <T extends A12ApplicationConfig>(cfg: T) =>
  addWrapper<T>(({ children }) => {
    const authState = useSelector(UaaSelectors.state);

    if (authState === AuthenticationState.NOT_AUTHENTICATED) {
      return <SampleLoginPage />;
    }

    if (authState === AuthenticationState.AUTHENTICATING) {
      return <ProgressIndicator label="Logging In..." id="loading-medium" />;
    }

    return <>{children}</>;
  }, "inner")(cfg);

export const withEditableRowTable = <T extends A12ApplicationConfig>(cfg: T) =>
  addLayout<T>("ContentLayout", { component: () => <EditableRowTable /> })(cfg);

export const withFrameLayout = <T extends A12ApplicationConfig>(cfg: T) =>
  modifyLayout<T>("ApplicationFrame", (layout) => {
    return {
      ...layout,
      component: (props: FrameViews.LayoutProps) => (
        <FrameViews.ApplicationFrameLayout
          {...props}
          additionalHeaderItems={[
            {
              orientation: "leftSlots-right",
              item: <HeaderLabel key="header-label" />,
            },
            {
              orientation: "leftSlots-right",
              item: <ImageComponent key="image" />,
            },
            {
              orientation: "rightSlots-left",
              item: (
                <UserInfoHeader
                  key="user-info"
                  logoutButtonLabel="Sign Out"
                  loggedInAsLabel="Signed in as"
                />
              ),
            },
          ]}
        />
      ),
    };
  })(cfg);

/**
 * Safety net: if the server-side logout fails, the internal UAA saga dispatches
 * logoutFailed instead of loggedOut. The A12 reset mechanism waits for loggedOut
 * to complete the reset. This saga ensures loggedOut is always dispatched so the
 * app doesn't freeze.
 */
export function* logoutFailedSaga() {
  yield* takeEvery(UaaActions.logoutFailed, function* () {
    yield* put(UaaActions.loggedOut());
  });
}
