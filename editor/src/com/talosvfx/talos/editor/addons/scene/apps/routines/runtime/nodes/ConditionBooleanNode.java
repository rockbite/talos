package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;

public class ConditionBooleanNode extends RoutineNode {

    @Override
    public void receiveSignal(String portName) {

        boolean result = fetchBooleanValue("value");

        if(result) {
            sendSignal("trueOutput");
        } else {
            sendSignal("falseOutput");
        }

    }
}
