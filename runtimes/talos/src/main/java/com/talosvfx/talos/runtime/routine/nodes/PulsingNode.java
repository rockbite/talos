package com.talosvfx.talos.runtime.routine.nodes;

import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.routine.TickableNode;

public class PulsingNode extends RoutineNode implements TickableNode {

    private float time;

    private boolean running = false;

    private Object payload;
    private int iteration;

    @Override
    public void receiveSignal(String portName) {
        this.iteration = 0;
        routineInstanceRef.beginDepth();
        if(portName.equals("startSignal")) {
            running = true;
            time = fetchFloatValue("interval");
            payload = routineInstanceRef.getSignalPayload();
            pulse();
        } else if(portName.equals("stopSignal")) {
            running = false;
            routineInstanceRef.endDepth();
        }
    }

    private void pulse() {
        routineInstanceRef.setDepthValue(iteration++);
        routineInstanceRef.setSignalPayload(payload);
        sendSignal("onComplete");

        if(iteration >= fetchIntValue("count")) {
            running = false;
            routineInstanceRef.endDepth();
        }
    }


    @Override
    public void tick(float delta) {
        if(running) {
            time -= delta;

            if (time <= 0) {
                time = fetchFloatValue("interval");
                pulse();
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
        running = false;
        iteration = 0;
    }
}
