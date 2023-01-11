package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.AsyncRoutineNodeState;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.ISizableComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.RoutineRendererComponent;

public class SizeToNode extends AsyncRoutineNode<GameObject, SizeToNode.SizeState> {

    private boolean additive;

    public static class SizeState extends AsyncRoutineNodeState<GameObject> {
        public Vector2 originalSize = new Vector2();
        public Vector2 targetSize = new Vector2();

        public ISizableComponent component;
    }

    @Override
    protected SizeToNode.SizeState obtainState() {
        SizeToNode.SizeState state = Pools.obtain(SizeToNode.SizeState.class);
        return state;
    }

    @Override
    protected boolean targetAdded(SizeToNode.SizeState state) {
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
    protected void stateTick(SizeToNode.SizeState state, float delta) {
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
