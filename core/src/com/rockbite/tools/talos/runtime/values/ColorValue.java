package com.rockbite.tools.talos.runtime.values;

import com.badlogic.gdx.graphics.Color;

public class ColorValue extends Value<Color> {

    public ColorValue() {
        object = new Color();
        reset();
    }

    @Override
    public void set(float value) {
        object.set(value, value, value, value);
    }

    @Override
    public void set(Value value) {
        if(value instanceof FloatValue) {
            float floatValue = (float) value.get();
            object.set(floatValue, floatValue, floatValue, floatValue);
        } else if(value instanceof ColorValue) {
            object.set((Color) value.get());
        }
    }

    @Override
    public void mul(Value value) {
        if(value instanceof FloatValue) {
            FloatValue floatValue = (FloatValue) value;
            object.r *= floatValue.get();
            object.g *= floatValue.get();
            object.b *= floatValue.get();
        }
    }

    @Override
    public void add(Value value) {
        if(value instanceof FloatValue) {
            FloatValue floatValue = (FloatValue) value;
            object.r += floatValue.get();
            object.g += floatValue.get();
            object.b += floatValue.get();
        }
    }

    @Override
    public void reset() {
        object.set(Color.WHITE);
    }
}
