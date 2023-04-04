package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.routine.RoutineInstance;
import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyType;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

public class EventNode extends RoutineNode {
    private String eventName;
    private Array<PropertyWrapper<?>> propertyWrappers = new Array<>();

    @Override
    public void receiveSignal(String portName) {
        super.receiveSignal(portName);

        for (PropertyWrapper<?> wrapper : propertyWrappers) {
            String propertyName = wrapper.propertyName;
            Object val = fetchValue(propertyName);
            if(val != null) {
                wrapper.setValueUnsafe(val);
            }
        }

        RuntimeContext.getInstance().routineEventInterface.onEventFromRoutines(eventName, new Array<>(propertyWrappers));
    }

    @Override
    protected void configureNode(JsonValue properties) {
        eventName = properties.getString("eventName");
        Json json = new Json();
        JsonValue propertyWrappersJson = properties.get("customParams");
        if (propertyWrappersJson != null) {
            for (JsonValue propertyWrapperJson : propertyWrappersJson) {
                String className = propertyWrapperJson.getString("className", "");
                JsonValue property = propertyWrapperJson.get("property");
                if (property != null) {
                    try {
                        Class clazz = ClassReflection.forName(className);
                        PropertyWrapper propertyWrapper = (PropertyWrapper) ClassReflection.newInstance(clazz);
                        propertyWrapper.read(json, property);
                        propertyWrappers.add(propertyWrapper);
                    } catch (ReflectionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        for (PropertyWrapper<?> propertyWrapper : propertyWrappers) {
            PropertyType type = propertyWrapper.getType();

            RoutineNode.Port port = new RoutineNode.Port();
            port.name = propertyWrapper.propertyName;
            port.nodeRef = this;
            port.connectionType = RoutineNode.ConnectionType.DATA;
            port.dataType = makeDataType(type);
            port.portType = RoutineNode.PortType.INPUT;
            inputs.put(port.name, port);
        }

        configured = true;
    }

    private DataType makeDataType(PropertyType type) {
        if (type == PropertyType.FLOAT) return DataType.NUMBER;
        if (type == PropertyType.ASSET) return DataType.ASSET;
        if (type == PropertyType.BOOLEAN) return DataType.BOOLEAN;
        if (type == PropertyType.COLOR) return DataType.COLOR;
        if (type == PropertyType.VECTOR2) return DataType.VECTOR2;

        return DataType.NUMBER;
    }
}
