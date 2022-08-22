package com.talosvfx.talos.editor.addons.scene.utils.scriptProperties;

public class ScriptPropertyBooleanWrapper extends ScriptPropertyWrapper<Boolean> {
    @Override
    public Boolean parseValueFromString (String value) {
        return Boolean.valueOf(value);
    }
}
