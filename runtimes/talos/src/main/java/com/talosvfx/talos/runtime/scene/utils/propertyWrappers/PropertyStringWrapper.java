package com.talosvfx.talos.runtime.scene.utils.propertyWrappers;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class PropertyStringWrapper extends PropertyWrapper<String> {

    public PropertyStringWrapper () {
        defaultValue = "";
    }

    @Override
    public String parseValueFromString (String value) {
        return value;
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        defaultValue = jsonData.getString("defaultValue", "");
        value = jsonData.getString("value", defaultValue);
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("value", value);
        json.writeValue("defaultValue", defaultValue);
    }

    @Override
    public PropertyType getType() {
        return PropertyType.STRING;
    }
}
