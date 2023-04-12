package com.talosvfx.talos.runtime.scene.utils.propertyWrappers;

import com.talosvfx.talos.runtime.routine.misc.PropertyTypeWrapperMapper;
import com.talosvfx.talos.runtime.scene.GameObject;

public class PropertyGameObjectWrapper extends PropertyWrapper<GameObject> {

    public PropertyGameObjectWrapper () {
        defaultValue = new GameObject();
    }

    @Override
    public GameObject parseValueFromString(String value) {
        return null;
    }

    @Override
    public PropertyType getType() {
        return PropertyType.GAME_OBJECT;
    }
}
