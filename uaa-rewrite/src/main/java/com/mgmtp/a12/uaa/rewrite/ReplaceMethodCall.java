/*
 * (c) copyright 2012-2025 mgm technology partners GmbH
 *
 * This software, the underlying source code and other artifacts are protected by copyright.
 * All rights, in particular the right to use, reproduce, publish and edit are reserved.
 * A simple right of use (license) can be acquired for use, duplication, publication, editing etc..
 *
 * Requests for this can be made at A12-license@mgm-tp.com or other official channels of the copyright holder.
 */
package com.mgmtp.a12.uaa.rewrite;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.J;

public class ReplaceMethodCall extends Recipe {

	@Option(displayName = "Existing Method pattern",
		description = "A method patterns that are called",
		example = "com.mgmtp.a12.uaa.authentication.jwt.TokenSupport generateToken(org.springframework.security.core.userdetails.UserDetails)")
	private String existingMethodPattern;

	@Option(displayName = "New method patternMethod template",
		description = "A method template which will replace original call",
		example = "#{any(com.mgmtp.a12.uaa.authentication.jwt.TokenSupport)}" +
			".generateToken(#{any(org.springframework.security.core.userdetails.UserDetails)}).getToken()")
	private String newMethodTemplate;

	@Option(displayName = "Parameter indexes referenced from method template",
		description = "When the new method template reference parameters here you define it's indexes",
		example = "[0]")
	private List<Integer> parameterIndexes;

	public ReplaceMethodCall() {
	}

	public ReplaceMethodCall(String existingMethodPattern, String newMethodTemplate, List<Integer> parameterIndexes) {
		this.existingMethodPattern = existingMethodPattern;
		this.newMethodTemplate = newMethodTemplate;
		this.parameterIndexes = parameterIndexes;
	}

	@Override
	public String getDisplayName() {
		return "Replace method call with new one";
	}

	@Override
	public String getDescription() {
		return "Replace method with new call chain. 1-st parameter is the selector others are configurable.";
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		MethodMatcher methodToMatch = new MethodMatcher(existingMethodPattern);
		return Preconditions.check(new UsesMethod<>(methodToMatch), new JavaVisitor<ExecutionContext>() {

			private final JavaTemplate template = JavaTemplate
				.builder(newMethodTemplate)
				.build();

			@Override
			public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
				final J.MethodInvocation result = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);
				if (methodToMatch.matches(method)) {
					List<Object> parameters = new LinkedList<>();
					parameters.add(result.getSelect());
					Optional.ofNullable(parameterIndexes).orElse(Collections.emptyList())
						.forEach(index -> parameters.add(result.getArguments().get(index)));
					return template.apply(updateCursor(result), result.getCoordinates().replace(), parameters.toArray());
				}
				return result;
			}
		});
	}

	public String getExistingMethodPattern() {
		return existingMethodPattern;
	}

	public String getNewMethodTemplate() {
		return newMethodTemplate;
	}

	public List<Integer> getParameterIndexes() {
		return parameterIndexes;
	}

}
