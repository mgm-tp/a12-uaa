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
package com.mgmtp.a12.uaa.client.rest.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.mgmtp.a12.uaa.client.rest.config.UAARestClientPropertiesResolver;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

/**
 * Resolves configuration by self-configuration if needed 
 *
 */
public class SelfconfigurationBeanPostProcessor implements BeanPostProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(SelfconfigurationBeanPostProcessor.class);

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		Object result = bean;
		if (bean instanceof UAARestClientAutoconfigProperties loadedProperties) {
			UAARestClientProperties restConfig = loadedProperties.getRest();
			UAARestClientProperties finalConfiguration = UAARestClientPropertiesResolver.resolve(restConfig);
			if (!restConfig.equals(finalConfiguration)) {
				LOGGER.info("Rest clientProperties has been resolved from server URL");
			}
			loadedProperties.setRest(finalConfiguration);
			result = loadedProperties;
		}
		return result;
	}

}
