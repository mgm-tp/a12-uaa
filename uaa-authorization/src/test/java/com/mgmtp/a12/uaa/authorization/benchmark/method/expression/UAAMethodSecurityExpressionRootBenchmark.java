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
package com.mgmtp.a12.uaa.authorization.benchmark.method.expression;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.mockito.MockitoAnnotations;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.mgmtp.a12.uaa.authentication.AuthenticationType;
import com.mgmtp.a12.uaa.authentication.principal.AccessRight;
import com.mgmtp.a12.uaa.authentication.principal.Role;
import com.mgmtp.a12.uaa.authentication.principal.UAAPrincipal;
import com.mgmtp.a12.uaa.authentication.security.login.internal.TypedUsernamePasswordAuthenticationToken;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionService;
import com.mgmtp.a12.uaa.authorization.config.UAAMethodSecurityConfig;
import com.mgmtp.a12.uaa.authorization.integration.TestUserDetailsService;
import com.mgmtp.a12.uaa.authorization.internal.RuntimeAuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.model.Company;
import com.mgmtp.a12.uaa.authorization.model.Employee;
import com.mgmtp.a12.uaa.authorization.model.FamilyMember;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyRightsValidator;
import com.mgmtp.a12.uaa.authorization.property.internal.UAADataMasking;
import com.mgmtp.a12.uaa.authorization.security.DataMasking;
import com.mgmtp.a12.uaa.authorization.security.PolicyDecisionPoint;
import com.mgmtp.a12.uaa.authorization.security.PropertyChangesChecker;
import com.mgmtp.a12.uaa.authorization.security.UAAPolicyEnforcementPoint;
import com.mgmtp.a12.uaa.authorization.security.spel.internal.SpelPolicyProcessorFactory;
import com.mgmtp.a12.uaa.authorization.security.spel.internal.UAAMethodSecurityExpressionRoot;
import com.mgmtp.a12.uaa.authorization.security.spel.internal.UAAMethodService;
import com.mgmtp.a12.uaa.authorization.security.spel.internal.UAAPolicyDecisionPoint;

