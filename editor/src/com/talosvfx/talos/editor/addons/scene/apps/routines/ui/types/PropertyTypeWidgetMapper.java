package com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types;

import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.nodes.widgets.CustomGameObjectWidget;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.*;

public class PropertyTypeWidgetMapper {

	private static ObjectMap<PropertyType, Class<? extends ATypeWidget>> widgetMap = new ObjectMap<>();

	private static ObjectMap<PropertyType, Class<? extends PropertyWrapper>> wrapperMap = new ObjectMap<>();

	static {
		widgetMap.put(PropertyType.FLOAT, CustomFloatWidget.class);
		widgetMap.put(PropertyType.VECTOR2, CustomVector2Widget.class);
		widgetMap.put(PropertyType.COLOR, CustomColorWidget.class);
		widgetMap.put(PropertyType.ASSET, CustomAssetWidget.class);
		widgetMap.put(PropertyType.BOOLEAN, CustomBooleanWidget.class);
		widgetMap.put(PropertyType.STRING, CustomStringWidget.class);
		widgetMap.put(PropertyType.GAME_OBJECT, CustomGameObjectWidget.class);

		wrapperMap.put(PropertyType.FLOAT, PropertyFloatWrapper.class);
		wrapperMap.put(PropertyType.VECTOR2, PropertyVec2Wrapper.class);
		wrapperMap.put(PropertyType.COLOR, PropertyColorWrapper.class);
		wrapperMap.put(PropertyType.ASSET, PropertyGameAssetWrapper.class);
		wrapperMap.put(PropertyType.BOOLEAN, PropertyBooleanWrapper.class);
		wrapperMap.put(PropertyType.STRING, PropertyStringWrapper.class);
		wrapperMap.put(PropertyType.GAME_OBJECT, PropertyGameObjectWrapper.class);

	}
	public static Class<? extends PropertyWrapper> getWrapperForPropertyType (PropertyType propertyType) {
		return wrapperMap.get(propertyType);
	}
	public static Class<? extends ATypeWidget> getWidgetForPropertyTYpe (PropertyType propertyType) {
		return widgetMap.get(propertyType);
	}

}
