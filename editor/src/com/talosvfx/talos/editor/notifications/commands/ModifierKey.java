package com.talosvfx.talos.editor.notifications.commands;

import com.badlogic.gdx.Input;

public enum ModifierKey {
    ALT(Input.Keys.ALT_LEFT, Input.Keys.ALT_RIGHT), CTRL(Input.Keys.CONTROL_LEFT, Input.Keys.CONTROL_RIGHT),
    SHIFT(Input.Keys.SHIFT_LEFT, Input.Keys.SHIFT_RIGHT), CMD(Input.Keys.SYM);
    public int[] responsibleKeys;

    ModifierKey (int... keys) {
        responsibleKeys = keys;
    }

    public static ModifierKey getModifierFromKey (int key) {
        ModifierKey[] values = ModifierKey.values();
        for (ModifierKey value : values) {
            for (int responsibleKey : value.responsibleKeys) {
                if (responsibleKey == key) {
                    return value;
                }
            }
        }

        return null;
    }
}
