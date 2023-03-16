package com.talosvfx.talos.runtime.scene.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;
import lombok.Getter;

public class DataComponent extends AComponent implements Json.Serializable{
    @Getter
    private Array<PropertyWrapper<?>> properties = new Array<>();

    @Override
    public void write(Json json) {
        json.writeValue("properties", properties);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        properties.clear();
        JsonValue propertiesJson = jsonData.get("properties");
        if (propertiesJson != null) {
            for (JsonValue propertyJson : propertiesJson) {
                properties.add(json.readValue(PropertyWrapper.class, propertyJson));
            }
        }
    }
}
