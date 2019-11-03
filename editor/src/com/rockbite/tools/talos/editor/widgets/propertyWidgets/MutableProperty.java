package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

public abstract class MutableProperty<T> extends Property<T> {
	public MutableProperty (String propertyName, T initialValue) {
		super(propertyName, initialValue);
	}

	public abstract void changed (T newValue);
}
