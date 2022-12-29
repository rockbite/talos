package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;

public class LoopNode extends RoutineNode {

    private int index;

    @Override
    public void receiveSignal(String portName) {
        index = 0;

        int from = fetchIntValue("from");
        int to = fetchIntValue("to");
        float step = fetchFloatValue("step");

        Object signalPayload = routineInstanceRef.getSignalPayload();

        routineInstanceRef.beginDepth();
        for(int i = from; from < to ? i < to : i > to; i += step) {
            index = i;
            routineInstanceRef.setSignalPayload(signalPayload);
            sendSignal("body");
            routineInstanceRef.setDepthValue(i);
        }
        routineInstanceRef.endDepth();

        routineInstanceRef.setSignalPayload(signalPayload);
        sendSignal("onComplete");
    }

    @Override
    public Object queryValue(String targetPortName) {
        if(targetPortName.equals("index")) {
            return index;
        }

        return null;
    }
}
