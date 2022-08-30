package com.talosvfx.talos.editor.addons.scene.utils.scriptProperties;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ValueProperty;

public class ScriptPropertyFloatWrapper extends ScriptPropertyNumberWrapper<Float> {

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
            step = 1f;
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
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        defaultValue= jsonData.getFloat("defaultValue", 0);
        value = jsonData.getFloat("value", defaultValue);

        minValue = jsonData.getFloat("minValue");
        maxValue = jsonData.getFloat("maxValue");
        step = jsonData.getFloat("step");
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("value", value);
        json.writeValue("defaultValue", defaultValue);
    }
}
