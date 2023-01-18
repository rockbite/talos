package com.talosvfx.talos.runtime.routine;

import com.badlogic.gdx.utils.Pool;
import lombok.Getter;
import lombok.Setter;

public class AsyncRoutineNodeState<T> implements Pool.Poolable {
    public float alpha;
    public float interpolatedAlpha;
    @Setter@Getter
    T target;

    @Setter@Getter
    private float duration;

    public float direction;

    @Override
    public void reset() {
        alpha = 0;
        direction = 1;
    }
}
