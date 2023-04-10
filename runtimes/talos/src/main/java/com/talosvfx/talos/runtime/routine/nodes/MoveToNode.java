package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.runtime.routine.AsyncRoutineNodeState;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;
import lombok.Getter;

public class MoveToNode extends AsyncRoutineNode<GameObject, MoveToNode.PositionTargetState> {


    public static class PositionTargetState extends AsyncRoutineNodeState<GameObject> {
        @Getter
        private Vector2 originalPosition = new Vector2();
        @Getter
        private Vector2 to = new Vector2();
    }

    @Override
    protected PositionTargetState obtainState() {
        PositionTargetState state = Pools.obtain(PositionTargetState.class);
        return state;
    }

    @Override
    protected boolean targetAdded(PositionTargetState state) {
        GameObject target = state.getTarget();
        TransformComponent component = target.getComponent(TransformComponent.class);
        if(component == null) return false;
        state.getOriginalPosition().set(component.position);

        float x = fetchFloatValue("X");
        float y = fetchFloatValue("Y");

        state.getTo().set(x, y);

        return true;
    }

    @Override
    protected void stateTick(PositionTargetState state, float delta) {
        GameObject target = state.getTarget();
        TransformComponent component = target.getComponent(TransformComponent.class);
        Vector2 orig = state.getOriginalPosition();
        Vector2 to = state.to;
        component.position.x = orig.x + (to.x - orig.x) * state.interpolatedAlpha;
        component.position.y = orig.y + (to.y - orig.y) * state.interpolatedAlpha;
    }
}
