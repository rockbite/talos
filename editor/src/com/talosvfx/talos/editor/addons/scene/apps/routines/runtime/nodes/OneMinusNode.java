package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;

public class OneMinusNode extends RoutineNode {

    @Override
    public Object queryValue(String targetPortName) {
        float value = fetchFloatValue("value");

        return value * -1;
    }
}
