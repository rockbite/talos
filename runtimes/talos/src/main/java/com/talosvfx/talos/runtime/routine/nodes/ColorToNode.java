package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.runtime.routine.AsyncRoutineNodeState;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.components.SpineRendererComponent;
import com.talosvfx.talos.runtime.scene.components.SpriteRendererComponent;
import lombok.Getter;

public class ColorToNode extends AsyncRoutineNode<GameObject, ColorToNode.ColorState> {

    private boolean alphaOnly;

    public static class ColorState extends AsyncRoutineNodeState<GameObject> {
        @Getter
        private Color originalColor = new Color();
        @Getter
        private Color targetColor = new Color();
    }

    @Override
    protected ColorState obtainState() {
        ColorState state = Pools.obtain(ColorState.class);
        return state;
    }

    @Override
    protected boolean targetAdded(ColorState state) {
        GameObject target = state.getTarget();
        if(target.hasComponent(SpriteRendererComponent.class)) {
            SpriteRendererComponent component = target.getComponent(SpriteRendererComponent.class);
            state.originalColor.set(component.color);
        } else if(target.hasComponent(SpineRendererComponent.class)) {
            SpineRendererComponent component = target.getComponent(SpineRendererComponent.class);
            state.originalColor.set(component.color);
        } else {
            return false;
        }

        Color color = fetchColorValue("color");
        state.targetColor.set(color);

        alphaOnly = fetchBooleanValue("alphaOnly");

        return true;
    }

    @Override
    protected void stateTick(ColorState state, float delta) {
        GameObject target = state.getTarget();

        if(target.hasComponent(SpriteRendererComponent.class)) {
            SpriteRendererComponent component = target.getComponent(SpriteRendererComponent.class);
            if(!alphaOnly) {
                component.color.r = state.originalColor.r + (state.targetColor.r - state.originalColor.r) * state.interpolatedAlpha;
                component.color.g = state.originalColor.g + (state.targetColor.g - state.originalColor.g) * state.interpolatedAlpha;
                component.color.b = state.originalColor.b + (state.targetColor.b - state.originalColor.b) * state.interpolatedAlpha;
            }
            component.color.a = state.originalColor.a + (state.targetColor.a - state.originalColor.a) * state.interpolatedAlpha;
        }
        if(target.hasComponent(SpineRendererComponent.class)) {
            SpineRendererComponent component = target.getComponent(SpineRendererComponent.class);
            if(!alphaOnly) {
                component.color.r = state.originalColor.r + (state.targetColor.r - state.originalColor.r) * state.interpolatedAlpha;
                component.color.g = state.originalColor.g + (state.targetColor.g - state.originalColor.g) * state.interpolatedAlpha;
                component.color.b = state.originalColor.b + (state.targetColor.b - state.originalColor.b) * state.interpolatedAlpha;
            }
            component.color.a = state.originalColor.a + (state.targetColor.a - state.originalColor.a) * state.interpolatedAlpha;
        }
    }
}
