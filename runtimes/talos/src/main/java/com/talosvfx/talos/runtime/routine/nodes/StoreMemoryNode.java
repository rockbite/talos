package com.talosvfx.talos.runtime.routine.nodes;

import com.talosvfx.talos.runtime.routine.RoutineNode;

public class StoreMemoryNode extends RoutineNode {


    @Override
    public void receiveSignal(String portName) {
        String name = fetchStringValue("name");
        Object input = fetchValue("input");

        String key = name;
        if(fetchBooleanValue("local") && routineInstanceRef.getSignalPayload() != null) {
            key = name + (routineInstanceRef.getSignalPayload().hashCode() + "");
        }

        routineInstanceRef.storeMemory(key, input);

        sendSignal("next");
    }
}
