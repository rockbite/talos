package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;

public abstract class AsyncRoutineNode<T extends AsyncRoutineNode.AsyncRoutineNodeState> extends RoutineNode {

    public static class AsyncRoutineNodeState {
        float alpha;
    }

    public void tick() {

    }
}
