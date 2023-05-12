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
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnEventNode extends RoutineNode {
    private static final Logger logger = LoggerFactory.getLogger(OnEventNode.class);

    @Getter
    private String eventName;



    private Array<PropertyWrapper<?>> propertyWrappers = new Array<>();

    @Override
    public void receiveSignal(String portName) {

    }

    public void fireEvent (Array<PropertyWrapper<?>> dataFromEvent) {

        for (PropertyWrapper<?> dynamicWrapper : dataFromEvent) {
            for (PropertyWrapper<?> eventWrapper : propertyWrappers) {
                if (eventWrapper.propertyName.equalsIgnoreCase(dynamicWrapper.propertyName)) {
                    eventWrapper.setValueUnsafe(dynamicWrapper.getValue());
                } else {
                    logger.info("Event doesn't have property " + dynamicWrapper.propertyName);
                }
            }
        }

        sendSignal("onEvent");
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

            Port port = new Port();
            port.name = propertyWrapper.propertyName;
            port.nodeRef = this;
            port.connectionType = ConnectionType.DATA;
            port.dataType = makeDataType(type);
            port.portType = PortType.OUTPUT;
            outputs.put(port.name, port);
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
