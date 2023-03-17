package com.talosvfx.talos.runtime.scene.utils.propertyWrappers;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.scene.components.DataComponent;

public class DataPropertiesUtil {
    public DataComponent instance;

    private static PropertyWrappers dataPropertyWrappers = new PropertyWrappers();

    public static Array<String> getListOfPrimitives () {
        Array<String> primitiveTypeNames = dataPropertyWrappers.getPrimitiveTypeNames();
        // TODO: 16.03.23 need to add support for GameObjects
        primitiveTypeNames.removeValue("GameObject", false);
        return primitiveTypeNames;
    }

    public static PropertyWrapper makeWrapper (String parameterClassName, String parameterName) {
        PropertyWrapper<?> propertyWrapper = dataPropertyWrappers.createPropertyWrapperForClazzName(parameterClassName);
        propertyWrapper.setDefault();
        propertyWrapper.propertyName = parameterName;
        return propertyWrapper;
    }
}
