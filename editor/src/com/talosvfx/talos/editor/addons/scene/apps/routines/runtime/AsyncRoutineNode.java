package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime;

public abstract class AsyncRoutineNode<T extends AsyncRoutineNode.AsyncRoutineNodeState> extends RoutineNode {

    public static class AsyncRoutineNodeState {
        float alpha;
    }

    public void tick() {

    }
}
