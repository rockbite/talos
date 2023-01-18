package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.runtime.routine.AsyncRoutineNodeState;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.ISizableComponent;
import com.talosvfx.talos.runtime.scene.components.RoutineRendererComponent;

public class SizeToNode extends AsyncRoutineNode<GameObject, SizeToNode.SizeState> {

    private boolean additive;

    public static class SizeState extends AsyncRoutineNodeState<GameObject> {
        public Vector2 originalSize = new Vector2();
        public Vector2 targetSize = new Vector2();

        public ISizableComponent component;
    }

    @Override
    protected SizeState obtainState() {
        SizeState state = Pools.obtain(SizeState.class);
        return state;
    }

    @Override
    protected boolean targetAdded(SizeState state) {
        GameObject target = state.getTarget();
        state.component = target.findComponent(ISizableComponent.class);
        if(state.component == null) return false;
        state.originalSize.set(state.component.getWidth(), state.component.getHeight());

        float width = fetchFloatValue("width");
        float height = fetchFloatValue("height");

        additive = fetchBooleanValue("additive");

        state.targetSize.set(width, height);

        return true;
    }

    @Override
    protected void stateTick(SizeState state, float delta) {
        ISizableComponent component = state.component;
        GameObject target = state.getTarget();
        if(additive) {
            float w = state.originalSize.x + state.targetSize.x * state.interpolatedAlpha;
            float h = state.originalSize.y + state.targetSize.y * state.interpolatedAlpha;
            component.setWidth(w);
            component.setHeight(h);
        } else {
            float w = state.originalSize.x + (state.targetSize.x - state.originalSize.x) * state.interpolatedAlpha;
            float h = state.originalSize.y + (state.targetSize.y - state.originalSize.y) * state.interpolatedAlpha;
            component.setWidth(w);
            component.setHeight(h);
        }

        if(target.hasComponent(RoutineRendererComponent.class)) {
            RoutineRendererComponent rt = target.getComponent(RoutineRendererComponent.class);
            rt.routineInstance.setDirty();
        }
    }

}
