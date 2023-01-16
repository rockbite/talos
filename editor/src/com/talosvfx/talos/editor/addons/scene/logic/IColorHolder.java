package com.talosvfx.talos.editor.addons.scene.logic;

import com.badlogic.gdx.graphics.Color;

public interface IColorHolder {
    boolean shouldInheritParentColor();
    Color getColor ();
    Color getFinalColor();
}
