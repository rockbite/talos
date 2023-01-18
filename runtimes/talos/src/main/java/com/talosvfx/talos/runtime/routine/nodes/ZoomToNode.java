package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.runtime.routine.AsyncRoutineNodeState;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.components.CameraComponent;

public class ZoomToNode extends AsyncRoutineNode<GameObject, ZoomToNode.State> {

    public static class State extends AsyncRoutineNodeState<GameObject> {
        public float original;
        public float target;
    }

    @Override
    protected State obtainState() {
        return Pools.obtain(State.class);
    }

    @Override
    protected boolean targetAdded(State state) {
        GameObject target = state.getTarget();
        CameraComponent component = target.findComponent(CameraComponent.class);
        if(component == null) return false;
        state.original = component.zoom;

        float zoom = fetchFloatValue("zoom");

        state.target  = zoom;

        return true;
    }

    @Override
    protected void stateTick(State state, float delta) {
        GameObject target = state.getTarget();
        CameraComponent component = target.findComponent(CameraComponent.class);
        if(component == null) return;

        component.zoom = state.original + (state.target - state.original) * state.interpolatedAlpha;
    }


}
