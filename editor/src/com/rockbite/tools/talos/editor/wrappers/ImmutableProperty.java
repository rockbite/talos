package com.rockbite.tools.talos.editor.wrappers;

public class ImmutableProperty<T> extends Property<T> {

	public ImmutableProperty (String propertyName, T initialValue) {
		super(propertyName, initialValue);
	}


}
