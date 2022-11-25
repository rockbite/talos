package com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.nodes;

import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineNode;

public class GetMemoryNode extends RoutineNode {

    @Override
    public Object queryValue(String targetPortName) {
        String name = fetchStringValue("name");

        return routineInstanceRef.fetchMemory(name);
    }
}