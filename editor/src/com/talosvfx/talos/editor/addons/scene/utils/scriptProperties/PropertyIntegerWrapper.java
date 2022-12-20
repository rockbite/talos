package com.talosvfx.talos.editor.addons.scene.utils.scriptProperties;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class PropertyIntegerWrapper extends PropertyNumberWrapper<Integer> {

    @Override
    public void collectAttributes (Array<String> attributes) {
        super.collectAttributes(attributes);
        if (minValue == null) {
            minValue = Integer.MIN_VALUE;
        }
        if (maxValue == null) {
            maxValue = Integer.MAX_VALUE;
        }
        if (step == null) {
            step = 1;
        }
        if (defaultValue == null) {
            defaultValue = 0;
        }
    }

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

        minValue = jsonData.getInt("minValue", Integer.MIN_VALUE);
        maxValue = jsonData.getInt("maxValue", Integer.MAX_VALUE);
        step = jsonData.getInt("step", 1);
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("value", value);
        json.writeValue("defaultValue", defaultValue);
    }
}
