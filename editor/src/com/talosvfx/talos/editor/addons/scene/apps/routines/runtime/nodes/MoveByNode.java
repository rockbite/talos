package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.AsyncRoutineNodeState;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import lombok.Getter;

public class MoveByNode extends AsyncRoutineNode<GameObject, MoveByNode.PositionTargetState> {


    public static class PositionTargetState extends AsyncRoutineNodeState<GameObject> {
        @Getter
        private Vector2 originalPosition = new Vector2();
        @Getter
        private Vector2 offset = new Vector2();
    }

    @Override
    protected MoveByNode.PositionTargetState obtainState() {
        PositionTargetState state = Pools.obtain(PositionTargetState.class);
        return state;
    }

    @Override
    protected void targetAdded(MoveByNode.PositionTargetState state) {
        GameObject target = state.getTarget();
        TransformComponent component = target.getComponent(TransformComponent.class);
        state.getOriginalPosition().set(component.position);

        float x = fetchFloatValue("X");
        float y = fetchFloatValue("Y");

        state.getOffset().set(x, y);
    }

    @Override
    protected void stateTick(MoveByNode.PositionTargetState state, float delta) {
        GameObject target = state.getTarget();
        TransformComponent component = target.getComponent(TransformComponent.class);
        component.position.x = state.getOriginalPosition().x + state.getOffset().x * state.interpolatedAlpha;
        component.position.y = state.getOriginalPosition().y + state.getOffset().y * state.interpolatedAlpha;
    }
}
