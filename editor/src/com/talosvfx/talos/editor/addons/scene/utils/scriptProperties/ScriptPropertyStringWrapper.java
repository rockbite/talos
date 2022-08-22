package com.talosvfx.talos.editor.addons.scene.utils.scriptProperties;

public class ScriptPropertyStringWrapper extends ScriptPropertyWrapper<String> {
    @Override
    public String parseValueFromString (String value) {
        return value;
    }
}
