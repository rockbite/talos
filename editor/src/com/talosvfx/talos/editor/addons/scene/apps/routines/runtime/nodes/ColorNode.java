package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;

public class ColorNode extends RoutineNode {

    @Override
    public Object queryValue(String targetPortName) {
        return fetchColorValue("color");
    }
}
