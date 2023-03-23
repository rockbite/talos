package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.runtime.routine.AsyncRoutineNodeState;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;
import lombok.Getter;

public class RotateBy extends AsyncRoutineNode<GameObject, RotateBy.State> {


    public static class State extends AsyncRoutineNodeState<GameObject> {
        @Getter
        private float original;
        @Getter
        private float offset;
    }

    @Override
    protected RotateBy.State obtainState() {
        return Pools.obtain(RotateBy.State.class);
    }

    @Override
    protected boolean targetAdded(RotateBy.State state) {
        GameObject target = state.getTarget();
        TransformComponent component = target.getComponent(TransformComponent.class);
        if(component == null) return false;
        state.original = component.rotation;

        float a = fetchFloatValue("angle");

        state.offset = a;

        float pivotX = fetchFloatValue("pivotX");
        float pivotY = fetchFloatValue("pivotY");
        component.pivot.set(pivotX, pivotY);

        return true;
    }

    @Override
    protected void stateTick(RotateBy.State state, float delta) {
        GameObject target = state.getTarget();
        TransformComponent component = target.getComponent(TransformComponent.class);

        component.rotation = state.original + state.offset * state.interpolatedAlpha;
    }
}
