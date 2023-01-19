package com.talosvfx.talos.runtime.routine.nodes;

import com.talosvfx.talos.runtime.routine.RoutineNode;

public class CompleteRoutineNode extends RoutineNode {

    @Override
    public void receiveSignal(String portName) {
        routineInstanceRef.complete();
    }
}
