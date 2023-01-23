package com.talosvfx.talos.runtime.routine.nodes;

import com.talosvfx.talos.runtime.routine.AsyncRoutineNodeState;
import com.talosvfx.talos.runtime.scene.GameObject;
public class DelayNode extends AsyncRoutineNode<GameObject, AsyncRoutineNodeState<GameObject>> {

    @Override
    protected void stateTick(AsyncRoutineNodeState<GameObject> state, float delta) {
        // literally do nothing here :D Delay is all about wasting time
    }

    @Override
    protected boolean targetAdded(AsyncRoutineNodeState<GameObject> state) {
        return true;
    }

    @Override
    protected boolean supportsConcurrent() {
        return true;
    }
}
