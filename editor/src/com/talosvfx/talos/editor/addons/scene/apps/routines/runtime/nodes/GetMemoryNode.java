package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;

public class GetMemoryNode extends RoutineNode {

    @Override
    public Object queryValue(String targetPortName) {
        String name = fetchStringValue("name");

        String key = name;
        if(fetchBooleanValue("local") && routineInstanceRef.getSignalPayload() != null) {
            key = name + (routineInstanceRef.getSignalPayload().hashCode() + "");
        }

        return routineInstanceRef.fetchMemory(key);
    }
}
