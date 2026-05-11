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
package com.mgmtp.a12.uaa.authorization.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.expression.Expression;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.example.Address;
import com.mgmtp.a12.uaa.authorization.example.Family;
import com.mgmtp.a12.uaa.authorization.internal.UAAUserDetails;
import com.mgmtp.a12.uaa.authorization.property.internal.UAADataMasking;
import com.mgmtp.a12.uaa.authorization.property.internal.UaaPropertyAccessor;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UAADataMaskingTest {

	private UAADataMasking dataMaskingSupport;
	@Mock
	private UAAUserDetails uaaUserDetails;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private Authentication authentication;
	@Mock
	private AuthorizationDefinitionRepository authorizationDefinitionRepository;

	@BeforeEach
	void setUp() {
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(authentication.getPrincipal()).thenReturn(uaaUserDetails);
		Mockito.when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);

		Set<Rights> rights = new LinkedHashSet<>();
		Rights right = new Rights();
		right.setRead(Set.of("R1", "name", "R3"));
		right.setWrite(Set.of("description", "W2", "W3", "primaryAddress"));
		rights.add(right);

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(rights);
		dataMaskingSupport = new UAADataMasking(authorizationDefinitionRepository);
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void checkMaskDataReturnNullIfObjectIsNull() {
		Object result = dataMaskingSupport.maskData(null, null);
		Assertions.assertNull(result);
	}

	@Test
	void checkNotMaskDataRunInPrivilegeMode() {
		BDDMockito.given(uaaUserDetails.runPrivileged()).willReturn(true);
		PropertyPermission propertyPermission = new PropertyPermission();
		propertyPermission.setName("checkNotMaskDataRunInPrivilegeMode");
		propertyPermission.setRightsRef(Set.of("test"));
		Object resource = "data";
		Object result = dataMaskingSupport.maskData(resource, propertyPermission);
		Assertions.assertEquals(resource, result);
	}

	@Test
	void checkMaskDataWithReadRight() {
		Address address = new Address();
		address.setNumber(7);
		address.setStreetName("PCT");
		Family resource = new Family();
		resource.setName("Beth");
		resource.setAge(18);
		resource.setDescription("Young");
		resource.setPrimaryAddress(address);
		PropertyPermission propertyPermission = new PropertyPermission();
		propertyPermission.setName("checkMaskDataWithReadRight");
		propertyPermission.setRightsRef(Set.of("test"));
		Family result = dataMaskingSupport.maskData(resource, propertyPermission);
		Assertions.assertEquals("Beth", result.getName());
		Assertions.assertNull(result.getAge());
		Assertions.assertEquals("Young", result.getDescription());
		Assertions.assertEquals(7, result.getPrimaryAddress().getNumber());
		Assertions.assertEquals("PCT", result.getPrimaryAddress().getStreetName());
	}

	@Test
	public void checkEmptyRights() {

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Collections.emptySet());

		Address address = new Address();
		address.setNumber(7);
		address.setStreetName("PCT");
		Family resource = new Family();
		resource.setName("Beth");
		resource.setAge(18);
		resource.setDescription("Young");
		resource.setPrimaryAddress(address);

		PropertyPermission propertyPermission = new PropertyPermission();
		propertyPermission.setName("checkEmptyRights");
		Family result = dataMaskingSupport.maskData(resource, propertyPermission);

		Assertions.assertNull(result.getName());
		Assertions.assertNull(result.getAge());
		Assertions.assertNull(result.getDescription());
		Assertions.assertNull(result.getPrimaryAddress());
	}

	@Test
	public void checkCollection() {
		Set<Rights> rights = new LinkedHashSet<>();
		Rights right = new Rights();
		right.setRead(Set.of("name", "nested", "nested[].nestedName", "nested[].nestedDescription", "nested[].inner", "nested[].inner[].nestedDescription"));
		right.setWrite(Set.of("description"));
		right.setMask(
			Set.of("nested[].nestedDescription::'filtered::' + nestedDescription", "nested[].inner[].nestedDescription::'filtered::' + nestedDescription"));
		rights.add(right);

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(rights);

		Resource resource = new Resource("test");
		NestedResource nestedResource = new NestedResource("nestedName", "nestedDescription");
		nestedResource.addInner(new NestedResource("Inner", "innerDescription"));
		nestedResource.addInner(new NestedResource("Inner-2", "innerDescription-2"));
		resource.addNested(nestedResource);
		resource.addNested(new NestedResource("nestedName-2", "nestedDescription-2"));
		PropertyPermission propertyPermission = new PropertyPermission();
		propertyPermission.setName("checkCollection");
		propertyPermission.setRightsRef(Set.of("test"));
		Resource result = dataMaskingSupport.maskData(resource, propertyPermission);

		Assertions.assertNotNull(result.getNested().get(0).getNestedName());
		Assertions.assertNotNull(result.getNested().get(0).getNestedDescription());
		Assertions.assertEquals("filtered::nestedDescription", result.getNested().get(0).getNestedDescription());

		Assertions.assertNotNull(result.getNested().get(1).getNestedName());
		Assertions.assertNotNull(result.getNested().get(1).getNestedDescription());
		Assertions.assertEquals("filtered::nestedDescription-2", result.getNested().get(1).getNestedDescription());

		Assertions.assertNull(result.getNested().get(0).getInner().get(0).getNestedName());
		Assertions.assertNotNull(result.getNested().get(0).getInner().get(0).getNestedDescription());
		Assertions.assertEquals("filtered::innerDescription", result.getNested().get(0).getInner().get(0).getNestedDescription());

		Assertions.assertNull(result.getNested().get(0).getInner().get(1).getNestedName());
		Assertions.assertNotNull(result.getNested().get(0).getInner().get(1).getNestedDescription());
		Assertions.assertEquals("filtered::innerDescription-2", result.getNested().get(0).getInner().get(1).getNestedDescription());

	}

	@Test
	public void checkCollection2() {

		Resource r = new Resource("test");
		r.addNested(new NestedResource("nestedName", "nestedDescription"));
		r.addNested(new NestedResource("nestedName-2", "nestedDescription-2"));

		StandardEvaluationContext evaluationContext = new StandardEvaluationContext(r);
		PropertyAccessor reflectivePropertyAccessor = new UaaPropertyAccessor(true);
		evaluationContext.addPropertyAccessor(reflectivePropertyAccessor);

		SpelExpressionParser parser = new SpelExpressionParser();

		Expression expression = parser.parseExpression("name");
		expression.setValue(evaluationContext, "nothing");

		//nested[].nestedName
		expression = parser.parseExpression("nested");
		Collection<?> nested = expression.getValue(evaluationContext, Collection.class);
		nested.forEach(resource -> {
			StandardEvaluationContext nestedEvaluationContext = new StandardEvaluationContext(resource);
			nestedEvaluationContext.addPropertyAccessor(reflectivePropertyAccessor);
			Expression nestedExpression = parser.parseExpression("nestedName");
			nestedExpression.setValue(nestedEvaluationContext, "blah");
		});
	}
	
	@Test
	public void checkMap() {
		Set<Rights> rights = new LinkedHashSet<>();
		Rights right = new Rights();
		right.setRead(Set.of("name", "nestedMap", "nestedMap[].nestedName"));
		right.setWrite(Set.of("description"));
		rights.add(right);

		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(rights);
		
		Resource resource = new Resource("test");
		resource.addNestedMap("nestedName", new NestedResource("nestedName", "nestedDescription"));
		resource.addNestedMap("nestedName-2", new NestedResource("nestedName-2", "nestedDescription-2"));
		resource.addNested(new NestedResource("nestedName", "nestedDescription"));
		resource.addNested(new NestedResource("nestedName-2", "nestedDescription-2"));
		PropertyPermission propertyPermission = new PropertyPermission();
		propertyPermission.setName("checkMap");
		propertyPermission.setRightsRef(Set.of("test"));
		
		Resource result = dataMaskingSupport.maskData(resource, propertyPermission);
		Assertions.assertNotNull(result.getName());
		Assertions.assertNotNull(result.getNestedMap());
		Assertions.assertNotNull(result.getNestedMap().get("nestedName").getNestedName());
		Assertions.assertNull(result.getNestedMap().get("nestedName").getNestedDescription());
		Assertions.assertNotNull(result.getNestedMap().get("nestedName-2").getNestedName());
		Assertions.assertNull(result.getNestedMap().get("nestedName-2").getNestedDescription());
	}

	@Test
	void checkNullDataWithREADRight(){
		PropertyPermission propertyPermission = new PropertyPermission();
		propertyPermission.setName("checkNullDataWithREADRight");
		propertyPermission.setRightsRef(Set.of("WRITE Only"));

		Rights rights = new Rights();
		rights.setWrite(Set.of("id","name"));

		PropertyRight propertyRight = new PropertyRight();
		propertyRight.setName("WRITE Only");
		propertyRight.setRights(rights);
		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));
		Assertions.assertDoesNotThrow(() -> dataMaskingSupport.maskData(new Object(), propertyPermission));
	}

	@Test
	void checkNullDataWithWRITERight(){
		PropertyPermission propertyPermission = new PropertyPermission();
		propertyPermission.setName("checkNullDataWithWRITERight");
		propertyPermission.setRightsRef(Set.of("READ Only"));

		Rights rights = new Rights();
		rights.setRead(Set.of("id","name"));

		PropertyRight propertyRight = new PropertyRight();
		propertyRight.setName("READ Only");
		propertyRight.setRights(rights);
		Mockito.when(authorizationDefinitionRepository.getPropertyRightsByNames(Mockito.any())).thenReturn(Set.of(rights));
		Assertions.assertDoesNotThrow(() -> dataMaskingSupport.maskData(new Object(), propertyPermission));
	}


	public static class Resource {

		private String name;
		private List<NestedResource> nested = new LinkedList<>();
		private Map<String, NestedResource> nestedMap = new HashMap<>();

		public Resource(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void addNested(NestedResource nested) {
			this.nested.add(nested);
		}

		public List<NestedResource> getNested() {
			return nested;
		}

		public void addNestedMap(String key, NestedResource nested) {
			this.nestedMap.put(key, nested);
		}

		public Map<String, NestedResource> getNestedMap() {
			return nestedMap;
		}

		@Override
		public String toString() {
			return "Resource [name=" + name + ", nested=" + nested + "]";
		}

	}

	public static class NestedResource {
		private String nestedName;
		private String nestedDescription;
		private List<NestedResource> inner = new LinkedList<>();

		public NestedResource(String nestedName, String nestedDescription) {
			this.nestedName = nestedName;
			this.nestedDescription = nestedDescription;
		}

		public String getNestedDescription() {
			return nestedDescription;
		}

		public String getNestedName() {
			return nestedName;
		}

		public void addInner(NestedResource nested) {
			this.inner.add(nested);
		}

		public List<NestedResource> getInner() {
			return inner;
		}

		@Override
		public String toString() {
			return "NestedResource [nestedName=" + nestedName + ", nestedDescription=" + nestedDescription + ", inner=" + inner + "]";
		}

	}
}