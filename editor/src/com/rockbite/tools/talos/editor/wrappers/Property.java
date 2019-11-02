package com.rockbite.tools.talos.editor.wrappers;

public abstract class Property<T> {
	private T propertyInitialValue;
	protected String propertyName;

	public Property (String propertyName, T initialValue) {
		this.propertyInitialValue = initialValue;
		this.propertyName = propertyName;
	}

	public T getValue () {
		return propertyInitialValue;
	}

	public String getPropertyName () {
		return propertyName;
	}

	public Class getValueClass () {
		return propertyInitialValue.getClass();
	}
}
