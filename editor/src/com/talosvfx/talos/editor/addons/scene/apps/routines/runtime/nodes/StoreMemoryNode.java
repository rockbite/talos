package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;

public class StoreMemoryNode extends RoutineNode {


    @Override
    public void receiveSignal(String portName) {
        String name = fetchStringValue("name");
        Object input = fetchValue("input");

        routineInstanceRef.storeMemory(name, input);
    }
}
