package com.talosvfx.talos.editor.addons.scene.utils.scriptProperties;

import com.badlogic.gdx.utils.Array;

public abstract class ScriptPropertyNumberWrapper<T> extends ScriptPropertyWrapper<T> {

    public T minValue;
    public T maxValue;
    public T step;

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
    public ScriptPropertyNumberWrapper<T> clone () {
        ScriptPropertyNumberWrapper<T> clone = (ScriptPropertyNumberWrapper<T>) super.clone();
        clone.maxValue = this.maxValue;
        clone.minValue = this.minValue;
        clone.step = this.step;
        return clone;
    }
}
