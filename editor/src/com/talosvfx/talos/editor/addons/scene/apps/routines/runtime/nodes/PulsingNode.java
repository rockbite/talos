package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.TickableNode;

public class PulsingNode extends RoutineNode implements TickableNode {

    private float time;

    private boolean running = false;

    private Object payload;

    @Override
    public void receiveSignal(String portName) {
        if(portName.equals("startSignal")) {
            running = true;
            time = fetchFloatValue("interval");
            payload = routineInstanceRef.getSignalPayload();

            routineInstanceRef.setSignalPayload(payload);
            sendSignal("onComplete");
        } else if(portName.equals("stopSignal")) {
            running = false;
        }
    }

    @Override
    public void tick(float delta) {
        if(running) {
            time -= delta;

            if (time <= 0) {
                time = fetchFloatValue("interval");

                routineInstanceRef.setSignalPayload(payload);
                sendSignal("onComplete");
            }
        }
    }
}
