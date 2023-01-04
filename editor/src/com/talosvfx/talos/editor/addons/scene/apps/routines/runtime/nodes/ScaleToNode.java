package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.AsyncRoutineNodeState;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.RoutineRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import lombok.Getter;

public class ScaleToNode extends AsyncRoutineNode<GameObject, ScaleToNode.ScaleState> {

    public static class ScaleState extends AsyncRoutineNodeState<GameObject> {
        public Vector2 originalScale = new Vector2();
        public Vector2 targetScale = new Vector2();
    }

    @Override
    protected ScaleToNode.ScaleState obtainState() {
        ScaleToNode.ScaleState state = Pools.obtain(ScaleToNode.ScaleState.class);
        return state;
    }

    @Override
    protected boolean targetAdded(ScaleState state) {
        GameObject target = state.getTarget();
        TransformComponent component = target.getComponent(TransformComponent.class);
        if(component == null) return false;
        state.originalScale.set(component.scale);

        float scaleX = fetchFloatValue("scaleX");
        float scaleY = fetchFloatValue("scaleY");

        state.targetScale.set(scaleX, scaleY);

        return true;
    }

    @Override
    protected void stateTick(ScaleState state, float delta) {
        GameObject target = state.getTarget();
        TransformComponent component = target.getComponent(TransformComponent.class);
        component.scale.x = state.originalScale.x + (state.targetScale.x - state.originalScale.x) * state.interpolatedAlpha;
        component.scale.y = state.originalScale.y + (state.targetScale.y - state.originalScale.y) * state.interpolatedAlpha;

        if(target.hasComponent(RoutineRendererComponent.class)) {
            RoutineRendererComponent rt = target.getComponent(RoutineRendererComponent.class);
            rt.routineInstance.setDirty();
        }
    }
}
