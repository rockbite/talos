package com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class PropertyColorWrapper extends PropertyWrapper<Color> {
    @Override
    public Color parseValueFromString(String value) {
        return Color.valueOf(value);
    }

    @Override
    public PropertyWrapper<Color> clone() {
        PropertyColorWrapper clone = new PropertyColorWrapper();
        clone.value.set(value);
        clone.defaultValue.set(defaultValue);
        clone.propertyName = propertyName;
        clone.index = index;
        return clone;
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
    }

    @Override
    public void write(Json json) {
        super.write(json);
    }

    @Override
    public PropertyType getType() {
        return PropertyType.COLOR;
    }
}
