package com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.nodes;

import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineNode;

public class LoopNode extends RoutineNode {

    private int index;

    @Override
    public void receiveSignal(String portName) {
        index = 0;

        int from = fetchIntValue("from");
        int to = fetchIntValue("to");
        float step = fetchFloatValue("step");

        for(int i = from; from < to ? i < to : i > to; i += step) {
            index = i;
            sendSignal("body");
        }

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
