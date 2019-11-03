package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

public abstract class ImmutableProperty<T> extends Property<T> {

	public ImmutableProperty (String propertyName, T initialValue) {
		super(propertyName, initialValue);
	}


}
