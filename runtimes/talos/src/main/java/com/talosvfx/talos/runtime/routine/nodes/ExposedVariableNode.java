package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.routine.RoutineInstance;
import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

public class ExposedVariableNode extends RoutineNode {

    public int index;
    private String key;
    private PropertyWrapper<?> propertyWrapper;

    @Override
    public Object queryValue (String targetPortName) {

        PropertyWrapper instance = routineInstanceRef.getProperties().get(key);
        if(instance == null) {
            if (propertyWrapper == null) {
                return 0;
            } else {
                return propertyWrapper.defaultValue;
            }
        } else {
            return instance.value;
        }
    }

    @Override
    public void loadFrom(RoutineInstance routineInstance, JsonValue nodeData) {
        super.loadFrom(routineInstance, nodeData);
        configureNode(propertiesJson); //todo: this is hack due to booboo there
        propertyWrapper = routineInstance.getPropertyWrapperWithIndex(index);
    }

    @Override
    protected void configureNode (JsonValue properties) {
        index = properties.getInt("index");
        key = properties.getString("key");

        configured = true;
    }

    public void updateForPropertyWrapper (PropertyWrapper<?> propertyWrapper) {
        if (propertyWrapper != null) {
            index = propertyWrapper.index;
            this.propertyWrapper = propertyWrapper;
        }
    }
}
