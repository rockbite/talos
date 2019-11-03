package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.utils.ObjectMap;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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

	public <E extends PropertyWidget> E getPropertyWidgetClass() {
		Type genericType = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		if(genericType == String.class) {
			return (E) new LabelWidget();
		}
		if(genericType == Boolean.class) {
			return (E) new CheckboxWidget();
		}
		if(genericType == Float.class) {
			return (E) new FloatWidget();
		}

		return null;
	}

}
