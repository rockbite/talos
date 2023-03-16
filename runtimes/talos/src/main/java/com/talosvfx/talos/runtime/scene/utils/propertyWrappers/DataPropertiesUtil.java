package com.talosvfx.talos.runtime.scene.utils.propertyWrappers;

import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.components.DataComponent;

public class DataPropertiesUtil {
    public DataComponent instance;

    private static PropertyWrappers dataPropertyWrappers = new PropertyWrappers();

    static {
        dataPropertyWrappers.registerPropertyWrapper(Float.class, PropertyFloatWrapper.class);
        dataPropertyWrappers.registerPropertyWrapper(Boolean.class, PropertyBooleanWrapper.class);
        dataPropertyWrappers.registerPropertyWrapper(Integer.class, PropertyIntegerWrapper.class);
        dataPropertyWrappers.registerPropertyWrapper(String.class, PropertyStringWrapper.class);
        dataPropertyWrappers.registerPropertyWrapper(GameObject.class, PropertyGameObjectWrapper.class);
    }

    public static void addWrapper (DataComponent component, String parameterClassName, String parameterName) {
        PropertyWrapper<?> propertyWrapper = dataPropertyWrappers.createPropertyWrapperForClazzName(parameterClassName);
        propertyWrapper.setDefault();
        propertyWrapper.propertyName = parameterName;
        component.getProperties().add(propertyWrapper);
    }
}
