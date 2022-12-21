package com.talosvfx.talos.editor.addons.scene.utils.scriptProperties;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class PropertyVec2Wrapper extends PropertyWrapper<Vector2> {

    @Override
    public Vector2 parseValueFromString (String value) {
        return null;
    }

    public PropertyVec2Wrapper() {
        defaultValue = new Vector2();
        value = new Vector2();
    }

    @Override
    public PropertyWrapper<Vector2> clone () {
        PropertyVec2Wrapper clone = new PropertyVec2Wrapper();
        clone.value.set(value);
        clone.defaultValue.set(defaultValue);
        clone.propertyName = propertyName;
        clone.index = index;
        return clone;
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        defaultValue.setZero();
        value.setZero();
        if (jsonData.has("defaultValue")) {
            JsonValue defaultValueJSON = jsonData.get("defaultValue");
            defaultValue = json.readValue(Vector2.class, defaultValueJSON);
        }
        if (jsonData.has("value")) {
            value = json.readValue(Vector2.class, jsonData.get("value"));
        }
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("value", value);
        json.writeValue("defaultValue", defaultValue);
    }
}