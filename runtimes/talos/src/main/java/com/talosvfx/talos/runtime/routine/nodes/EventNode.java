package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyType;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventNode extends RoutineNode {
    private static final Logger logger = LoggerFactory.getLogger(EventNode.class);

    private String eventName;
    private Array<PropertyWrapper<?>> propertyWrappers = new Array<>();

    @Override
    public void receiveSignal(String portName) {
        super.receiveSignal(portName);

        for (PropertyWrapper<?> wrapper : propertyWrappers) {
            String propertyName = wrapper.propertyName;

            if (isInputAndConnected(propertyName)) {
                Object val = fetchValue(propertyName);
                if (val != null) {
                    wrapper.setValueUnsafe(val);
                }
            }
        }

        routineInstanceRef.onEventFromRoutines(eventName, new Array<>(propertyWrappers));

        for (PropertyWrapper<?> propertyWrapper : propertyWrappers) {
            if (propertyWrapper.getType() == PropertyType.GAME_OBJECT) {
                if (propertyWrapper.getValue() instanceof Array) {
                    Array<GameObject> gameObjects = (Array<GameObject>) propertyWrapper.getValue();
                    for (GameObject gameObject : gameObjects) {
                        if (gameObject != null) {
                            gameObject.onEventFromRoutines(eventName, new Array<>(propertyWrappers));
                        }
                    }
                } else {
                    logger.warn("Received bad data for PropertyWrapper, should be Array<GameObject> instead.");
                }
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
