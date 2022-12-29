package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.AsyncRoutineNodeState;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;

public class DelayNode extends AsyncRoutineNode<GameObject, AsyncRoutineNodeState<GameObject>> {

    @Override
    protected void stateTick(AsyncRoutineNodeState<GameObject> state, float delta) {
        // literally do nothing here :D Delay is all about wasting time
    }
}
