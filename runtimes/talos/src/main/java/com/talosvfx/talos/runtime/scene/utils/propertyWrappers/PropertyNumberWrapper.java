package com.talosvfx.talos.runtime.scene.utils.propertyWrappers;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

public abstract class PropertyNumberWrapper<T extends Number> extends PropertyWrapper<T> {

    public T minValue;
    public T maxValue;
    public T step;

    public boolean isRanged;

    @Override
    public void collectAttributes (Array<String> attributes) {
        super.collectAttributes(attributes);
        for (int i = 0; i < attributes.size; i+=2) {
            String type = attributes.get(i);
            if (type.equals("minValue")) {
                minValue = parseValueFromString(attributes.get(i+1));
            }
            if (type.equals("maxValue")) {
                maxValue = parseValueFromString(attributes.get(i+1));
            }
            if (type.equals("step")) {
                step = parseValueFromString(attributes.get(i+1));
            }
        }
    }

    @Override
    public PropertyNumberWrapper<T> clone () {
        PropertyNumberWrapper<T> clone = (PropertyNumberWrapper<T>) super.clone();
        clone.maxValue = this.maxValue;
        clone.minValue = this.minValue;
        clone.step = this.step;
        clone.isRanged = this.isRanged;
        return clone;
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("minValue", minValue);
        json.writeValue("maxValue", maxValue);
        json.writeValue("step", step);
        json.writeValue("isRanged", isRanged);
    }
}
