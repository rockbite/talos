package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;

public class ModConditionDataNode extends RoutineNode {

    @Override
    public Object queryValue(String targetPortName) {

        int input = fetchIntValue("input");

        Object valueEven = fetchValue("valueA");
        Object valueOdd = fetchValue("valueB");

        if(input % 2 == 0) {
            return valueEven;
        } else {
            return valueOdd;
        }

    }
}
