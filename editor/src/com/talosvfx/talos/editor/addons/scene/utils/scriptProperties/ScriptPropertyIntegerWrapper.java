package com.talosvfx.talos.editor.addons.scene.utils.scriptProperties;

public class ScriptPropertyIntegerWrapper extends ScriptPropertyNumberWrapper<Integer> {
    @Override
    public Integer parseValueFromString (String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
