package com.talosvfx.talos.runtime.scene;

import com.badlogic.gdx.graphics.Color;

public interface IColorHolder {
    boolean shouldInheritParentColor();
    Color getColor ();
    Color getFinalColor();
}
