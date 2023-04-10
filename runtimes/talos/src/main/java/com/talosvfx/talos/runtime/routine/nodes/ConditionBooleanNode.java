package com.talosvfx.talos.runtime.routine.nodes;

import com.talosvfx.talos.runtime.routine.RoutineNode;

public class ConditionBooleanNode extends RoutineNode {

    @Override
    public void receiveSignal(String portName) {
        boolean result = false;
        if (isPortConnected("value")) {
            result = fetchBooleanValue("value");
        }

        if(result) {
            sendSignal("trueOutput");
        } else {
            sendSignal("falseOutput");
        }

    }
}
