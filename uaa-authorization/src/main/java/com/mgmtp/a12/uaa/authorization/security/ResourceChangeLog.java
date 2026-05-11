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
package com.mgmtp.a12.uaa.authorization.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.javers.core.changelog.ChangeProcessor;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.diff.Change;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ObjectRemoved;
import org.javers.core.diff.changetype.PropertyChange;
import org.javers.core.diff.changetype.ReferenceChange;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.diff.changetype.container.ArrayChange;
import org.javers.core.diff.changetype.container.ContainerChange;
import org.javers.core.diff.changetype.container.ListChange;
import org.javers.core.diff.changetype.container.SetChange;
import org.javers.core.diff.changetype.map.MapChange;
import org.javers.core.metamodel.object.GlobalId;
import org.springframework.core.GenericTypeResolver;

import com.mgmtp.a12.uaa.authorization.property.PropertyChangePathConverter;
import com.mgmtp.a12.uaa.authorization.property.internal.PropertyChangeConverter;

public class ResourceChangeLog implements ChangeProcessor<Set<String>> {

	private Set<String> changedPaths = new HashSet<>();
	private List<PropertyChangePathConverter> propertyChangeConverters;

	public ResourceChangeLog(Optional<List<PropertyChangePathConverter>> propertyChangeConverters) {
		//it guarantee at least default implementation since anyone instantiate it with deprecated constructor(s)
		this.propertyChangeConverters = propertyChangeConverters.orElse(Arrays.asList(new PropertyChangeConverter()));
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onContainerChange(ContainerChange containerChange) {
		//nothing here
	}

	@Override
	public void onListChange(ListChange listChange) {
		//nothing here	
	}

	@Override
	public void onObjectRemoved(ObjectRemoved objectRemoved) {
		//nothing here
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onPropertyChange(PropertyChange propertyChangeBase) {

		List<String> convertedProperties = propertyChangeConverters.stream()
			.filter(converter -> GenericTypeResolver.resolveTypeArgument(converter.getClass(), PropertyChangePathConverter.class)
				.isAssignableFrom(propertyChangeBase.getClass()))
			.findFirst()
			.map(converter -> converter.convertPropertyPath(propertyChangeBase))
			.orElse(Collections.emptyList());

		changedPaths.addAll(convertedProperties);
	}

	@Override
	public void onAffectedObject(GlobalId globalId) {
		//nothing here
	}

	@Override
	public Set<String> result() {
		return changedPaths;
	}

	@Override
	public void onCommit(CommitMetadata commitMetadata) {
		//nothing here

	}

	@Override
	public void beforeChangeList() {
		//nothing here

	}

	@Override
	public void afterChangeList() {
		//nothing here

	}

	@Override
	public void beforeChange(Change change) {
		//nothing here

	}

	@Override
	public void afterChange(Change change) {
		//nothing here

	}

	@Override
	public void onValueChange(ValueChange valueChange) {
		//nothing here

	}

	@Override
	public void onReferenceChange(ReferenceChange referenceChange) {
		//nothing here

	}

	@Override
	public void onNewObject(NewObject newObject) {
		//nothing here

	}

	@Override
	public void onSetChange(SetChange setChange) {
		//nothing here
	}

	@Override
	public void onArrayChange(ArrayChange arrayChange) {
		//nothing here

	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onMapChange(MapChange mapChange) {
		//nothing here

	}

}
