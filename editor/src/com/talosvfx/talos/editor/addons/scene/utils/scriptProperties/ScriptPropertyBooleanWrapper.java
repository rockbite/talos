package com.talosvfx.talos.editor.addons.scene.utils.scriptProperties;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class ScriptPropertyBooleanWrapper extends ScriptPropertyWrapper<Boolean> {
    @Override
    public Boolean parseValueFromString (String value) {
        return Boolean.valueOf(value);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        defaultValue = jsonData.getBoolean("defaultValue", false);
        value = jsonData.getBoolean("value", defaultValue);
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("value", value);
        json.writeValue("defaultValue", defaultValue);
    }
}
