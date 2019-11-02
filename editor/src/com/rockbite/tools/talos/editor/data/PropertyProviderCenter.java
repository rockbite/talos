package com.rockbite.tools.talos.editor.data;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.rockbite.tools.talos.editor.addons.bvb.AttachmentPoint;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.*;
import com.rockbite.tools.talos.editor.wrappers.Property;

public class PropertyProviderCenter {

	private ObjectMap<Class, Class<? extends PropertyWidget>> propertyWidgetMap = new ObjectMap<>();

	private static PropertyProviderCenter instance;

	public static PropertyProviderCenter Instance () {
		if (instance == null) {
			instance = new PropertyProviderCenter();
			instance.registerMaps();
		}

		return instance;
	}

	public PropertyWidget obtainWidgetForProperty (Property property) {
		Class<? extends PropertyWidget> aClass = propertyWidgetMap.get(property.getValueClass());
		PropertyWidget obtain = null;
		try {
			obtain = aClass.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		obtain.configureForProperty(property);

		return obtain;
	}

	private void registerMaps () {
		propertyWidgetMap.put(String.class, LabelWidget.class);
		propertyWidgetMap.put(Float.class, FloatWidget.class);
		propertyWidgetMap.put(Boolean.class, CheckboxWidget.class);
		propertyWidgetMap.put(Array.class, GlobalValueWidget.class);
	}
}
