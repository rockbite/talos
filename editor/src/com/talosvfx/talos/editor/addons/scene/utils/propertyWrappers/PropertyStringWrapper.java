package com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class PropertyStringWrapper extends PropertyWrapper<String> {
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
        // TODO: 12/23/2022 IMPLEMENT TYPE
        return null;
    }
}
