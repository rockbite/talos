package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.Gdx;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.AsyncRoutineNodeState;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineInstance;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;

public class PlaybackSpeedToNode extends AsyncRoutineNode<GameObject, AsyncRoutineNodeState<GameObject>> {

    private float timeScaleTo;
    private float startValue;

    @Override
    protected boolean targetAdded(AsyncRoutineNodeState<GameObject> state) {
        startValue = routineInstanceRef.getTimeScale();
        timeScaleTo = fetchFloatValue("timeScale");

        return true;
    }

    @Override
    protected void stateTick(AsyncRoutineNodeState<GameObject> state, float delta) {
        RoutineInstance target = routineInstanceRef;
        float currTimeScale = routineInstanceRef.getTimeScale();
        float newTimeScale = (timeScaleTo - currTimeScale) * state.alpha + startValue;
        target.setTimeScale(newTimeScale);
    }

    @Override
    protected float processDelta(float delta) {
        return Gdx.graphics.getDeltaTime();
    }
}
