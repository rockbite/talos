package com.talosvfx.talos.editor.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class InputUtils {

    public static boolean isOsX () {
        String osName = System.getProperty("os.name").toLowerCase();
        boolean isMacOs = osName.startsWith("mac os x");
        return isMacOs;
    }

    public static boolean ctrlPressed () {
        if (isOsX()) {
            return Gdx.input.isKeyPressed(Input.Keys.SYM) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
        } else {
            return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
        }
    }
}
