package com.talosvfx.talos.editor.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.talosvfx.talos.TalosMain;

public class InputUtils {
    public static boolean ctrlPressed () {
        if (TalosMain.Instance().isOsX()) {
            return Gdx.input.isKeyPressed(Input.Keys.SYM) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
        } else {
            return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
        }
    }
}
