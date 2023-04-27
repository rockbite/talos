package com.talosvfx.talos.runtime.scene.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyType;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;
import lombok.Getter;

public class DataComponent extends AComponent implements Json.Serializable{
    @Getter
    private Array<PropertyWrapper<?>> properties = new Array<>();


    /**
     * \Runtime only access, loaded on deserialization
     */
    private transient ObjectMap<String, PropertyWrapper> propertyWrapperObjectMap = new ObjectMap<>();

    @Override
    public void write(Json json) {
        json.writeValue("properties", properties);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        properties.clear();
        propertyWrapperObjectMap.clear();
        JsonValue propertiesJson = jsonData.get("properties");
        if (propertiesJson != null) {
            for (JsonValue propertyJson : propertiesJson) {
                PropertyWrapper value = json.readValue(PropertyWrapper.class, propertyJson);
                properties.add(value);
                propertyWrapperObjectMap.put(value.propertyName, value);
            }
        }
    }

    public @Null PropertyWrapper<?> getWrapperOrNull (String propertyName) {
        if (propertyWrapperObjectMap.containsKey(propertyName)) {
            return propertyWrapperObjectMap.get(propertyName);
        }
        return null;
    }


    /**
     * @param propertyName
     * @return null if no property found
     */
    public @Null Boolean getBooleanOrNull (String propertyName) {
        PropertyWrapper<?> wrapperOrNull = getWrapperOrNull(propertyName);
        if (wrapperOrNull != null) {
            if (wrapperOrNull.getType() == PropertyType.BOOLEAN) return (Boolean) wrapperOrNull.getValue();
        }
        return null;
    }

    /**
     * @param propertyName
     * @return null if no property found
     */
    public @Null String getStringOrNull (String propertyName) {
        PropertyWrapper<?> wrapperOrNull = getWrapperOrNull(propertyName);
        if (wrapperOrNull != null) {
            if (wrapperOrNull.getType() == PropertyType.STRING) return (String) wrapperOrNull.getValue();
        }
        return null;
    }

    /**
     * @param propertyName
     * @return null if no property found
     */
    public @Null Float getFloatOrNull (String propertyName) {
        PropertyWrapper<?> wrapperOrNull = getWrapperOrNull(propertyName);
        if (wrapperOrNull != null) {
            if (wrapperOrNull.getType() == PropertyType.FLOAT) return (Float) wrapperOrNull.getValue();
        }
        return null;
    }

    /**
     * @param propertyName
     * @return null if no property found
     */
    public @Null Vector2 getVector2OrNull (String propertyName) {
        PropertyWrapper<?> wrapperOrNull = getWrapperOrNull(propertyName);
        if (wrapperOrNull != null) {
            if (wrapperOrNull.getType() == PropertyType.VECTOR2) return (Vector2) wrapperOrNull.getValue();
        }
        return null;
    }

    /**
     * @param propertyName
     * @return null if no property found
     */
    public @Null GameObject getGameObjectOrNull (String propertyName) {
        PropertyWrapper<?> wrapperOrNull = getWrapperOrNull(propertyName);
        if (wrapperOrNull != null) {
            if (wrapperOrNull.getType() == PropertyType.GAME_OBJECT) return (GameObject) wrapperOrNull.getValue();
        }
        return null;
    }

    /**
     * @param propertyName
     * @return null if no property found
     */
    public @Null Color getColorOrNull (String propertyName) {
        PropertyWrapper<?> wrapperOrNull = getWrapperOrNull(propertyName);
        if (wrapperOrNull != null) {
            if (wrapperOrNull.getType() == PropertyType.COLOR) return (Color) wrapperOrNull.getValue();
        }
        return null;
    }
}
