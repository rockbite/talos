package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.Gdx;
import com.talosvfx.talos.runtime.routine.AsyncRoutineNodeState;
import com.talosvfx.talos.runtime.routine.RoutineInstance;
import com.talosvfx.talos.runtime.scene.GameObject;
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
        float newTimeScale = (timeScaleTo - startValue) * state.alpha + startValue;
        target.setTimeScale(newTimeScale);
    }

    @Override
    protected float processDelta(float delta) {
        return Gdx.graphics.getDeltaTime();
    }
}
