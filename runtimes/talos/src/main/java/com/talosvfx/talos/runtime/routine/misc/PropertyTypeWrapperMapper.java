package com.talosvfx.talos.runtime.routine.misc;

import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.*;

public class PropertyTypeWrapperMapper {

	private static ObjectMap<PropertyType, Class<? extends PropertyWrapper>> wrapperMap = new ObjectMap<>();

	static {

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

}
