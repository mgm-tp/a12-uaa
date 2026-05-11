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
package com.mgmtp.a12.uaa.authorization;

import java.lang.reflect.Field;
import java.util.Collections;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.ReflectionUtils;

import com.mgmtp.a12.uaa.authorization.internal.UAADelegatedUserDetail;
import com.mgmtp.a12.uaa.authorization.internal.UAAUserDetails;
import com.mgmtp.a12.uaa.authorization.security.spel.internal.UAAPolicyDecisionPoint;

/**
 * This class allows you to execute piece of code without SECURITY. If you need such functionality then you just inject the
 * call into your's implementation and implement the {@link SecurityFreeCallback} interface.
 * This option is only available during application start-up because it is using static property.
 * The bypass is disabled after spring context is started. The bypass is registered on the {@link ContextRefreshedEvent} with lowest precedence.
 * After the event is processed then there is no way to run any code with security bypass.
 * You can use the {@link UAASecurityBypass} in following situations.
 * <ul>
 * <li>Inside event listener for {@link ContextRefreshedEvent}</li>
 * <li>Inside method with annotation {@link PostConstruct}</li>
 * </ul>
 * In case that you are not able to wrap the code into {@link SecurityFreeCallback}
 * then you can use configuration property mgm.authorzation.disableSecurityOnStartUp=true. This will disable complete
 * security during whole start-up for all calls. No need to use the {@link UAASecurityBypass} at all.
 * <p>
 * NOTE: use this method carefully because you might compromise you application security during star-up!! 
 * <p>
 * NOTE 2: Before using this class think if you really need it and if there is no other way to do it with properly secured API calls.
 */
public class UAASecurityBypass {

	@Inject
	ApplicationContext applicationContext;

	private static final Logger LOGGER = LoggerFactory.getLogger(UAASecurityBypass.class);

	private static final String PROPERTY_SECURITY_BYPASS = "bypassPermissions";

	private boolean bypassDisabled = false;

	public UAASecurityBypass(boolean enabledOnStartUp) {
		if (enabledOnStartUp) {
			preparePrivilegedSecurityContext();
			modifySecurityDisabledFlag(true);
		}
	}

	@EventListener
	@Order
	void disableBypass(ContextRefreshedEvent event) {
		if (applicationContext.equals(event.getApplicationContext())) {
			LOGGER.info("Security bypass is disabled after application start-up");
			bypassDisabled = true;
			modifySecurityDisabledFlag(false);
			SecurityContextHolder.clearContext();
		}
	}

	/**
	 * Provides access to state of SecurityBypass
	 *
	 * @return true if the services security is currently disabled, false otherwise
	 */
	public Boolean isSecurityBypassRunning() {
		try {
			return getSecurityDisabledField().getBoolean(null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException("Unable to get status of security bypass", e);
		}
	}

	/**
	 * Method switches off security GLOBALLY, executes callback and enables security GLOBALLY. This method can be used
	 * only during application start-up while security  is either being initialized or if security should be ignored
	 * until server is properly initialized.
	 *
	 * @param callback that should be executed without security
	 * @throws RuntimeException if call is being executed after the security initialization has finished
	 */
	public void runWithSecurityBypass(SecurityFreeCallback callback) {
		if (bypassDisabled) {
			throw new RuntimeException("Security bypass usage is not allowed after application has been initialized.");
		}
		preparePrivilegedSecurityContext();
		try {
			modifySecurityDisabledFlag(true);
			callback.executeWithoutSecurityCheck();
		} catch (final Exception e) {
			throw new RuntimeException("Security free callback failed", e);
		} finally {
			modifySecurityDisabledFlag(false);
			SecurityContextHolder.clearContext();
		}
	}

	private void preparePrivilegedSecurityContext() {
		UAAUserDetails principal = new UAADelegatedUserDetail(new User("BYPASS", "NA", Collections.emptyList()));
		//run in privileged mode (just in case)
		principal.permissionCheckStarted();
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	private static void modifySecurityDisabledFlag(boolean value) {
		Field field = getSecurityDisabledField();
		try {
			field.setBoolean(null, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException("Unable to change security bypass", e);
		}
	}

	private static Field getSecurityDisabledField() {
		Field field = ReflectionUtils.findField(UAAPolicyDecisionPoint.class, PROPERTY_SECURITY_BYPASS);
		field.setAccessible(true);
		return field;

	}
}
