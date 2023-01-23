package com.talosvfx.talos.runtime.routine.nodes;

import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.scene.GameObject;
public class RemoveEntityNode extends RoutineNode {

    @Override
    public void receiveSignal(String portName) {
        GameObject target = (GameObject) routineInstanceRef.getSignalPayload();
        target.parent.removeObject(target);

        sendSignal("onComplete");
    }
}
