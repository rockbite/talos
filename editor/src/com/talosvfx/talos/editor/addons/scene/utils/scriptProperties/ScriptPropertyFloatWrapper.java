package com.talosvfx.talos.editor.addons.scene.utils.scriptProperties;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ValueProperty;

public class ScriptPropertyFloatWrapper extends ScriptPropertyNumberWrapper<Float> {
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
