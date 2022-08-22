package com.talosvfx.talos.editor.addons.scene.utils.scriptProperties;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class ScriptPropertyIntegerWrapper extends ScriptPropertyNumberWrapper<Integer> {
    @Override
    public Integer parseValueFromString (String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        defaultValue = jsonData.getInt("defaultValue", 0);
        value = jsonData.getInt("value", defaultValue);

        minValue = jsonData.getInt("minValue");
        maxValue = jsonData.getInt("maxValue");
        step = jsonData.getInt("step");
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("value", value);
        json.writeValue("defaultValue", defaultValue);
    }
}
