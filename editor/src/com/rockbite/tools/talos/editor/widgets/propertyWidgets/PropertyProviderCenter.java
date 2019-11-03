package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class PropertyProviderCenter {

	private static PropertyProviderCenter instance;

	public static PropertyProviderCenter Instance () {
		if (instance == null) {
			instance = new PropertyProviderCenter();
		}

		return instance;
	}

	public PropertyWidget obtainWidgetForProperty (Property property) {
		PropertyWidget propertyWidget = property.getPropertyWidgetClass();
		propertyWidget.configureForProperty(property);
		return propertyWidget;
	}
}
