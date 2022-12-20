package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;

public class ConditionNode extends RoutineNode {

    @Override
    public void receiveSignal(String portName) {

        float input = fetchFloatValue("value");
        float compare = fetchFloatValue("compare");

        String condition = fetchStringValue("condition");

        boolean result = false;

        if(condition.equals("equal")) {
            if(input == compare) result = true;
        } else if(condition.equals("bigger")) {
            if(input > compare) result = true;
        } else if(condition.equals("smaller")) {
            if(input < compare) result = true;
        }

        if(result) {
            sendSignal("trueOutput");
        } else {
            sendSignal("falseOutput");
        }

    }
}
