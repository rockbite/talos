package com.talosvfx.talos.runtime.routine.nodes;

import com.talosvfx.talos.runtime.routine.RoutineNode;

public class ColorNode extends RoutineNode {

    @Override
    public Object queryValue(String targetPortName) {
        return fetchColorValue("color");
    }
}
