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

        routineInstanceRef.beginDepth();
        for(int i = from; from < to ? i < to : i > to; i += step) {
            index = i;
            sendSignal("body");
            routineInstanceRef.setDepthValue(i);
        }
        routineInstanceRef.endDepth();

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
