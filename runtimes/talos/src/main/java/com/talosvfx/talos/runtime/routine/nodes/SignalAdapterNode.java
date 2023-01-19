package com.talosvfx.talos.runtime.routine.nodes;

import com.talosvfx.talos.runtime.routine.RoutineNode;

public class SignalAdapterNode extends RoutineNode {

    @Override
    public void receiveSignal(String portName) {
        sendSignal("out");
    }
}
