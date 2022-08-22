package com.talosvfx.talos.editor.addons.scene.utils.scriptProperties;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ValueProperty;

public class ScriptPropertyFloatWrapper extends ScriptPropertyWrapper<Float> {

    public float minValue;
    public float maxValue;
    public float step;

    @Override
    public void collectAttributes (Array<String> attributes) {
        // TODO: 8/18/2022 do this with annotations and nice reflection :(
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
    public ScriptPropertyFloatWrapper clone () {
        ScriptPropertyFloatWrapper clone = (ScriptPropertyFloatWrapper) super.clone();
        clone.maxValue = this.maxValue;
        clone.minValue = this.minValue;
        clone.step = this.step;
        return clone;
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
}
