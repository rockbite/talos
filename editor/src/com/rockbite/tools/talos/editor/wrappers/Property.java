package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public abstract class Property<T> {

	private T propertyInitialValue;
	protected String propertyName;

	ObjectMap<String, Object> additionalProperties = new ObjectMap<>();

	public Property (String propertyName, T initialValue) {
		this.propertyInitialValue = initialValue;
		this.propertyName = propertyName;
	}

	public abstract T getValue ();

	public String getPropertyName () {
		return propertyName;
	}

	public Class getValueClass () {
		return propertyInitialValue.getClass();
	}

	public void addAdditionalProperty (String name, Object value) {
		additionalProperties.put(name, value);
	}

	public Object getAdditionalProperty (String value) {
		return additionalProperties.get(value);
	}

}
