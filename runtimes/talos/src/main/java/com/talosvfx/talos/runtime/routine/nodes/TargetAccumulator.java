package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.routine.RoutineNode;

public class TargetAccumulator extends RoutineNode {

    Array<Object> list = new Array<>();

    @Override
    public void receiveSignal(String portName) {

        if(portName.equals("addSignal")) {
            list.add(routineInstanceRef.getSignalPayload());
        } else if(portName.equals("startSignal")) {
            routineInstanceRef.setSignalPayload(list);
            routineInstanceRef.storeGlobal("executedTargets", list);
            for(Object target: list) {
                routineInstanceRef.setSignalPayload(target);
                sendSignal("onComplete");
            }
            list.clear();
        }
    }

    @Override
    public void reset() {
        super.reset();
        list.clear();
    }
}
