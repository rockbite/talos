package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.graphics.Color;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;

public class ColorModifyNode extends RoutineNode {

    private Color tmp = new Color();

    @Override
    public Object queryValue(String targetPortName) {

        Color color = fetchColorValue("color");
        if(color == null) color = Color.WHITE;
        tmp.set(color);
        float brightness = fetchFloatValue("brightness");

        tmp.r *= brightness;
        tmp.g *= brightness;
        tmp.b *= brightness;

        return tmp;
    }
}
