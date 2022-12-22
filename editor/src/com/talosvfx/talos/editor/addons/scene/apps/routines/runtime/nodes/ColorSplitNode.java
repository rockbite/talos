package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.graphics.Color;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;

public class ColorSplitNode extends RoutineNode {

    @Override
    public Object queryValue(String targetPortName) {

        Color color = fetchColorValue("color");

        if(targetPortName.equals("r")) return color.r;
        if(targetPortName.equals("g")) return color.g;
        if(targetPortName.equals("b")) return color.b;
        if(targetPortName.equals("a")) return color.a;

        return 0;
    }
}