@Fork(1)
@Threads(10)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class UAAMethodSecurityExpressionRootBenchmark {

	@State(Scope.Thread)
	public static class PredefinedState {

		private UAAMethodSecurityExpressionRoot uaaMethodSecurityExpressionRoot;
		private AnnotationConfigApplicationContext context;
		private UAAMethodService uaaMethodService;

		@Setup(Level.Trial)
		public void setUp() throws IllegalAccessException {
			MockitoAnnotations.openMocks(this);
			context = new AnnotationConfigApplicationContext(BenchmarkConfiguration.class);
			uaaMethodSecurityExpressionRoot = context.getBean(UAAMethodSecurityExpressionRoot.class);
			uaaMethodService = context.getBean(UAAMethodService.class);
		}

		@TearDown
		public void tearDown() {
			context.close();
			SecurityContextHolder.clearContext();
		}
	}

	@Benchmark
	public void isResourceName(Blackhole bh, PredefinedState state) {
		bh.consume(state.uaaMethodSecurityExpressionRoot.isResourceName(UAAMethodSecurityExpressionRootBenchmark.class,
			UAAMethodSecurityExpressionRootBenchmark.class.getSimpleName()));
	}

	@Benchmark
	public void print(Blackhole bh, PredefinedState state) {
		bh.consume(state.uaaMethodSecurityExpressionRoot.print("This is message"));
	}

	@Benchmark
	public void hasObjectWithPropertyValue(Blackhole bh, PredefinedState state) {
		bh.consume(state.uaaMethodSecurityExpressionRoot.hasObjectWithPropertyValue(
			Collections.singleton(SecurityContextHolder.getContext().getAuthentication().getPrincipal()), "username", "admin"));
	}

	@Benchmark
	public void hasNestedObjectWithPropertyValue(Blackhole bh, PredefinedState state) {
		FamilyMember familyMember1 = new FamilyMember(1L, "male", "David");
		FamilyMember familyMember2 = new FamilyMember(2L, "female", "Emma");
		Employee employee1 = new Employee(1L, "firstName1", "lastName1", 23, List.of(familyMember1, familyMember2));
		Employee employee2 = new Employee(2L, "firstName2", "lastName2", 23, List.of(familyMember1, familyMember2));
		Company company = new Company("mgm");
		company.setId(3L);
		company.setEmployees(new LinkedList<>(Set.of(employee1, employee2)));
		bh.consume(
			state.uaaMethodSecurityExpressionRoot.hasNestedObjectWithPropertyValue(Collections.singleton(company), "employees", "firstName", "firstName2"));
	}

	@Benchmark
	public void hasAccessRight(Blackhole bh, PredefinedState state) {
		bh.consume(state.uaaMethodSecurityExpressionRoot.hasAccessRight("READ"));
	}

	@Benchmark
	public void containsAnyRole(Blackhole bh, PredefinedState state) {
		bh.consume(state.uaaMethodSecurityExpressionRoot.containsAnyRole(Collections.singleton("Admin")));
	}

	@Benchmark
	public void hasUAAPermission(Blackhole bh, PredefinedState state) {
		bh.consume(state.uaaMethodService.hasUAAPermission());
	}

	@Benchmark
	public void generateRepositoryPermissions(Blackhole bh, PredefinedState state) {
		bh.consume(state.uaaMethodService.generateRepositoryPermissions());
	}

	@Benchmark
	public void hasUAAPropertyPermissionReadMode(Blackhole bh, PredefinedState state) {
		FamilyMember familyMember1 = new FamilyMember(1L, "male", "David");
		FamilyMember familyMember2 = new FamilyMember(2L, "female", "Emma");
		Employee employee1 = new Employee(3L, "firstName1", "lastName1", 23, List.of(familyMember1, familyMember2));
		Employee employee2 = new Employee(4L, "firstName2", "lastName2", 23, List.of(familyMember1, familyMember2));
		Company company = new Company("mgm");
		company.setId(5L);
		company.setOwner("admin");
		company.setTaxNumber("98494651684651");
		company.setEmployees(new LinkedList<>(Set.of(employee1, employee2)));
		bh.consume(state.uaaMethodService.hasUAAPropertyPermissionReadMode(company));
	}

	@Benchmark
	public void hasUAAPropertyPermissionWriteMode(Blackhole bh, PredefinedState state) {
		FamilyMember familyMember1 = new FamilyMember(1L, "male", "David");
		FamilyMember familyMember2 = new FamilyMember(2L, "female", "Emma");

		Employee employee1 = new Employee(3L, "firstName1", "lastName1", 23, List.of(familyMember1, familyMember2));
		Employee employee2 = new Employee(4L, "firstName2", "lastName2", 23, List.of(familyMember1, familyMember2));
		Company company1 = new Company("mgm1");
		company1.setOwner("user");
		company1.setTaxNumber("98494651684651");
		company1.setId(5L);
		company1.setEmployees(new LinkedList<>(Set.of(employee1, employee2)));

		FamilyMember familyMember3 = new FamilyMember(6L, "female", "Liva");
		FamilyMember familyMember4 = new FamilyMember(7L, "male", "Ommi");
		Employee employee3 = new Employee(8L, "firstName3", "lastName3", 23, List.of(familyMember3, familyMember4));
		Employee employee4 = new Employee(9L, "firstName4", "lastName4", 23, List.of(familyMember3, familyMember4));
		Company company2 = new Company("mgm2");
		company2.setOwner("admin");
		company2.setTaxNumber("54540261654651015246");
		company2.setEmployees(new LinkedList<>(Set.of(employee3, employee4)));
		company2.setId(10L);
		bh.consume(state.uaaMethodService.hasUAAPropertyPermissionWriteMode(company1, company2));
	}

	@Configuration
	@Import({ UAAMethodSecurityConfig.class })
	static class BenchmarkConfiguration {

		@Bean
		public UAAMethodService createUAAMethodService() {
			return new UAAMethodService();
		}

		@Bean
		public AuthorizationDefinitionRepository createAuthorizationDefinitionRepository() {
			return new RuntimeAuthorizationDefinitionRepository();
		}

		@Bean
		public AuthorizationDefinitionService createAuthorizationDefinitionService() {
			return new AuthorizationDefinitionService("classpath:testAuthorizationDefinitionBenchmark.json", null);
		}

		@Bean
		public UserDetailsService createUserDetailsService() {
			return new TestUserDetailsService();
		}

		@Bean
		public PolicyDecisionPoint createPolicyDecisionPoint(AuthorizationDefinitionRepository authorizationDefinitionRepository,
			PropertyRightsValidator propertyRightsValidator, DataMasking dataMasking, PropertyChangesChecker propertyChangesChecker,
			SpelPolicyProcessorFactory spelPolicyProcessorFactory) {
			return new UAAPolicyDecisionPoint(new StandardEvaluationContext(), authorizationDefinitionRepository, List.of(spelPolicyProcessorFactory),
				propertyRightsValidator, dataMasking, propertyChangesChecker);
		}

		@Bean
		public SpelPolicyProcessorFactory createSpelPolicyProcessorFactory() {
			return new SpelPolicyProcessorFactory();
		}

		@Bean
		public UAAPolicyEnforcementPoint createUAAPolicyEnforcementPoint(PolicyDecisionPoint policyDecisionPoint,
			AuthorizationDefinitionRepository authorizationDefinitionRepository) {
			return new UAAPolicyEnforcementPoint(policyDecisionPoint, authorizationDefinitionRepository);
		}

		@Bean
		public PropertyRightsValidator createPropertyPermissionValidator() {
			return new PropertyRightsValidator();
		}

		@Bean
		public DataMasking createDataMasking(AuthorizationDefinitionRepository authorizationDefinitionRepository) {
			return new UAADataMasking(authorizationDefinitionRepository);
		}

		@Bean
		public PropertyChangesChecker createPropertyChangesChecker() {
			return new PropertyChangesChecker(List.of("com.mgmtp"));
		}

		@Bean
		public Authentication createAuthentication() {
			AccessRight accessRight = new AccessRight();
			accessRight.setName("READ");
			FamilyMember familyMember = new FamilyMember(100L, "male", "Olive");
			UAAPrincipal<Object> uaaPrincipal =
				new UAAPrincipal<>("admin", "***", Collections.singleton(new Role.Builder("Admin").withAccessRights(Set.of(accessRight)).build()),
					familyMember);
			UsernamePasswordAuthenticationToken user = new TypedUsernamePasswordAuthenticationToken<>(uaaPrincipal, "***", AuthenticationType.LOCAL,
				Collections.singleton(new Role.Builder("Admin").withAccessRights(Set.of(accessRight)).build()));
			SecurityContextHolder.getContext().setAuthentication(user);
			return user;
		}

		@Bean
		public UAAMethodSecurityExpressionRoot createUAAMethodSecurityExpressionRoot(Authentication authentication,
			UAAPolicyEnforcementPoint uaaPolicyEnforcementPoint) {
			return new UAAMethodSecurityExpressionRoot(authentication, uaaPolicyEnforcementPoint);
		}
	}
}
