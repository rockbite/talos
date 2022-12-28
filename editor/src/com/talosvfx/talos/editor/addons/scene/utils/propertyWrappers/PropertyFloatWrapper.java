package com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class PropertyFloatWrapper extends PropertyNumberWrapper<Float> {

    @Override
    public void collectAttributes (Array<String> attributes) {
        super.collectAttributes(attributes);
        if (minValue == null) {
            minValue = -Float.MAX_VALUE;
        }
        if (maxValue == null) {
            maxValue = Float.MAX_VALUE;
        }
        if (step == null) {
            step = 0.1f;
        }
        if (defaultValue == null) {
            defaultValue = 0f;
        }
    }

    @Override
    public Float parseValueFromString (String value) {
        try {
            return Float.valueOf(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0f;
        }
    }

    @Override
    public PropertyType getType() {
        return PropertyType.FLOAT;
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        defaultValue = jsonData.getFloat("defaultValue", 0);
        value = jsonData.getFloat("value", defaultValue);

        minValue = jsonData.getFloat("minValue", -Float.MAX_VALUE);
        maxValue = jsonData.getFloat("maxValue", Float.MAX_VALUE);
        step = jsonData.getFloat("step", 0.1f);

        isRanged = jsonData.getBoolean("isRanged", false);
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("value", value);
        json.writeValue("defaultValue", defaultValue);
    }
}
