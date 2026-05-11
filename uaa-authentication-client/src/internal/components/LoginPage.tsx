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
import { useContext, useRef, useState } from "react";
import { useSelector } from "react-redux";

import { LoginLayout } from "@com.mgmtp.a12.widgets/widgets-core/lib/layout/login-layout/main/login-layout.view.js";
import { MessageBox } from "@com.mgmtp.a12.widgets/widgets-core/lib/message-box/main/message-box.view.js";
import { Icon } from "@com.mgmtp.a12.widgets/widgets-core/lib/icon/main/icon.view.js";
import { TextLineStateless } from "@com.mgmtp.a12.widgets/widgets-core/lib/input/text-line/main/template/text-line.tpl.view.js";
import { Button } from "@com.mgmtp.a12.widgets/widgets-core/lib/button/main/button.view.js";
import { provider as DeviceDetector } from "@com.mgmtp.a12.widgets/widgets-core/lib/common/main/device-detector.js";
import {
	localizableFromLocalizationTreeMap,
	LocalizationTreeMap
} from "@com.mgmtp.a12.utils/utils-localization/lib/main/index.js";
import { LocalizerContext } from "@com.mgmtp.a12.utils/utils-localization-react/lib/main/index.js";

import {
	UaaActions,
	UaaLdapClient,
	UaaLocalClient,
	UaaSelectors
} from "../../index.js";

import { AUTH_KEYS } from "../locale/index.js";
import { en_US } from "../locale/internal/en_US.js";
import { de_DE } from "../locale/internal/de_DE.js";
import { reduxStore } from "../utils/index.js";

interface LoginFieldState {
	readonly value: string;
	readonly errorMessageKey?: string;
}

interface StateType {
	readonly username: LoginFieldState;
	readonly password: LoginFieldState;
}

/**
 *
 * @param state
 */
function validate(state: StateType): StateType {
	return [validateUsername, validatePassword].reduce(
		(newState, validator) => validator(newState),
		state
	);
}

/**
 *
 * @param root0
 * @param root0.username
 * @param root0.username.value
 */
function validateUsername({
	username: { value },
	...remaining
}: StateType): StateType {
	return {
		...remaining,
		username: {
			value,
			errorMessageKey: isEmpty(value)
				? AUTH_KEYS.auth.form.username.error
				: undefined
		}
	};
}

/**
 *
 * @param root0
 * @param root0.password
 * @param root0.password.value
 */
function validatePassword({
	password: { value },
	...remaining
}: StateType): StateType {
	return {
		...remaining,
		password: {
			value,
			errorMessageKey: isEmpty(value)
				? AUTH_KEYS.auth.form.password.error
				: undefined
		}
	};
}

/**
 *
 * @param text
 */
function isEmpty(text?: string): boolean {
	return text === undefined || text.length === 0;
}

export interface LoginPageProps {
	readonly logoURL?: string;
	readonly imageURL?: string;
	readonly uaaClient?: UaaLocalClient | UaaLdapClient;
	readonly additionalFormItems?: React.ReactNode[];
	readonly additionalFooterItems?: React.ReactNode;
}

type InputChangeHandler = (
	event:
		| React.KeyboardEvent<HTMLInputElement>
		| React.FocusEvent<HTMLInputElement>
		| React.ChangeEvent<HTMLInputElement>
) => void;

/**
 *
 * @param event
 */
function isBlurEvent(event: React.SyntheticEvent): event is React.FocusEvent {
	return event.type === "blur";
}

/**
 *
 * @param event
 */
function isChangeEvent(
	event: React.SyntheticEvent
): event is React.ChangeEvent {
	return event.type === "change";
}

/**
 *
 * @param event
 */
function isKeyDownEvent(
	event: React.SyntheticEvent
): event is React.KeyboardEvent {
	return event.type === "keydown";
}

const DEFAULT_TRANSLATIONS: LocalizationTreeMap = {
	en: en_US,
	de: de_DE
};

/**
 *
 * @param loginPageProps
 */
