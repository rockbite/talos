package com.talosvfx.talos.editor.notifications;

import com.badlogic.gdx.Input;
import lombok.Getter;

public enum GlobalActions {
    CUT(new int[]{Input.Keys.CONTROL_LEFT}),
    COPY(new int[]{Input.Keys.CONTROL_LEFT}),
    PASTE(new int[]{Input.Keys.CONTROL_LEFT}),
    DELETE(new int[]{Input.Keys.CONTROL_LEFT});

    @Getter
    private int[] keys;
    GlobalActions(int[] keys) {
        this.keys = keys;
    }
}
