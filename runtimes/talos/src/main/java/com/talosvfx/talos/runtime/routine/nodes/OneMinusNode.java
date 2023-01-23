package com.talosvfx.talos.runtime.routine.nodes;

import com.talosvfx.talos.runtime.routine.RoutineNode;

public class OneMinusNode extends RoutineNode {

    @Override
    public Object queryValue(String targetPortName) {
        float value = fetchFloatValue("value");

        return value * -1;
    }
}