export const LoginPage = (loginPageProps: LoginPageProps) => {
	const usernameInputRef = useRef<HTMLInputElement | null>(null);
	const passwordInputRef = useRef<HTMLInputElement | null>(null);
	const [credentialState, setCredentialState] = useState<StateType>({
		username: { value: "" },
		password: { value: "" }
	});

	const localizer = useContext(LocalizerContext).localizer;
	const localize = (key: string | undefined): string | undefined => {
		if (key) {
			return localizer(
				localizableFromLocalizationTreeMap(key, DEFAULT_TRANSLATIONS)
			);
		}
		return undefined;
	};

	const headLine = localize(AUTH_KEYS.auth.form.title);
	const authenticationErrorKey: string | undefined = useSelector(
		UaaSelectors.error
	);

	const authenticationError = localize(authenticationErrorKey);

	const handleUsernameChange: InputChangeHandler = event => {
		if (isKeyDownEvent(event) && event.keyCode === 13) {
			submitForm();
		} else if (event.currentTarget.value === "") {
			setCredentialState({
				...credentialState,
				username: { ...credentialState.username, value: "" }
			});
		} else if (isBlurEvent(event) || isChangeEvent(event)) {
			setCredentialState(
				validateUsername({
					...credentialState,
					username: {
						...credentialState.username,
						value: event.currentTarget.value
					}
				})
			);
		}
	};

	const submitForm = () => {
		const validatedCredential = validate(credentialState);
		setCredentialState(validatedCredential);
		callback(validatedCredential);
	};

	const handlePasswordChange: InputChangeHandler = event => {
		if (isKeyDownEvent(event) && event.keyCode === 13) {
			submitForm();
		} else if (event.currentTarget.value === "") {
			setCredentialState({
				...credentialState,
				password: { ...credentialState.password, value: "" }
			});
		} else if (isBlurEvent(event) || isChangeEvent(event)) {
			setCredentialState(
				validatePassword({
					...credentialState,
					password: {
						...credentialState.password,
						value: event.currentTarget.value
					}
				})
			);
		}
	};

	const callback = (validatedCredential: StateType) => {
		if (
			validatedCredential.username.errorMessageKey ===
			AUTH_KEYS.auth.form.username.error
		) {
			usernameInputRef?.current?.focus();
			return;
		}

		if (
			validatedCredential.password.errorMessageKey ===
			AUTH_KEYS.auth.form.password.error
		) {
			passwordInputRef?.current?.focus();
			return;
		}

		if (document.activeElement instanceof HTMLElement) {
			document.activeElement.blur();
		}

		if (loginPageProps.uaaClient) {
			loginPageProps.uaaClient.login(
				validatedCredential.username.value,
				validatedCredential.password.value
			);
		} else {
			reduxStore.dispatch(
				UaaActions.loggingInLocal({
					username: validatedCredential.username.value,
					password: validatedCredential.password.value
				})
			);
		}
	};

	return (
		<LoginLayout
			fullscreen
			mobile={DeviceDetector.get() === "phone"}
			backgroundImage={loginPageProps.imageURL}
			noGutter={false}
		>
			{loginPageProps.logoURL && (
				<LoginLayout.Logo>
					<img src={loginPageProps.logoURL} />
				</LoginLayout.Logo>
			)}
			{headLine && <LoginLayout.Headline>{headLine}</LoginLayout.Headline>}
			<LoginLayout.Form>
				{authenticationError && (
					<LoginLayout.FormItem key="error">
						<MessageBox
							icon={<Icon iconTheme="custom">error</Icon>}
							label={authenticationError}
							focusOnMessage={true}
						/>
					</LoginLayout.FormItem>
				)}
				<LoginLayout.FormItem key="username">
					<TextLineStateless
						id="username"
						inputRef={ref => {
							usernameInputRef.current = ref;
						}}
						label={localize(AUTH_KEYS.auth.form.username.label)}
						value={credentialState.username?.value}
						error={authenticationError !== undefined}
						errorMessage={localize(credentialState.username?.errorMessageKey)}
						onKeyDown={handleUsernameChange}
						onChange={handleUsernameChange}
						onBlur={handleUsernameChange}
						autoFocus
					/>
				</LoginLayout.FormItem>
				<LoginLayout.FormItem key="password">
					<TextLineStateless
						id="password"
						inputRef={ref => {
							passwordInputRef.current = ref;
						}}
						label={localize(AUTH_KEYS.auth.form.password.label)}
						value={credentialState.password?.value}
						error={authenticationError !== undefined}
						errorMessage={localize(credentialState.password?.errorMessageKey)}
						onKeyDown={handlePasswordChange}
						onChange={handlePasswordChange}
						onBlur={handlePasswordChange}
						inputProps={{ type: "password" }}
					/>
				</LoginLayout.FormItem>
				{loginPageProps.additionalFormItems &&
					React.Children.map(loginPageProps.additionalFormItems, (x, i) => (
						<LoginLayout.FormItem key={`child-${i}`}>{x}</LoginLayout.FormItem>
					))}
			</LoginLayout.Form>
			<LoginLayout.Footer>
				<Button primary block onClick={submitForm}>
					{localize(AUTH_KEYS.auth.form.submit.label)}
				</Button>
				{loginPageProps.additionalFooterItems
					? loginPageProps.additionalFooterItems
					: undefined}
			</LoginLayout.Footer>
		</LoginLayout>
	);
};
