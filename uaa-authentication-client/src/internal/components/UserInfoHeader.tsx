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
import { JSX } from "react";
import { useDispatch, useSelector } from "react-redux";

import {
	Button,
	HeaderTrigger,
	HeaderTriggerProps,
	List,
	PopUpMenu,
	PopUpMenuProps
} from "@com.mgmtp.a12.widgets/widgets-core";

import * as UaaActions from "../actions.js";
import * as UaaSelectors from "../selectors.js";
import {
	isUaaOidcUser,
	isUaaUser,
	UaaExtendedUser,
	UaaOidcUser,
	UaaUser
} from "../interfaces/index.js";

/**
 * @param props
 */
export const UserInfoHeader = (
	props: UserInfoHeaderProps
): JSX.Element | null => {
	const dispatch = useDispatch();
	const user: UaaUser | UaaOidcUser | UaaExtendedUser = useSelector(
		UaaSelectors.user
	);
	const { Item } = List;

	if (user === undefined) {
		return <></>;
	}

	const userInfo = mapUserToLabels(user);
	return (
		<>
			{props.customUserInfoHeader ? (
				props.customUserInfoHeader
			) : (
				<PopUpMenu
					triggerElement={
						<HeaderTrigger
							{...props.additionalStyles?.headerTrigger}
							graphic="account_circle"
							text={!props.mobileMode ? userInfo.shortName : undefined}
							meta={!props.mobileMode ? "arrow_drop_down" : undefined}
							hideHiddenText
						/>
					}
					{...props.additionalStyles?.popUpMenu}
				>
					<List divider border>
						{props.additionalItems
							?.filter(item => item.orientation === "top")
							.map((item, index) => (
								<React.Fragment key={index}>{item.element}</React.Fragment>
							))}
						<Item
							text={userInfo.displayName}
							secondaryText={props.loggedInAsLabel}
							readonly
							flipped
						/>
						<Item
							text={
								<Button
									primary
									destructive
									onClick={() => dispatch(UaaActions.logoutRequested())}
								>
									{props.logoutButtonLabel}
								</Button>
							}
							readonly
						/>
						{props.additionalItems
							?.filter(item => item.orientation === "bottom")
							.map((item, index) => (
								<React.Fragment key={index}>{item.element}</React.Fragment>
							))}
					</List>
				</PopUpMenu>
			)}
		</>
	);
};

/**
 * @param user
 */
function mapUserToLabels(user?: UaaOidcUser | UaaUser | UaaExtendedUser): {
	shortName: string | undefined;
	userName: string | undefined;
	displayName: string | undefined;
} {
	if (user === undefined || isUaaOidcUser(user)) {
		return {
			shortName: (user as UaaOidcUser).profile.preferred_username,
			userName: (user as UaaOidcUser).profile.preferred_username,
			displayName: (user as UaaOidcUser).profile.preferred_username
		};
	}

	if (isUaaUser(user)) {
		return {
			shortName: (user as UaaUser).username,
			userName: (user as UaaUser).username,
			displayName: (user as UaaUser).displayName
		};
	}

	return {
		shortName: "???",
		userName: "???",
		displayName: "???"
	};
}

export interface UserInfoHeaderProps {
	readonly logoutButtonLabel?: string;
	readonly loggedInAsLabel?: string;
	readonly mobileMode?: boolean;
	readonly additionalItems?: {
		readonly orientation: "top" | "bottom";
		readonly element: React.ReactNode;
	}[];
	readonly additionalStyles?: {
		readonly popUpMenu?: PopUpMenuProps;
		readonly headerTrigger?: HeaderTriggerProps;
	};
	customUserInfoHeader?: React.ReactNode;
}
