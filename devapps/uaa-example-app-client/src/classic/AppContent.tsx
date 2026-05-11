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
import * as React from "react";
import {
  ApplicationFrame,
  ApplicationHeader,
} from "@com.mgmtp.a12.widgets/widgets-core";
import { provider as DeviceDetector } from "@com.mgmtp.a12.widgets/widgets-core/lib/common/index.js";
import {
  addPrefix,
  joinClassNames,
} from "@com.mgmtp.a12.widgets/widgets-core/lib/common/main/utils.js";
import { UserInfoHeader } from "@com.mgmtp.a12.uaa/uaa-authentication-client";
import { LocalizerContext } from "@com.mgmtp.a12.utils/utils-localization-react/lib/main/index.js";
import { currentVersion } from "../common/version.js";
import { EditableRowTable } from "../common/components/EditableRowTable.js";
import { ImageComponent } from "../common/components/ImageComponent.js";

export const AppContent = () => {
  const { localizer } = React.useContext(LocalizerContext);
  const [subExpanded, setSubExpanded] = React.useState(() => {
    const stored = localStorage.getItem("subExpanded");
    return stored ? JSON.parse(stored) : DeviceDetector.get() === "desktop";
  });

  const localize = (key: string) => localizer({ key });

  const handleExpansionChange = React.useCallback(
    (expanded?: boolean) => {
      if (subExpanded !== expanded) {
        setSubExpanded(!!expanded);
        localStorage.setItem("subExpanded", JSON.stringify(!!expanded));
      }
    },
    [subExpanded],
  );

  const phone = DeviceDetector.get() === "phone";

  return (
    <ApplicationFrame
      className={joinClassNames({
        "-sc-wrapper": DeviceDetector.get() === "phone",
      })}
      main={
        <ApplicationHeader
          leftSlots={[
            <span key="title">
              {localize("headerLabel")} - {localize("versionLabel")}{" "}
              {currentVersion}
            </span>,
            <ImageComponent key="image" />,
          ]}
          rightSlots={[
            <UserInfoHeader
              key="user-info"
              mobileMode={phone}
              loggedInAsLabel={localize("loggedInAsLabel") || "Sign In as"}
              logoutButtonLabel={localize("logoutButtonLabel") || "Sign Out"}
              additionalStyles={{
                popUpMenu: {
                  triggerButtonTitle: "Open menu",
                },
              }}
            />,
          ]}
          className={addPrefix("-u-width-full")}
        />
      }
      disableCollapsingSub={DeviceDetector.get() === "desktop"}
      subExpanded={subExpanded}
      content={<EditableRowTable />}
      onExpansionChange={handleExpansionChange}
    />
  );
};
